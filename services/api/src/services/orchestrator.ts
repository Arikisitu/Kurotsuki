/**
 * @file orchestrator.ts
 * @description Provider orchestration service managing failovers, caching, circuit breaking, and deduplication.
 */

import { config } from '../config';
import { AppError, ProviderError } from '../errors';
import { Anime, Manga, SearchResult } from '../models';
import { AnimeProvider } from '../providers/types';
import { ProviderRegistry, registry as defaultRegistry } from '../providers/registry';
import { CacheService, cache as defaultCache } from './cache';
import { CircuitBreaker, circuitBreaker as defaultCircuitBreaker } from './circuit-breaker';

export interface OrchestratorOptions {
  registry?: ProviderRegistry;
  cache?: CacheService;
  circuitBreaker?: CircuitBreaker;
  enableRace?: boolean;
}

export class ProviderOrchestrator {
  private static instance: ProviderOrchestrator;
  private readonly registry: ProviderRegistry;
  private readonly cache: CacheService;
  private readonly circuitBreaker: CircuitBreaker;
  private readonly inFlightRequests: Map<string, Promise<any>> = new Map();

  constructor(options?: OrchestratorOptions) {
    this.registry = options?.registry ?? defaultRegistry;
    this.cache = options?.cache ?? defaultCache;
    this.circuitBreaker = options?.circuitBreaker ?? defaultCircuitBreaker;
  }

  public static getInstance(): ProviderOrchestrator {
    if (!ProviderOrchestrator.instance) {
      ProviderOrchestrator.instance = new ProviderOrchestrator();
    }
    return ProviderOrchestrator.instance;
  }

  /**
   * Exponential backoff with jitter delay helper.
   */
  private async backoff(attempt: number, baseMs: number = 100, maxMs: number = 2000): Promise<void> {
    const exp = Math.min(maxMs, baseMs * Math.pow(2, attempt));
    const jitter = Math.random() * 0.3 * exp; // up to 30% jitter
    await new Promise((resolve) => setTimeout(resolve, exp + jitter));
  }

  /**
   * Executes an operation with failover across providers, caching, circuit breaker, and deduplication.
   */
  public async executeWithFailover<T>(
    operationName: string,
    operationArgs: unknown,
    action: (provider: AnimeProvider) => Promise<T>,
    ttlSeconds: number = 300
  ): Promise<T> {
    const cacheKey = this.cache.generateKey(operationName, operationArgs);

    // 1. Check Cache
    const cached = this.cache.get<T>(cacheKey);
    if (cached) {
      if (cached.isStale) {
        // Trigger background revalidation with primary provider
        this.revalidateInBackground(cacheKey, action, ttlSeconds);
      }
      return cached.data;
    }

    // 2. Request Deduplication (in-flight sharing)
    if (this.inFlightRequests.has(cacheKey)) {
      return this.inFlightRequests.get(cacheKey) as Promise<T>;
    }

    const requestPromise = (async () => {
      const primary = this.registry.getPrimary();
      const secondaries = this.registry.getSecondaries();
      const candidateProviders = [primary, ...secondaries];

      const errors: Error[] = [];

      for (const provider of candidateProviders) {
        // Skip provider if its circuit breaker is currently open
        if (this.circuitBreaker.isOpen(provider.name)) {
          continue;
        }

        // Attempt execution with retry & backoff
        const maxRetries = 1;
        for (let attempt = 0; attempt <= maxRetries; attempt++) {
          try {
            if (attempt > 0) {
              await this.backoff(attempt);
            }

            const result = await this.circuitBreaker.execute(provider.name, () =>
              action(provider)
            );

            // Successfully retrieved: write to cache and return
            if (result !== null && result !== undefined) {
              this.cache.set(cacheKey, result, ttlSeconds, config.cache.staleTtlSeconds);
            }
            return result;
          } catch (err: any) {
            errors.push(err);
            // If it's a 404 or validation error, don't failover or retry
            if (err instanceof AppError && err.statusCode === 404) {
              return null as unknown as T;
            }
            // For rate-limit or timeout errors, break retry loop and failover to next provider
            if (err.statusCode === 429 || err.code === 'RATE_LIMITED' || err.code === 'CIRCUIT_BREAKER_OPEN') {
              break;
            }
          }
        }
      }

      // If all candidate providers failed
      const combinedMessage = errors.map((e) => e.message).join(' | ');
      throw new ProviderError(
        'ORCHESTRATOR',
        `All providers failed for '${operationName}'. Causes: ${combinedMessage}`,
        { statusCode: 502, code: 'ALL_PROVIDERS_FAILED', details: errors.map((e) => String(e)) }
      );
    })();

    this.inFlightRequests.set(cacheKey, requestPromise);
    try {
      return await requestPromise;
    } finally {
      this.inFlightRequests.delete(cacheKey);
    }
  }

  /**
   * Concurrently query multiple providers and return the fastest valid response.
   */
  public async executeRace<T>(
    operationName: string,
    operationArgs: unknown,
    action: (provider: AnimeProvider) => Promise<T>,
    ttlSeconds: number = 300
  ): Promise<T> {
    const cacheKey = this.cache.generateKey(`race:${operationName}`, operationArgs);

    const cached = this.cache.get<T>(cacheKey);
    if (cached) return cached.data;

    const availableProviders = this.registry
      .getAll()
      .filter((p) => !this.circuitBreaker.isOpen(p.name));

    if (availableProviders.length === 0) {
      return this.executeWithFailover(operationName, operationArgs, action, ttlSeconds);
    }

    try {
      const winner = await Promise.any(
        availableProviders.map((p) =>
          this.circuitBreaker.execute(p.name, () => action(p))
        )
      );
      this.cache.set(cacheKey, winner, ttlSeconds, config.cache.staleTtlSeconds);
      return winner;
    } catch {
      // If race fails, fallback to standard sequential failover
      return this.executeWithFailover(operationName, operationArgs, action, ttlSeconds);
    }
  }

  private revalidateInBackground<T>(
    cacheKey: string,
    action: (provider: AnimeProvider) => Promise<T>,
    ttlSeconds: number
  ): void {
    const primary = this.registry.getPrimary();
    this.circuitBreaker
      .execute(primary.name, () => action(primary))
      .then((fresh) => {
        if (fresh) {
          this.cache.set(cacheKey, fresh, ttlSeconds, config.cache.staleTtlSeconds);
        }
      })
      .catch(() => {
        // Silently preserve stale cache on background revalidation failure
      });
  }

  // High-level Anime APIs
  public async getTrending(page: number = 1): Promise<SearchResult<Anime>> {
    return this.executeWithFailover(
      'anime:trending',
      { page },
      (p) => p.getTrending(page),
      config.cache.trendingTtlSeconds
    );
  }

  public async getPopular(page: number = 1): Promise<SearchResult<Anime>> {
    return this.executeWithFailover(
      'anime:popular',
      { page },
      (p) => p.getPopular(page),
      config.cache.popularTtlSeconds
    );
  }

  public async getSeasonal(
    year: number,
    season: string,
    page: number = 1
  ): Promise<SearchResult<Anime>> {
    return this.executeWithFailover(
      'anime:seasonal',
      { year, season: season.toUpperCase(), page },
      (p) => p.getSeasonal(year, season, page),
      config.cache.seasonalTtlSeconds
    );
  }

  public async searchAnime(query: string, page: number = 1): Promise<SearchResult<Anime>> {
    return this.executeWithFailover(
      'anime:search',
      { query: query.toLowerCase().trim(), page },
      (p) => p.searchAnime(query, page),
      config.cache.searchTtlSeconds
    );
  }

  public async getAnimeById(id: string): Promise<Anime | null> {
    return this.executeWithFailover(
      'anime:id',
      { id },
      (p) => p.getAnimeById(id),
      config.cache.detailsTtlSeconds
    );
  }

  // High-level Manga APIs
  public async getMangaTrending(page: number = 1): Promise<SearchResult<Manga>> {
    return this.executeWithFailover(
      'manga:trending',
      { page },
      (p) => p.searchManga('', page),
      config.cache.trendingTtlSeconds
    );
  }

  public async getMangaPopular(page: number = 1): Promise<SearchResult<Manga>> {
    return this.executeWithFailover(
      'manga:popular',
      { page },
      (p) => p.searchManga('', page),
      config.cache.popularTtlSeconds
    );
  }

  public async searchManga(query: string, page: number = 1): Promise<SearchResult<Manga>> {
    return this.executeWithFailover(
      'manga:search',
      { query: query.toLowerCase().trim(), page },
      (p) => p.searchManga(query, page),
      config.cache.searchTtlSeconds
    );
  }

  public async getMangaById(id: string): Promise<Manga | null> {
    return this.executeWithFailover(
      'manga:id',
      { id },
      (p) => p.getMangaById(id),
      config.cache.detailsTtlSeconds
    );
  }
}

export const orchestrator = ProviderOrchestrator.getInstance();
