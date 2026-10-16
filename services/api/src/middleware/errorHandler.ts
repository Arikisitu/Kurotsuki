/**
 * @file errorHandler.ts
 * @description Global Express error handling middleware with structured JSON output.
 */

import { NextFunction, Request, Response } from 'express';
import { ZodError } from 'zod';
import { config } from '../config';
import { AppError, formatErrorResponse, ValidationError } from '../errors';

export function errorHandler(
  err: unknown,
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  if (res.headersSent) {
    return;
  }

  // Handle Zod Schema Validation Errors
  if (err instanceof ZodError) {
    const validationError = new ValidationError('Request validation failed', {
      issues: err.errors.map((e) => ({
        path: e.path.join('.'),
        message: e.message,
        code: e.code,
      })),
    });
    res.status(400).json(validationError.toResponse());
    return;
  }

  // Handle AppError and subclasses
  if (err instanceof AppError) {
    const formatted = err.toResponse();
    res.status(err.statusCode).json(formatted);
    return;
  }

  // Handle generic / unexpected runtime errors
  const fallback = formatErrorResponse(err);
  if (config.env === 'development' && err instanceof Error) {
    fallback.error.details = { stack: err.stack };
  }
  res.status(fallback.error.statusCode).json(fallback);
}
