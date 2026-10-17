/**
 * @file app.ts
 * @description Express application setup with middleware, security headers, routes, and error handling.
 */

import express, { Express } from 'express';
import helmet from 'helmet';
import { NotFoundError } from './errors';
import { corsMiddleware } from './middleware/cors';
import { errorHandler } from './middleware/errorHandler';
import { requestLogger } from './middleware/logger';
import { globalRateLimiter } from './middleware/rateLimiter';
import { animeRouter, authRouter, healthRouter, mangaRouter, streamRouter } from './routes';

export function createApp(): Express {
  const app = express();

  // Security Headers
  app.use(
    helmet({
      crossOriginResourcePolicy: { policy: 'cross-origin' },
      contentSecurityPolicy: false,
    })
  );

  // CORS
  app.use(corsMiddleware);

  // Body Parsing
  app.use(express.json({ limit: '1mb' }));
  app.use(express.urlencoded({ extended: true, limit: '1mb' }));

  // Telemetry & Logging
  app.use(requestLogger);

  // Rate Limiting
  app.use(globalRateLimiter);

  // Mount API Routes
  app.use('/api/v1/anime', animeRouter);
  app.use('/api/v1/manga', mangaRouter);
  app.use('/api/v1/stream', streamRouter);
  app.use('/api/v1/auth', authRouter);
  app.use('/api/v1', healthRouter);

  // Catch-all 404 Route Handler
  app.use((req, _res, next) => {
    next(new NotFoundError(`Route ${req.method} ${req.path}`));
  });

  // Global Error Handler
  app.use(errorHandler);

  return app;
}

export const app = createApp();
