import { spawnSync } from 'node:child_process';
import { join, resolve } from 'node:path';
import { readFileSync, writeFileSync } from 'node:fs';

const shell = process.platform === 'win32'
  ? join(process.env.ProgramFiles ?? 'C:/Program Files', 'Git', 'bin', 'bash.exe')
  : 'bash';
const script = resolve('node_modules/@hexlet/java-task-manager-frontend/bin/build.sh');
const result = spawnSync(shell, [script.replaceAll('\\', '/')], { stdio: 'inherit' });
if (result.error) {
  console.error(result.error.message);
}
if (result.status === 0) {
  const index = resolve('src/main/resources/static/index.html');
  const html = readFileSync(index, 'utf8').replace('<title>Vite + React + TS</title>', '<title>Task Manager</title>');
  writeFileSync(index, html);
}
process.exit(result.status ?? 1);
