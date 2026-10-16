import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CacheService } from '../../services/cache';

describe('CacheService', () => {
  let cache: CacheService;

  beforeEach(() => {
    cache = new CacheService();
  });

  afterEach(() => {
    cache.destroy();
  });

  it('sets and retrieves fresh cached values', () => {
    cache.set('test-key', { foo: 'bar' }, 10, 60);

    const result = cache.get<{ foo: string }>('test-key');
    expect(result).not.toBeNull();
    expect(result?.data).toEqual({ foo: 'bar' });
    expect(result?.isStale).toBe(false);
  });

  it('marks data as stale after TTL expires but before maxAge', () => {
    cache.set('stale-key', 'data-val', 1, 10);

    // Fast-forward 2 seconds (past 1s TTL, within 10s stale grace)
    vi.setSystemTime(Date.now() + 2000);

    const result = cache.get<string>('stale-key');
    expect(result).not.toBeNull();
    expect(result?.data).toBe('data-val');
    expect(result?.isStale).toBe(true);

    vi.useRealTimers();
  });

  it('evicts data completely once past maxAge (TTL + staleGrace)', () => {
    cache.set('expired-key', 'old-val', 1, 2);

    // Fast-forward past 3 seconds (1s TTL + 2s grace)
    vi.setSystemTime(Date.now() + 4000);

    const result = cache.get<string>('expired-key');
    expect(result).toBeNull();

    vi.useRealTimers();
  });

  it('handles getOrSet with cache miss and background revalidation', async () => {
    const fetchFn = vi.fn().mockResolvedValue('computed-value');

    // 1. Initial call (cache miss)
    const val1 = await cache.getOrSet('auto-key', fetchFn, 2, 5);
    expect(val1).toBe('computed-value');
    expect(fetchFn).toHaveBeenCalledTimes(1);

    // 2. Second call immediately (cache hit fresh)
    const val2 = await cache.getOrSet('auto-key', fetchFn, 2, 5);
    expect(val2).toBe('computed-value');
    expect(fetchFn).toHaveBeenCalledTimes(1); // not called again

    // 3. Third call when stale: returns stale data immediately, triggers background refresh
    vi.setSystemTime(Date.now() + 3000);
    const backgroundFetch = vi.fn().mockResolvedValue('fresh-value');
    const val3 = await cache.getOrSet('auto-key', backgroundFetch, 2, 5);
    expect(val3).toBe('computed-value'); // stale returned immediately
    expect(backgroundFetch).toHaveBeenCalledTimes(1); // background refresh triggered

    vi.useRealTimers();
  });

  it('deletes and clears cached entries properly', () => {
    cache.set('key1', 1, 60);
    cache.set('key2', 2, 60);

    expect(cache.delete('key1')).toBe(true);
    expect(cache.get('key1')).toBeNull();
    expect(cache.get('key2')?.data).toBe(2);

    cache.clear();
    expect(cache.get('key2')).toBeNull();
    expect(cache.getStats().size).toBe(0);
  });
});
