/**
 * @file cors.ts
 * @description Cross-Origin Resource Sharing (CORS) middleware configuration.
 */

import cors, { CorsOptions } from 'cors';
import { config } from '../config';

const corsOptions: CorsOptions = {
  origin: (origin, callback) => {
    // Allow non-browser requests or matching origins
    if (!origin || config.corsOrigins.includes(origin) || config.corsOrigins.includes('*')) {
      callback(null, true);
    } else {
      callback(null, true); // Permissive in dev, or customizable
    }
  },
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept', 'X-Client-Version'],
  exposedHeaders: ['Retry-After', 'X-RateLimit-Limit', 'X-RateLimit-Remaining', 'X-RateLimit-Reset'],
  credentials: true,
  maxAge: 86400, // 24 hours preflight cache
};

export const corsMiddleware = cors(corsOptions);
