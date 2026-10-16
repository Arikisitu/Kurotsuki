/**
 * @file provider.ts
 * @description Metadata provider definitions, capabilities, runtime statuses, and configurations.
 */

export type ProviderType =
  | 'ANILIST'
  | 'JIKAN'
  | 'KITSU'
  | 'MYANIMELIST'
  | 'CUSTOM';

export type ProviderCapability =
  | 'GRAPHQL'
  | 'REST'
  | 'ANIME_SEARCH'
  | 'MANGA_SEARCH'
  | 'SEASONAL'
  | 'AIRING_SCHEDULE'
  | 'AUTH'
  | 'USER_LISTS'
  | 'REVIEWS'
  | 'RECOMMENDATIONS'
  | 'CHARACTER_STAFF'
  | 'TRENDING'
  | 'POPULAR';

export type ProviderStatus =
  | 'OPERATIONAL'
  | 'DEGRADED'
  | 'DOWN'
  | 'RATE_LIMITED'
  | 'MAINTENANCE';

export type ProviderErrorCode =
  | 'NETWORK_ERROR'
  | 'RATE_LIMITED'
  | 'TIMEOUT'
  | 'UNAUTHORIZED'
  | 'NOT_FOUND'
  | 'SCHEMA_MISMATCH'
  | 'BAD_GATEWAY'
  | 'PROVIDER_DOWN'
  | 'UNKNOWN';

export interface ProviderErrorInfo {
  provider: ProviderType;
  code: ProviderErrorCode;
  message: string;
  httpStatus?: number;
  retryAfterSeconds?: number;
  details?: Record<string, unknown> | null;
  timestamp: string;
}

export interface ProviderHealth {
  provider: ProviderType;
  name: string;
  status: ProviderStatus;
  latencyMs: number;
  lastCheckedAt: string;
  rateLimitRemaining?: number;
  rateLimitResetSeconds?: number;
  circuitBreakerState?: 'CLOSED' | 'HALF_OPEN' | 'OPEN';
  details?: Record<string, unknown>;
}

export interface ProviderConfig {
  provider: ProviderType;
  name: string;
  endpointUrl: string;
  timeoutMs: number;
  maxRetries: number;
  rateLimitPerMinute: number;
  defaultHeaders?: Record<string, string>;
}
