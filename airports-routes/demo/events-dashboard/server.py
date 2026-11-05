#!/usr/bin/env python3
"""Demo dashboard of the airports-routes events (Assignment 2): RabbitMQ and the read models. Not for production.

Every second it reads RabbitMQ (broker up, the exchange, one read-model queue per replica) and the
replicas (ready or not, and what each one's read model lists), and it follows every event through a
queue of its own bound to the exchange. Everything goes to the browser (server-sent events). It also
sends commands and causes failures when the page asks: create a route, register an airport, send a
copy of a flight (C5), stop or start a replica, stop or start RabbitMQ.

Runs on this computer next to a local setup (RabbitMQ in Docker, the login and three replicas, see
docs/postman/README.md, "Assignment 2"). Python standard library only.

    python3 airports-routes/demo/events-dashboard/server.py        # then open http://127.0.0.1:8098
"""
import base64
import http.server
import json
import os
import queue
import random
import secrets
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", "..", ".."))
PORT = int(os.environ.get("DASHBOARD_PORT", "8098"))
LOGIN = os.environ.get("LOGIN_URL", "http://localhost:8085")
RABBIT = os.environ.get("RABBIT_API", "http://127.0.0.1:15672/api")
RABBIT_AUTH = "Basic " + base64.b64encode(os.environ.get("RABBIT_USER_PASSWORD", "guest:guest").encode()).decode()
RABBIT_CONTAINER = os.environ.get("RABBIT_CONTAINER", "rabbitmq")
EXCHANGE = "airports-routes.events"
FEED_QUEUE = "dashboard-events"
REPLICAS = [{"id": f"ar{i}", "port": 18090 + i} for i in range(3)]
PEERS = ",".join(f"http://{r['id']}.localtest.me:{r['port']}" for r in REPLICAS)
JAR = os.path.join(REPO, "airports-routes", "target", "airports-routes-0.0.1-SNAPSHOT.jar")
LOGS = os.path.join(REPO, "airports-routes", "target", "dashboard-logs")
NEW_AIRPORTS = [
    ("FAO", "Faro Airport", "Faro", "Portugal", 37.0144, -7.9659), ("BCN", "Barcelona El Prat", "Barcelona", "Spain", 41.2971, 2.0785),
    ("ZRH", "Zurich Airport", "Zurich", "Switzerland", 47.4582, 8.5555), ("FRA", "Frankfurt Airport", "Frankfurt", "Germany", 50.0379, 8.5622),
    ("AMS", "Amsterdam Schiphol", "Amsterdam", "Netherlands", 52.3105, 4.7683), ("DUB", "Dublin Airport", "Dublin", "Ireland", 53.4264, -6.2499),
    ("FCO", "Rome Fiumicino", "Rome", "Italy", 41.8003, 12.2389), ("VIE", "Vienna Airport", "Vienna", "Austria", 48.1103, 16.5697),
]

# Asked on every POST: only this page has it (it is written into the page it serves), so another
# site open in the same browser cannot send commands to this server.
TOKEN = secrets.token_urlsafe(32)

clients, clients_lock = [], threading.Lock()
tokens = {}
state = {"broker": False, "exchange": None, "queues": {}, "replicas": {}}


# --- HTTP helpers ---------------------------------------------------------------------------------

def call(method, url, body=None, headers=None, timeout=3):
    """(status, parsed JSON or None); status 0 when nothing answered."""
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(url, data=data, method=method, headers=dict(headers or {}))
    if data is not None:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read()
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as e:
        raw = e.read()
        try:
            return e.code, json.loads(raw) if raw else None
        except ValueError:
            return e.code, None
    except (urllib.error.URLError, OSError, ValueError):
        return 0, None


def rabbit(method, path, body=None):
    return call(method, RABBIT + path, body, {"Authorization": RABBIT_AUTH})


def token(user):
    if user not in tokens:
        status, body = call("POST", LOGIN + "/api/auth/login", {"username": user, "password": user + "123"})
        if status != 200:
            raise RuntimeError(f"login as {user} failed (is the login running on {LOGIN}?)")
        tokens[user] = body["token"]
    return tokens[user]


def api(method, replica, path, body=None, user="atcc"):
    status, result = call(method, f"http://127.0.0.1:{replica['port']}{path}", body,
                          {"Authorization": "Bearer " + token(user)}, timeout=5)
    if status == 401:
        tokens.pop(user, None)
    return status, result


def embedded(body):
    if isinstance(body, list):
        return body
    return [x for v in ((body or {}).get("_embedded") or {}).values() for x in v]


def broadcast(message):
    data = json.dumps(message)
    with clients_lock:
        for q in clients:
            q.put(data)


def now_ms():
    return int(time.time() * 1000)


# --- What is going on ----------------------------------------------------------------------------

def read_replica(r):
    """Ready or not, and what its read model lists (only asked when ready)."""
    status, _ = call("GET", f"http://127.0.0.1:{r['port']}/actuator/health/readiness", timeout=1)
    info = {"up": status == 200}
    if not info["up"]:
        return info
    try:
        _, routes = api("GET", r, "/api/routes/search?size=1000")
        _, airports = api("GET", r, "/api/airports/search?size=1000", user="operator")
        _, active = api("GET", r, "/api/routes?status=active&sortBy=popularity")
        active = embedded(active)
        info.update(routes=len(embedded(routes)), airports=len(embedded(airports)),
                    flights=sum(x.get("popularity") or 0 for x in active),
                    top=[{"route": x["originIataCode"] + "-" + x["destinationIataCode"], "flights": x.get("popularity") or 0}
                         for x in active[:3]])
    except RuntimeError as e:
        info["error"] = str(e)
    return info


def poll_state():
    while True:
        status, _ = rabbit("GET", "/overview")
        state["broker"] = status == 200
        if state["broker"]:
            _, qs = rabbit("GET", "/queues/%2F")
            state["queues"] = {q["name"].rsplit(".", 1)[-1]: {"ready": q.get("messages_ready", 0),
                                                               "unacked": q.get("messages_unacknowledged", 0),
                                                               "consumers": q.get("consumers", 0)}
                               for q in (qs or []) if q["name"].startswith("airports-routes.read-model.")}
            _, ex = rabbit("GET", "/exchanges/%2F/" + EXCHANGE)
            state["exchange"] = None if not ex or "name" not in ex else {
                "rate": ((ex.get("message_stats") or {}).get("publish_in_details") or {}).get("rate", 0)}
        else:
            state["queues"], state["exchange"] = {}, None
        threads = [threading.Thread(target=lambda r=r: state["replicas"].__setitem__(r["id"], read_replica(r))) for r in REPLICAS]
        [t.start() for t in threads]
        [t.join() for t in threads]
        broadcast({"type": "state", "t": now_ms(), **state})
        time.sleep(1)


def follow_events():
    """A queue of the dashboard, bound to every event of the exchange, emptied twice a second."""
    bound = False
    while True:
        time.sleep(0.5)
        if not state["broker"] or state["exchange"] is None:
            bound = False
            continue
        if not bound:
            rabbit("PUT", "/queues/%2F/" + FEED_QUEUE, {"durable": True, "auto_delete": False})
            rabbit("POST", f"/bindings/%2F/e/{EXCHANGE}/q/{FEED_QUEUE}", {"routing_key": "#"})
            rabbit("DELETE", f"/queues/%2F/{FEED_QUEUE}/contents")
            bound = True
        status, messages = rabbit("POST", f"/queues/%2F/{FEED_QUEUE}/get",
                                  {"count": 50, "ackmode": "ack_requeue_false", "encoding": "auto"})
        if status != 200:
            bound = False
            continue
        for m in messages or []:
            try:
                event = json.loads(m["payload"])
            except ValueError:
                continue
            broadcast({"type": "event", "t": now_ms(), "routingKey": m["routing_key"], "event": event})


def feed(kind, text):
    broadcast({"type": "feed", "kind": kind, "t": now_ms(), "text": text})


# --- What the page can ask -----------------------------------------------------------------------

def replica(a):
    up = [r for r in REPLICAS if state["replicas"].get(r["id"], {}).get("up")]
    chosen = next((r for r in REPLICAS if r["id"] == a.get("replica")), None) or (random.choice(up) if up else None)
    if chosen is None:
        raise RuntimeError("no replica is up")
    return chosen


def create_route(a):
    r = replica(a)
    _, airports = api("GET", r, "/api/airports/search?size=1000", user="operator")
    _, routes = api("GET", r, "/api/routes/search?size=1000")
    codes = sorted(x["iataCode"] for x in embedded(airports))
    taken = {(x["originIataCode"], x["destinationIataCode"]) for x in embedded(routes)}
    free = [(o, d) for o in codes for d in codes if o != d and (o, d) not in taken]
    if not free:
        raise RuntimeError("every pair of airports already has a route: register an airport first")
    o, d = random.choice(free)
    status, _ = api("POST", r, "/api/routes", {"originIataCode": o, "destinationIataCode": d, "estimatedFlightTime": random.randint(50, 200),
                                               "minimumRange": 1000.0, "minimumCapacity": 100, "createdBy": "dashboard"})
    return f"POST /api/routes {o}-{d} on {r['id']}: {status}"


def register_airport(a):
    r = replica(a)
    _, airports = api("GET", r, "/api/airports/search?size=1000", user="operator")
    existing = {x["iataCode"] for x in embedded(airports)}
    free = [x for x in NEW_AIRPORTS if x[0] not in existing]
    if not free:
        raise RuntimeError("every demo airport is already registered")
    code, name, city, country, lat, lon = random.choice(free)
    status, _ = api("POST", r, "/api/airports", {"iataCode": code, "name": name, "city": city, "country": country, "region": "Europe",
                                                 "timezone": "Europe/Lisbon", "latitude": lat, "longitude": lon,
                                                 "runways": [{"name": "09/27", "length": 3000, "orientation": "090/270"}]}, user="operator")
    return f"POST /api/airports {code} on {r['id']}: {status}"


def flight_copy(a):
    r = replica(a)
    _, active = api("GET", r, "/api/routes?status=active&sortBy=popularity")
    active = embedded(active)
    if not active:
        raise RuntimeError("no active route")
    route = random.choice(active)
    fid = "dashboard-" + str(now_ms())
    departure = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(time.time() + 86400 * random.randint(10, 90)))
    arrival = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime(time.time() + 86400 * 100))
    status, _ = api("PUT", r, "/internal/flights/" + fid, {
        "id": fid, "departureDateTime": departure, "arrivalDateTime": arrival, "status": "SCHEDULED",
        "originIataCode": route["originIataCode"], "destinationIataCode": route["destinationIataCode"],
        "aircraftRegistration": "CS-TKA", "version": 1})
    return f"PUT /internal/flights/{fid} ({route['originIataCode']}-{route['destinationIataCode']}, C5) on {r['id']}: {status}"


def pid_on(port):
    out = subprocess.run(["lsof", "-ti", f"tcp:{port}", "-sTCP:LISTEN"], capture_output=True, text=True).stdout.split()
    return [int(p) for p in out]


def stop_replica(a):
    r = next(x for x in REPLICAS if x["id"] == a["replica"])
    pids = pid_on(r["port"])
    for p in pids:
        os.kill(p, 15)
    return f"{r['id']} stopped" if pids else f"{r['id']} was not running"


def start_replica(a):
    r = next(x for x in REPLICAS if x["id"] == a["replica"])
    if pid_on(r["port"]):
        return f"{r['id']} is already running"
    os.makedirs(LOGS, exist_ok=True)
    env = dict(os.environ, EVENTS_ENABLED="true", NODE_ID=r["id"], PORT=str(r["port"]), PEERS=PEERS, SEED_NODE="ar0",
               READ_MODEL_RECONCILE_MS="15000")
    subprocess.Popen(["java", "-jar", JAR], env=env, cwd=REPO, stdout=open(os.path.join(LOGS, r["id"] + ".log"), "ab"),
                     stderr=subprocess.STDOUT, start_new_session=True)
    return f"{r['id']} starting (empty: it gets its data back from its peers)"


def broker(a):
    on = bool(a.get("on"))
    done = subprocess.run(["docker", "start" if on else "stop", RABBIT_CONTAINER], capture_output=True, text=True)
    if done.returncode != 0:
        raise RuntimeError(done.stderr.strip() or "docker failed")
    return "RabbitMQ " + ("starting" if on else "stopped")


ACTIONS = {"route": create_route, "airport": register_airport, "flight": flight_copy,
           "stop": stop_replica, "start": start_replica, "broker": broker}


def do_action(a):
    text = ACTIONS[a["op"]](a)
    kind = "fail" if a["op"] == "stop" or (a["op"] == "broker" and not a.get("on")) else "cmd"
    feed(kind, text)
    return {"ok": True, "text": text}


# --- Web server ----------------------------------------------------------------------------------

ALLOWED_HOSTS = {f"127.0.0.1:{PORT}", f"localhost:{PORT}"}


class Handler(http.server.BaseHTTPRequestHandler):
    def log_message(self, *args):
        pass

    def same_origin(self):
        """Refuses requests from other sites (Origin) and DNS rebinding (Host)."""
        origin = self.headers.get("Origin")
        return self.headers.get("Host") in ALLOWED_HOSTS and (origin is None or origin.split("://", 1)[-1] in ALLOWED_HOSTS)

    def reply(self, status, body, content_type="application/json"):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if not self.same_origin():
            return self.reply(403, b'{"ok": false}')
        if self.path == "/events":
            self.send_response(200)
            self.send_header("Content-Type", "text/event-stream")
            self.send_header("Cache-Control", "no-cache")
            self.end_headers()
            q = queue.Queue()
            with clients_lock:
                clients.append(q)
            try:
                while True:
                    try:
                        self.wfile.write(f"data: {q.get(timeout=15)}\n\n".encode())
                    except queue.Empty:
                        self.wfile.write(b": keep-alive\n\n")
                    self.wfile.flush()
            except OSError:
                pass
            finally:
                with clients_lock:
                    clients.remove(q)
            return
        page = open(os.path.join(HERE, "index.html"), encoding="utf-8").read().replace("__TOKEN__", TOKEN)
        self.reply(200, page.encode(), "text/html; charset=utf-8")

    def do_POST(self):
        if (not self.same_origin() or self.headers.get("X-Dashboard-Token") != TOKEN
                or self.headers.get("Content-Type") != "application/json"):
            return self.reply(403, b'{"ok": false, "text": "refused"}')
        length = int(self.headers.get("Content-Length", 0))
        try:
            result = do_action(json.loads(self.rfile.read(length) or b"{}"))
        except Exception as e:
            feed("fail", str(e))
            result = {"ok": False, "text": str(e)}
        self.reply(200, json.dumps(result).encode())


if __name__ == "__main__":
    if not os.path.exists(JAR):
        sys.exit(f"Build airports-routes first: ./mvnw -pl airports-routes -am package -DskipTests ({JAR})")
    threading.Thread(target=poll_state, daemon=True).start()
    threading.Thread(target=follow_events, daemon=True).start()
    server = http.server.ThreadingHTTPServer(("127.0.0.1", PORT), Handler)
    server.daemon_threads = True
    print(f"Events dashboard on http://127.0.0.1:{PORT}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        rabbit("DELETE", "/queues/%2F/" + FEED_QUEUE)
