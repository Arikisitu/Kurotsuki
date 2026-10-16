import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AniListProvider } from '../../providers/anilist';

describe('AniListProvider', () => {
  let provider: AniListProvider;

  beforeEach(() => {
    provider = new AniListProvider('https://graphql.anilist.co', 5000);
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  const mockAniListMedia = {
    id: 16498,
    idMal: 16498,
    title: {
      romaji: 'Shingeki no Kyojin',
      english: 'Attack on Titan',
      native: '進撃の巨人',
      userPreferred: 'Attack on Titan',
    },
    type: 'ANIME',
    format: 'TV',
    status: 'FINISHED',
    description: 'Centuries ago, mankind was slaughtered to near extinction...',
    startDate: { year: 2013, month: 4, day: 7 },
    endDate: { year: 2013, month: 9, day: 29 },
    season: 'SPRING',
    seasonYear: 2013,
    episodes: 25,
    duration: 24,
    source: 'MANGA',
    countryOfOrigin: 'JP',
    coverImage: {
      extraLarge: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx16498.jpg',
      large: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx16498.jpg',
      medium: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/small/bx16498.jpg',
      color: '#e45b5b',
    },
    bannerImage: 'https://s4.anilist.co/file/anilistcdn/media/anime/banner/16498.jpg',
    genres: ['Action', 'Drama', 'Fantasy', 'Mystery'],
    averageScore: 85,
    meanScore: 86,
    popularity: 500000,
    trending: 120,
    favourites: 45000,
    isAdult: false,
    studios: {
      nodes: [
        { id: 858, name: 'Wit Studio', isAnimationStudio: true, siteUrl: 'https://witstudio.co.jp' },
      ],
    },
  };

  it('successfully fetches and normalizes trending anime', async () => {
    const mockResponse = {
      data: {
        Page: {
          pageInfo: {
            total: 100,
            perPage: 20,
            currentPage: 1,
            lastPage: 5,
            hasNextPage: true,
          },
          media: [mockAniListMedia],
        },
      },
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as any);

    const result = await provider.getTrending(1);

    expect(result.results.items).toHaveLength(1);
    const item = result.results.items[0]!;
    expect(item.id).toBe(16498);
    expect(item.title.english).toBe('Attack on Titan');
    expect(item.type).toBe('ANIME');
    expect(item.format).toBe('TV');
    expect(item.averageScore).toBe(85);
    expect(item.artwork.cover.large).toBe(mockAniListMedia.coverImage.large);
    expect(item.studios[0]?.name).toBe('Wit Studio');
    expect(result.results.pageInfo.hasNextPage).toBe(true);
  });

  it('successfully searches anime with normalized result', async () => {
    const mockResponse = {
      data: {
        Page: {
          pageInfo: {
            total: 1,
            perPage: 20,
            currentPage: 1,
            lastPage: 1,
            hasNextPage: false,
          },
          media: [mockAniListMedia],
        },
      },
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as any);

    const result = await provider.searchAnime('Attack on Titan', 1);

    expect(result.query).toBe('Attack on Titan');
    expect(result.results.items).toHaveLength(1);
    expect(result.results.items[0]!.id).toBe(16498);
  });

  it('successfully fetches anime by id with full relations', async () => {
    const mockResponse = {
      data: {
        Media: {
          ...mockAniListMedia,
          tags: [{ id: 1, name: 'Military', category: 'Theme' }],
          relations: { edges: [] },
          characters: { edges: [] },
          externalLinks: [{ id: 1, site: 'Crunchyroll', url: 'https://crunchyroll.com' }],
        },
      },
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as any);

    const anime = await provider.getAnimeById('16498');

    expect(anime).not.toBeNull();
    expect(anime?.id).toBe(16498);
    expect(anime?.tags[0]?.name).toBe('Military');
    expect(anime?.externalLinks?.[0]?.site).toBe('Crunchyroll');
  });

  it('handles 429 rate limit errors properly', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: false,
      status: 429,
      headers: {
        get: (h: string) => (h.toLowerCase() === 'retry-after' ? '30' : null),
      },
    } as any);

    await expect(provider.getTrending(1)).rejects.toThrow(/Rate limit/);
  });

  it('handles 404 / media not found gracefully by returning null', async () => {
    const mockResponse = {
      errors: [{ message: 'Not Found.' }],
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as any);

    const result = await provider.getAnimeById('999999999');
    expect(result).toBeNull();
  });
});
