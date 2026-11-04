#!/usr/bin/env bash
# Failure test: sends requests without pause to the virtual IPs while it causes one failure at a
# time, then counts the requests that failed or got a wrong answer. The goal is zero.
# Run from a computer that reaches the virtual IPs, with the configuration in deploy/config.sh (the
# same as for deploy/setup.sh):
#   deploy/manifests/failure-test.sh                        # every scenario, in this order
#   deploy/manifests/failure-test.sh replica-crash deploy   # only these
#
#   replica-crash   an aircraft-maintenance replica is killed
#   routes-crash    an airports-routes replica is killed
#   flight-ops-crash a flight-ops replica is killed
#   login-crash     a login is killed
#   rabbitmq-crash  a RabbitMQ broker is killed (Assignment 2: events and the Saga's commands)
#   deploy          every service restarts, one replica at a time (as deploy/setup.sh does)
#   node-off        a node loses power
#   node-freeze     a node freezes (its kernel stops; the watchdog has to restart it)
#
# The node scenarios work the same on a VM and on a physical machine: the node's own kernel is
# told to power off or to crash. Each request is logged in target/failure-test/<run>/.
set -euo pipefail
source "$(dirname "$0")/../lib.sh"
cd "$DEPLOY_DIR/.."

APP_URL=${APP_URL:-https://$VIP_AIRCRAFT_MAINTENANCE}
ROUTES_URL=${ROUTES_URL:-https://$VIP_AIRPORTS_ROUTES}
FLIGHTS_URL=${FLIGHTS_URL:-https://$VIP_FLIGHT_OPS}
LOGIN_URL=${LOGIN_URL:-https://$VIP_LOGIN}
TARGET_NODE=${TARGET_NODE:-${NODE_IPS[${#NODE_IPS[@]}-1]}}   # the node hit by node-off and node-freeze
INTERVAL=${INTERVAL:-0.2}                     # pause between requests, in each worker
SETTLE=${SETTLE:-15}                          # seconds of traffic once the system is back to normal
MAX_RECOVERY=${MAX_RECOVERY:-600}             # seconds to wait for the system to be normal again
ALL_SCENARIOS=(replica-crash routes-crash flight-ops-crash login-crash rabbitmq-crash deploy node-off node-freeze)

# Milliseconds since 1970. The date of macOS has no milliseconds: perl, which macOS has, gives them.
if [[ $(date +%s%3N) =~ ^[0-9]+$ ]]; then
  now_ms() { date +%s%3N; }
else
  now_ms() { perl -MTime::HiRes=time -e 'printf "%d\n", time * 1000'; }
fi

# --- What "normal" means: system_normal and wait_normal, in lib.sh ----------------------------

# Right after the failure the system can still look normal (a frozen node is only noticed ~20 s
# later), so first wait until it shows the failure, then until it is normal again.
wait_recovery() {
  local deadline=$((SECONDS + 90))
  while system_normal; do
    [ "$SECONDS" -lt "$deadline" ] || return 0
    sleep 2
  done
  wait_normal "$MAX_RECOVERY"
}

# --- Traffic ----------------------------------------------------------------------------------

# One line per request: time (ms), check, HTTP status (000 = no answer), ok/fail/wrong, seconds
# the request took, detail.
record() { printf '%s %s %s %s %s %s\n' "$(now_ms)" "$1" "$2" "$3" "$TOOK" "${4:-}" >> "$LOG"; }

# A request fails when it gets no answer in CLIENT_TIMEOUT seconds, as for a client that waits a
# little (a browser waits much longer); how long the slowest one took is in the report.
CLIENT_TIMEOUT=${CLIENT_TIMEOUT:-15}

call() {   # call METHOD URL [CURL-ARGS...]: sets STATUS and TOOK; the answer goes to $BODY and $HEADERS
  local method=$1 url=$2 out
  shift 2
  out=$(curl -s --cacert "$ENTRY_CERT" -o "$BODY" -D "$HEADERS" -w '%{http_code} %{time_total}' --max-time "$CLIENT_TIMEOUT" \
    -X "$method" "$@" "$url") || true
  STATUS=${out% *} TOOK=${out#* }
}

json=(-H 'Content-Type: application/json')

random_letters() {   # random_letters COUNT
  local letters=ABCDEFGHIJKLMNOPQRSTUVWXYZ out=
  for _ in $(seq "$1"); do out+=${letters:RANDOM%26:1}; done
  echo "$out"
}

# An aircraft that exists must always be found.
read_worker() {
  while :; do
    call GET "$APP_URL/api/aircrafts/CS-TKA" -H "$AUTH"
    case $STATUS in
      200) record read 200 ok ;;
      404) record read 404 wrong "existing aircraft not found" ;;
      *) record read "$STATUS" fail ;;
    esac
    sleep "$INTERVAL"
  done
}

# The list must be complete: not flagged partial, with every seeded aircraft.
list_worker() {
  while :; do
    call GET "$APP_URL/api/aircrafts?size=1000" -H "$AUTH"
    if [ "$STATUS" != 200 ]; then
      record list "$STATUS" fail
    elif grep -qi '^x-partial-result: *true' "$HEADERS"; then
      record list 200 wrong "partial result"
    elif ! grep -q CS-TKA "$BODY" || ! grep -q CS-TKB "$BODY" || ! grep -q CS-TKC "$BODY"; then
      record list 200 wrong "seeded aircraft missing"
    else
      record list 200 ok
    fi
    sleep "$INTERVAL"
  done
}

# Creates an aircraft, reads it back and deletes it, with a random registration each time
# (26^5 possible: an aircraft left behind by an earlier run is not hit by chance).
write_worker() {
  local reg
  while :; do
    reg=$(random_letters 2)-$(random_letters 3)
    call POST "$APP_URL/api/aircrafts" -H "$AUTH" "${json[@]}" -d "{\"registrationNumber\":\"$reg\",
      \"modelName\":\"Airbus A320neo\",\"manufacturingDate\":\"2020-01-01\",\"seatCapacity\":150,
      \"range\":5000.0,\"status\":\"AVAILABLE\",\"features\":[]}"
    if [ "$STATUS" != 201 ]; then
      record create "$STATUS" fail "$reg"
      sleep "$INTERVAL"
      continue
    fi
    record create 201 ok
    call GET "$APP_URL/api/aircrafts/$reg" -H "$AUTH"
    case $STATUS in
      200) record read-new 200 ok ;;
      404) record read-new 404 wrong "aircraft created just before not found: $reg" ;;
      *) record read-new "$STATUS" fail "$reg" ;;
    esac
    call DELETE "$APP_URL/api/aircrafts/$reg" -H "$AUTH"
    case $STATUS in
      204) record delete 204 ok ;;
      404) record delete 404 wrong "aircraft created just before not found: $reg" ;;
      *) record delete "$STATUS" fail "$reg" ;;
    esac
    sleep "$INTERVAL"
  done
}

# A route that exists must always be found.
route_read_worker() {
  while :; do
    call GET "$ROUTES_URL/api/routes/LIS/OPO" -H "$AUTH"
    case $STATUS in
      200) record route-read 200 ok ;;
      404) record route-read 404 wrong "existing route not found" ;;
      *) record route-read "$STATUS" fail ;;
    esac
    sleep "$INTERVAL"
  done
}

# The routes of every replica must be listed: not flagged partial, with seeded routes held by
# different replicas.
route_list_worker() {
  local route
  while :; do
    call GET "$ROUTES_URL/api/routes/search?size=1000" -H "$AUTH"
    if [ "$STATUS" != 200 ]; then
      record route-list "$STATUS" fail
    elif grep -qi '^x-partial-result: *true' "$HEADERS"; then
      record route-list 200 wrong "partial result"
    else
      for route in OPO:LIS LIS:MAD CDG:LHR; do
        grep -q "\"originIataCode\":\"${route%:*}\",\"destinationIataCode\":\"${route#*:}\"" "$BODY" || break
        route=
      done
      if [ -n "$route" ]; then record route-list 200 wrong "seeded route missing: $route"; else record route-list 200 ok; fi
    fi
    sleep "$INTERVAL"
  done
}

# Creates a route that is not in the sample data, reads it back and deletes it. Creating it again
# after the deletion is a newer change, which wins over the deletion on every replica. The workers
# of the previous scenario were stopped at any point, maybe between creating and deleting: the
# route they left behind is deleted first, without counting.
route_write_worker() {
  curl -s --cacert "$ENTRY_CERT" -o /dev/null --max-time "$CLIENT_TIMEOUT" -X DELETE -H "$AUTH" "$ROUTES_URL/api/routes/LHR/MAD" || true
  while :; do
    call POST "$ROUTES_URL/api/routes" -H "$AUTH" "${json[@]}" -d '{"originIataCode":"LHR","destinationIataCode":"MAD",
      "estimatedFlightTime":150,"minimumRange":1300.0,"minimumCapacity":120,"createdBy":"failure-test"}'
    if [ "$STATUS" != 201 ]; then
      record route-create "$STATUS" fail
      # A create that got no answer may still have been stored: delete it, without counting, or
      # every later create of the same route is refused as a duplicate.
      curl -s --cacert "$ENTRY_CERT" -o /dev/null --max-time "$CLIENT_TIMEOUT" -X DELETE -H "$AUTH" "$ROUTES_URL/api/routes/LHR/MAD" || true
      sleep "$INTERVAL"
      continue
    fi
    record route-create 201 ok
    call GET "$ROUTES_URL/api/routes/LHR/MAD" -H "$AUTH"
    case $STATUS in
      200) record route-read-new 200 ok ;;
      404) record route-read-new 404 wrong "route created just before not found" ;;
      *) record route-read-new "$STATUS" fail ;;
    esac
    call DELETE "$ROUTES_URL/api/routes/LHR/MAD" -H "$AUTH"
    case $STATUS in
      204) record route-delete 204 ok ;;
      404) record route-delete 404 wrong "route created just before not found" ;;
      *) record route-delete "$STATUS" fail ;;
    esac
    sleep "$INTERVAL"
  done
}


# Schedules a flight with random schedule, reads it back.
flight_worker() {
  local ac year month day hour min dep arr id
  # Sample aircraft that can take flights (CS-TKB is under maintenance: its flights get 409)
  local aircrafts=(CS-TKA CS-TKC)
  while :; do
    ac=${aircrafts[$((RANDOM % ${#aircrafts[@]}))]}
    year=$((2100 + RANDOM % 7000))
    month=$(printf '%02d' $((1 + RANDOM % 12)))
    day=$(printf '%02d' $((1 + RANDOM % 28)))
    hour=$(printf '%02d' $((RANDOM % 24)))
    min=$(printf '%02d' $((RANDOM % 59)))
    dep="${year}-${month}-${day}T${hour}:${min}:00Z"
    arr="${year}-${month}-${day}T${hour}:${min}:30Z"
    call POST "$FLIGHTS_URL/api/flights" -H "$AUTH" "${json[@]}" -d "{\"aircraftId\":\"$ac\",
      \"originIataCode\":\"LIS\",\"destinationIataCode\":\"OPO\",\"departureDateTime\":\"$dep\",\"arrivalDateTime\":\"$arr\"}"
    if [ "$STATUS" != 201 ]; then
      record flight-schedule "$STATUS" fail
      sleep "$INTERVAL"
      continue
    fi
    record flight-schedule 201 ok
    id=$(sed -n 's/.*"id":\([0-9]*\).*/\1/p' "$BODY")
    call GET "$FLIGHTS_URL/api/flights?aircraftId=$ac" -H "$AUTH"
    case $STATUS in
      200)
        if { [ -n "$id" ] && grep -q "\"id\":$id" "$BODY"; } || grep -q "\"departureDateTime\":\"$dep\"" "$BODY"; then
          record flight-read 200 ok
        else
          record flight-read 200 wrong "flight scheduled just before not found"
        fi ;;
      *) record flight-read "$STATUS" fail ;;
    esac
    sleep "$INTERVAL"
  done
}

# Valid credentials must always be accepted, by whichever login answers.
login_worker() {   # login_worker CHECK USER PASSWORD
  while :; do
    call POST "$LOGIN_URL/api/auth/login" "${json[@]}" -d "{\"username\":\"$2\",\"password\":\"$3\"}"
    case $STATUS in
      200) record "$1" 200 ok ;;
      401) record "$1" 401 wrong "valid credentials refused" ;;
      *) record "$1" "$STATUS" fail ;;
    esac
    sleep "$INTERVAL"
  done
}

WORKERS=()
start_workers() {
  local name
  for name in read list write route_read route_list route_write flight; do
    BODY=$RUN/.body-$name HEADERS=$RUN/.headers-$name "${name}_worker" &
    WORKERS+=($!)
  done
  BODY=$RUN/.body-login HEADERS=$RUN/.headers-login login_worker login atcc atcc123 &
  WORKERS+=($!)
  BODY=$RUN/.body-login-new HEADERS=$RUN/.headers-login-new login_worker login-new "$NEW_USER" "$NEW_PASSWORD" &
  WORKERS+=($!)
}

stop_workers() {
  [ "${#WORKERS[@]}" -eq 0 ] && return
  kill "${WORKERS[@]}" 2> /dev/null || true
  wait "${WORKERS[@]}" 2> /dev/null || true
  WORKERS=()
}
trap stop_workers EXIT

# --- Failures ---------------------------------------------------------------------------------

# Tells a node's kernel to power off at once (o) or to crash and stay frozen (c). The SSH
# connection dies with the node, so it is not waited for.
node_kernel() {
  "${NODE_SSH[@]}" "$NODE_USER@$TARGET_NODE" \
    "sudo sh -c 'sysctl -q kernel.panic=0; echo $1 > /proc/sysrq-trigger'" < /dev/null > /dev/null 2>&1 &
}

cause() {
  case $1 in
    replica-crash) kube -n aisafe delete pod aircraft-maintenance-1 --grace-period=0 --force 2> /dev/null ;;
    routes-crash) kube -n aisafe delete pod airports-routes-1 --grace-period=0 --force 2> /dev/null ;;
    flight-ops-crash) kube -n aisafe delete pod flight-ops-1 --grace-period=0 --force 2> /dev/null ;;
    login-crash) kube -n aisafe delete "$(kube -n aisafe get pods -l app=login -o name | head -1)" --grace-period=0 --force 2> /dev/null ;;
    rabbitmq-crash) kube -n aisafe delete pod rabbitmq-1 --grace-period=0 --force 2> /dev/null ;;
    deploy)
      kube -n aisafe rollout restart statefulset/aircraft-maintenance statefulset/airports-routes statefulset/flight-ops statefulset/login
      kube -n aisafe rollout status statefulset/aircraft-maintenance --timeout=600s
      kube -n aisafe rollout status statefulset/airports-routes --timeout=600s
      kube -n aisafe rollout status statefulset/flight-ops --timeout=600s
      kube -n aisafe rollout status statefulset/login --timeout=600s ;;
    node-off) node_kernel o ;;
    node-freeze) node_kernel c ;;
    *) echo "Unknown scenario: $1 (known: ${ALL_SCENARIOS[*]})" >&2; return 1 ;;
  esac
}

# --- Report -----------------------------------------------------------------------------------

# One line per check: requests, failed and wrong answers (with their HTTP status), the slowest
# answer, and when the first and last bad ones happened, in seconds after the failure was caused.
report() {   # report LOG T0
  awk -v t0="$2" '
    { total[$2]++; if (!($2 in order)) order[$2] = ++n; if ($5 > slowest[$2]) slowest[$2] = $5 }
    $4 != "ok" {
      bad[$2]++; codes[$2, $4 " " $3]++; seen[$2] = seen[$2] SUBSEP $4 " " $3
      t = ($1 - t0) / 1000; if (!($2 in first)) first[$2] = t; last[$2] = t
    }
    END {
      printf "  %-10s %8s %8s %9s  %-28s %s\n", "check", "requests", "bad", "slowest", "status (fail/wrong)", "when (s after failure)"
      for (i = 1; i <= n; i++) for (c in order) if (order[c] == i) {
        detail = ""; split(seen[c], keys, SUBSEP); delete done
        for (k in keys) if (keys[k] != "" && !(keys[k] in done)) {
          done[keys[k]] = 1; detail = detail keys[k] "×" codes[c, keys[k]] " "
        }
        when = (c in first) ? sprintf("%.1f to %.1f", first[c], last[c]) : ""
        printf "  %-10s %8d %8d %8.1fs  %-28s %s\n", c, total[c], bad[c], slowest[c], detail, when
      }
    }' "$1"
}

# --- Run --------------------------------------------------------------------------------------

scenarios=("$@")
[ "${#scenarios[@]}" -gt 0 ] || scenarios=("${ALL_SCENARIOS[@]}")
for scenario in "${scenarios[@]}"; do
  [[ " ${ALL_SCENARIOS[*]} " == *" $scenario "* ]] || { echo "Unknown scenario: $scenario (known: ${ALL_SCENARIOS[*]})" >&2; exit 1; }
done

RUN=target/failure-test/$(date +%Y%m%d-%H%M%S)
mkdir -p "$RUN"

log "Waiting until the system is normal"
wait_normal "$MAX_RECOVERY"
save_entry_cert   # the virtual IPs answer with HTTPS, with a self-signed certificate

token_of() {   # token_of USER PASSWORD
  local token
  token=$(curl -sf --cacert "$ENTRY_CERT" --max-time 5 "${json[@]}" -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$LOGIN_URL/api/auth/login" \
    | sed -n 's/.*"token":"\([^"]*\)".*/\1/p') || true
  [ -n "$token" ] || { echo "Could not log in as $1 at $LOGIN_URL." >&2; return 1; }
  echo "$token"
}
AUTH="Authorization: Bearer $(token_of atcc atcc123)"
# A user created now (only an administrator creates accounts): logging in with it checks that
# every login knows the accounts created later.
NEW_USER=failure-test-$(date +%s) NEW_PASSWORD=failure-test
curl -sf --cacert "$ENTRY_CERT" --max-time 5 -o /dev/null "${json[@]}" -H "Authorization: Bearer $(token_of admin admin123)" \
  -d "{\"username\":\"$NEW_USER\",\"password\":\"$NEW_PASSWORD\",\"role\":\"ATCC\"}" \
  "$LOGIN_URL/api/auth/register" || { echo "Could not register $NEW_USER at $LOGIN_URL." >&2; exit 1; }

summary=() failed=0
for scenario in "${scenarios[@]}"; do
  log "Scenario $scenario: waiting until the system is normal"
  wait_normal "$MAX_RECOVERY"
  LOG=$RUN/$scenario.log
  : > "$LOG"
  start_workers
  sleep 5   # some traffic before the failure, to compare
  log "Scenario $scenario: causing the failure"
  t0=$(now_ms)
  cause "$scenario"
  recovered=yes
  if [ "$scenario" = deploy ]; then   # the restart already finished: nothing left to notice
    wait_normal "$MAX_RECOVERY" || recovered=no
  else
    wait_recovery || recovered=no
  fi
  recovery_s=$(( ($(now_ms) - t0) / 1000 ))
  sleep "$SETTLE"
  stop_workers

  log "Scenario $scenario: normal again after ${recovery_s} s (recovered: $recovered)"
  report "$LOG" "$t0" | tee "$RUN/$scenario.report"
  bad=$(awk '$4 != "ok"' "$LOG" | wc -l)
  summary+=("$(printf '%-14s %10s %10s %8s' "$scenario" "${recovery_s} s" "$(wc -l < "$LOG")" "$bad")")
  [ "$bad" -eq 0 ] && [ "$recovered" = yes ] || failed=$((failed + 1))
  [ "$recovered" = yes ] || break
done

log "Summary (details in $RUN)"
printf '%-14s %10s %10s %8s\n' scenario recovery requests bad
printf '%s\n' "${summary[@]}"
[ "$failed" -eq 0 ]
