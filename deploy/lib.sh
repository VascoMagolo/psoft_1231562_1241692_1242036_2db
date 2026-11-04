# shellcheck shell=bash
# Shared by setup.sh and failure-test.sh: the configuration and how to reach the nodes.
# Needs bash, ssh and curl on this computer.

DEPLOY_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
CONFIG=${CONFIG:-$DEPLOY_DIR/config.sh}
if [ ! -f "$CONFIG" ]; then
  echo "Missing $CONFIG: copy deploy/config.example.sh to it and adjust it (see deploy/README.md)." >&2
  exit 1
fi
# shellcheck source=config.example.sh
source "$CONFIG"

NODE_NAMES=() NODE_IPS=()
for entry in "${NODES[@]}"; do
  read -r name ip <<< "$entry"
  NODE_NAMES+=("$name")
  NODE_IPS+=("$ip")
done

# One SSH connection per host, reused by the next commands; a dead host is noticed within ~6 s.
# The nodes' identities are kept apart (a recreated VM has a new one).
KNOWN_HOSTS=$DEPLOY_DIR/.known_hosts
SSH=(ssh -o BatchMode=yes -o ConnectTimeout=5 -o StrictHostKeyChecking=accept-new
  -o "UserKnownHostsFile=$KNOWN_HOSTS"
  -o ControlMaster=auto -o "ControlPath=${XDG_RUNTIME_DIR:-/tmp}/aisafe-ssh-%C" -o ControlPersist=60
  -o ServerAliveInterval=2 -o ServerAliveCountMax=2)
NODE_SSH=("${SSH[@]}")
[ -z "${SSH_JUMP:-}" ] || NODE_SSH+=(-J "$SSH_JUMP")

log() { printf '\n==> %s\n' "$*"; }

# Runs a command as root on a node. Its stdin goes to the command.
on_node() {   # on_node IP COMMAND...
  local ip=$1
  shift
  "${NODE_SSH[@]}" "$NODE_USER@$ip" "sudo bash -euo pipefail -c $(printf '%q' "$*")"
}

# kubectl on the first node that answers (the control plane runs on every node).
kube() {
  local ip
  for ip in "${NODE_IPS[@]}"; do
    # The probe must not read stdin: that is where "kube apply -f -" gets the manifest.
    if "${NODE_SSH[@]}" "$NODE_USER@$ip" true < /dev/null 2> /dev/null; then
      "${NODE_SSH[@]}" "$NODE_USER@$ip" "sudo k3s kubectl $(printf '%q ' "$@")"
      return
    fi
  done
  echo "No node answers." >&2
  return 1
}

# The entries' certificate (HTTPS on the virtual IPs, made by setup.sh), saved here for the clients
# on this computer to trust it: it is self-signed.
ENTRY_CERT=$DEPLOY_DIR/entry.crt
save_entry_cert() {
  kube -n aisafe get secret entry-tls -o 'jsonpath={.data.tls\.crt}' | base64 -d > "$ENTRY_CERT.new"
  mv "$ENTRY_CERT.new" "$ENTRY_CERT"
}

# Every node ready and without the vigia's marks, every pod of AISafe running and ready, and one
# replica of each service (and one login) per node.
system_normal() {
  local nodes pods app
  # One line per node: "True" when ready, then its marks.
  nodes=$(kube get nodes -o 'jsonpath={range .items[*]}{.status.conditions[?(@.type=="Ready")].status} {.spec.taints[*].key}{"\n"}{end}' 2> /dev/null) || return 1
  [ "$(grep -c '^True' <<< "$nodes")" -eq "${#NODE_IPS[@]}" ] || return 1
  ! grep -q -E 'aisafe/|out-of-service|unreachable|not-ready' <<< "$nodes" || return 1
  pods=$(kube -n aisafe get pods --no-headers -o wide 2> /dev/null) || return 1
  awk '{ split($2, r, "/"); if ($3 != "Running" || r[1] != r[2]) bad = 1 } END { exit bad }' <<< "$pods" || return 1
  for app in aircraft-maintenance airports-routes flight-ops login rabbitmq; do
    [ "$(grep -c "^$app-[0-9]" <<< "$pods")" -eq "${#NODE_IPS[@]}" ] || return 1
    [ "$(awk -v a="^$app-[0-9]" '$1 ~ a { print $7 }' <<< "$pods" | sort -u | wc -l)" -eq "${#NODE_IPS[@]}" ] || return 1
  done
}

wait_normal() {   # wait_normal SECONDS
  local deadline=$((SECONDS + $1))
  until system_normal; do
    [ "$SECONDS" -lt "$deadline" ] || { echo "The system is not normal after $1 s." >&2; return 1; }
    sleep 2
  done
}
