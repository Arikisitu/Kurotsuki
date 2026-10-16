/**
 * @file index.ts
 * @description Main server entry point for SAHASRA API orchestration service.
 */

import { app } from './app';
import { config } from './config';
import { cache } from './services/cache';

const server = app.listen(config.port, config.host, () => {
  console.log(`===============================================`);
  console.log(`  SAHASRA API Orchestration Service           `);
  console.log(`  Environment: ${config.env}                   `);
  console.log(`  Listening on http://${config.host}:${config.port} `);
  console.log(`  Base Endpoint: /api/v1                      `);
  console.log(`===============================================`);
});

// Graceful shutdown handling
function gracefulShutdown(signal: string) {
  console.log(`Received ${signal}. Shutting down SAHASRA API service gracefully...`);
  cache.destroy();
  server.close(() => {
    console.log('HTTP server closed.');
    process.exit(0);
  });

  setTimeout(() => {
    console.error('Forcefully exiting due to shutdown timeout.');
    process.exit(1);
  }, 10000).unref();
}

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

export default server;
