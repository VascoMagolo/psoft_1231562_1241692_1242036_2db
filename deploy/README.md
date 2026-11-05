# Deployment

One script builds the whole AISafe environment: three machines, a k3s cluster on them, the
services with three replicas each, a login on every machine, and the vigia that turns machines
back on and moves replicas when a machine is lost. Running it again brings the environment up to
date and changes only what changed.

```
deploy/
├── config.example.sh     # everything that differs per person: copy to config.sh
├── setup.sh              # builds or updates the environment
├── lib.sh                # shared by the scripts: configuration, SSH to the nodes
├── proxmox/              # only for VMs on Proxmox (the demo)
├── manifests/            # the services (aisafe.yaml), the entries (envoy.yaml), failure-test.sh
├── vigia/                # the watcher of the machines (runs in the cluster)
└── dashboard/            # live map of the cluster and its requests, for demonstrations
```

## What it builds

| Piece                                                                | Where                                        | Why                                                                                    |
| -------------------------------------------------------------------- | -------------------------------------------- | -------------------------------------------------------------------------------------- |
| 3 machines (k3s nodes)                                               | VMs on Proxmox, or existing machines         | No single point of failure: any one can be lost                                        |
| k3s, control plane on the 3                                          | every node                                   | The cluster keeps being managed with one node down                                     |
| MetalLB                                                              | every node                                   | Virtual IPs that move to another node when theirs is lost                              |
| Entry (Envoy)                                                        | every node                                   | Sends each request to a ready replica, and tries another when one fails                |
| aircraft-maintenance ×3, airports-routes ×3, flight-ops ×3, login ×3 | one of each per node, each with its own disk | Replication between replicas (see the system documentation)                            |
| RabbitMQ ×3 (Assignment 2)                                           | one broker per node, each with its own disk  | Events and commands between the services; quorum queues survive the loss of a node     |
| Vigia                                                                | every node, one acting                       | Turns lost machines back on, isolates those that do not come back, gives replicas back |

On Proxmox, the VMs also get a watchdog: a VM whose system freezes is restarted by Proxmox.

## Before starting

- A computer with `bash`, `ssh`, `curl` and `git`: Linux, macOS, or Windows **with WSL**. Git Bash does
  not work: the scripts reuse one SSH connection for many commands, which the SSH of Windows does
  not support.
- **Proxmox:** SSH as root to the Proxmox host with a key, and a network bridge for the VMs with
  internet access. The VMs accept the keys that log in to Proxmox as root.
- **Existing machines:** three machines with Debian or Ubuntu, reachable by SSH with a key, as a
  user with `sudo`.
- About 4 GB of memory per machine.
- **Internet on the machines**: they download Debian packages, k3s, MetalLB and the images (Docker
  Hub and GitHub). A network that needs a proxy, or blocks those sites, makes the setup fail.
- **Fixed addresses you can choose**: three for the machines and a few for the virtual IPs, free and
  in the machines' network (outside the router's DHCP range). Where addresses cannot be chosen
  (e.g. eduroam), give the VMs an internal Proxmox bridge, with Proxmox routing it to the internet.
- If this computer cannot reach the machines directly, set `SSH_JUMP` (e.g. `root@<proxmox>`).
- Tested with Proxmox VE 9.2 (at least 7.2 is needed) and Debian 13 machines; the machines must use
  `apt` (Debian or Ubuntu).

### Proxmox inside another VM (VMware, VirtualBox, libvirt)

Proxmox does not need to run on its own machine, but the program that runs it must allow two things:

| Requirement                                                          | Why                                                                                                                                                                            | Where                                                                                                                                                                                                        |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Virtualization inside the VM (Intel VT-x or AMD-V passed to Proxmox) | The cluster's VMs use the host's processor: without it they do not start                                                                                                       | VMware: "Virtualize Intel VT-x/EPT or AMD-V/RVI" in the Proxmox VM's processor settings. VirtualBox: "Enable Nested VT-x/AMD-V". libvirt: CPU mode `host-passthrough`                                        |
| A network that accepts addresses that are not the Proxmox VM's own   | The cluster's VMs and the virtual IPs have their own network addresses; if only the Proxmox VM's own is let through, the VMs have no network and the virtual IPs do not answer | VMware ESXi: accept "Promiscuous mode", "MAC address changes" and "Forged transmits" on the port group (Workstation in bridged mode usually works). VirtualBox: "Promiscuous Mode: Allow All" on the adapter |

Putting the VMs on an internal Proxmox bridge, with Proxmox routing it, avoids the second one.

## Setting up

```sh
cp deploy/config.example.sh deploy/config.sh
# adjust deploy/config.sh: machines, network, virtual IPs
deploy/setup.sh
```

It takes about 15 minutes the first time. At the end it waits until everything is normal, tries a
login and a request through the virtual IPs, and prints the addresses.

The virtual IPs answer only with **HTTPS** (port 443). The certificate is made by `setup.sh app` on a
node, for the four virtual IPs, and kept in the Secret `entry-tls` (made again when the virtual IPs
change). It is self-signed, so clients must trust it: `setup.sh` saves it in `deploy/entry.crt`
(`curl --cacert deploy/entry.crt https://<virtual IP>/...`). Inside the cluster the requests still go
in plain HTTP (see the limitations in the system documentation).

| Stage    | Command                    | What it does                                                                                       |
| -------- | -------------------------- | -------------------------------------------------------------------------------------------------- |
| machines | `deploy/setup.sh machines` | Creates or updates the VMs on Proxmox (nothing with `MACHINES=existing`)                           |
| cluster  | `deploy/setup.sh cluster`  | Prepares the nodes, installs k3s one node at a time, and MetalLB                                   |
| app      | `deploy/setup.sh app`      | Deploys RabbitMQ and its users, the services, the logins and the entries; also after a code change |
| vigia    | `deploy/setup.sh vigia`    | Deploys the vigia and gives it access to the machines                                              |
| check    | `deploy/setup.sh check`    | Waits until everything is normal and tries the services                                            |

## RabbitMQ (Assignment 2)

`setup.sh app` deploys three brokers (`deploy/manifests/rabbitmq.yaml`) before the services, and:

- makes the Secret `aisafe-rabbitmq` the first time: the Erlang cookie of the brokers, the password of
  `admin`, and one password per service and for `postman`, all random;
- creates, inside a broker, one user per service with the permissions it needs and nothing more (its
  own exchange and queues, the shared exchanges of commands and dead letters, the events of the
  services it reads), and the user `postman` for the tests;
- starts the services with `EVENTS_ENABLED=true` and their own user.

The console and the management API are at `https://<VIP_RABBITMQ>` (add `VIP_RABBITMQ` to an older
`deploy/config.sh`, from `config.example.sh`), with the entries' certificate. To log in as `admin`:

```sh
ssh sidis@<node> "sudo k3s kubectl -n aisafe get secret aisafe-rabbitmq -o go-template='{{index .data \"admin-password\"}}'" | base64 -d
```

`deploy/manifests/failure-test.sh rabbitmq-crash` kills a broker while requests run.

## Images of the services

| `IMAGES`         | Where they are built                                                                                | When to use                                                                          |
| ---------------- | --------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------ |
| `ghcr` (default) | GitHub Actions, at every push (`.github/workflows/images.yml`), published in `ghcr.io/<repository>` | Normal use: nobody needs Java to deploy, and a new node fetches the images by itself |
| `local`          | Here (needs Java 25), then copied to every node                                                     | Testing changes not pushed yet                                                       |

With `ghcr`, `IMAGE_TAG` is a branch (its latest commit on GitHub) or a commit. The images must be
public (package settings on GitHub).

## The vigia

| The machine is…                                      | The vigia…                                                                                                    |
| ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------- |
| off                                                  | marks the node (its replicas leave the traffic and wait for it) and turns it on                               |
| down for `REVIVE_TIMEOUT` seconds, certainly stopped | isolates the node: its replicas are reborn on the other nodes, empty, and recover their data from their peers |
| back and ready                                       | removes the marks, and gives the replicas back one at a time                                                  |
| on but not answering (network cut)                   | nothing: its replicas may be alive                                                                            |
| tagged `maintenance` in Proxmox                      | does not turn it on, and isolates it as soon as it is off                                                     |

It asks the machines through a driver. On Proxmox it uses the API, with a token that can only see
and turn on or off the cluster's VMs; port 8006 of Proxmox must be open from the nodes. With
`MACHINES=existing` it knows nothing about the machines: it still gives replicas back, but a
person turns machines on and isolates a lost one:

```sh
kubectl taint node <node> node.kubernetes.io/out-of-service=nodeshutdown:NoExecute
```

On physical machines, a driver for their management boards (for example Redfish) would answer the
same questions as the Proxmox one (`deploy/vigia/vigia.sh`).

## Checking that no request fails

```sh
deploy/manifests/failure-test.sh                        # every scenario
deploy/manifests/failure-test.sh node-off               # only one
```

It sends requests without pause to the virtual IPs while it causes one failure at a time (a replica
or a login killed, a deploy, a node powered off, a node frozen), and counts the requests that failed
or got a wrong answer. Run it from a computer that reaches the virtual IPs (Linux, macOS or WSL).

## Watching it live (demo dashboard)

```sh
deploy/dashboard/dashboard.sh                           # then open http://localhost:8099
```

A map in the browser, for demonstrations (not for production), of the whole path of a request:
this computer on top, the network the machines share (behind the router: on Proxmox, the Proxmox
host), and the three machines at the corners of a triangle. There are only two kinds of lines: the
network line down to each machine, carrying the virtual IPs that machine answers for (MetalLB), and
one link between each pair of machines. Every request travels along them, in its service's colour:

- from the client through the network to the machine that answers for the virtual IP, and into its
  entry (Envoy);
- from the entry to a replica: on the same machine, or out along the link to another machine (again
  to another replica when one fails);
- between two replicas: asking for a record, or sending a copy (labelled with the answer);
- between services, through the entry of the machine.

The page shows what changes as it happens: a replica starting, stopping or failing; a machine off,
starting, frozen or isolated, and for how long; a virtual IP moving to another machine; a replica
away from its machine (and, in its machine, where it is); a replica that stopped asking a peer
(✕ and the peer's number on the replica); the vigia that acts, and the vigia turning a machine on through the Proxmox API. From the page you can send requests (through a virtual IP,
or straight to one replica), start steady traffic, kill a replica (click it), power off or freeze a
machine, and restart a service one replica at a time. The counters show what the requests sent from
the page got: "try again" is the 503 with `Retry-After` that a replica gives while it cannot be sure
a record does not exist.

It runs on this computer, outside the cluster, so that it keeps showing everything while machines
fail; it needs `python3`, and to reach the nodes (port 6443) and the virtual IPs. It follows the
logs: the entries write one line per request and one per attempt, and the replicas log each call to
a peer, all under the request's correlation id (`X-Correlation-Id`, given by the entry).

## Reading the audit trail

Every replica writes one JSON line per request to the API it answered (who, what, result, when),
in `/data/audit.log` on its own disk, apart from its other logs:

```sh
kubectl -n aisafe exec aircraft-maintenance-0 -- tail -n 20 /data/audit.log
kubectl -n aisafe exec flight-ops-1 -- grep '"status":403' /data/audit.log    # refused requests
```

<!-- prettier-ignore -->
```json
{"time":"2026-10-26T15:40:02.114Z","user":"atcc","roles":["ATCC"],"method":"POST","path":"/api/flights","status":201,"node":"flight-ops-1","correlationId":"9c2e…","forwardedBy":null,"ms":184}
```

## When something fails

The script stops at the first error and says where. After fixing it, run it again.

| Symptom                                 | Likely cause                                                            | Check                                                        |
| --------------------------------------- | ----------------------------------------------------------------------- | ------------------------------------------------------------ |
| "does not answer by SSH"                | The VM did not start, or did not get its IP                             | Proxmox console of the VM; `qm cloudinit dump <id> network`  |
| k3s fails to download                   | No internet or DNS on the node                                          | `ping` the gateway and `getent hosts get.k3s.io` on the node |
| "No public image"                       | GitHub Actions did not build that commit yet, or the package is private | The Images workflow on GitHub; the package settings          |
| "cannot reach the Proxmox API"          | A firewall between the nodes and Proxmox                                | `curl -k https://<PROXMOX_API_IP>:8006` from a node          |
| Replicas stay `Pending`                 | Not enough memory on a node                                             | `kubectl -n aisafe describe pod <pod>`                       |
| A machine that was off is not turned on | The vigia is not running, or the VM is tagged `maintenance`             | `kubectl -n aisafe-vigia logs -l app=vigia`                  |
