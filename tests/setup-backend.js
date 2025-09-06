const { spawn } = require('child_process');
const path = require('path');

// Script to start the backend server for integration tests
const backendPath = path.join(__dirname, '..', 'backend');

console.log('Starting backend server for tests...');

const backend = spawn('./scripts/dev.sh', {
  cwd: backendPath,
  stdio: 'inherit',
  shell: true
});

backend.on('error', (err) => {
  console.error('Failed to start backend:', err);
  process.exit(1);
});

backend.on('exit', (code) => {
  console.log(`Backend server exited with code ${code}`);
});

// Handle process termination
process.on('SIGINT', () => {
  console.log('Stopping backend server...');
  backend.kill('SIGINT');
  process.exit(0);
});

process.on('SIGTERM', () => {
  console.log('Stopping backend server...');
  backend.kill('SIGTERM');
  process.exit(0);
});

// Keep the process alive
setInterval(() => {}, 1000);