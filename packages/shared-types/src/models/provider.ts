/**
 * @file provider.ts
 * @description Metadata provider definitions, capabilities, runtime statuses, and resilience errors.
 */

/**
 * Supported upstream metadata and tracking providers.
 */
export type ProviderType =
  | 'ANILIST'
  | 'MYANIMELIST'
  | 'KITSU'
  | 'CUSTOM';

/**
 * Functional capabilities supported by upstream providers.
 */
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
  | 'CHARACTER_STAFF';

/**
 * Operational health and availability status of a provider.
 */
export type ProviderStatus =
  | 'OPERATIONAL'
  | 'DEGRADED'
  | 'DOWN'
  | 'RATE_LIMITED'
  | 'MAINTENANCE';

/**
 * Standardized provider error classifications.
 */
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

/**
 * Standardized error structure for upstream provider failures.
 */
export interface ProviderError {
  /** Upstream provider source identifier */
  provider: ProviderType;
  /** Categorized error code */
  code: ProviderErrorCode;
  /** User-friendly descriptive error message */
  message: string;
  /** Upstream HTTP status code if available */
  httpStatus?: number;
  /** Suggested backoff wait period in seconds before retrying */
  retryAfterSeconds?: number;
  /** Raw upstream diagnostic error details */
  details?: Record<string, unknown> | null;
  /** Timestamp when error was captured */
  timestamp: string;
}

/**
 * Real-time operational telemetry for an upstream provider.
 */
export interface ProviderHealth {
  /** Identifier of the provider */
  provider: ProviderType;
  /** Current operational state */
  status: ProviderStatus;
  /** Round-trip ping latency in milliseconds */
  latencyMs: number;
  /** ISO timestamp of last heartbeat check */
  lastCheckedAt: string;
  /** Remaining queries permitted before hitting upstream rate-limit */
  rateLimitRemaining?: number;
  /** Seconds until rate-limit quota resets */
  rateLimitResetSeconds?: number;
}

/**
 * Runtime client configuration for an upstream provider integration.
 */
export interface ProviderConfig {
  /** Target provider type */
  provider: ProviderType;
  /** Root API or GraphQL endpoint URL */
  endpointUrl: string;
  /** Request timeout threshold in milliseconds */
  timeoutMs: number;
  /** Maximum automatic retry attempts upon transient failure */
  maxRetries: number;
  /** Maximum allowable requests per minute to conform to rate limit policies */
  rateLimitPerMinute: number;
  /** Optional custom HTTP headers */
  defaultHeaders?: Record<string, string>;
}
