/**
 * @file responses.ts
 * @description Standardized API response envelopes, error structures, and domain response models.
 */

import {
  AiringSchedule,
  Anime,
  Manga,
  MediaTag,
  Pagination,
  ProviderHealth,
  UserListEntry,
  UserProfile,
} from '@sahasra/shared-types';

/**
 * Common API error classifications.
 */
export type ApiErrorCode =
  | 'BAD_REQUEST'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'NOT_FOUND'
  | 'CONFLICT'
  | 'VALIDATION_ERROR'
  | 'RATE_LIMITED'
  | 'UPSTREAM_ERROR'
  | 'INTERNAL_SERVER_ERROR'
  | 'SERVICE_UNAVAILABLE';

/**
 * Validation failure details for a specific payload field.
 */
export interface FieldValidationError {
  /** Field parameter path (e.g. 'query', 'perPage', 'score') */
  field: string;
  /** Description of the validation constraint violation */
  message: string;
  /** The rejected input value */
  rejectedValue?: unknown;
}

/**
 * Standardized error payload returned upon non-2xx responses.
 */
export interface ApiErrorResponse {
  /** Error classification code */
  code: ApiErrorCode;
  /** Human-readable explanation of error */
  message: string;
  /** Granular input validation failure issues */
  validationErrors?: FieldValidationError[];
  /** Optional debug or upstream error context (omitted in production) */
  debugInfo?: Record<string, unknown>;
  /** Timestamp when error occurred */
  timestamp: string;
}

/**
 * Execution and caching metadata attached to API responses.
 */
export interface ResponseMetadata {
  /** ISO timestamp when response was emitted */
  timestamp: string;
  /** Unique request trace identifier */
  requestId: string;
  /** Total server processing duration in milliseconds */
  executionTimeMs?: number;
  /** Whether the response was resolved from edge KV/cache */
  cached?: boolean;
  /** Cache expiration or max-age in seconds */
  cacheTtlSeconds?: number;
}

/**
 * Master unified response envelope for all SAHASRA endpoints.
 */
export interface ApiResponse<T> {
  /** Indicates whether the operation completed successfully */
  success: boolean;
  /** The payload data (present if success is true) */
  data?: T;
  /** Error details (present if success is false) */
  error?: ApiErrorResponse;
  /** Edge execution and telemetry metadata */
  meta?: ResponseMetadata;
}

/**
 * Health check telemetry data payload.
 */
export interface SystemHealthData {
  /** Service status: 'healthy' | 'degraded' | 'unhealthy' */
  status: 'healthy' | 'degraded' | 'unhealthy';
  /** System version identifier */
  version: string;
  /** Service uptime in seconds */
  uptimeSeconds: number;
  /** Provider health diagnostics */
  providers: ProviderHealth[];
}

// --- Specialized Typed Response Aliases ---

/** Response containing paginated anime records */
export type AnimeListResponse = ApiResponse<Pagination<Anime>>;

/** Response containing single anime complete details */
export type AnimeDetailsResponse = ApiResponse<Anime>;

/** Response containing paginated manga records */
export type MangaListResponse = ApiResponse<Pagination<Manga>>;

/** Response containing single manga complete details */
export type MangaDetailsResponse = ApiResponse<Manga>;

/** Response containing seasonal anime */
export type SeasonalAnimeResponse = ApiResponse<Pagination<Anime>>;

/** Response containing upcoming airing schedule */
export type AiringScheduleResponse = ApiResponse<Pagination<AiringSchedule>>;

/** Response containing user library entries */
export type UserLibraryResponse = ApiResponse<Pagination<UserListEntry>>;

/** Response containing user profile details */
export type UserProfileResponse = ApiResponse<UserProfile>;

/** Response containing system health status */
export type HealthResponse = ApiResponse<SystemHealthData>;

/** Response containing list of available genre strings */
export type GenresResponse = ApiResponse<string[]>;

/** Response containing list of available media tags */
export type TagsResponse = ApiResponse<MediaTag[]>;
