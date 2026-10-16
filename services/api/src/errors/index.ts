/**
 * @file index.ts
 * @description Standardized error hierarchy, classifications, and structured response formatters.
 */

import { ProviderType } from '../models/provider';

export interface StructuredErrorResponse {
  success: false;
  error: {
    name: string;
    code: string;
    message: string;
    statusCode: number;
    timestamp: string;
    details?: unknown;
    retryAfterSeconds?: number;
    provider?: ProviderType | string;
  };
}

/**
 * Base Application Error
 */
export class AppError extends Error {
  public readonly statusCode: number;
  public readonly code: string;
  public readonly isOperational: boolean;
  public readonly timestamp: string;
  public readonly details?: unknown;

  constructor(
    message: string,
    statusCode: number = 500,
    code: string = 'INTERNAL_SERVER_ERROR',
    details?: unknown,
    isOperational: boolean = true
  ) {
    super(message);
    this.name = this.constructor.name;
    this.statusCode = statusCode;
    this.code = code;
    this.details = details;
    this.isOperational = isOperational;
    this.timestamp = new Date().toISOString();
    Error.captureStackTrace(this, this.constructor);
  }

  public toResponse(): StructuredErrorResponse {
    return {
      success: false,
      error: {
        name: this.name,
        code: this.code,
        message: this.message,
        statusCode: this.statusCode,
        timestamp: this.timestamp,
        ...(this.details !== undefined && { details: this.details }),
      },
    };
  }
}

/**
 * Upstream Provider Error (AniList, Jikan, Kitsu, etc.)
 */
export class ProviderError extends AppError {
  public readonly provider: ProviderType | string;
  public readonly httpStatus?: number;
  public readonly retryAfterSeconds?: number;

  constructor(
    provider: ProviderType | string,
    message: string,
    options?: {
      statusCode?: number;
      code?: string;
      httpStatus?: number;
      retryAfterSeconds?: number;
      details?: unknown;
    }
  ) {
    super(
      `[${provider}] ${message}`,
      options?.statusCode ?? (options?.httpStatus && options.httpStatus >= 400 && options.httpStatus < 500 ? options.httpStatus : 502),
      options?.code ?? 'PROVIDER_ERROR',
      options?.details
    );
    this.provider = provider;
    this.httpStatus = options?.httpStatus;
    this.retryAfterSeconds = options?.retryAfterSeconds;
  }

  public override toResponse(): StructuredErrorResponse {
    const base = super.toResponse();
    base.error.provider = this.provider;
    if (this.retryAfterSeconds) {
      base.error.retryAfterSeconds = this.retryAfterSeconds;
    }
    return base;
  }
}

/**
 * Authentication Error (401)
 */
export class AuthenticationError extends AppError {
  constructor(message: string = 'Authentication required', details?: unknown) {
    super(message, 401, 'UNAUTHORIZED', details);
  }
}

/**
 * Forbidden Error (403)
 */
export class ForbiddenError extends AppError {
  constructor(message: string = 'Access denied', details?: unknown) {
    super(message, 403, 'FORBIDDEN', details);
  }
}

/**
 * Not Found Error (404)
 */
export class NotFoundError extends AppError {
  constructor(resource: string = 'Resource', details?: unknown) {
    super(`${resource} not found`, 404, 'NOT_FOUND', details);
  }
}

/**
 * Rate Limit Exceeded Error (429)
 */
export class RateLimitError extends AppError {
  public readonly retryAfterSeconds?: number;

  constructor(message: string = 'Rate limit exceeded', retryAfterSeconds?: number, details?: unknown) {
    super(message, 429, 'RATE_LIMIT_EXCEEDED', details);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  public override toResponse(): StructuredErrorResponse {
    const base = super.toResponse();
    if (this.retryAfterSeconds) {
      base.error.retryAfterSeconds = this.retryAfterSeconds;
    }
    return base;
  }
}

/**
 * Network / Connectivity / Gateway Timeout Error (502 / 504)
 */
export class NetworkError extends AppError {
  constructor(message: string = 'Network connectivity or timeout failure', details?: unknown) {
    super(message, 502, 'NETWORK_ERROR', details);
  }
}

/**
 * Request Validation Error (400)
 */
export class ValidationError extends AppError {
  constructor(message: string = 'Invalid request parameters', details?: unknown) {
    super(message, 400, 'VALIDATION_ERROR', details);
  }
}

/**
 * Classify HTTP status code into an appropriate AppError instance.
 */
export function classifyHttpStatus(
  status: number,
  message?: string,
  provider?: ProviderType | string
): AppError {
  const defaultMsg = message ?? `Request failed with status code ${status}`;
  switch (status) {
    case 400:
      return new ValidationError(defaultMsg);
    case 401:
      return new AuthenticationError(defaultMsg);
    case 403:
      return new ForbiddenError(defaultMsg);
    case 404:
      return new NotFoundError(defaultMsg);
    case 429:
      return provider
        ? new ProviderError(provider, defaultMsg, { statusCode: 429, code: 'RATE_LIMITED', httpStatus: 429 })
        : new RateLimitError(defaultMsg);
    case 502:
    case 503:
    case 504:
      return provider
        ? new ProviderError(provider, defaultMsg, { statusCode: 502, code: 'BAD_GATEWAY', httpStatus: status })
        : new NetworkError(defaultMsg);
    default:
      return provider
        ? new ProviderError(provider, defaultMsg, { statusCode: 500, code: 'PROVIDER_ERROR', httpStatus: status })
        : new AppError(defaultMsg, status >= 400 && status < 600 ? status : 500);
  }
}

/**
 * Formats any error (unknown, Error, AppError) into a structured JSON response object.
 */
export function formatErrorResponse(err: unknown): StructuredErrorResponse {
  if (err instanceof AppError) {
    return err.toResponse();
  }

  const message = err instanceof Error ? err.message : 'An unexpected error occurred';
  return {
    success: false,
    error: {
      name: 'InternalServerError',
      code: 'INTERNAL_SERVER_ERROR',
      message,
      statusCode: 500,
      timestamp: new Date().toISOString(),
    },
  };
}
