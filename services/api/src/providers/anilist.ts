/**
 * @file anilist.ts
 * @description AniList GraphQL provider implementation with normalization and rate limit awareness.
 */

import { config } from '../config';
import { ProviderError } from '../errors';
import {
  Anime,
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

// AniList GraphQL queries
const MEDIA_FRAGMENT = `
  id
  idMal
  title {
    romaji
    english
    native
    userPreferred
  }
  type
  format
  status
  description(asHtml: false)
  startDate {
    year
    month
    day
  }
  endDate {
    year
    month
    day
  }
  season
  seasonYear
  episodes
  duration
  chapters
  volumes
  countryOfOrigin
  source
  trailer {
    id
    site
    thumbnail
  }
  coverImage {
    extraLarge
    large
    medium
    color
  }
  bannerImage
  genres
  averageScore
  meanScore
  popularity
  trending
  favourites
  isAdult
  nextAiringEpisode {
    id
    airingAt
    timeUntilAiring
    episode
    mediaId
  }
  studios(isMain: true) {
    nodes {
      id
      name
      isAnimationStudio
      siteUrl
    }
  }
  staff(perPage: 10) {
    edges {
      role
      node {
        id
        name {
          first
          last
          full
          native
          userPreferred
        }
        image {
          large
          medium
        }
      }
    }
  }
`;

const ANIME_PAGE_QUERY = `
  query ($page: Int, $perPage: Int, $sort: [MediaSort], $season: MediaSeason, $seasonYear: Int, $search: String) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        perPage
        currentPage
        lastPage
        hasNextPage
      }
      media(type: ANIME, sort: $sort, season: $season, seasonYear: $seasonYear, search: $search, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

const MANGA_PAGE_QUERY = `
  query ($page: Int, $perPage: Int, $sort: [MediaSort], $search: String) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        perPage
        currentPage
        lastPage
        hasNextPage
      }
      media(type: MANGA, sort: $sort, search: $search, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

const MEDIA_BY_ID_QUERY = `
  query ($id: Int, $type: MediaType) {
    Media(id: $id, type: $type) {
      ${MEDIA_FRAGMENT}
      tags {
        id
        name
        description
        category
        rank
        isGeneralSpoiler
        isMediaSpoiler
        isAdult
      }
      relations {
        edges {
          relationType
          node {
            id
            type
            title {
              romaji
              english
              native
              userPreferred
            }
            format
            status
            coverImage {
              large
              medium
            }
            averageScore
          }
        }
      }
      characters(perPage: 12, sort: [ROLE, RELEVANCE]) {
        edges {
          role
          node {
            id
            name {
              first
              middle
              last
              full
              native
              userPreferred
            }
            image {
              large
              medium
            }
          }
          voiceActors(language: JAPANESE) {
            id
            name {
              first
              last
              full
              native
              userPreferred
            }
            image {
              large
              medium
            }
          }
        }
      }
      externalLinks {
        id
        url
        site
        icon
        color
        type
      }
      streamingEpisodes {
        title
        thumbnail
        url
        site
      }
    }
  }
`;

export class AniListProvider implements AnimeProvider {
  public readonly name = 'AniList';
  public readonly type: ProviderType = 'ANILIST';
  public readonly capabilities: ProviderCapability[] = [
    'GRAPHQL',
    'ANIME_SEARCH',
    'MANGA_SEARCH',
    'SEASONAL',
    'AIRING_SCHEDULE',
    'CHARACTER_STAFF',
    'TRENDING',
    'POPULAR',
  ];

  private readonly endpoint: string;
  private readonly timeoutMs: number;

  constructor(endpoint?: string, timeoutMs?: number) {
    this.endpoint = endpoint || config.providers.anilist.endpoint;
    this.timeoutMs = timeoutMs || config.providers.anilist.timeoutMs;
  }

  public async isAvailable(): Promise<boolean> {
    try {
      const response = await this.executeGraphQL<{ Page: { pageInfo: { total: number } } }>(
        `query { Page(page: 1, perPage: 1) { pageInfo { total } } }`,
        {},
        3000
      );
      return Boolean(response?.Page?.pageInfo);
    } catch {
      return false;
    }
  }

  public async getTrending(page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.executeGraphQL<{ Page: { pageInfo: any; media: any[] } }>(
      ANIME_PAGE_QUERY,
      { page, perPage: 20, sort: ['TRENDING_DESC', 'POPULARITY_DESC'] }
    );

    const items = (data.Page?.media || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: { format: 'ALL' },
      results: {
        items,
        pageInfo: {
          total: data.Page?.pageInfo?.total || items.length,
          perPage: data.Page?.pageInfo?.perPage || 20,
          currentPage: data.Page?.pageInfo?.currentPage || page,
          lastPage: data.Page?.pageInfo?.lastPage || page,
          hasNextPage: Boolean(data.Page?.pageInfo?.hasNextPage),
        },
      },
    };
  }

  public async getPopular(page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.executeGraphQL<{ Page: { pageInfo: any; media: any[] } }>(
      ANIME_PAGE_QUERY,
      { page, perPage: 20, sort: ['POPULARITY_DESC'] }
    );

    const items = (data.Page?.media || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: {},
      results: {
        items,
        pageInfo: {
          total: data.Page?.pageInfo?.total || items.length,
          perPage: data.Page?.pageInfo?.perPage || 20,
          currentPage: data.Page?.pageInfo?.currentPage || page,
          lastPage: data.Page?.pageInfo?.lastPage || page,
          hasNextPage: Boolean(data.Page?.pageInfo?.hasNextPage),
        },
      },
    };
  }

  public async getSeasonal(
    year: number,
    season: string,
    page: number = 1
  ): Promise<SearchResult<Anime>> {
    const normalizedSeason = season.toUpperCase() as AnimeSeason;
    const data = await this.executeGraphQL<{ Page: { pageInfo: any; media: any[] } }>(
      ANIME_PAGE_QUERY,
      {
        page,
        perPage: 20,
        season: normalizedSeason,
        seasonYear: year,
        sort: ['POPULARITY_DESC'],
      }
    );

    const items = (data.Page?.media || []).map((m) => this.normalizeAnime(m));
    return {
      query: '',
      appliedFilters: { year, season: normalizedSeason },
      results: {
        items,
        pageInfo: {
          total: data.Page?.pageInfo?.total || items.length,
          perPage: data.Page?.pageInfo?.perPage || 20,
          currentPage: data.Page?.pageInfo?.currentPage || page,
          lastPage: data.Page?.pageInfo?.lastPage || page,
          hasNextPage: Boolean(data.Page?.pageInfo?.hasNextPage),
        },
      },
    };
  }

  public async searchAnime(query: string, page: number = 1): Promise<SearchResult<Anime>> {
    const data = await this.executeGraphQL<{ Page: { pageInfo: any; media: any[] } }>(
      ANIME_PAGE_QUERY,
      {
        page,
        perPage: 20,
        search: query.trim() || undefined,
        sort: query.trim() ? ['SEARCH_MATCH', 'POPULARITY_DESC'] : ['POPULARITY_DESC'],
      }
    );

    const items = (data.Page?.media || []).map((m) => this.normalizeAnime(m));
    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total: data.Page?.pageInfo?.total || items.length,
          perPage: data.Page?.pageInfo?.perPage || 20,
          currentPage: data.Page?.pageInfo?.currentPage || page,
          lastPage: data.Page?.pageInfo?.lastPage || page,
          hasNextPage: Boolean(data.Page?.pageInfo?.hasNextPage),
        },
      },
    };
  }

  public async searchManga(query: string, page: number = 1): Promise<SearchResult<Manga>> {
    const data = await this.executeGraphQL<{ Page: { pageInfo: any; media: any[] } }>(
      MANGA_PAGE_QUERY,
      {
        page,
        perPage: 20,
        search: query.trim() || undefined,
        sort: query.trim() ? ['SEARCH_MATCH', 'POPULARITY_DESC'] : ['POPULARITY_DESC'],
      }
    );

    const items = (data.Page?.media || []).map((m) => this.normalizeManga(m));
    return {
      query,
      appliedFilters: { search: query },
      results: {
        items,
        pageInfo: {
          total: data.Page?.pageInfo?.total || items.length,
          perPage: data.Page?.pageInfo?.perPage || 20,
          currentPage: data.Page?.pageInfo?.currentPage || page,
          lastPage: data.Page?.pageInfo?.lastPage || page,
          hasNextPage: Boolean(data.Page?.pageInfo?.hasNextPage),
        },
      },
    };
  }

  public async getAnimeById(id: string): Promise<Anime | null> {
    const numericId = parseInt(id, 10);
    if (isNaN(numericId)) return null;

    try {
      const data = await this.executeGraphQL<{ Media: any }>(MEDIA_BY_ID_QUERY, {
        id: numericId,
        type: 'ANIME',
      });

      if (!data.Media) return null;
      return this.normalizeAnime(data.Media);
    } catch (err) {
      if (err instanceof ProviderError && err.httpStatus === 404) {
        return null;
      }
      throw err;
    }
  }

  public async getMangaById(id: string): Promise<Manga | null> {
    const numericId = parseInt(id, 10);
    if (isNaN(numericId)) return null;

    try {
      const data = await this.executeGraphQL<{ Media: any }>(MEDIA_BY_ID_QUERY, {
        id: numericId,
        type: 'MANGA',
      });

      if (!data.Media) return null;
      return this.normalizeManga(data.Media);
    } catch (err) {
      if (err instanceof ProviderError && err.httpStatus === 404) {
        return null;
      }
      throw err;
    }
  }

  private async executeGraphQL<T>(
    query: string,
    variables: Record<string, unknown> = {},
    customTimeoutMs?: number
  ): Promise<T> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), customTimeoutMs || this.timeoutMs);

    try {
      const res = await fetch(this.endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
        body: JSON.stringify({ query, variables }),
        signal: controller.signal,
      });

      if (res.status === 429) {
        const retryAfter = res.headers.get('Retry-After');
        const retrySeconds = retryAfter ? parseInt(retryAfter, 10) : 60;
        throw new ProviderError(this.type, 'Rate limit exceeded on AniList GraphQL API', {
          statusCode: 429,
          httpStatus: 429,
          code: 'RATE_LIMITED',
          retryAfterSeconds: retrySeconds,
        });
      }

      if (!res.ok) {
        const errorText = await res.text().catch(() => '');
        throw new ProviderError(this.type, `AniList API HTTP error: ${res.statusText}`, {
          statusCode: res.status >= 500 ? 502 : res.status,
          httpStatus: res.status,
          details: { status: res.status, body: errorText },
        });
      }

      const json = (await res.json()) as { data?: T; errors?: Array<{ message: string }> };

      if (json.errors && json.errors.length > 0) {
        const msg = json.errors.map((e) => e.message).join('; ');
        const isNotFound = json.errors.some((e) => e.message.toLowerCase().includes('not found'));
        throw new ProviderError(this.type, msg, {
          statusCode: isNotFound ? 404 : 502,
          httpStatus: isNotFound ? 404 : 500,
          code: isNotFound ? 'NOT_FOUND' : 'GRAPHQL_ERROR',
          details: json.errors,
        });
      }

      if (!json.data) {
        throw new ProviderError(this.type, 'Malformed AniList response, no data field returned', {
          statusCode: 502,
          code: 'SCHEMA_MISMATCH',
        });
      }

      return json.data;
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
      throw new ProviderError(this.type, `Network or unexpected failure: ${error.message || String(error)}`, {
        statusCode: 502,
        code: 'NETWORK_ERROR',
        details: { originalError: String(error) },
      });
    } finally {
      clearTimeout(timeout);
    }
  }

  private normalizeAnime(raw: any): Anime {
    const artwork: MediaArtwork = {
      cover: {
        extraLarge: raw.coverImage?.extraLarge || null,
        large: raw.coverImage?.large || null,
        medium: raw.coverImage?.medium || null,
        color: raw.coverImage?.color || null,
      },
      banner: raw.bannerImage || null,
      dominantColor: raw.coverImage?.color || null,
    };

    const studios: StudioNode[] = (raw.studios?.nodes || []).map((node: any) => ({
      id: node.id,
      name: node.name,
      isMain: Boolean(node.isAnimationStudio),
      siteUrl: node.siteUrl || null,
    }));

    const title: MediaTitle = {
      romaji: raw.title?.romaji || null,
      english: raw.title?.english || null,
      native: raw.title?.native || null,
      userPreferred: raw.title?.userPreferred || raw.title?.english || raw.title?.romaji || 'Unknown Title',
    };

    return {
      id: raw.id,
      idMal: raw.idMal || null,
      type: 'ANIME',
      format: (raw.format as AnimeFormat) || 'TV',
      status: (raw.status as AnimeStatus) || 'FINISHED',
      season: (raw.season as AnimeSeason) || null,
      seasonYear: raw.seasonYear || null,
      title,
      description: raw.description || '',
      startDate: raw.startDate ? (raw.startDate as FuzzyDate) : null,
      endDate: raw.endDate ? (raw.endDate as FuzzyDate) : null,
      episodes: raw.episodes ?? null,
      duration: raw.duration ?? null,
      source: (raw.source as AnimeSource) || null,
      countryOfOrigin: raw.countryOfOrigin || 'JP',
      genres: raw.genres || [],
      tags: (raw.tags || []).map((t: any) => ({
        id: t.id,
        name: t.name,
        description: t.description || null,
        category: t.category || null,
        rank: t.rank || null,
        isGeneralSpoiler: Boolean(t.isGeneralSpoiler),
        isMediaSpoiler: Boolean(t.isMediaSpoiler),
        isAdult: Boolean(t.isAdult),
      })),
      averageScore: raw.averageScore ?? null,
      meanScore: raw.meanScore ?? null,
      popularity: raw.popularity ?? 0,
      trending: raw.trending ?? 0,
      favourites: raw.favourites ?? 0,
      isAdult: Boolean(raw.isAdult),
      artwork,
      trailer: raw.trailer
        ? {
            id: raw.trailer.id || null,
            site: raw.trailer.site || null,
            thumbnail: raw.trailer.thumbnail || null,
          }
        : null,
      nextAiringEpisode: raw.nextAiringEpisode || null,
      studios,
      externalIdentifiers: [
        { source: 'ANILIST', id: raw.id, url: `https://anilist.co/anime/${raw.id}` },
        ...(raw.idMal ? [{ source: 'MAL' as const, id: raw.idMal, url: `https://myanimelist.net/anime/${raw.idMal}` }] : []),
      ],
      externalLinks: (raw.externalLinks || []).map((l: any) => ({
        id: l.id,
        url: l.url,
        site: l.site,
        icon: l.icon || null,
        color: l.color || null,
        type: l.type || null,
      })),
      streamingEpisodes: (raw.streamingEpisodes || []).map((s: any) => ({
        site: s.site,
        title: s.title || null,
        thumbnail: s.thumbnail || null,
        url: s.url,
      })),
      characters: (raw.characters?.edges || []).map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
        voiceActors: (edge.voiceActors || []).map((va: any) => ({
          id: va.id,
          name: va.name,
          image: va.image,
        })),
      })),
      staff: (raw.staff?.edges || []).map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
      })),
      relations: (raw.relations?.edges || []).map((edge: any) => ({
        relationType: edge.relationType,
        node: {
          id: edge.node.id,
          type: edge.node.type,
          title: edge.node.title,
          format: edge.node.format,
          status: edge.node.status,
          coverImage: edge.node.coverImage,
          averageScore: edge.node.averageScore,
        },
      })),
    };
  }

  private normalizeManga(raw: any): Manga {
    const artwork: MediaArtwork = {
      cover: {
        extraLarge: raw.coverImage?.extraLarge || null,
        large: raw.coverImage?.large || null,
        medium: raw.coverImage?.medium || null,
        color: raw.coverImage?.color || null,
      },
      banner: raw.bannerImage || null,
      dominantColor: raw.coverImage?.color || null,
    };

    const title: MediaTitle = {
      romaji: raw.title?.romaji || null,
      english: raw.title?.english || null,
      native: raw.title?.native || null,
      userPreferred: raw.title?.userPreferred || raw.title?.english || raw.title?.romaji || 'Unknown Title',
    };

    const authors = (raw.staff?.edges || [])
      .filter((edge: any) => /story|author|creator|original/i.test(edge.role || ''))
      .map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
      }));

    const artists = (raw.staff?.edges || [])
      .filter((edge: any) => /art|illustrat/i.test(edge.role || ''))
      .map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
      }));

    return {
      id: raw.id,
      idMal: raw.idMal || null,
      type: 'MANGA',
      format: (raw.format as MangaFormat) || 'MANGA',
      status: (raw.status as MangaStatus) || 'FINISHED',
      title,
      description: raw.description || '',
      startDate: raw.startDate ? (raw.startDate as FuzzyDate) : null,
      endDate: raw.endDate ? (raw.endDate as FuzzyDate) : null,
      chapters: raw.chapters ?? null,
      volumes: raw.volumes ?? null,
      countryOfOrigin: raw.countryOfOrigin || 'JP',
      genres: raw.genres || [],
      tags: (raw.tags || []).map((t: any) => ({
        id: t.id,
        name: t.name,
        description: t.description || null,
        category: t.category || null,
        rank: t.rank || null,
        isGeneralSpoiler: Boolean(t.isGeneralSpoiler),
        isMediaSpoiler: Boolean(t.isMediaSpoiler),
        isAdult: Boolean(t.isAdult),
      })),
      averageScore: raw.averageScore ?? null,
      meanScore: raw.meanScore ?? null,
      popularity: raw.popularity ?? 0,
      trending: raw.trending ?? 0,
      favourites: raw.favourites ?? 0,
      isAdult: Boolean(raw.isAdult),
      artwork,
      authors: authors.length > 0 ? authors : (raw.staff?.edges || []).slice(0, 1).map((e: any) => ({
        id: e.node.id,
        name: e.node.name,
        image: e.node.image,
        role: e.role,
      })),
      artists,
      externalIdentifiers: [
        { source: 'ANILIST', id: raw.id, url: `https://anilist.co/manga/${raw.id}` },
        ...(raw.idMal ? [{ source: 'MAL' as const, id: raw.idMal, url: `https://myanimelist.net/manga/${raw.idMal}` }] : []),
      ],
      externalLinks: (raw.externalLinks || []).map((l: any) => ({
        id: l.id,
        url: l.url,
        site: l.site,
        icon: l.icon || null,
        color: l.color || null,
        type: l.type || null,
      })),
      characters: (raw.characters?.edges || []).map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
      })),
      staff: (raw.staff?.edges || []).map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        image: edge.node.image,
        role: edge.role,
      })),
      relations: (raw.relations?.edges || []).map((edge: any) => ({
        relationType: edge.relationType,
        node: {
          id: edge.node.id,
          type: edge.node.type,
          title: edge.node.title,
          format: edge.node.format,
          status: edge.node.status,
          coverImage: edge.node.coverImage,
          averageScore: edge.node.averageScore,
        },
      })),
    };
  }
}
