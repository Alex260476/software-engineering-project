#!/usr/bin/env node
// Starts MongoDB (if needed), the Spring Boot backend, and the Vite frontend in one terminal.
// Ctrl+C stops everything this script started.
//
//   npm start               start everything and open the browser
//   npm start -- --verbose  also stream backend/frontend logs to the terminal
//   npm start -- --no-open  don't open the browser
//
// Uses only Node built-ins so it works on macOS, Windows, and Linux without an npm install.

import { spawn, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import net from 'node:net';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const BACKEND_DIR = path.join(ROOT, 'cinema-ebooking-backend');
const FRONTEND_DIR = path.join(ROOT, 'frontend');
const LOG_DIR = path.join(ROOT, '.logs');
const MONGO_DATA_DIR = path.join(ROOT, '.data', 'mongo');

const IS_WINDOWS = process.platform === 'win32';
const IS_MAC = process.platform === 'darwin';
const VERBOSE = process.argv.includes('--verbose');
const OPEN_BROWSER = !process.argv.includes('--no-open');

const APP_URL = 'http://localhost:5173';
const BACKEND_HEALTH_URL = 'http://localhost:8080/api/v1/movies/genres';
const SETUP_HINT = IS_WINDOWS ? 'setup.cmd' : 'bash setup.sh';

// ---------- output helpers ----------

const color = (code) => (text) => (process.stdout.isTTY ? `\x1b[${code}m${text}\x1b[0m` : text);
const green = color('32');
const red = color('31');
const yellow = color('33');
const cyan = color('36');
const bold = color('1');
const dim = color('2');

const ok = (msg) => console.log(`${green('✔')} ${msg}`);
const info = (msg) => console.log(`${cyan('•')} ${msg}`);
const fail = (msg) => console.error(`${red('✖')} ${msg}`);

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// ---------- environment checks ----------

const run = (cmd, args, opts = {}) => {
  const res = spawnSync(cmd, args, { encoding: 'utf8', shell: IS_WINDOWS, ...opts });
  return { ok: res.status === 0, out: `${res.stdout ?? ''}${res.stderr ?? ''}` };
};

// Parses "21.0.5" / "17" / "1.8.0_392" from `java -version` output
const javaMajor = (javaBin) => {
  const res = run(javaBin, ['-version'], { shell: false });
  if (!res.ok) return null;
  const match = res.out.match(/version "(\d+)(?:\.(\d+))?/);
  if (!match) return null;
  const major = Number(match[1]);
  return major === 1 ? Number(match[2]) : major;
};

// Returns { home, major } for a JDK 17+, looking where each OS's installers put it
const findJava = () => {
  const candidates = [];
  if (process.env.JAVA_HOME) candidates.push(process.env.JAVA_HOME);
  if (IS_MAC) {
    const javaHome = run('/usr/libexec/java_home', ['-v', '17+'], { shell: false });
    if (javaHome.ok) candidates.push(javaHome.out.trim().split('\n').pop());
    for (const formula of ['openjdk@21', 'openjdk@17', 'openjdk']) {
      for (const prefix of ['/opt/homebrew/opt', '/usr/local/opt']) {
        candidates.push(path.join(prefix, formula, 'libexec', 'openjdk.jdk', 'Contents', 'Home'));
      }
    }
  }
  if (IS_WINDOWS) {
    for (const base of ['C:\\Program Files\\Eclipse Adoptium', 'C:\\Program Files\\Java', 'C:\\Program Files\\Microsoft']) {
      try {
        for (const dir of fs.readdirSync(base)) candidates.push(path.join(base, dir));
      } catch { /* not installed there */ }
    }
  }
  for (const home of candidates) {
    const bin = path.join(home, 'bin', IS_WINDOWS ? 'java.exe' : 'java');
    if (!fs.existsSync(bin)) continue;
    const major = javaMajor(bin);
    if (major && major >= 17) return { home, major };
  }
  // Fall back to whatever `java` is on PATH (JAVA_HOME left unset for mvnw to discover it)
  const major = javaMajor('java');
  if (major && major >= 17) return { home: null, major };
  return null;
};

const findMongod = () => {
  const exe = IS_WINDOWS ? 'mongod.exe' : 'mongod';
  const which = run(IS_WINDOWS ? 'where' : 'which', [exe], { shell: false });
  if (which.ok) return which.out.trim().split(/\r?\n/)[0];
  const candidates = IS_WINDOWS ? [] : ['/opt/homebrew/bin/mongod', '/usr/local/bin/mongod'];
  if (IS_WINDOWS) {
    const base = 'C:\\Program Files\\MongoDB\\Server';
    try {
      for (const version of fs.readdirSync(base).sort().reverse()) {
        candidates.push(path.join(base, version, 'bin', exe));
      }
    } catch { /* not installed there */ }
  }
  return candidates.find((p) => fs.existsSync(p)) ?? null;
};

const isPortOpen = (port) =>
  new Promise((resolve) => {
    const socket = net.connect({ host: '127.0.0.1', port });
    socket.setTimeout(1000);
    socket.once('connect', () => { socket.destroy(); resolve(true); });
    socket.once('timeout', () => { socket.destroy(); resolve(false); });
    socket.once('error', () => resolve(false));
  });

// True if nothing else is listening: try to bind the port ourselves (IPv6 dual-stack, then IPv4)
const canListen = (port, host = '::') =>
  new Promise((resolve) => {
    const server = net.createServer();
    server.once('error', (err) => {
      if (host === '::' && err.code === 'EAFNOSUPPORT') resolve(canListen(port, '0.0.0.0'));
      else resolve(false);
    });
    server.listen({ port, host, exclusive: true }, () => server.close(() => resolve(true)));
  });

const isPortInUse = async (port) => (await isPortOpen(port)) || !(await canListen(port));

const waitFor = async (check, { timeoutMs, child }) => {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    if (child && child.exitCode !== null) return false;
    if (await check()) return true;
    await sleep(1000);
  }
  return false;
};

const httpOk = (url) => async () => {
  try {
    const res = await fetch(url, { signal: AbortSignal.timeout(2000) });
    return res.ok;
  } catch {
    return false;
  }
};

// ---------- process management ----------

const children = [];
let shuttingDown = false;

const tailLog = (file, lines = 25) => {
  try {
    const content = fs.readFileSync(file, 'utf8').trimEnd().split(/\r?\n/);
    return content.slice(-lines).map((l) => dim(`    ${l}`)).join('\n');
  } catch {
    return dim('    (no log output)');
  }
};

const startProcess = (name, cmd, args, { cwd, env, shell = false }) => {
  fs.mkdirSync(LOG_DIR, { recursive: true });
  const logFile = path.join(LOG_DIR, `${name}.log`);
  const log = fs.createWriteStream(logFile);
  const child = spawn(cmd, args, {
    cwd,
    env: { ...process.env, ...env },
    shell,
    stdio: ['ignore', 'pipe', 'pipe'],
    // Own process group on macOS/Linux so Ctrl+C cleanup can stop grandchildren too
    detached: !IS_WINDOWS,
    windowsHide: true,
  });
  const onData = (chunk) => {
    log.write(chunk);
    if (VERBOSE) {
      for (const line of chunk.toString().split(/\r?\n/)) {
        if (line.trim()) console.log(`${dim(`[${name}]`)} ${line}`);
      }
    }
  };
  child.stdout.on('data', onData);
  child.stderr.on('data', onData);
  child.on('exit', (code) => {
    log.end();
    if (!shuttingDown) {
      fail(`${name} stopped unexpectedly (exit code ${code}). Last log lines from .logs/${name}.log:`);
      console.error(tailLog(logFile));
      shutdown(1);
    }
  });
  children.push({ name, child, logFile });
  return { child, logFile };
};

const killTree = (child) => {
  if (child.exitCode !== null) return;
  try {
    if (IS_WINDOWS) {
      spawnSync('taskkill', ['/pid', String(child.pid), '/T', '/F'], { stdio: 'ignore' });
    } else {
      process.kill(-child.pid, 'SIGTERM');
    }
  } catch { /* already gone */ }
};

async function shutdown(code = 0) {
  if (shuttingDown) return;
  shuttingDown = true;
  if (children.length) console.log(`\n${cyan('•')} Stopping ${children.map((c) => c.name).join(', ')}...`);
  // Stop in reverse start order (frontend, backend, then MongoDB)
  for (const { child } of [...children].reverse()) killTree(child);
  const deadline = Date.now() + 10000;
  while (children.some(({ child }) => child.exitCode === null && child.signalCode === null) && Date.now() < deadline) {
    await sleep(200);
  }
  for (const { child } of children) {
    if (child.exitCode === null && child.signalCode === null && !IS_WINDOWS) {
      try { process.kill(-child.pid, 'SIGKILL'); } catch { /* gone */ }
    }
  }
  if (code === 0) ok('Everything stopped.');
  process.exit(code);
}

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));

// ---------- main ----------

const main = async () => {
  console.log(bold('\nCinemaFlex · Cinema E-Booking System\n'));

  // 1. Tools
  const nodeMajor = Number(process.versions.node.split('.')[0]);
  if (nodeMajor < 18) {
    fail(`Node.js ${process.versions.node} is too old (need 18+). Run ${bold(SETUP_HINT)} first.`);
    process.exit(1);
  }

  const java = findJava();
  if (!java) {
    fail(`Java 17+ was not found. Run ${bold(SETUP_HINT)} first.`);
    process.exit(1);
  }
  ok(`Java ${java.major}`);

  if (!fs.existsSync(path.join(FRONTEND_DIR, 'node_modules'))) {
    info('Installing frontend packages (first run only)...');
    const res = spawnSync(IS_WINDOWS ? 'npm.cmd' : 'npm', ['install'], { cwd: FRONTEND_DIR, stdio: 'inherit', shell: IS_WINDOWS });
    if (res.status !== 0) {
      fail(`npm install failed. Run ${bold(SETUP_HINT)} and try again.`);
      process.exit(1);
    }
  }

  // 2. Ports
  for (const [port, what] of [[8080, 'backend'], [5173, 'frontend']]) {
    if (await isPortInUse(port)) {
      fail(`Port ${port} (${what}) is already in use. Is the app already running in another terminal? Stop it with Ctrl+C and try again.`);
      process.exit(1);
    }
  }

  // 3. MongoDB
  if (await isPortOpen(27017)) {
    ok('MongoDB is already running');
  } else {
    const mongod = findMongod();
    if (!mongod) {
      fail(`MongoDB was not found. Run ${bold(SETUP_HINT)} first.`);
      process.exit(1);
    }
    fs.mkdirSync(MONGO_DATA_DIR, { recursive: true });
    info('Starting MongoDB...');
    const { child, logFile } = startProcess('mongodb', mongod, ['--dbpath', MONGO_DATA_DIR, '--port', '27017', '--bind_ip', '127.0.0.1'], { cwd: ROOT });
    if (!(await waitFor(() => isPortOpen(27017), { timeoutMs: 30000, child }))) {
      if (!shuttingDown) {
        fail('MongoDB did not start. Last log lines:');
        console.error(tailLog(logFile));
        await shutdown(1);
      }
      return;
    }
    ok('MongoDB started');
  }

  // 4. Backend
  info(`Starting backend ${dim('(the first run downloads dependencies and can take a few minutes)')}...`);
  const mvnw = IS_WINDOWS ? 'mvnw.cmd' : './mvnw';
  const backend = startProcess('backend', mvnw, ['-B', 'spring-boot:run'], {
    cwd: BACKEND_DIR,
    env: java.home ? { JAVA_HOME: java.home } : {},
    shell: IS_WINDOWS,
  });
  if (!(await waitFor(httpOk(BACKEND_HEALTH_URL), { timeoutMs: 300000, child: backend.child }))) {
    if (!shuttingDown) {
      fail('Backend did not become ready. Last log lines from .logs/backend.log:');
      console.error(tailLog(backend.logFile));
      await shutdown(1);
    }
    return;
  }
  ok('Backend ready on http://localhost:8080');

  // 5. Frontend
  info('Starting frontend...');
  const frontend = startProcess('frontend', IS_WINDOWS ? 'npm.cmd' : 'npm', ['run', 'dev', '--', '--port', '5173', '--strictPort'], {
    cwd: FRONTEND_DIR,
    shell: IS_WINDOWS,
  });
  if (!(await waitFor(httpOk(APP_URL), { timeoutMs: 60000, child: frontend.child }))) {
    if (!shuttingDown) {
      fail('Frontend did not become ready. Last log lines from .logs/frontend.log:');
      console.error(tailLog(frontend.logFile));
      await shutdown(1);
    }
    return;
  }
  ok('Frontend ready');

  console.log(`\n${green(bold('CinemaFlex is running →'))} ${bold(APP_URL)}`);
  console.log(dim(`Logs: .logs/  ·  Swagger: http://localhost:8080/swagger-ui.html`));
  console.log(`${bold('Press Ctrl+C to stop everything.')}\n`);

  if (OPEN_BROWSER) {
    if (IS_MAC) spawn('open', [APP_URL], { stdio: 'ignore', detached: true }).unref();
    else if (IS_WINDOWS) spawn('cmd', ['/c', 'start', '', APP_URL], { stdio: 'ignore', detached: true }).unref();
    else spawn('xdg-open', [APP_URL], { stdio: 'ignore', detached: true }).on('error', () => {}).unref();
  }
};

main().catch(async (err) => {
  fail(err?.stack ?? String(err));
  await shutdown(1);
});
