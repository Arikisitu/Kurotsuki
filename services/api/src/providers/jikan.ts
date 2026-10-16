/**
 * @file jikan.ts
 * @description Jikan v4 (MyAnimeList REST API) provider with rate limiting and normalization.
 */

import { config } from '../config';
import { ProviderError } from '../errors';
import {
  Anime,
  AnimeBroadcast,
  AnimeFormat,
  AnimeSeason,
  AnimeSource,
  AnimeStatus,
  FuzzyDate,
  Manga,
  MangaFormat,
  MangaStatus,
  MediaArtwork,
  MediaTitle,
  ProviderCapability,
  ProviderType,
  SearchResult,
  StudioNode,
} from '../models';
import { AnimeProvider } from './types';

export class JikanProvider implements AnimeProvider {
  public readonly name = 'Jikan';
  public readonly type: ProviderType = 'JIKAN';
  public readonly capabilities: ProviderCapability[] = [
    'REST',
    'ANIME_SEARCH',
    'MANGA_SEARCH',
    'SEASONAL',
    'CHARACTER_STAFF',
    'TRENDING',
    'POPULAR',
  ];

  private readonly endpoint: string;
  private readonly timeoutMs: number;
  private lastRequestTime = 0;
  private readonly minIntervalMs = 350; // ~2.8 requests per second max

  constructor(endpoint?: string, timeoutMs?: number) {
    this.endpoint = endpoint || config.providers.jikan.endpoint;
    this.timeoutMs = timeoutMs || config.providers.jikan.timeoutMs;
  }

  public async isAvailable(): Promise<boolean> {
    try {
      const res = await this.get<{ data: any[] }>(`/seasons/now?limit=1`, 3000);
      return Array.isArray(res?.data);
    } catch {
      return false;
    }
  }

  public async getTrending(page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.get<{ data: any[]; pagination: any }>(
      `/top/anime?filter=airing&page=${page}&limit=20`
    );

    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: { status: 'RELEASING' },
      results: {
        items,
        pageInfo: {
          total: data.pagination?.items?.total || items.length,
          perPage: data.pagination?.items?.per_page || 20,
          currentPage: data.pagination?.current_page || page,
          lastPage: data.pagination?.last_visible_page || page,
          hasNextPage: Boolean(data.pagination?.has_next_page),
        },
      },
    };
  }

  public async getPopular(page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.get<{ data: any[]; pagination: any }>(
      `/top/anime?filter=bypopularity&page=${page}&limit=20`
    );

    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: {},
      results: {
        items,
        pageInfo: {
          total: data.pagination?.items?.total || items.length,
          perPage: data.pagination?.items?.per_page || 20,
          currentPage: data.pagination?.current_page || page,
          lastPage: data.pagination?.last_visible_page || page,
          hasNextPage: Boolean(data.pagination?.has_next_page),
        },
      },
    };
  }

  public async getSeasonal(
    year: number,
    season: string,
    page: number = 1
  ): Promise<SearchResult<Anime>> {
    const normalizedSeason = season.toLowerCase();
    const data = await this.get<{ data: any[]; pagination: any }>(
      `/seasons/${year}/${normalizedSeason}?page=${page}&limit=20`
    );

    const items = (data.data || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: { year, season: season.toUpperCase() },
      results: {
        items,
        pageInfo: {
          total: data.pagination?.items?.total || items.length,
          perPage: data.pagination?.items?.per_page || 20,
          currentPage: data.pagination?.current_page || page,
          lastPage: data.pagination?.last_visible_page || page,
          hasNextPage: Boolean(data.pagination?.has_next_page),
        },
      },
    };
  }

  public async searchAnime(query: string, page: number = 1): Promise<SearchResult<Anime>> {
    const path = query.trim()
      ? `/anime?q=${encodeURIComponent(query.trim())}&page=${page}&limit=20`
      : `/top/anime?page=${page}&limit=20`;

    const data = await this.get<{ data: any[]; pagination: any }>(path);
    const items = (data.data || []).map((m) => this.normalizeAnime(m));

    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total: data.pagination?.items?.total || items.length,
          perPage: data.pagination?.items?.per_page || 20,
          currentPage: data.pagination?.current_page || page,
          lastPage: data.pagination?.last_visible_page || page,
          hasNextPage: Boolean(data.pagination?.has_next_page),
        },
      },
    };
  }

  public async searchManga(query: string, page: number = 1): Promise<SearchResult<Manga>> {
    const path = query.trim()
      ? `/manga?q=${encodeURIComponent(query.trim())}&page=${page}&limit=20`
      : `/top/manga?page=${page}&limit=20`;

    const data = await this.get<{ data: any[]; pagination: any }>(path);
    const items = (data.data || []).map((m) => this.normalizeManga(m));

    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total: data.pagination?.items?.total || items.length,
          perPage: data.pagination?.items?.per_page || 20,
          currentPage: data.pagination?.current_page || page,
          lastPage: data.pagination?.last_visible_page || page,
          hasNextPage: Boolean(data.pagination?.has_next_page),
        },
      },
    };
  }

  public async getAnimeById(id: string): Promise<Anime | null> {
    try {
      const data = await this.get<{ data: any }>(`/anime/${id}/full`);
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
      const data = await this.get<{ data: any }>(`/manga/${id}/full`);
      if (!data.data) return null;
      return this.normalizeManga(data.data);
    } catch (err) {
      if (err instanceof ProviderError && err.httpStatus === 404) {
        return null;
      }
      throw err;
    }
  }

  private async throttle(): Promise<void> {
    const now = Date.now();
    const timeSinceLast = now - this.lastRequestTime;
    if (timeSinceLast < this.minIntervalMs) {
      const waitTime = this.minIntervalMs - timeSinceLast;
      await new Promise((resolve) => setTimeout(resolve, waitTime));
    }
    this.lastRequestTime = Date.now();
  }

  private async get<T>(path: string, customTimeoutMs?: number): Promise<T> {
    await this.throttle();

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), customTimeoutMs || this.timeoutMs);
    const url = `${this.endpoint}${path}`;

    try {
      const res = await fetch(url, {
        headers: {
          Accept: 'application/json',
          'User-Agent': 'SAHASRA-API/1.0',
        },
        signal: controller.signal,
      });

      if (res.status === 429) {
        throw new ProviderError(this.type, 'Rate limit exceeded on Jikan API (MAL)', {
          statusCode: 429,
          httpStatus: 429,
          code: 'RATE_LIMITED',
          retryAfterSeconds: 2,
        });
      }

      if (res.status === 404) {
        throw new ProviderError(this.type, `Jikan resource not found: ${path}`, {
          statusCode: 404,
          httpStatus: 404,
          code: 'NOT_FOUND',
        });
      }

      if (!res.ok) {
        throw new ProviderError(this.type, `Jikan HTTP Error: ${res.statusText}`, {
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
      throw new ProviderError(this.type, `Network failure reaching Jikan API: ${error.message || String(error)}`, {
        statusCode: 502,
        code: 'NETWORK_ERROR',
        details: { originalError: String(error) },
      });
    } finally {
      clearTimeout(timeout);
    }
  }

  private normalizeAnime(raw: any): Anime {
    const format = this.mapAnimeFormat(raw.type);
    const status = this.mapAnimeStatus(raw.status);

    const title: MediaTitle = {
      romaji: raw.title || null,
      english: raw.title_english || null,
      native: raw.title_japanese || null,
      userPreferred: raw.title_english || raw.title || 'Unknown Title',
    };

    const artwork: MediaArtwork = {
      cover: {
        extraLarge: raw.images?.jpg?.large_image_url || null,
        large: raw.images?.jpg?.large_image_url || raw.images?.jpg?.image_url || null,
        medium: raw.images?.jpg?.image_url || null,
        color: null,
      },
      banner: null,
      dominantColor: null,
    };

    const startDate: FuzzyDate | null = raw.aired?.prop?.from
      ? {
          year: raw.aired.prop.from.year || null,
          month: raw.aired.prop.from.month || null,
          day: raw.aired.prop.from.day || null,
        }
      : null;

    const endDate: FuzzyDate | null = raw.aired?.prop?.to
      ? {
          year: raw.aired.prop.to.year || null,
          month: raw.aired.prop.to.month || null,
          day: raw.aired.prop.to.day || null,
        }
      : null;

    const studios: StudioNode[] = (raw.studios || []).map((s: any) => ({
      id: s.mal_id,
      name: s.name,
      isMain: true,
      siteUrl: s.url || null,
    }));

    const broadcast: AnimeBroadcast | null = raw.broadcast
      ? {
          day: raw.broadcast.day || null,
          time: raw.broadcast.time || null,
          timezone: raw.broadcast.timezone || null,
          string: raw.broadcast.string || null,
        }
      : null;

    return {
      id: raw.mal_id,
      idMal: raw.mal_id,
      type: 'ANIME',
      format,
      status,
      season: raw.season ? (raw.season.toUpperCase() as AnimeSeason) : null,
      seasonYear: raw.year || null,
      title,
      description: raw.synopsis || '',
      startDate,
      endDate,
      episodes: raw.episodes ?? null,
      duration: raw.duration ? this.parseDuration(raw.duration) : null,
      source: raw.source ? (raw.source.toUpperCase().replace(/\s+/g, '_') as AnimeSource) : null,
      countryOfOrigin: 'JP',
      genres: (raw.genres || []).map((g: any) => g.name),
      tags: [...(raw.themes || []), ...(raw.demographics || [])].map((t: any) => ({
        id: t.mal_id,
        name: t.name,
        category: 'Theme',
      })),
      averageScore: raw.score ? Math.round(raw.score * 10) : null,
      meanScore: raw.score ? Math.round(raw.score * 10) : null,
      popularity: raw.popularity ?? 0,
      trending: 0,
      favourites: raw.favorites ?? 0,
      isAdult: raw.rating?.includes('Rx') || raw.rating?.includes('Hentai') || false,
      artwork,
      trailer: raw.trailer?.youtube_id
        ? {
            id: raw.trailer.youtube_id,
            site: 'youtube',
            thumbnail: raw.trailer.images?.maximum_image_url || null,
          }
        : null,
      broadcast,
      studios,
      externalIdentifiers: [
        { source: 'MAL', id: raw.mal_id, url: `https://myanimelist.net/anime/${raw.mal_id}` },
      ],
      relations: (raw.relations || []).flatMap((rel: any) =>
        (rel.entry || []).map((entry: any) => ({
          relationType: this.mapRelationType(rel.relation),
          node: {
            id: entry.mal_id,
            type: entry.type === 'manga' ? 'MANGA' : 'ANIME',
            title: { userPreferred: entry.name },
          },
        }))
      ),
    };
  }

  private normalizeManga(raw: any): Manga {
    const format = this.mapMangaFormat(raw.type);
    const status = this.mapMangaStatus(raw.status);

    const title: MediaTitle = {
      romaji: raw.title || null,
      english: raw.title_english || null,
      native: raw.title_japanese || null,
      userPreferred: raw.title_english || raw.title || 'Unknown Title',
    };

    const artwork: MediaArtwork = {
      cover: {
        extraLarge: raw.images?.jpg?.large_image_url || null,
        large: raw.images?.jpg?.large_image_url || raw.images?.jpg?.image_url || null,
        medium: raw.images?.jpg?.image_url || null,
      },
    };

    const authors = (raw.authors || []).map((a: any) => ({
      id: a.mal_id,
      name: { full: a.name, userPreferred: a.name },
      role: 'Author',
    }));

    return {
      id: raw.mal_id,
      idMal: raw.mal_id,
      type: 'MANGA',
      format,
      status,
      title,
      description: raw.synopsis || '',
      chapters: raw.chapters ?? null,
      volumes: raw.volumes ?? null,
      countryOfOrigin: 'JP',
      genres: (raw.genres || []).map((g: any) => g.name),
      tags: [...(raw.themes || []), ...(raw.demographics || [])].map((t: any) => ({
        id: t.mal_id,
        name: t.name,
      })),
      averageScore: raw.score ? Math.round(raw.score * 10) : null,
      meanScore: raw.score ? Math.round(raw.score * 10) : null,
      popularity: raw.popularity ?? 0,
      trending: 0,
      favourites: raw.favorites ?? 0,
      isAdult: false,
      artwork,
      authors,
      artists: [],
      externalIdentifiers: [
        { source: 'MAL', id: raw.mal_id, url: `https://myanimelist.net/manga/${raw.mal_id}` },
      ],
    };
  }

  private mapAnimeFormat(type?: string): AnimeFormat {
    switch (type?.toLowerCase()) {
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

  private mapAnimeStatus(status?: string): AnimeStatus {
    switch (status?.toLowerCase()) {
      case 'currently airing':
        return 'RELEASING';
      case 'not yet aired':
        return 'NOT_YET_RELEASED';
      default:
        return 'FINISHED';
    }
  }

  private mapMangaFormat(type?: string): MangaFormat {
    switch (type?.toLowerCase()) {
      case 'lightnovel':
      case 'novel':
        return 'NOVEL';
      case 'oneshot':
        return 'ONE_SHOT';
      default:
        return 'MANGA';
    }
  }

  private mapMangaStatus(status?: string): MangaStatus {
    switch (status?.toLowerCase()) {
      case 'publishing':
        return 'RELEASING';
      case 'on hiatus':
        return 'HIATUS';
      case 'discontinued':
        return 'CANCELLED';
      default:
        return 'FINISHED';
    }
  }

  private mapRelationType(rel?: string): any {
    switch (rel?.toLowerCase()) {
      case 'adaptation':
        return 'ADAPTATION';
      case 'prequel':
        return 'PREQUEL';
      case 'sequel':
        return 'SEQUEL';
      case 'side story':
        return 'SIDE_STORY';
      case 'character':
        return 'CHARACTER';
      case 'summary':
        return 'SUMMARY';
      case 'spin-off':
        return 'SPIN_OFF';
      default:
        return 'OTHER';
    }
  }

  private parseDuration(str: string): number | null {
    const match = str.match(/(\d+)\s*min/i);
    return match && match[1] ? parseInt(match[1], 10) : null;
  }
}
