/**
 * @file logger.ts
 * @description HTTP request logging middleware with automatic secret and token redaction.
 */

import { NextFunction, Request, Response } from 'express';

const REDACTED = '[REDACTED]';
const SENSITIVE_KEYS = new Set([
  'authorization',
  'cookie',
  'set-cookie',
  'password',
  'token',
  'secret',
  'apikey',
  'api-key',
  'access_token',
  'refresh_token',
]);

export function redactSensitiveData(obj: any): any {
  if (!obj || typeof obj !== 'object') return obj;

  if (Array.isArray(obj)) {
    return obj.map((item) => redactSensitiveData(item));
  }

  const sanitized: Record<string, any> = {};
  for (const [key, val] of Object.entries(obj)) {
    if (SENSITIVE_KEYS.has(key.toLowerCase())) {
      sanitized[key] = REDACTED;
    } else if (typeof val === 'object' && val !== null) {
      sanitized[key] = redactSensitiveData(val);
    } else {
      sanitized[key] = val;
    }
  }
  return sanitized;
}

export function requestLogger(req: Request, res: Response, next: NextFunction): void {
  const start = Date.now();
  const { method, originalUrl, ip } = req;

  res.on('finish', () => {
    const duration = Date.now() - start;
    const statusCode = res.statusCode;
    const logLevel = statusCode >= 500 ? 'ERROR' : statusCode >= 400 ? 'WARN' : 'INFO';

    // In test environment or quiet mode, skip log printing
    if (process.env.NODE_ENV === 'test') return;

    // Format log entry
    console.log(
      `[${new Date().toISOString()}] [${logLevel}] ${method} ${originalUrl} ${statusCode} ${duration}ms - IP: ${ip}`
    );
  });

  next();
}
