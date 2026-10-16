/**
 * @file rateLimiter.ts
 * @description API rate limiting middleware utilizing express-rate-limit.
 */

import rateLimit from 'express-rate-limit';
import { config } from '../config';
import { RateLimitError } from '../errors';

export const globalRateLimiter = rateLimit({
  windowMs: config.rateLimit.windowMs,
  max: config.rateLimit.max,
  standardHeaders: true,
  legacyHeaders: false,
  handler: (_req, res, _next, options) => {
    const retryAfter = Math.ceil(options.windowMs / 1000);
    const err = new RateLimitError(
      'Too many requests from this IP, please try again later.',
      retryAfter
    );
    res.status(429).json(err.toResponse());
  },
});

export const strictRateLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 30, // 30 requests per minute
  standardHeaders: true,
  legacyHeaders: false,
  handler: (_req, res, _next, options) => {
    const retryAfter = Math.ceil(options.windowMs / 1000);
    const err = new RateLimitError(
      'Strict rate limit exceeded for sensitive operation.',
      retryAfter
    );
    res.status(429).json(err.toResponse());
  },
});
