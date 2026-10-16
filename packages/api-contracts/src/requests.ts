/**
 * @file requests.ts
 * @description Strongly typed request parameters, payloads, and mutation inputs for the SAHASRA API.
 */

import {
  AnimeFormat,
  AnimeSeason,
  AnimeStatus,
  MangaFormat,
  MangaStatus,
  MediaType,
  UserMediaListStatus,
} from '@sahasra/shared-types';

/**
 * Standard pagination query parameters.
 */
export interface PaginationParams {
  /** Page number to fetch (1-based index) */
  page?: number;
  /** Number of items to retrieve per page (max 50) */
  perPage?: number;
}

/**
 * Anime search and filtering query parameters.
 */
export interface AnimeSearchRequest extends PaginationParams {
  /** Text search query string */
  query?: string;
  /** Genre names to filter by */
  genres?: string[];
  /** Genre names to strictly exclude */
  genresExclude?: string[];
  /** Categorical tags to filter by */
  tags?: string[];
  /** Categorical tags to strictly exclude */
  tagsExclude?: string[];
  /** Specific anime distribution format */
  format?: AnimeFormat;
  /** Broadcast release status */
  status?: AnimeStatus;
  /** Premiere season quarter */
  season?: AnimeSeason;
  /** Premiere release year */
  seasonYear?: number;
  /** Minimum average score percentage (0-100) */
  minScore?: number;
  /** Maximum average score percentage (0-100) */
  maxScore?: number;
  /** Include adult/NSFW content */
  isAdult?: boolean;
  /** Sorting criteria: POPULARITY_DESC, SCORE_DESC, TRENDING_DESC, START_DATE_DESC, TITLE_ROMAJI */
  sort?: 'POPULARITY_DESC' | 'SCORE_DESC' | 'TRENDING_DESC' | 'START_DATE_DESC' | 'TITLE_ROMAJI' | 'EPISODES_DESC';
}

/**
 * Seasonal anime release query parameters.
 */
export interface SeasonalAnimeRequest extends PaginationParams {
  /** Season quarter (WINTER, SPRING, SUMMER, FALL) */
  season: AnimeSeason;
  /** Calendar year (e.g. 2024, 2025) */
  year: number;
  /** Sort order */
  sort?: 'POPULARITY_DESC' | 'SCORE_DESC' | 'TRENDING_DESC';
}

/**
 * Airing calendar query parameters.
 */
export interface AiringScheduleRequest extends PaginationParams {
  /** Start Unix timestamp in seconds */
  startTimestamp?: number;
  /** End Unix timestamp in seconds */
  endTimestamp?: number;
  /** Restrict exclusively to episodes that have not yet aired */
  notYetAired?: boolean;
}

/**
 * Manga search and catalog query parameters.
 */
export interface MangaSearchRequest extends PaginationParams {
  /** Free-text title search */
  query?: string;
  /** Genre names to include */
  genres?: string[];
  /** Genre names to strictly exclude */
  genresExclude?: string[];
  /** Descriptive tags */
  tags?: string[];
  /** Specific manga format */
  format?: MangaFormat;
  /** Serialization status */
  status?: MangaStatus;
  /** Minimum average rating */
  minScore?: number;
  /** Include adult/18+ works */
  isAdult?: boolean;
  /** Sorting criteria: POPULARITY_DESC, SCORE_DESC, TRENDING_DESC, START_DATE_DESC, CHAPTERS_DESC */
  sort?: 'POPULARITY_DESC' | 'SCORE_DESC' | 'TRENDING_DESC' | 'START_DATE_DESC' | 'CHAPTERS_DESC';
}

/**
 * Detailed media resource fetch parameters.
 */
export interface MediaDetailsRequest {
  /** Unique Media ID */
  id: number;
  /** Include franchise relations */
  includeRelations?: boolean;
  /** Include character cast */
  includeCharacters?: boolean;
  /** Include staff credits */
  includeStaff?: boolean;
}

/**
 * User library collection retrieval query.
 */
export interface UserLibraryRequest extends PaginationParams {
  /** Target user identifier */
  userId: string | number;
  /** Filter by media type */
  mediaType?: MediaType;
  /** Filter by library status (CURRENT, COMPLETED, etc.) */
  status?: UserMediaListStatus;
  /** Sort criteria */
  sort?: 'UPDATED_AT_DESC' | 'SCORE_DESC' | 'PROGRESS_DESC' | 'TITLE';
}

/**
 * Payload to create a new media tracking entry in user's library.
 */
export interface CreateLibraryEntryRequest {
  /** Media identifier (Anime or Manga ID) */
  mediaId: number;
  /** Media type */
  mediaType: MediaType;
  /** Initial tracking status */
  status: UserMediaListStatus;
  /** Rating score (0-100 or 0-10) */
  score?: number;
  /** Initial episode/chapter progress */
  progress?: number;
  /** Initial volume progress */
  progressVolumes?: number;
  /** Personal notes */
  notes?: string;
  /** Set entry as private */
  private?: boolean;
}

/**
 * Payload to mutate an existing library entry.
 */
export interface UpdateLibraryEntryRequest {
  /** Updated tracking status */
  status?: UserMediaListStatus;
  /** Updated score */
  score?: number;
  /** Watched episode or read chapter count */
  progress?: number;
  /** Read volume count */
  progressVolumes?: number;
  /** Rewatch or reread iteration count */
  repeat?: number;
  /** Personal notes or review */
  notes?: string;
  /** Privacy flag */
  private?: boolean;
}

/**
 * Real-time video playback watch progress sync payload.
 */
export interface UpdateWatchProgressRequest {
  /** Target Anime ID */
  mediaId: number;
  /** Episode number */
  episodeNumber: number;
  /** Current playback position in seconds */
  currentTimeSeconds: number;
  /** Total video duration in seconds */
  durationSeconds: number;
  /** Completion flag */
  completed: boolean;
}

/**
 * Real-time reader chapter progress sync payload.
 */
export interface UpdateReadingProgressRequest {
  /** Target Manga ID */
  mediaId: number;
  /** Chapter number */
  chapterNumber: number;
  /** Volume number if applicable */
  volumeNumber?: number;
  /** Current page index */
  currentPage: number;
  /** Total page count */
  totalPages: number;
  /** Completion flag */
  completed: boolean;
}
