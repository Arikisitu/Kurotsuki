/**
 * Search and Debounce hooks for Anime and Manga
 */

import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Anime, Manga } from '@sahasra/shared-types';
import { anilistFetch } from '../lib/anilist/client';
import { SEARCH_ANIME_QUERY, SEARCH_MANGA_QUERY } from '../lib/anilist/queries';
import { AniListPageData } from '../lib/anilist/types';
import { normalizeAnime, normalizeManga } from '../lib/anilist/normalizer';

/**
 * Debounce hook to throttle fast typing
 */
export function useDebounce<T>(value: T, delay = 350): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

export interface AnimeSearchParams {
  search?: string;
  genre?: string[];
  year?: number;
  season?: string;
  format?: string[];
  status?: string;
  sort?: string[];
  page?: number;
  perPage?: number;
}

export function useAnimeSearch(params: AnimeSearchParams) {
  const {
    search,
    genre,
    year,
    season,
    format,
    status,
    sort = ['POPULARITY_DESC'],
    page = 1,
    perPage = 24,
  } = params;

  return useQuery({
    queryKey: ['anime', 'search', search, genre, year, season, format, status, sort, page, perPage],
    queryFn: async () => {
      const variables: Record<string, unknown> = {
        page,
        perPage,
        sort,
      };

      if (search && search.trim().length > 0) variables.search = search.trim();
      if (genre && genre.length > 0) variables.genre = genre;
      if (year) variables.year = year;
      if (season) variables.season = season;
      if (format && format.length > 0) variables.format = format;
      if (status) variables.status = status;

      const data = await anilistFetch<{ Page: AniListPageData }>(SEARCH_ANIME_QUERY, variables);

      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime) as Anime[],
      };
    },
    staleTime: 1000 * 60 * 5,
  });
}

export interface MangaSearchParams {
  search?: string;
  genre?: string[];
  format?: string[];
  status?: string;
  sort?: string[];
  page?: number;
  perPage?: number;
}

export function useMangaSearch(params: MangaSearchParams) {
  const {
    search,
    genre,
    format,
    status,
    sort = ['POPULARITY_DESC'],
    page = 1,
    perPage = 24,
  } = params;

  return useQuery({
    queryKey: ['manga', 'search', search, genre, format, status, sort, page, perPage],
    queryFn: async () => {
      const variables: Record<string, unknown> = {
        page,
        perPage,
        sort,
      };

      if (search && search.trim().length > 0) variables.search = search.trim();
      if (genre && genre.length > 0) variables.genre = genre;
      if (format && format.length > 0) variables.format = format;
      if (status) variables.status = status;

      const data = await anilistFetch<{ Page: AniListPageData }>(SEARCH_MANGA_QUERY, variables);

      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeManga) as Manga[],
      };
    },
    staleTime: 1000 * 60 * 5,
  });
}
