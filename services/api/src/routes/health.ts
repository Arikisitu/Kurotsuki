/**
 * @file health.ts
 * @description Health, uptime, and provider status diagnostic endpoints.
 */

import { Router } from 'express';
import { registry } from '../providers/registry';
import { cache } from '../services/cache';
import { circuitBreaker } from '../services/circuit-breaker';

export const healthRouter = Router();

const startTime = Date.now();

/**
 * GET /health
 * Basic service telemetry, uptime, and memory statistics
 */
healthRouter.get('/health', (_req, res) => {
  const uptimeSeconds = Math.floor((Date.now() - startTime) / 1000);
  const memoryUsage = process.memoryUsage();

  res.json({
    status: 'healthy',
    service: 'sahasra-api',
    uptimeSeconds,
    timestamp: new Date().toISOString(),
    memory: {
      rssMb: Math.round(memoryUsage.rss / (1024 * 1024)),
      heapUsedMb: Math.round(memoryUsage.heapUsed / (1024 * 1024)),
      heapTotalMb: Math.round(memoryUsage.heapTotal / (1024 * 1024)),
    },
    cache: cache.getStats(),
  });
});

/**
 * GET /providers/status
 * Upstream provider availability, latencies, and circuit breaker states
 */
healthRouter.get('/providers/status', async (_req, res, next) => {
  try {
    const providerHealth = await registry.checkHealthAll();
    const circuitStates = circuitBreaker.getAllStates();

    // Enrich provider status with circuit breaker state
    const enriched = Object.entries(providerHealth).map(([name, health]) => {
      const cb = circuitStates[name.toLowerCase()];
      return {
        ...health,
        circuitBreaker: {
          state: cb?.state || 'CLOSED',
          failures: cb?.failures || 0,
        },
      };
    });

    res.json({
      success: true,
      timestamp: new Date().toISOString(),
      providers: enriched,
    });
  } catch (err) {
    next(err);
  }
});
