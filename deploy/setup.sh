#!/usr/bin/env bash
# Builds a whole AISafe environment as described in deploy/config.sh, or brings an existing one up
# to date: it can be run as many times as needed, and changes only what changed.
# Runs on any computer with bash, ssh, curl and git: Linux, macOS, or Windows with WSL.
#   deploy/setup.sh               # everything, in this order:
#   deploy/setup.sh machines      #   the VMs on Proxmox (nothing with MACHINES=existing)
#   deploy/setup.sh cluster       #   k3s on every node, with MetalLB
#   deploy/setup.sh app           #   the services, the logins and the entries (after a code change)
#   deploy/setup.sh vigia         #   the watcher of the machines
#   deploy/setup.sh check         #   waits until everything is normal and tries a few requests
# Several stages can be given: deploy/setup.sh app check
set -euo pipefail
source "$(dirname "$0")/lib.sh"
REPO_DIR=$(cd "$DEPLOY_DIR/.." && pwd)

# Short fingerprint of stdin, to restart pods when their configuration changes.
hash16() {
  if command -v sha256sum > /dev/null; then sha256sum; else shasum -a 256; fi | cut -c1-16
}

# A Secret as YAML, written here so that its values never appear on a command line of a node.
secret_yaml() {   # secret_yaml NAMESPACE NAME KEY=VALUE...
  printf 'apiVersion: v1\nkind: Secret\nmetadata:\n  name: %s\n  namespace: %s\ndata:\n' "$2" "$1"
  shift 2
  local kv
  for kv in "$@"; do
    printf '  %s: %s\n' "${kv%%=*}" "$(printf '%s' "${kv#*=}" | base64 | tr -d '\n')"
  done
}

# --- machines ---------------------------------------------------------------------------------

on_proxmox() {   # on_proxmox SCRIPT: runs the script on Proxmox, after this configuration
  cat "$CONFIG" "$1" | "${SSH[@]}" "$PROXMOX" "bash -euo pipefail -s"
}

wait_ssh() {   # wait_ssh IP
  for _ in $(seq 90); do
    on_node "$1" true < /dev/null 2> /dev/null && return
    sleep 2
  done
  echo "Node $1 does not answer by SSH after 3 minutes." >&2
  return 1
}

# Waits for a new VM to answer by SSH, starting it again if it stops (the vigia that would do it
# runs in the cluster, which does not exist yet). Seen once: QEMU itself crashed seconds after
# starting a new VM.
wait_vm() {   # wait_vm IP ID
  for _ in $(seq 18); do
    "${SSH[@]}" "$PROXMOX" "qm status $2 | grep -q running || { echo 'VM $2 stopped: starting it again' >&2; qm start $2; }" > /dev/null
    for _ in $(seq 5); do
      on_node "$1" true < /dev/null 2> /dev/null && return
      sleep 2
    done
  done
  echo "Node $1 (VM $2) does not answer by SSH after 3 minutes." >&2
  return 1
}

wait_node_ready() {   # wait_node_ready NAME
  for _ in $(seq 120); do
    [ "$(kube get node "$1" -o 'jsonpath={.status.conditions[?(@.type=="Ready")].status}' 2> /dev/null)" = True ] && return
    sleep 2
  done
  echo "Node $1 is not ready in k3s." >&2
  return 1
}

stage_machines() {
  [ "$MACHINES" = proxmox ] || { log "Machines: already exist (MACHINES=$MACHINES)"; return; }
  log "Machines: VMs ${VM_IDS[*]} on Proxmox ($PROXMOX)"
  local reboots id i
  # A VM created now has a new SSH identity
  for i in "${!NODE_IPS[@]}"; do
    "${SSH[@]}" "$PROXMOX" "qm status ${VM_IDS[$i]}" &> /dev/null || ssh-keygen -q -R "${NODE_IPS[$i]}" -f "$KNOWN_HOSTS" &> /dev/null || true
  done
  reboots=$(on_proxmox "$DEPLOY_DIR/proxmox/create-vms.sh")
  for i in "${!NODE_IPS[@]}"; do wait_vm "${NODE_IPS[$i]}" "${VM_IDS[$i]}"; done
  # Changes that only apply when a VM starts again (e.g. its memory): one VM at a time, each
  # back in the cluster before the next.
  # The list is read in full first: the SSH commands below would otherwise read the rest of it.
  local to_restart=()
  read -r -a to_restart <<< "$(awk '$1 == "reboot" { print $2 }' <<< "$reboots" | tr '\n' ' ')"
  for id in ${to_restart[@]+"${to_restart[@]}"}; do
    for i in "${!VM_IDS[@]}"; do
      [ "${VM_IDS[$i]}" = "$id" ] || continue
      log "Restarting ${NODE_NAMES[$i]} ($id) to apply its new settings"
      "${SSH[@]}" "$PROXMOX" "qm reboot $id" < /dev/null
      wait_ssh "${NODE_IPS[$i]}"
      on_node "${NODE_IPS[$i]}" "command -v k3s" < /dev/null > /dev/null 2>&1 && wait_node_ready "${NODE_NAMES[$i]}"
    done
  done
}

# --- cluster ----------------------------------------------------------------------------------

# What every node needs, VM or physical machine: the modules of the kernel's load balancer (IPVS,
# used by kube-proxy) and the watchdog, which restarts the machine if its system freezes.
prepare_node() {   # prepare_node IP
  local agent=""
  [ "$MACHINES" = proxmox ] && agent=qemu-guest-agent
  on_node "$1" "
    cloud-init status --wait > /dev/null 2>&1 || true   # first boot of a VM: still installing
    export DEBIAN_FRONTEND=noninteractive
    apt-get -o DPkg::Lock::Timeout=300 update -q > /dev/null
    apt-get -o DPkg::Lock::Timeout=300 install -y -q ipvsadm ipset curl openssl $agent > /dev/null
    [ -z '$agent' ] || systemctl start qemu-guest-agent
    printf '%s\n' ip_vs ip_vs_rr ip_vs_wrr ip_vs_sh nf_conntrack br_netfilter overlay > /etc/modules-load.d/k3s.conf
    for m in ip_vs ip_vs_rr ip_vs_wrr ip_vs_sh nf_conntrack br_netfilter overlay; do modprobe \$m; done
    mkdir -p /etc/systemd/system.conf.d
    printf '[Manager]\nRuntimeWatchdogSec=%s\nRebootWatchdogSec=2min\n' '${WATCHDOG_SEC:-30s}' > /etc/systemd/system.conf.d/watchdog.conf
    systemctl daemon-reexec"
}

k3s_config() {   # k3s_config IP
  cat <<EOF
# Written by deploy/setup.sh from deploy/config.sh. Do not edit by hand.
node-ip: $1
tls-san: [$1]
# No servicelb nor traefik: the virtual IPs come from MetalLB, the entries are Envoy.
disable: [servicelb, traefik]
# IPVS with strict ARP: required by MetalLB in L2 mode.
kube-proxy-arg: [proxy-mode=ipvs, ipvs-strict-arp=true]
# Take a silent node as lost after $NODE_GRACE (default: ~50 s).
kube-controller-manager-arg: [node-monitor-grace-period=$NODE_GRACE]
write-kubeconfig-mode: "0600"
EOF
}

# Installs k3s, or installs it again when its configuration changed. One node at a time, each
# ready before the next, so the cluster never loses its control plane.
install_k3s() {   # install_k3s IP NAME TOKEN [FIRST-IP]   (without FIRST-IP: creates the cluster)
  local ip=$1 name=$2 token=$3 first=${4:-} mode config
  if [ -z "$first" ]; then mode="--cluster-init"; else mode="--server https://$first:6443"; fi
  config=$(k3s_config "$ip")
  if on_node "$ip" "command -v k3s > /dev/null && cmp -s /etc/rancher/k3s/config.yaml -" <<< "$config"; then
    echo "k3s on $name: installed, configuration unchanged"
    return
  fi
  log "Installing or reconfiguring k3s $K3S_VERSION on $name"
  on_node "$ip" "mkdir -p /etc/rancher/k3s && cat > /etc/rancher/k3s/config.yaml" <<< "$config"
  on_node "$ip" "curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION='$K3S_VERSION' K3S_TOKEN='$token' sh -s - server $mode > /dev/null"
  wait_node_ready "$name"
}

install_metallb() {
  log "MetalLB $METALLB_VERSION (virtual IPs $VIP_POOL)"
  kube apply -f "https://raw.githubusercontent.com/metallb/metallb/$METALLB_VERSION/config/manifests/metallb-native.yaml" > /dev/null
  kube -n metallb-system rollout status deploy/controller --timeout=300s
  kube -n metallb-system rollout status ds/speaker --timeout=300s
  # The cluster is AISafe's: address pools from before (e.g. a proof of concept) would overlap.
  kube -n metallb-system delete ipaddresspools,l2advertisements --field-selector metadata.name!=aisafe > /dev/null
  # MetalLB checks its configuration; right after it starts, the check may not answer yet.
  for _ in $(seq 30); do
    kube apply -f - > /dev/null 2>&1 <<EOF && return
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: aisafe
  namespace: metallb-system
spec:
  addresses: ["$VIP_POOL"]
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: aisafe
  namespace: metallb-system
spec:
  ipAddressPools: [aisafe]
EOF
    sleep 2
  done
  echo "Could not configure MetalLB's virtual IPs." >&2
  return 1
}

stage_cluster() {
  local i first=${NODE_IPS[0]} token
  for ip in "${NODE_IPS[@]}"; do
    log "Preparing node $ip"
    prepare_node "$ip"
  done
  if on_node "$first" "test -f /var/lib/rancher/k3s/server/token" 2> /dev/null; then
    token=$(on_node "$first" "cat /var/lib/rancher/k3s/server/token")
  else
    token=$(head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n')
  fi
  install_k3s "$first" "${NODE_NAMES[0]}" "$token"
  for i in "${!NODE_IPS[@]}"; do
    [ "$i" -eq 0 ] || install_k3s "${NODE_IPS[$i]}" "${NODE_NAMES[$i]}" "$token" "$first"
  done
  for name in "${NODE_NAMES[@]}"; do wait_node_ready "$name"; done
  # The vigia finds each node's machine by this label
  if [ "$MACHINES" = proxmox ]; then
    for i in "${!NODE_NAMES[@]}"; do
      kube label node "${NODE_NAMES[$i]}" "aisafe/machine=${VM_IDS[$i]}" --overwrite > /dev/null
    done
  fi
  install_metallb
}

# --- app --------------------------------------------------------------------------------------

# Sets IMAGE_PREFIX, IMAGE_TAG and IMAGE_PULL_POLICY for the manifests.
prepare_images() {
  local version svc commit token
  if [ "$IMAGES" = local ]; then
    version=$(sed -n 's:.*<version>\(.*-SNAPSHOT\)</version>.*:\1:p' "$REPO_DIR/pom.xml" | head -1)
    log "Building the images here (Java 25)"
    (cd "$REPO_DIR" && ./mvnw -q -B -pl aircraft-maintenance,airports-routes,flight-ops,login -am -DskipTests package jib:buildTar)
    for ip in "${NODE_IPS[@]}"; do
      for svc in aircraft-maintenance airports-routes flight-ops login; do
        log "Copying aisafe/$svc:$version to $ip"
        on_node "$ip" "k3s ctr images import -" < "$REPO_DIR/$svc/target/jib-image.tar" > /dev/null
      done
    done
    IMAGE_PREFIX=aisafe IMAGE_TAG=$version IMAGE_PULL_POLICY=Never
    return
  fi
  # ghcr: the images of one commit (GitHub Actions tags each build with its commit); a branch
  # stands for its latest commit on GitHub.
  commit=$IMAGE_TAG
  if ! [[ $commit =~ ^[0-9a-f]{40}$ ]]; then
    commit=$(git ls-remote "https://github.com/$GITHUB_REPO" "refs/heads/$IMAGE_TAG" | cut -f1)
    [ -n "$commit" ] || { echo "Branch $IMAGE_TAG not found on github.com/$GITHUB_REPO." >&2; return 1; }
  fi
  IMAGE_PREFIX=ghcr.io/$(tr '[:upper:]' '[:lower:]' <<< "$GITHUB_REPO") IMAGE_TAG=$commit IMAGE_PULL_POLICY=IfNotPresent
  for svc in aircraft-maintenance airports-routes flight-ops login; do
    token=$(curl -sf "https://ghcr.io/token?scope=repository:${IMAGE_PREFIX#ghcr.io/}/$svc:pull" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p') || true
    if ! curl -sf -o /dev/null -H "Authorization: Bearer $token" \
        -H 'Accept: application/vnd.oci.image.index.v1+json, application/vnd.oci.image.manifest.v1+json, application/vnd.docker.distribution.manifest.v2+json' \
        "https://ghcr.io/v2/${IMAGE_PREFIX#ghcr.io/}/$svc/manifests/$commit"; then
      echo "No public image $IMAGE_PREFIX/$svc:$commit yet: GitHub Actions builds it after the push" \
        "(see the Images workflow), and the package must be public." >&2
      return 1
    fi
  done
  echo "Images of commit $commit"
}

# A random password of letters and digits (also usable as the Erlang cookie).
random_secret() {
  head -c 64 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 32
}

# RabbitMQ users, created inside a broker from the Secret mounted there: no password goes through
# this computer, the node or a command line outside the broker. Each service may only declare and use its own exchange and queues,
# the shared exchanges of commands and dead letters, and the events of the services it reads.
RABBITMQ_USERS="aircraft-maintenance airports-routes flight-ops login postman"
rabbitmq_users() {
  on_node "${NODE_IPS[0]}" '
    ctl() { k3s kubectl -n aisafe exec rabbitmq-0 -c rabbitmq -- rabbitmqctl -q "$@"; }
    # The password is read inside the broker, from the Secret mounted there (rabbitmq.yaml), and
    # never leaves it. rabbitmqctl does not read a password from its input there, so it is given as
    # an argument, inside the broker only, where the same Secret is already readable.
    add_user() {
      k3s kubectl -n aisafe exec rabbitmq-0 -c rabbitmq -- \
        sh -c "rabbitmqctl -q add_user \"\$0\" \"\$(cat /etc/aisafe-rabbitmq/\$0-password)\"" "$1" > /dev/null
    }
    existing=$(ctl list_users --no-table-headers | cut -f1)
    for user in '"$RABBITMQ_USERS"'; do
      grep -qx "$user" <<< "$existing" || add_user "$user"
    done
    permit() {   # permit USER EXCHANGES-IT-READS (other services, separated by |)
      local own="$1\..*|aisafe\.commands|aisafe\.dead-letter" reads=""
      [ -z "$2" ] || reads="|($2)\.events"
      ctl set_permissions -p / "$1" "^($own$reads)\$" "^($own|amq\.default)\$" "^($own$reads|amq\.rabbitmq\.reply-to.*)\$"
    }
    permit aircraft-maintenance ""
    permit airports-routes "aircraft-maintenance"
    permit flight-ops "aircraft-maintenance|airports-routes"
    permit login ""
    # The Postman tests: their own queues, bound to the events of every service; nothing else
    ctl set_permissions -p / postman "^postman\..*\$" "^postman\..*\$" "^(postman\..*|.*\.events)\$"
    ctl set_user_tags postman management'
}

stage_app() {
  local jwt vips user
  local rabbitmq_secrets=()
  : "${VIP_AIRPORTS_ROUTES:?missing in deploy/config.sh: add it from deploy/config.example.sh}"
  : "${VIP_FLIGHT_OPS:?missing in deploy/config.sh: add it from deploy/config.example.sh}"
  : "${VIP_RABBITMQ:?missing in deploy/config.sh: add it from deploy/config.example.sh}"
  prepare_images
  log "Namespace, JWT secret, RabbitMQ passwords and the entries' configuration and certificate"
  kube create namespace aisafe --dry-run=client -o yaml | kube apply -f - > /dev/null
  if ! kube -n aisafe get secret aisafe-jwt > /dev/null 2>&1; then
    jwt=$(head -c 32 /dev/urandom | base64)
    secret_yaml aisafe aisafe-jwt "secret=$jwt" | kube apply -f - > /dev/null
  fi
  if ! kube -n aisafe get secret aisafe-rabbitmq > /dev/null 2>&1; then
    rabbitmq_secrets=("erlang-cookie=$(random_secret)" "admin-password=$(random_secret)")
    for user in $RABBITMQ_USERS; do
      rabbitmq_secrets+=("$user-password=$(random_secret)")
    done
    secret_yaml aisafe aisafe-rabbitmq "${rabbitmq_secrets[@]}" | kube apply -f - > /dev/null
  fi
  # HTTPS on the virtual IPs: a self-signed certificate for all of them, made on a node (its
  # key never leaves the cluster) the first time and again when the virtual IPs change.
  vips="$VIP_AIRCRAFT_MAINTENANCE $VIP_AIRPORTS_ROUTES $VIP_FLIGHT_OPS $VIP_LOGIN $VIP_RABBITMQ"
  if [ "$(kube -n aisafe get secret entry-tls -o 'jsonpath={.metadata.annotations.aisafe/vips}' 2> /dev/null)" != "$vips" ]; then
    on_node "${NODE_IPS[0]}" "
      dir=\$(mktemp -d)
      openssl req -x509 -newkey rsa:2048 -nodes -days 825 -subj '/CN=AISafe entry' \\
        -addext 'subjectAltName=IP:${vips// /,IP:}' -keyout \$dir/tls.key -out \$dir/tls.crt 2> /dev/null
      k3s kubectl -n aisafe create secret tls entry-tls --cert=\$dir/tls.crt --key=\$dir/tls.key \\
        --dry-run=client -o yaml | k3s kubectl apply -f - > /dev/null
      k3s kubectl -n aisafe annotate --overwrite secret entry-tls aisafe/vips='$vips' > /dev/null
      rm -rf \$dir"
  fi
  save_entry_cert
  kube -n aisafe create configmap entry --from-file=envoy.yaml=/dev/stdin --dry-run=client -o yaml \
    < "$DEPLOY_DIR/manifests/envoy.yaml" | kube apply -f - > /dev/null

  log "RabbitMQ (three brokers) and its users"
  sed -e "s|@VIP_RABBITMQ@|$VIP_RABBITMQ|g" \
      -e "s|@RABBITMQ_CONFIG_HASH@|$(hash16 < "$DEPLOY_DIR/manifests/rabbitmq.yaml")|g" \
      "$DEPLOY_DIR/manifests/rabbitmq.yaml" | kube apply -f - > /dev/null
  kube -n aisafe rollout status statefulset/rabbitmq --timeout=900s
  rabbitmq_users

  log "Applying the manifests"
  sed -e "s|@VIP_AIRCRAFT_MAINTENANCE@|$VIP_AIRCRAFT_MAINTENANCE|g" -e "s|@VIP_LOGIN@|$VIP_LOGIN|g" \
      -e "s|@VIP_AIRPORTS_ROUTES@|$VIP_AIRPORTS_ROUTES|g" \
      -e "s|@VIP_FLIGHT_OPS@|$VIP_FLIGHT_OPS|g" \
      -e "s|@IMAGE_PREFIX@|$IMAGE_PREFIX|g" -e "s|@IMAGE_TAG@|$IMAGE_TAG|g" \
      -e "s|@IMAGE_PULL_POLICY@|$IMAGE_PULL_POLICY|g" \
      -e "s|@ENTRY_CONFIG_HASH@|$(cat "$DEPLOY_DIR/manifests/envoy.yaml" "$ENTRY_CERT" | hash16)|g" \
      "$DEPLOY_DIR/manifests/aisafe.yaml" | kube apply -f - > /dev/null
  # Local images keep their name from one build to the next: restart to run the new ones. Images
  # from GitHub have a new name per commit, so applying already restarted what changed.
  if [ "$IMAGES" = local ]; then
    kube -n aisafe rollout restart statefulset/aircraft-maintenance statefulset/airports-routes statefulset/flight-ops statefulset/login > /dev/null
  fi
  kube -n aisafe rollout status statefulset/aircraft-maintenance --timeout=900s
  kube -n aisafe rollout status statefulset/airports-routes --timeout=900s
  kube -n aisafe rollout status statefulset/flight-ops --timeout=900s
  kube -n aisafe rollout status statefulset/login --timeout=900s
  kube -n aisafe rollout status daemonset/entry --timeout=300s
}

# --- vigia ------------------------------------------------------------------------------------

stage_vigia() {
  local driver=none access
  log "Vigia (watcher of the machines)"
  kube create namespace aisafe-vigia --dry-run=client -o yaml | kube apply -f - > /dev/null
  if [ "$MACHINES" = proxmox ]; then
    driver=proxmox
    access=$(on_proxmox "$DEPLOY_DIR/proxmox/vigia-access.sh")
    on_node "${NODE_IPS[0]}" "curl -s -o /dev/null --max-time 5 -k https://$PROXMOX_API_IP:8006/" \
      || { echo "The nodes cannot reach the Proxmox API at $PROXMOX_API_IP:8006: the vigia needs it." >&2; return 1; }
    secret_yaml aisafe-vigia vigia-proxmox \
      "token=$(sed -n 's/^token=//p' <<< "$access")" \
      "host=$(sed -n 's/^host=//p' <<< "$access")" \
      "ca.pem=$(sed -n 's/^ca=//p' <<< "$access" | base64 --decode)" \
      | kube apply -f - > /dev/null
  fi
  kube -n aisafe-vigia create configmap vigia --dry-run=client -o yaml \
    --from-file=vigia.sh=/dev/stdin --from-literal=machine-driver="$driver" \
    --from-literal=revive-timeout="$REVIVE_TIMEOUT" --from-literal=proxmox-ip="${PROXMOX_API_IP:-}" \
    < "$DEPLOY_DIR/vigia/vigia.sh" | kube apply -f - > /dev/null
  sed "s|@VIGIA_CONFIG_HASH@|$(cat "$DEPLOY_DIR/vigia/vigia.sh" "$CONFIG" | hash16)|" \
    "$DEPLOY_DIR/vigia/vigia.yaml" | kube apply -f - > /dev/null
  # A new token for Proxmox was made above: the vigias must read it again
  kube -n aisafe-vigia rollout restart daemonset/vigia > /dev/null
  kube -n aisafe-vigia rollout status daemonset/vigia --timeout=300s
}

# --- check ------------------------------------------------------------------------------------

stage_check() {
  log "Waiting until everything is normal"
  wait_normal 900
  log "Trying the services through the virtual IPs (from ${NODE_NAMES[0]})"
  save_entry_cert
  # Right after the pods are ready, the entries may still be finding them: try for a minute.
  on_node "${NODE_IPS[0]}" "
    ca=\$(mktemp)
    printf '%s\\n' '$(cat "$ENTRY_CERT")' > \$ca
    curl() { command curl --cacert \"\$ca\" \"\$@\"; }
    for _ in \$(seq 30); do
      token=\$(curl -sf --max-time 10 -H 'Content-Type: application/json' -d '{\"username\":\"atcc\",\"password\":\"atcc123\"}' https://$VIP_LOGIN/api/auth/login | sed -n 's/.*\"token\":\"\\([^\"]*\\)\".*/\\1/p') || true
      if [ -n \"\$token\" ] && curl -sf --max-time 10 -o /dev/null -H \"Authorization: Bearer \$token\" https://$VIP_AIRCRAFT_MAINTENANCE/api/aircrafts \
          && curl -sf --max-time 10 -o /dev/null -H \"Authorization: Bearer \$token\" https://$VIP_AIRPORTS_ROUTES/api/routes/search \
          && curl -sf --max-time 10 -o /dev/null -H \"Authorization: Bearer \$token\" 'https://$VIP_FLIGHT_OPS/api/flights?aircraftId=CS-TKA'; then
        exit 0
      fi
      sleep 2
    done
    echo 'No login through $VIP_LOGIN, or no answer from aircraft-maintenance through $VIP_AIRCRAFT_MAINTENANCE, airports-routes through $VIP_AIRPORTS_ROUTES or flight-ops through $VIP_FLIGHT_OPS, for a minute.' >&2
    exit 1"
  kube get nodes -o wide
  kube -n aisafe get pods -o wide
  cat <<EOF

AISafe is up:
  aircraft-maintenance  https://$VIP_AIRCRAFT_MAINTENANCE   (Swagger: /swagger-ui.html)
  airports-routes       https://$VIP_AIRPORTS_ROUTES   (Swagger: /swagger-ui.html)
  flight-ops            https://$VIP_FLIGHT_OPS   (Swagger: /swagger-ui.html)
  login                 https://$VIP_LOGIN
Their certificate is self-signed: clients trust it with deploy/entry.crt (curl --cacert deploy/entry.crt).
To check that no request fails while machines and replicas fail, from a computer that reaches
these addresses: deploy/manifests/failure-test.sh
EOF
}

stages=("$@")
[ "${#stages[@]}" -gt 0 ] || stages=(machines cluster app vigia check)
for stage in "${stages[@]}"; do
  case $stage in
    machines | cluster | app | vigia | check) "stage_$stage" ;;
    *) echo "Unknown stage: $stage (machines, cluster, app, vigia, check)" >&2; exit 1 ;;
  esac
done
