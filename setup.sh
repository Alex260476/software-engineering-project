#!/usr/bin/env bash
# One-time setup for macOS: checks for Java 17+, Node.js 18+, and MongoDB,
# installs anything missing with Homebrew, then downloads project dependencies.
# Safe to run again; anything already installed is skipped.
#
#   bash setup.sh          ask before installing each missing tool
#   bash setup.sh --yes    install missing tools without asking

set -uo pipefail
cd "$(dirname "$0")"
ROOT="$(pwd)"

ASSUME_YES=false
[[ "${1:-}" == "--yes" || "${1:-}" == "-y" ]] && ASSUME_YES=true

if [[ -t 1 ]]; then
  GREEN=$'\033[32m'; RED=$'\033[31m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; BOLD=$'\033[1m'; DIM=$'\033[2m'; RESET=$'\033[0m'
else
  GREEN=''; RED=''; YELLOW=''; CYAN=''; BOLD=''; DIM=''; RESET=''
fi
ok()   { echo "${GREEN}✔${RESET} $*"; }
info() { echo "${CYAN}•${RESET} $*"; }
warn() { echo "${YELLOW}!${RESET} $*"; }
fail() { echo "${RED}✖${RESET} $*" >&2; }
step() { echo; echo "${BOLD}$*${RESET}"; }

ask() {
  $ASSUME_YES && return 0
  local reply
  read -r -p "  $1 [Y/n] " reply
  [[ -z "$reply" || "$reply" =~ ^[Yy] ]]
}

PROBLEMS=0

echo "${BOLD}CinemaFlex setup${RESET}"

if [[ "$(uname)" != "Darwin" ]]; then
  warn "This script installs tools with Homebrew on macOS."
  warn "On Linux, install Java 17+, Node.js 18+, and MongoDB with your package manager, then run this again to download dependencies."
fi

# ---------- Homebrew ----------

load_brew() {
  for b in /opt/homebrew/bin/brew /usr/local/bin/brew; do
    [[ -x "$b" ]] && eval "$("$b" shellenv)" && return 0
  done
  command -v brew >/dev/null 2>&1
}

if [[ "$(uname)" == "Darwin" ]]; then
  step "1/5  Homebrew"
  if load_brew; then
    ok "Homebrew $(brew --version | head -1 | awk '{print $2}')"
  else
    warn "Homebrew (the macOS package manager) is not installed. It's needed to install the other tools."
    if ask "Install Homebrew now? (it may ask for your Mac password)"; then
      /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)" && load_brew \
        && ok "Homebrew installed" || { fail "Homebrew install failed. See https://brew.sh"; exit 1; }
    else
      fail "Homebrew is required. Install it from https://brew.sh and run this script again."
      exit 1
    fi
  fi
fi

HAVE_BREW=false
command -v brew >/dev/null 2>&1 && HAVE_BREW=true

brew_install() {
  # brew_install <display name> <brew args...>
  local name="$1"; shift
  if ! $HAVE_BREW; then
    fail "$name is missing. Install it, then run this script again."
    PROBLEMS=$((PROBLEMS + 1))
    return 1
  fi
  if ask "Install $name with Homebrew?"; then
    info "Running: brew install $*"
    if brew install "$@"; then
      ok "$name installed"
      return 0
    fi
    fail "Installing $name failed (see the output above)."
  else
    fail "Skipped $name. The app can't run without it."
  fi
  PROBLEMS=$((PROBLEMS + 1))
  return 1
}

# ---------- Java 17+ ----------

java_major() {
  local out
  out="$("$1" -version 2>&1)" || return 1
  local v
  v="$(echo "$out" | sed -n 's/.*version "\([0-9][0-9]*\)\(\.\([0-9]*\)\)\{0,1\}.*/\1 \3/p' | head -1)"
  [[ -z "$v" ]] && return 1
  set -- $v
  if [[ "$1" == "1" ]]; then echo "${2:-0}"; else echo "$1"; fi
}

find_java_home() {
  local candidates=()
  [[ -n "${JAVA_HOME:-}" ]] && candidates+=("$JAVA_HOME")
  if [[ -x /usr/libexec/java_home ]]; then
    local jh; jh="$(/usr/libexec/java_home -v 17+ 2>/dev/null)" && candidates+=("$jh")
  fi
  for f in openjdk@21 openjdk@17 openjdk; do
    for p in /opt/homebrew/opt /usr/local/opt; do
      candidates+=("$p/$f/libexec/openjdk.jdk/Contents/Home")
    done
  done
  local home major
  for home in "${candidates[@]}"; do
    [[ -x "$home/bin/java" ]] || continue
    major="$(java_major "$home/bin/java")" || continue
    if (( major >= 17 )); then echo "$home"; return 0; fi
  done
  # Linux / other: java on PATH
  if command -v java >/dev/null 2>&1; then
    major="$(java_major java)" || return 1
    if (( major >= 17 )); then
      home="$(dirname "$(dirname "$(readlink -f "$(command -v java)" 2>/dev/null || command -v java)")")"
      echo "$home"; return 0
    fi
  fi
  return 1
}

step "2/5  Java 17+"
if JAVA_HOME_FOUND="$(find_java_home)"; then
  ok "Java $(java_major "$JAVA_HOME_FOUND/bin/java")  ${DIM}($JAVA_HOME_FOUND)${RESET}"
else
  warn "Java 17 or newer was not found."
  brew_install "Java 21 (OpenJDK)" openjdk@21 && JAVA_HOME_FOUND="$(find_java_home)"
fi
[[ -n "${JAVA_HOME_FOUND:-}" ]] && export JAVA_HOME="$JAVA_HOME_FOUND"

# ---------- Node.js 18+ ----------

node_major() { node --version 2>/dev/null | sed 's/^v\([0-9]*\).*/\1/'; }

step "3/5  Node.js 18+"
if command -v node >/dev/null 2>&1 && (( $(node_major) >= 18 )); then
  ok "Node.js $(node --version)"
else
  if command -v node >/dev/null 2>&1; then
    warn "Node.js $(node --version) is too old (need 18+)."
  else
    warn "Node.js was not found."
  fi
  brew_install "Node.js" node && hash -r
  if ! command -v node >/dev/null 2>&1 || (( $(node_major) < 18 )); then
    warn "If you use nvm, run: nvm install --lts"
  fi
fi

# ---------- MongoDB ----------

find_mongod() {
  command -v mongod 2>/dev/null && return 0
  for p in /opt/homebrew/bin/mongod /usr/local/bin/mongod; do
    [[ -x "$p" ]] && echo "$p" && return 0
  done
  return 1
}

port_open() { (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null; }

step "4/5  MongoDB"
if MONGOD="$(find_mongod)"; then
  ok "MongoDB $("$MONGOD" --version 2>/dev/null | sed -n 's/^db version v//p' | head -1)"
elif port_open 27017; then
  ok "A MongoDB server is already running on port 27017"
else
  warn "MongoDB was not found."
  if $HAVE_BREW; then
    info "MongoDB comes from MongoDB's official Homebrew tap (mongodb/brew)."
    brew tap mongodb/brew >/dev/null 2>&1
    # Newer Homebrew versions refuse to use third-party taps until they're marked trusted
    if brew help trust >/dev/null 2>&1; then
      brew trust mongodb/brew >/dev/null 2>&1 && ok "Trusted the mongodb/brew tap"
    fi
  fi
  brew_install "MongoDB Community" mongodb/brew/mongodb-community
fi

# ---------- Project dependencies ----------

step "5/5  Project dependencies"

if command -v npm >/dev/null 2>&1; then
  info "Installing frontend packages (npm install)..."
  if (cd "$ROOT/frontend" && npm install --no-fund --no-audit); then
    ok "Frontend packages installed"
  else
    fail "npm install failed (see the output above)."
    PROBLEMS=$((PROBLEMS + 1))
  fi
else
  fail "Skipping frontend packages because npm isn't available."
  PROBLEMS=$((PROBLEMS + 1))
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  chmod +x "$ROOT/cinema-ebooking-backend/mvnw"
  info "Downloading backend dependencies and compiling ${DIM}(first run can take a few minutes)${RESET}..."
  if (cd "$ROOT/cinema-ebooking-backend" && ./mvnw -q -B -DskipTests compile); then
    ok "Backend compiled"
  else
    fail "Backend build failed (see the output above)."
    PROBLEMS=$((PROBLEMS + 1))
  fi
else
  fail "Skipping backend build because Java isn't available."
  PROBLEMS=$((PROBLEMS + 1))
fi

# ---------- Summary ----------

echo
if (( PROBLEMS == 0 )); then
  echo "${GREEN}${BOLD}✔ Setup complete!${RESET} Start the app with:"
  echo
  echo "    ${BOLD}npm start${RESET}"
  echo
else
  echo "${RED}${BOLD}✖ Setup finished with $PROBLEMS problem(s).${RESET} Fix the items marked ✖ above, then run ${BOLD}bash setup.sh${RESET} again."
  echo "  ${DIM}Tip: if a tool was just installed but still isn't found, close this terminal, open a new one, and rerun setup.${RESET}"
  exit 1
fi
