/**
 * React Query hooks for Anime discovery and details
 */

import { useQuery } from '@tanstack/react-query';
import { Anime, AnimeSeason } from '@sahasra/shared-types';
import { anilistFetch } from '../lib/anilist/client';
import {
  TRENDING_ANIME_QUERY,
  POPULAR_ANIME_QUERY,
  TOP_RATED_ANIME_QUERY,
  SEASONAL_ANIME_QUERY,
  UPCOMING_ANIME_QUERY,
  ANIME_DETAILS_QUERY,
  AIRING_SCHEDULE_QUERY,
} from '../lib/anilist/queries';
import { AniListMedia, AniListPageData } from '../lib/anilist/types';
import { normalizeAnime } from '../lib/anilist/normalizer';

export function getCurrentSeason(): { season: AnimeSeason; year: number } {
  const now = new Date();
  const month = now.getMonth(); // 0 to 11
  const year = now.getFullYear();

  let season: AnimeSeason = 'WINTER';
  if (month >= 2 && month <= 4) {
    season = 'SPRING';
  } else if (month >= 5 && month <= 7) {
    season = 'SUMMER';
  } else if (month >= 8 && month <= 10) {
    season = 'FALL';
  } else {
    season = 'WINTER';
  }

  return { season, year };
}

export function useTrendingAnime(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['anime', 'trending', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(TRENDING_ANIME_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime),
      };
    },
    staleTime: 1000 * 60 * 10, // 10 minutes
  });
}

export function usePopularAnime(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['anime', 'popular', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(POPULAR_ANIME_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime),
      };
    },
    staleTime: 1000 * 60 * 15,
  });
}

export function useTopRatedAnime(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['anime', 'top-rated', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(TOP_RATED_ANIME_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime),
      };
    },
    staleTime: 1000 * 60 * 30,
  });
}

export function useSeasonalAnime(
  season?: AnimeSeason,
  seasonYear?: number,
  page = 1,
  perPage = 20
) {
  const current = getCurrentSeason();
  const activeSeason = season || current.season;
  const activeYear = seasonYear || current.year;

  return useQuery({
    queryKey: ['anime', 'seasonal', activeSeason, activeYear, page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(SEASONAL_ANIME_QUERY, {
        season: activeSeason,
        seasonYear: activeYear,
        page,
        perPage,
      });
      return {
        season: activeSeason,
        year: activeYear,
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime),
      };
    },
    staleTime: 1000 * 60 * 15,
  });
}

export function useUpcomingAnime(page = 1, perPage = 20) {
  return useQuery({
    queryKey: ['anime', 'upcoming', page, perPage],
    queryFn: async () => {
      const data = await anilistFetch<{ Page: AniListPageData }>(UPCOMING_ANIME_QUERY, { page, perPage });
      return {
        pageInfo: data.Page.pageInfo,
        items: data.Page.media.map(normalizeAnime),
      };
    },
    staleTime: 1000 * 60 * 30,
  });
}

export function useAnimeDetails(id: number | null | undefined) {
  return useQuery({
    queryKey: ['anime', 'details', id],
    queryFn: async () => {
      if (!id) throw new Error('Valid Anime ID required');
      const data = await anilistFetch<{ Media: AniListMedia }>(ANIME_DETAILS_QUERY, { id });
      return normalizeAnime(data.Media);
    },
    enabled: typeof id === 'number' && !isNaN(id) && id > 0,
    staleTime: 1000 * 60 * 15,
  });
}

export interface AiringScheduleItem {
  id: number;
  airingAt: number;
  timeUntilAiring: number;
  episode: number;
  media: Anime;
}

export function useAiringSchedule(airingAt_greater: number, airingAt_lesser: number) {
  return useQuery({
    queryKey: ['anime', 'schedule', airingAt_greater, airingAt_lesser],
    queryFn: async () => {
      const data = await anilistFetch<{
        Page: {
          pageInfo: { hasNextPage: boolean; total: number };
          airingSchedules: {
            id: number;
            airingAt: number;
            timeUntilAiring: number;
            episode: number;
            media: AniListMedia;
          }[];
        };
      }>(AIRING_SCHEDULE_QUERY, {
        airingAt_greater,
        airingAt_lesser,
        perPage: 50,
      });

      return data.Page.airingSchedules.map((schedule) => ({
        id: schedule.id,
        airingAt: schedule.airingAt,
        timeUntilAiring: schedule.timeUntilAiring,
        episode: schedule.episode,
        media: normalizeAnime(schedule.media),
      }));
    },
    staleTime: 1000 * 60 * 5,
  });
}
