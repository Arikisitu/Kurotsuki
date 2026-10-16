import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { JikanProvider } from '../../providers/jikan';

describe('JikanProvider', () => {
  let provider: JikanProvider;

  beforeEach(() => {
    provider = new JikanProvider('https://api.jikan.moe/v4', 5000);
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  const mockJikanAnime = {
    mal_id: 1,
    title: 'Cowboy Bebop',
    title_english: 'Cowboy Bebop',
    title_japanese: 'カウボーイビバップ',
    type: 'TV',
    source: 'Original',
    episodes: 26,
    status: 'Finished Airing',
    airing: false,
    aired: {
      from: '1998-04-03T00:00:00+00:00',
      to: '1999-04-24T00:00:00+00:00',
      prop: {
        from: { day: 3, month: 4, year: 1998 },
        to: { day: 24, month: 4, year: 1999 },
      },
    },
    duration: '24 min per ep',
    rating: 'R - 17+ (violence & profanity)',
    score: 8.75,
    scored_by: 914301,
    rank: 43,
    popularity: 43,
    members: 1771531,
    favorites: 78525,
    synopsis: 'Crime is timeless. By the year 2071...',
    season: 'spring',
    year: 1998,
    broadcast: {
      day: 'Saturdays',
      time: '01:00',
      timezone: 'Asia/Tokyo',
      string: 'Saturdays at 01:00 (JST)',
    },
    images: {
      jpg: {
        image_url: 'https://cdn.myanimelist.net/images/anime/4/19644.jpg',
        large_image_url: 'https://cdn.myanimelist.net/images/anime/4/19644l.jpg',
      },
    },
    studios: [{ mal_id: 14, type: 'anime', name: 'Sunrise', url: 'https://myanimelist.net/anime/producer/14/Sunrise' }],
    genres: [{ mal_id: 1, type: 'anime', name: 'Action', url: 'https://myanimelist.net/anime/genre/1/Action' }],
  };

  it('normalizes Jikan trending anime response', async () => {
    const mockResponse = {
      data: [mockJikanAnime],
      pagination: {
        last_visible_page: 10,
        has_next_page: true,
        current_page: 1,
        items: { count: 1, total: 10, per_page: 20 },
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
    expect(item.id).toBe(1);
    expect(item.title.english).toBe('Cowboy Bebop');
    expect(item.format).toBe('TV');
    expect(item.status).toBe('FINISHED');
    // Score 8.75 converted to 100-point scale: 88
    expect(item.averageScore).toBe(88);
    expect(item.studios[0]?.name).toBe('Sunrise');
    expect(item.duration).toBe(24);
  });

  it('searches anime via Jikan REST API', async () => {
    const mockResponse = {
      data: [mockJikanAnime],
      pagination: {
        last_visible_page: 1,
        has_next_page: false,
        current_page: 1,
        items: { count: 1, total: 1, per_page: 20 },
      },
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => mockResponse,
    } as any);

    const result = await provider.searchAnime('Bebop', 1);

    expect(result.query).toBe('Bebop');
    expect(result.results.items[0]!.id).toBe(1);
  });

  it('handles 429 rate limit correctly', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: false,
      status: 429,
    } as any);

    await expect(provider.getTrending(1)).rejects.toThrow(/Rate limit exceeded/);
  });

  it('handles 404 not found by returning null', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: false,
      status: 404,
      statusText: 'Not Found',
    } as any);

    const result = await provider.getAnimeById('9999999');
    expect(result).toBeNull();
  });
});
