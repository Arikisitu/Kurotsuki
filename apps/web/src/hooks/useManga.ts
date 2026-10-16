/**
 * React Query hooks for Manga discovery and details
 */

import { useQuery } from '@tanstack/react-query';
import { anilistFetch } from '../lib/anilist/client';
import {
  TRENDING_MANGA_QUERY,
  POPULAR_MANGA_QUERY,
  TOP_RATED_MANGA_QUERY,
  MANGA_DETAILS_QUERY,
} from '../lib/anilist/queries';
import { AniListMedia, AniListPageData } from '../lib/anilist/types';
import { normalizeManga } from '../lib/anilist/normalizer';

export function useTrendingManga(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['manga', 'trending', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(TRENDING_MANGA_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeManga),
      };
    },
    staleTime: 1000 * 60 * 15,
  });
}

export function usePopularManga(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['manga', 'popular', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(POPULAR_MANGA_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeManga),
      };
    },
    staleTime: 1000 * 60 * 20,
  });
}

export function useTopRatedManga(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['manga', 'top-rated', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(TOP_RATED_MANGA_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeManga),
      };
    },
    staleTime: 1000 * 60 * 30,
  });
}

export function useMangaDetails(id: number | null | undefined) {
  return useQuery({
    queryKey: ['manga', 'details', id],
    queryFn: async () => {
      if (!id) throw new Error('Valid Manga ID required');
      const data = await anilistFetch<{ Media: AniListMedia }>(MANGA_DETAILS_QUERY, { id });
      return normalizeManga(data.Media);
    },
    enabled: typeof id === 'number' && !isNaN(id) && id > 0,
    staleTime: 1000 * 60 * 15,
  });
}
