/**
 * @file config.ts
 * @description Centralized application configuration with environment overrides.
 */

export interface AppConfig {
  env: 'development' | 'production' | 'test';
  port: number;
  host: string;
  corsOrigins: string[];
  rateLimit: {
    windowMs: number;
    max: number;
  };
  cache: {
    trendingTtlSeconds: number;
    popularTtlSeconds: number;
    seasonalTtlSeconds: number;
    searchTtlSeconds: number;
    detailsTtlSeconds: number;
    staleTtlSeconds: number;
  };
  circuitBreaker: {
    failureThreshold: number;
    recoveryTimeMs: number;
    successThreshold: number;
  };
  providers: {
    anilist: {
      endpoint: string;
      timeoutMs: number;
      rateLimitPerMinute: number;
    };
    jikan: {
      endpoint: string;
      timeoutMs: number;
      rateLimitPerSecond: number;
    };
    kitsu: {
      endpoint: string;
      timeoutMs: number;
      rateLimitPerMinute: number;
    };
  };
}

export const config: AppConfig = {
  env: (process.env.NODE_ENV as AppConfig['env']) || 'development',
  port: process.env.PORT ? parseInt(process.env.PORT, 10) : 4000,
  host: process.env.HOST || '0.0.0.0',
  corsOrigins: process.env.CORS_ORIGIN
    ? process.env.CORS_ORIGIN.split(',').map((o) => o.trim())
    : ['http://localhost:5173', 'http://localhost:3000', 'http://127.0.0.1:5173'],
  rateLimit: {
    windowMs: process.env.RATE_LIMIT_WINDOW_MS
      ? parseInt(process.env.RATE_LIMIT_WINDOW_MS, 10)
      : 15 * 60 * 1000, // 15 minutes
    max: process.env.RATE_LIMIT_MAX
      ? parseInt(process.env.RATE_LIMIT_MAX, 10)
      : 100, // 100 requests per window
  },
  cache: {
    trendingTtlSeconds: process.env.CACHE_TRENDING_TTL ? parseInt(process.env.CACHE_TRENDING_TTL, 10) : 300, // 5 min
    popularTtlSeconds: process.env.CACHE_POPULAR_TTL ? parseInt(process.env.CACHE_POPULAR_TTL, 10) : 600, // 10 min
    seasonalTtlSeconds: process.env.CACHE_SEASONAL_TTL ? parseInt(process.env.CACHE_SEASONAL_TTL, 10) : 1800, // 30 min
    searchTtlSeconds: process.env.CACHE_SEARCH_TTL ? parseInt(process.env.CACHE_SEARCH_TTL, 10) : 300, // 5 min
    detailsTtlSeconds: process.env.CACHE_DETAILS_TTL ? parseInt(process.env.CACHE_DETAILS_TTL, 10) : 3600, // 1 hour
    staleTtlSeconds: 120, // 2 minutes stale grace period
  },
  circuitBreaker: {
    failureThreshold: process.env.CB_FAILURE_THRESHOLD ? parseInt(process.env.CB_FAILURE_THRESHOLD, 10) : 5,
    recoveryTimeMs: process.env.CB_RECOVERY_TIME_MS ? parseInt(process.env.CB_RECOVERY_TIME_MS, 10) : 30000, // 30s
    successThreshold: 2,
  },
  providers: {
    anilist: {
      endpoint: process.env.ANILIST_API_URL || 'https://graphql.anilist.co',
      timeoutMs: process.env.ANILIST_TIMEOUT_MS ? parseInt(process.env.ANILIST_TIMEOUT_MS, 10) : 10000,
      rateLimitPerMinute: 90,
    },
    jikan: {
      endpoint: process.env.JIKAN_API_URL || 'https://api.jikan.moe/v4',
      timeoutMs: process.env.JIKAN_TIMEOUT_MS ? parseInt(process.env.JIKAN_TIMEOUT_MS, 10) : 10000,
      rateLimitPerSecond: 3,
    },
    kitsu: {
      endpoint: process.env.KITSU_API_URL || 'https://kitsu.io/api/edge',
      timeoutMs: process.env.KITSU_TIMEOUT_MS ? parseInt(process.env.KITSU_TIMEOUT_MS, 10) : 10000,
      rateLimitPerMinute: 60,
    },
  },
};
