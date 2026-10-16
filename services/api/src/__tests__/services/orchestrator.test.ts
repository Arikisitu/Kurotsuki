import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AnimeProvider } from '../../providers/types';
import { ProviderRegistry } from '../../providers/registry';
import { CacheService } from '../../services/cache';
import { CircuitBreaker } from '../../services/circuit-breaker';
import { ProviderOrchestrator } from '../../services/orchestrator';

describe('ProviderOrchestrator', () => {
  let orchestrator: ProviderOrchestrator;
  let registry: ProviderRegistry;
  let cache: CacheService;
  let cb: CircuitBreaker;

  let mockPrimary: AnimeProvider;
  let mockSecondary: AnimeProvider;

  beforeEach(() => {
    cache = new CacheService();
    cb = new CircuitBreaker();

    mockPrimary = {
      name: 'AniList',
      type: 'ANILIST',
      capabilities: ['ANIME_SEARCH'],
      isAvailable: vi.fn().mockResolvedValue(true),
      searchAnime: vi.fn(),
      searchManga: vi.fn(),
      getAnimeById: vi.fn(),
      getMangaById: vi.fn(),
      getTrending: vi.fn(),
      getPopular: vi.fn(),
      getSeasonal: vi.fn(),
    };

    mockSecondary = {
      name: 'Jikan',
      type: 'JIKAN',
      capabilities: ['ANIME_SEARCH'],
      isAvailable: vi.fn().mockResolvedValue(true),
      searchAnime: vi.fn(),
      searchManga: vi.fn(),
      getAnimeById: vi.fn(),
      getMangaById: vi.fn(),
      getTrending: vi.fn(),
      getPopular: vi.fn(),
      getSeasonal: vi.fn(),
    };

    registry = new ProviderRegistry({
      registerDefaults: false,
      primary: 'AniList',
      secondaries: ['Jikan'],
    });
    registry.register(mockPrimary);
    registry.register(mockSecondary);

    orchestrator = new ProviderOrchestrator({
      registry,
      cache,
      circuitBreaker: cb,
    });
  });

  it('uses primary provider when it succeeds', async () => {
    const mockResult = { results: { items: [{ id: 1, title: 'Attack on Titan' }] } };
    (mockPrimary.getTrending as any).mockResolvedValueOnce(mockResult);

    const result = await orchestrator.getTrending(1);

    expect(result).toEqual(mockResult);
    expect(mockPrimary.getTrending).toHaveBeenCalledWith(1);
    expect(mockSecondary.getTrending).not.toHaveBeenCalled();
  });

  it('fails over to secondary provider when primary fails', async () => {
    (mockPrimary.getTrending as any).mockRejectedValue(new Error('AniList 500 error'));
    const secondaryResult = { results: { items: [{ id: 1, title: 'Cowboy Bebop' }] } };
    (mockSecondary.getTrending as any).mockResolvedValueOnce(secondaryResult);

    const result = await orchestrator.getTrending(1);

    expect(result).toEqual(secondaryResult);
    expect(mockPrimary.getTrending).toHaveBeenCalled();
    expect(mockSecondary.getTrending).toHaveBeenCalledWith(1);
  });

  it('throws ProviderError when all providers fail', async () => {
    (mockPrimary.getTrending as any).mockRejectedValue(new Error('AniList down'));
    (mockSecondary.getTrending as any).mockRejectedValue(new Error('Jikan down'));

    await expect(orchestrator.getTrending(1)).rejects.toThrow(/All providers failed/);
  });

  it('serves results from cache without hitting providers on second call', async () => {
    const mockResult = { results: { items: [{ id: 100 }] } };
    (mockPrimary.getTrending as any).mockResolvedValueOnce(mockResult);

    // Call 1
    const res1 = await orchestrator.getTrending(1);
    expect(res1).toEqual(mockResult);
    expect(mockPrimary.getTrending).toHaveBeenCalledTimes(1);

    // Call 2
    const res2 = await orchestrator.getTrending(1);
    expect(res2).toEqual(mockResult);
    expect(mockPrimary.getTrending).toHaveBeenCalledTimes(1); // Provider NOT called again
  });

  it('deduplicates concurrent in-flight requests', async () => {
    let resolvePrimary: (val: any) => void;
    const delayedPromise = new Promise((resolve) => {
      resolvePrimary = resolve;
    });
    (mockPrimary.getPopular as any).mockReturnValueOnce(delayedPromise);

    // Launch 3 requests concurrently
    const p1 = orchestrator.getPopular(1);
    const p2 = orchestrator.getPopular(1);
    const p3 = orchestrator.getPopular(1);

    resolvePrimary!({ results: { items: ['item'] } });

    const [r1, r2, r3] = await Promise.all([p1, p2, p3]);

    expect(r1).toEqual({ results: { items: ['item'] } });
    expect(r2).toEqual({ results: { items: ['item'] } });
    expect(r3).toEqual({ results: { items: ['item'] } });
    expect(mockPrimary.getPopular).toHaveBeenCalledTimes(1); // called exactly once!
  });
});
