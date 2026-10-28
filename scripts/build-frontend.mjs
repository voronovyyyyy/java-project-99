import { spawnSync } from 'node:child_process';
import { join, resolve } from 'node:path';

const shell = process.platform === 'win32'
  ? join(process.env.ProgramFiles ?? 'C:/Program Files', 'Git', 'bin', 'bash.exe')
  : 'bash';
const script = resolve('node_modules/@hexlet/java-task-manager-frontend/bin/build.sh');
const result = spawnSync(shell, [script.replaceAll('\\', '/')], { stdio: 'inherit' });
if (result.error) {
  console.error(result.error.message);
}
process.exit(result.status ?? 1);
