/**
 * @file cache.ts
 * @description In-memory TTL cache with Stale-While-Revalidate support and key generation.
 */

interface CacheEntry<T> {
  value: T;
  expiresAt: number; // Timestamp when item becomes stale
  maxAgeAt: number;  // Timestamp when item is completely evicted
}

export interface CacheStats {
  size: number;
  hits: number;
  misses: number;
  staleHits: number;
}

export class CacheService {
  private static instance: CacheService;
  private readonly store: Map<string, CacheEntry<any>> = new Map();
  private readonly bgRevalidations: Set<string> = new Set();
  private stats: CacheStats = { size: 0, hits: 0, misses: 0, staleHits: 0 };
  private cleanupTimer?: NodeJS.Timeout;

  constructor() {
    this.startCleanupTimer();
  }

  public static getInstance(): CacheService {
    if (!CacheService.instance) {
      CacheService.instance = new CacheService();
    }
    return CacheService.instance;
  }

  private startCleanupTimer(): void {
    if (this.cleanupTimer) clearInterval(this.cleanupTimer);
    this.cleanupTimer = setInterval(() => this.prune(), 60000);
    if (this.cleanupTimer.unref) {
      this.cleanupTimer.unref();
    }
  }

  public generateKey(prefix: string, params?: unknown): string {
    if (!params) return prefix;
    if (typeof params === 'string' || typeof params === 'number') {
      return `${prefix}:${params}`;
    }
    try {
      const sortedJson = JSON.stringify(params, Object.keys(params as any).sort());
      return `${prefix}:${sortedJson}`;
    } catch {
      return `${prefix}:${String(params)}`;
    }
  }

  public get<T>(key: string): { data: T; isStale: boolean } | null {
    const entry = this.store.get(key) as CacheEntry<T> | undefined;
    if (!entry) {
      this.stats.misses++;
      return null;
    }

    const now = Date.now();
    if (now > entry.maxAgeAt) {
      this.store.delete(key);
      this.stats.misses++;
      return null;
    }

    const isStale = now > entry.expiresAt;
    if (isStale) {
      this.stats.staleHits++;
    } else {
      this.stats.hits++;
    }

    return { data: entry.value, isStale };
  }

  public set<T>(
    key: string,
    value: T,
    ttlSeconds: number,
    staleGraceSeconds: number = 120
  ): void {
    const now = Date.now();
    const expiresAt = now + ttlSeconds * 1000;
    const maxAgeAt = expiresAt + staleGraceSeconds * 1000;

    this.store.set(key, { value, expiresAt, maxAgeAt });
    this.stats.size = this.store.size;
  }

  public async getOrSet<T>(
    key: string,
    fetchFn: () => Promise<T>,
    ttlSeconds: number,
    staleGraceSeconds: number = 120
  ): Promise<T> {
    const cached = this.get<T>(key);

    if (cached) {
      if (cached.isStale && !this.bgRevalidations.has(key)) {
        // Asynchronous background revalidation
        this.bgRevalidations.add(key);
        fetchFn()
          .then((fresh) => {
            this.set(key, fresh, ttlSeconds, staleGraceSeconds);
          })
          .catch(() => {
            // Keep stale cache on revalidation error
          })
          .finally(() => {
            this.bgRevalidations.delete(key);
          });
      }
      return cached.data;
    }

    const fresh = await fetchFn();
    this.set(key, fresh, ttlSeconds, staleGraceSeconds);
    return fresh;
  }

  public delete(key: string): boolean {
    const deleted = this.store.delete(key);
    this.stats.size = this.store.size;
    return deleted;
  }

  public clear(): void {
    this.store.clear();
    this.stats.size = 0;
  }

  public prune(): number {
    const now = Date.now();
    let pruned = 0;
    for (const [key, entry] of this.store.entries()) {
      if (now > entry.maxAgeAt) {
        this.store.delete(key);
        pruned++;
      }
    }
    this.stats.size = this.store.size;
    return pruned;
  }

  public getStats(): CacheStats {
    return { ...this.stats, size: this.store.size };
  }

  public destroy(): void {
    if (this.cleanupTimer) {
      clearInterval(this.cleanupTimer);
    }
    this.clear();
  }
}

export const cache = CacheService.getInstance();
