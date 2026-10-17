/**
 * @file endpoints.ts
 * @description Master catalog of API endpoint routes and HTTP method contracts for the SAHASRA platform.
 */

/**
 * Standard HTTP methods supported by the API gateway.
 */
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

/**
 * Comprehensive mapping of API route endpoints.
 */
export const API_ENDPOINTS = {
  // --- Anime Catalog Endpoints ---
  ANIME: {
    SEARCH: '/api/v1/anime',
    TRENDING: '/api/v1/anime/trending',
    POPULAR: '/api/v1/anime/popular',
    TOP_RATED: '/api/v1/anime/top-rated',
    SEASONAL: '/api/v1/anime/seasonal',
    SCHEDULE: '/api/v1/anime/schedule',
    DETAILS: (id: string | number) => `/api/v1/anime/${id}`,
    RELATIONS: (id: string | number) => `/api/v1/anime/${id}/relations`,
    CHARACTERS: (id: string | number) => `/api/v1/anime/${id}/characters`,
    STAFF: (id: string | number) => `/api/v1/anime/${id}/staff`,
    EPISODES: (id: string | number) => `/api/v1/anime/${id}/episodes`,
    RECOMMENDATIONS: (id: string | number) => `/api/v1/anime/${id}/recommendations`,
  },

  // --- Manga Catalog Endpoints ---
  MANGA: {
    SEARCH: '/api/v1/manga',
    TRENDING: '/api/v1/manga/trending',
    POPULAR: '/api/v1/manga/popular',
    TOP_RATED: '/api/v1/manga/top-rated',
    DETAILS: (id: string | number) => `/api/v1/manga/${id}`,
    RELATIONS: (id: string | number) => `/api/v1/manga/${id}/relations`,
    CHARACTERS: (id: string | number) => `/api/v1/manga/${id}/characters`,
    STAFF: (id: string | number) => `/api/v1/manga/${id}/staff`,
    CHAPTERS: (id: string | number) => `/api/v1/manga/${id}/chapters`,
    VOLUMES: (id: string | number) => `/api/v1/manga/${id}/volumes`,
    RECOMMENDATIONS: (id: string | number) => `/api/v1/manga/${id}/recommendations`,
  },

  // --- Global Discovery & Taxonomy ---
  DISCOVERY: {
    GLOBAL_SEARCH: '/api/v1/search',
    GENRES: '/api/v1/genres',
    TAGS: '/api/v1/tags',
    STUDIOS: '/api/v1/studios',
    STAFF: '/api/v1/staff',
    CHARACTERS: '/api/v1/characters',
  },

  // --- User Profiles & Library Tracking ---
  USER: {
    ME: '/api/v1/users/me',
    PROFILE: (username: string) => `/api/v1/users/${username}`,
    LIBRARY: (userId: string | number) => `/api/v1/users/${userId}/library`,
    STATS: (userId: string | number) => `/api/v1/users/${userId}/stats`,
  },

  // --- Library Entry Mutations ---
  LIBRARY: {
    ENTRIES: '/api/v1/library/entries',
    ENTRY_BY_ID: (id: string | number) => `/api/v1/library/entries/${id}`,
    SYNC: '/api/v1/library/sync',
  },

  // --- Playback & Reading Progress ---
  PROGRESS: {
    WATCH: '/api/v1/progress/watch',
    READING: '/api/v1/progress/reading',
  },

  // --- Streaming & One-Click Source Resolution ---
  STREAM: {
    RESOLVE: '/api/v1/stream/resolve',
    EXTENSIONS: '/api/v1/stream/repo/extensions',
    PING: '/api/v1/stream/ping',
  },

  // --- System & Telemetry ---
  SYSTEM: {
    HEALTH: '/api/v1/health',
    METRICS: '/api/v1/metrics',
    PROVIDERS_STATUS: '/api/v1/providers/status',
  },
} as const;

/**
 * Route parameter definition helper.
 */
export type ApiEndpoints = typeof API_ENDPOINTS;
