/**
 * @file common.ts
 * @description Foundational utility, pagination, search, and sorting types for the SAHASRA ecosystem.
 */

/**
 * Represents a date where day or month may be unknown or unreleased.
 * Directly maps to AniList and standard anime/manga release dates.
 */
export interface FuzzyDate {
  /** 4-digit calendar year (e.g. 2024) */
  year?: number | null;
  /** Month index 1-12 (1 = January) */
  month?: number | null;
  /** Day of the month 1-31 */
  day?: number | null;
}

/**
 * Standard date range filter or bounds.
 */
export interface DateRange {
  /** Start date boundary */
  start?: FuzzyDate | string | null;
  /** End date boundary */
  end?: FuzzyDate | string | null;
}

/**
 * Standard metadata for paginated datasets.
 */
export interface PaginationMeta {
  /** Total count of records matching query */
  total: number;
  /** Number of items displayed per page */
  perPage: number;
  /** Current active page index (1-based) */
  currentPage: number;
  /** Final accessible page index */
  lastPage: number;
  /** Flag indicating if subsequent pages exist */
  hasNextPage: boolean;
}

/**
 * Generic wrapper container for paginated list payloads.
 */
export interface Pagination<T> {
  /** The collection of records for the active page */
  items: T[];
  /** Pagination traversal metadata */
  pageInfo: PaginationMeta;
}

/**
 * Sorting direction specification.
 */
export type SortDirection = 'ASC' | 'DESC';

/**
 * Generic sort descriptor structure.
 */
export interface SortOption<TField extends string = string> {
  /** The model field key used for ordering */
  field: TField;
  /** Direction: ascending or descending */
  direction: SortDirection;
  /** Human-readable label for UI selection */
  label?: string;
}

/**
 * Base filtering arguments applicable to media searches and catalogs.
 */
export interface FilterOptions {
  /** Free-text title, character, or studio search query */
  search?: string;
  /** List of genre names to include */
  genres?: string[];
  /** List of genre names to strictly exclude */
  genresExclude?: string[];
  /** List of descriptive tag names */
  tags?: string[];
  /** List of tag names to strictly exclude */
  tagsExclude?: string[];
  /** Primary release year */
  year?: number;
  /** Season of release */
  season?: string;
  /** Minimum average community score (0-100) */
  minScore?: number;
  /** Maximum average community score (0-100) */
  maxScore?: number;
  /** Whether to include adult / 18+ NSFW content */
  isAdult?: boolean;
  /** Country of origin code (e.g. JP, KR, CN) */
  countryOfOrigin?: string;
  /** Licensed streaming or reader service */
  source?: string;
}

/**
 * Standardized search result container.
 */
export interface SearchResult<T> {
  /** Search query string that generated these results */
  query: string;
  /** Active filter criteria applied */
  appliedFilters: Partial<FilterOptions>;
  /** Paginated payload of matched records */
  results: Pagination<T>;
  /** Execution time in milliseconds for edge telemetry */
  executionTimeMs?: number;
}

/**
 * Community score distribution histogram bucket.
 */
export interface ScoreDistribution {
  /** Score bucket (e.g. 10, 20, 30... 100) */
  score: number;
  /** Total count of users who submitted this score */
  amount: number;
}

/**
 * User library status distribution metric.
 */
export interface StatusDistribution {
  /** Status key (e.g. 'CURRENT', 'COMPLETED', etc.) */
  status: string;
  /** Total count of users with media in this status */
  amount: number;
}

/**
 * Official or community leaderboard ranking entry.
 */
export interface RankItem {
  /** Numerical rank position (e.g. 1 for Top 1) */
  rank: number;
  /** Classification of rank */
  type: 'RATED' | 'POPULAR';
  /** Whether ranking is all-time or seasonal */
  allTime: boolean;
  /** Calendar year if seasonal rank */
  year?: number | null;
  /** Calendar season if seasonal rank */
  season?: string | null;
}

/**
 * Recursive Partial helper for deep nested updates.
 */
export type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};

/**
 * Nullable helper type.
 */
export type Nullable<T> = T | null;
