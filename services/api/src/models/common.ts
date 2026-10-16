/**
 * @file common.ts
 * @description Foundational pagination, search, sorting, and filter types.
 */

export interface FuzzyDate {
  year?: number | null;
  month?: number | null;
  day?: number | null;
}

export interface DateRange {
  start?: FuzzyDate | string | null;
  end?: FuzzyDate | string | null;
}

export interface PaginationMeta {
  total: number;
  perPage: number;
  currentPage: number;
  lastPage: number;
  hasNextPage: boolean;
}

export interface Pagination<T> {
  items: T[];
  pageInfo: PaginationMeta;
}

export type SortDirection = 'ASC' | 'DESC';

export interface SortOption<TField extends string = string> {
  field: TField;
  direction: SortDirection;
  label?: string;
}

export interface FilterOptions {
  search?: string;
  genres?: string[];
  genresExclude?: string[];
  tags?: string[];
  tagsExclude?: string[];
  year?: number;
  season?: string;
  minScore?: number;
  maxScore?: number;
  isAdult?: boolean;
  countryOfOrigin?: string;
  source?: string;
  format?: string;
  status?: string;
}

export interface SearchResult<T> {
  query: string;
  appliedFilters: Partial<FilterOptions>;
  results: Pagination<T>;
  executionTimeMs?: number;
}

export interface ScoreDistribution {
  score: number;
  amount: number;
}

export interface StatusDistribution {
  status: string;
  amount: number;
}

export interface RankItem {
  rank: number;
  type: 'RATED' | 'POPULAR';
  allTime: boolean;
  year?: number | null;
  season?: string | null;
}
