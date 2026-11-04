# Events dashboard (Assignment 2, demo)

A live view of the airports-routes events and read models, for demonstrations: the write side, the
exchange `airports-routes.events`, one queue per replica and each replica's read model. Every event
travels across the picture in its entity's colour (`route.*`, `airport.*`, `flightcopy.*`); a queue
whose replica is down shows the messages waiting for it. Not for production.

From the page you can create a route, register an airport, send a copy of a flight (C5), stop and
start a replica, and stop and start RabbitMQ. The tables show what each replica's read model lists.

## Running it

Needs the local setup of [`docs/postman/README.md`](../../../docs/postman/README.md#assignment-2-airports-routes-events)
(RabbitMQ in Docker as the container `rabbitmq`, the login on 8085, the three replicas on
18090-18092 with `EVENTS_ENABLED=true`). Then, from the repository root:

```sh
python3 airports-routes/demo/events-dashboard/server.py     # then open http://127.0.0.1:8098
```

Python 3 standard library only. A replica started from the page logs to
`airports-routes/target/dashboard-logs/`.

| Variable | Default |
|---|---|
| `DASHBOARD_PORT` | 8098 |
| `LOGIN_URL` | http://localhost:8085 |
| `RABBIT_API` | http://127.0.0.1:15672/api |
| `RABBIT_USER_PASSWORD` | guest:guest |
| `RABBIT_CONTAINER` | rabbitmq |

The queue counts come from the RabbitMQ management API, which refreshes them every 5 s: a message
waiting for a stopped replica shows up a few seconds after its event.

## Security

It listens on 127.0.0.1 only. It answers only requests addressed to itself (`Host`), from its own
page (`Origin`); every command needs `Content-Type: application/json` and a token made on each start
and written into the page. Another site open in the same browser cannot send it commands.
