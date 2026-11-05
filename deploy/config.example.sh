# Configuration of one AISafe environment: everything that differs from one person (or one set of
# machines) to another. Copy it and adjust the copy, which is not committed:
#   cp deploy/config.example.sh deploy/config.sh
# Then deploy/setup.sh builds the environment (see deploy/README.md).

# --- Machines ---------------------------------------------------------------------------------
# proxmox: setup.sh creates the VMs on a Proxmox host (the demo).
# existing: the machines already exist (physical machines, or VMs made another way) with Debian
#           or Ubuntu, and NODE_USER can log in to them by SSH and use sudo.
MACHINES=proxmox

# The cluster nodes, "NAME IP" each. Three, so that losing any one leaves a working cluster.
NODES=(
  "k3s1 192.168.124.43"
  "k3s2 192.168.124.44"
  "k3s3 192.168.124.45"
)
NODE_USER=sidis
# SSH to the nodes through this host when this computer cannot reach them directly (for example
# root@<proxmox>). Empty: direct.
SSH_JUMP=

# --- Proxmox (only with MACHINES=proxmox) -------------------------------------------------------
PROXMOX=root@192.168.122.10        # SSH to the Proxmox host
VM_IDS=(143 144 145)               # one per node, in the order of NODES
BRIDGE=vmbr3                       # network of the VMs
CIDR=24
GATEWAY=192.168.124.1
DNS=192.168.122.1
STORAGE=local-lvm                  # where the VM disks go
CORES=2
MEMORY_MB=4096                     # each node runs aircraft-maintenance, airports-routes, flight-ops and a login
DISK_GB=12
WATCHDOG_SEC=30s                   # a VM that freezes for this long is restarted by Proxmox
# The Proxmox address as the nodes see it: the vigia asks Proxmox whether a VM is on, and turns
# it on (port 8006 must be open from the nodes).
PROXMOX_API_IP=192.168.124.1

# --- Cluster ------------------------------------------------------------------------------------
K3S_VERSION=v1.36.5+k3s1
METALLB_VERSION=v0.16.1
# How long k3s waits for a silent node before taking it as lost (k3s default: ~50 s).
NODE_GRACE=20s
# Virtual IPs: free addresses in the nodes' network, handed out by MetalLB.
VIP_POOL=192.168.124.25-192.168.124.31
VIP_AIRCRAFT_MAINTENANCE=192.168.124.25
VIP_AIRPORTS_ROUTES=192.168.124.26
VIP_FLIGHT_OPS=192.168.124.27
VIP_LOGIN=192.168.124.28
VIP_RABBITMQ=192.168.124.29              # RabbitMQ management console and API (Assignment 2)

# --- Images of the services -----------------------------------------------------------------------
# ghcr:  built by GitHub Actions at every push (.github/workflows/images.yml) and pulled by the
#        nodes. IMAGE_TAG is a branch (its latest commit on GitHub) or a commit.
# local: built on this computer (needs Java 25) and copied to the nodes; for changes not pushed.
IMAGES=ghcr
GITHUB_REPO=miguelsiilva1/sidis_group7_3da
IMAGE_TAG=main

# --- Vigia (watcher of the machines) -------------------------------------------------------------
# A node down for this long, and certainly stopped, is isolated: its replicas are reborn on the
# other nodes.
REVIVE_TIMEOUT=120
