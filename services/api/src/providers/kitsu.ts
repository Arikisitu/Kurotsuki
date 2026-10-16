/**
 * @file kitsu.ts
 * @description Kitsu JSON:API provider implementation with normalization.
 */

import { config } from '../config';
import { ProviderError } from '../errors';
import {
  Anime,
  AnimeFormat,
  AnimeStatus,
  FuzzyDate,
  Manga,
  MediaArtwork,
  MediaTitle,
  ProviderCapability,
  ProviderType,
  SearchResult,
} from '../models';
import { AnimeProvider } from './types';

export class KitsuProvider implements AnimeProvider {
  public readonly name = 'Kitsu';
  public readonly type: ProviderType = 'KITSU';
  public readonly capabilities: ProviderCapability[] = [
    'REST',
    'ANIME_SEARCH',
    'MANGA_SEARCH',
    'SEASONAL',
    'TRENDING',
    'POPULAR',
  ];

  private readonly endpoint: string;
  private readonly timeoutMs: number;

  constructor(endpoint?: string, timeoutMs?: number) {
    this.endpoint = endpoint || config.providers.kitsu.endpoint;
    this.timeoutMs = timeoutMs || config.providers.kitsu.timeoutMs;
  }

  public async isAvailable(): Promise<boolean> {
    try {
      const res = await this.get<{ data: any[] }>(`/anime?page[limit]=1`, 3000);
      return Array.isArray(res?.data);
    } catch {
      return false;
    }
  }

  public async getTrending(page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.get<{ data: any[] }>(`/trending/anime`);
    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    const offset = (page - 1) * 20;
    const paginatedItems = items.slice(offset, offset + 20);

    return {
      query: '',
      appliedFilters: { status: 'RELEASING' },
      results: {
        items: paginatedItems,
        pageInfo: {
          total: items.length,
          perPage: 20,
          currentPage: page,
          lastPage: Math.ceil(items.length / 20) || 1,
          hasNextPage: offset + 20 < items.length,
        },
      },
    };
  }

  public async getPopular(page: number = 1): Promise<SearchResult<Anime>> {
    const offset = (page - 1) * 20;
    const data = await this.get<{ data: any[]; meta: any }>(
      `/anime?sort=-userCount&page[limit]=20&page[offset]=${offset}`
    );

    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    const total = data.meta?.count || items.length;

    return {
      query: '',
      appliedFilters: {},
      results: {
        items,
        pageInfo: {
          total,
          perPage: 20,
          currentPage: page,
          lastPage: Math.ceil(total / 20) || 1,
          hasNextPage: offset + 20 < total,
        },
      },
    };
  }

  public async getSeasonal(
    year: number,
    season: string,
    page: number = 1
  ): Promise<SearchResult<Anime>> {
    const offset = (page - 1) * 20;
    const normalizedSeason = season.toLowerCase();
    const data = await this.get<{ data: any[]; meta: any }>(
      `/anime?filter[seasonYear]=${year}&filter[season]=${normalizedSeason}&page[limit]=20&page[offset]=${offset}`
    );

    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    const total = data.meta?.count || items.length;

    return {
      query: '',
      appliedFilters: { year, season: season.toUpperCase() },
      results: {
        items,
        pageInfo: {
          total,
          perPage: 20,
          currentPage: page,
          lastPage: Math.ceil(total / 20) || 1,
          hasNextPage: offset + 20 < total,
        },
      },
    };
  }

  public async searchAnime(query: string, page: number = 1): Promise<SearchResult<Anime>> {
    const offset = (page - 1) * 20;
    const path = query.trim()
      ? `/anime?filter[text]=${encodeURIComponent(query.trim())}&page[limit]=20&page[offset]=${offset}`
      : `/anime?sort=-userCount&page[limit]=20&page[offset]=${offset}`;

    const data = await this.get<{ data: any[]; meta: any }>(path);
    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    const total = data.meta?.count || items.length;

    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total,
          perPage: 20,
          currentPage: page,
          lastPage: Math.ceil(total / 20) || 1,
          hasNextPage: offset + 20 < total,
        },
      },
    };
  }

  public async searchManga(query: string, page: number = 1): Promise<SearchResult<Manga>> {
    const offset = (page - 1) * 20;
    const path = query.trim()
      ? `/manga?filter[text]=${encodeURIComponent(query.trim())}&page[limit]=20&page[offset]=${offset}`
      : `/manga?sort=-userCount&page[limit]=20&page[offset]=${offset}`;

    const data = await this.get<{ data: any[]; meta: any }>(path);
    const items = (data.data || []).map((m) => this.normalizeManga(m));
    const total = data.meta?.count || items.length;

    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total,
          perPage: 20,
          currentPage: page,
          lastPage: Math.ceil(total / 20) || 1,
          hasNextPage: offset + 20 < total,
        },
      },
    };
  }

  public async getAnimeById(id: string): Promise<Anime | null> {
    try {
      const data = await this.get<{ data: any }>(`/anime/${id}`);
      if (!data.data) return null;
      return this.normalizeAnime(data.data);
    } catch (err) {
      if (err instanceof ProviderError && err.httpStatus === 404) {
        return null;
      }
      throw err;
    }
  }

  public async getMangaById(id: string): Promise<Manga | null> {
    try {
      const data = await this.get<{ data: any }>(`/manga/${id}`);
      if (!data.data) return null;
      return this.normalizeManga(data.data);
    } catch (err) {
      if (err instanceof ProviderError && err.httpStatus === 404) {
        return null;
      }
      throw err;
    }
  }

  private async get<T>(path: string, customTimeoutMs?: number): Promise<T> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), customTimeoutMs || this.timeoutMs);
    const url = `${this.endpoint}${path}`;

    try {
      const res = await fetch(url, {
        headers: {
          Accept: 'application/vnd.api+json',
          'Content-Type': 'application/vnd.api+json',
        },
        signal: controller.signal,
      });

      if (res.status === 429) {
        throw new ProviderError(this.type, 'Rate limit exceeded on Kitsu API', {
          statusCode: 429,
          httpStatus: 429,
          code: 'RATE_LIMITED',
          retryAfterSeconds: 30,
        });
      }

      if (res.status === 404) {
        throw new ProviderError(this.type, `Kitsu resource not found: ${path}`, {
          statusCode: 404,
          httpStatus: 404,
          code: 'NOT_FOUND',
        });
      }

      if (!res.ok) {
        throw new ProviderError(this.type, `Kitsu HTTP Error: ${res.statusText}`, {
          statusCode: res.status >= 500 ? 502 : res.status,
          httpStatus: res.status,
        });
      }

      return (await res.json()) as T;
    } catch (error: any) {
      if (error instanceof ProviderError) {
        throw error;
      }
      if (error.name === 'AbortError') {
        throw new ProviderError(this.type, `Request timed out after ${customTimeoutMs || this.timeoutMs}ms`, {
          statusCode: 504,
          code: 'TIMEOUT',
        });
      }
      throw new ProviderError(this.type, `Network failure reaching Kitsu API: ${error.message || String(error)}`, {
        statusCode: 502,
        code: 'NETWORK_ERROR',
        details: { originalError: String(error) },
      });
    } finally {
      clearTimeout(timeout);
    }
  }

  private normalizeAnime(raw: any): Anime {
    const attrs = raw.attributes || {};
    const id = parseInt(raw.id, 10) || Math.floor(Math.random() * 100000);

    const title: MediaTitle = {
      romaji: attrs.titles?.en_jp || null,
      english: attrs.titles?.en || null,
      native: attrs.titles?.ja_jp || null,
      userPreferred: attrs.canonicalTitle || attrs.titles?.en || attrs.titles?.en_jp || 'Unknown Title',
    };

    const artwork: MediaArtwork = {
      cover: {
        extraLarge: attrs.posterImage?.original || null,
        large: attrs.posterImage?.large || attrs.posterImage?.medium || null,
        medium: attrs.posterImage?.medium || attrs.posterImage?.small || null,
      },
      banner: attrs.coverImage?.large || attrs.coverImage?.original || null,
    };

    const startDate = this.parseDateString(attrs.startDate);
    const endDate = this.parseDateString(attrs.endDate);

    const format = this.mapSubtypeToFormat(attrs.subtype);
    const status = this.mapStatus(attrs.status);

    const averageScore = attrs.averageRating ? Math.round(parseFloat(attrs.averageRating)) : null;

    return {
      id,
      type: 'ANIME',
      format,
      status,
      season: null,
      seasonYear: startDate?.year || null,
      title,
      description: attrs.synopsis || attrs.description || '',
      startDate,
      endDate,
      episodes: attrs.episodeCount ?? null,
      duration: attrs.episodeLength ?? null,
      countryOfOrigin: 'JP',
      genres: [],
      tags: [],
      averageScore,
      meanScore: averageScore,
      popularity: attrs.userCount ?? 0,
      trending: 0,
      favourites: attrs.favoritesCount ?? 0,
      isAdult: Boolean(attrs.ageRatingGuide && attrs.ageRatingGuide.includes('18+')),
      artwork,
      trailer: attrs.youtubeVideoId
        ? {
            id: attrs.youtubeVideoId,
            site: 'youtube',
          }
        : null,
      studios: [],
      externalIdentifiers: [
        { source: 'KITSU', id: raw.id, url: `https://kitsu.io/anime/${attrs.slug || raw.id}` },
      ],
    };
  }

  private normalizeManga(raw: any): Manga {
    const attrs = raw.attributes || {};
    const id = parseInt(raw.id, 10) || Math.floor(Math.random() * 100000);

    const title: MediaTitle = {
      romaji: attrs.titles?.en_jp || null,
      english: attrs.titles?.en || null,
      native: attrs.titles?.ja_jp || null,
      userPreferred: attrs.canonicalTitle || attrs.titles?.en || attrs.titles?.en_jp || 'Unknown Title',
    };

    const artwork: MediaArtwork = {
      cover: {
        extraLarge: attrs.posterImage?.original || null,
        large: attrs.posterImage?.large || attrs.posterImage?.medium || null,
        medium: attrs.posterImage?.medium || attrs.posterImage?.small || null,
      },
      banner: attrs.coverImage?.large || attrs.coverImage?.original || null,
    };

    const startDate = this.parseDateString(attrs.startDate);
    const endDate = this.parseDateString(attrs.endDate);
    const status = this.mapStatus(attrs.status);
    const averageScore = attrs.averageRating ? Math.round(parseFloat(attrs.averageRating)) : null;

    return {
      id,
      type: 'MANGA',
      format: 'MANGA',
      status,
      title,
      description: attrs.synopsis || attrs.description || '',
      startDate,
      endDate,
      chapters: attrs.chapterCount ?? null,
      volumes: attrs.volumeCount ?? null,
      countryOfOrigin: 'JP',
      genres: [],
      tags: [],
      averageScore,
      meanScore: averageScore,
      popularity: attrs.userCount ?? 0,
      trending: 0,
      favourites: attrs.favoritesCount ?? 0,
      isAdult: false,
      artwork,
      authors: [],
      artists: [],
      externalIdentifiers: [
        { source: 'KITSU', id: raw.id, url: `https://kitsu.io/manga/${attrs.slug || raw.id}` },
      ],
    };
  }

  private mapSubtypeToFormat(subtype?: string): AnimeFormat {
    switch (subtype?.toLowerCase()) {
      case 'movie':
        return 'MOVIE';
      case 'ova':
        return 'OVA';
      case 'ona':
        return 'ONA';
      case 'special':
        return 'SPECIAL';
      case 'music':
        return 'MUSIC';
      default:
        return 'TV';
    }
  }

  private mapStatus(status?: string): AnimeStatus {
    switch (status?.toLowerCase()) {
      case 'current':
        return 'RELEASING';
      case 'upcoming':
      case 'unreleased':
      case 'tba':
        return 'NOT_YET_RELEASED';
      default:
        return 'FINISHED';
    }
  }

  private parseDateString(dateStr?: string): FuzzyDate | null {
    if (!dateStr) return null;
    const parts = dateStr.split('-');
    return {
      year: parts[0] ? parseInt(parts[0], 10) : null,
      month: parts[1] ? parseInt(parts[1], 10) : null,
      day: parts[2] ? parseInt(parts[2], 10) : null,
    };
  }
}
