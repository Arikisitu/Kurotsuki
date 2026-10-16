/**
 * @file registry.ts
 * @description Provider registry managing provider lifecycles, capabilities, and health checks.
 */

import { ProviderHealth, ProviderStatus, ProviderType } from '../models/provider';
import { AniListProvider } from './anilist';
import { JikanProvider } from './jikan';
import { KitsuProvider } from './kitsu';
import { AnimeProvider } from './types';

export class ProviderRegistry {
  private static instance: ProviderRegistry;
  private readonly providers: Map<string, AnimeProvider> = new Map();
  private primaryProviderName: string = 'AniList';
  private secondaryProviderNames: string[] = ['Jikan', 'Kitsu'];

  constructor(options?: { registerDefaults?: boolean; primary?: string; secondaries?: string[] }) {
    if (options?.primary) this.primaryProviderName = options.primary;
    if (options?.secondaries) this.secondaryProviderNames = options.secondaries;
    if (options?.registerDefaults !== false) {
      this.registerDefaultProviders();
    }
  }

  public clear(): void {
    this.providers.clear();
  }

  public setPrimary(name: string): void {
    this.primaryProviderName = name;
  }

  public setSecondaries(names: string[]): void {
    this.secondaryProviderNames = names;
  }

  public static getInstance(): ProviderRegistry {
    if (!ProviderRegistry.instance) {
      ProviderRegistry.instance = new ProviderRegistry();
    }
    return ProviderRegistry.instance;
  }

  private registerDefaultProviders(): void {
    const anilist = new AniListProvider();
    const jikan = new JikanProvider();
    const kitsu = new KitsuProvider();

    this.register(anilist);
    this.register(jikan);
    this.register(kitsu);
  }

  public register(provider: AnimeProvider): void {
    this.providers.set(provider.name.toLowerCase(), provider);
  }

  public get(nameOrType: string): AnimeProvider | undefined {
    return (
      this.providers.get(nameOrType.toLowerCase()) ||
      Array.from(this.providers.values()).find(
        (p) => p.type.toLowerCase() === nameOrType.toLowerCase()
      )
    );
  }

  public getPrimary(): AnimeProvider {
    const primary = this.get(this.primaryProviderName);
    if (!primary) {
      const fallback = Array.from(this.providers.values())[0];
      if (!fallback) throw new Error('No providers registered in registry');
      return fallback;
    }
    return primary;
  }

  public getSecondaries(): AnimeProvider[] {
    return this.secondaryProviderNames
      .map((name) => this.get(name))
      .filter((p): p is AnimeProvider => p !== undefined);
  }

  public getAll(): AnimeProvider[] {
    return Array.from(this.providers.values());
  }

  public async checkHealth(name: string): Promise<ProviderHealth> {
    const provider = this.get(name);
    if (!provider) {
      return {
        provider: 'CUSTOM' as ProviderType,
        name,
        status: 'DOWN',
        latencyMs: 0,
        lastCheckedAt: new Date().toISOString(),
        details: { message: `Provider ${name} not found in registry` },
      };
    }

    const start = Date.now();
    try {
      const available = await provider.isAvailable();
      const latencyMs = Date.now() - start;
      const status: ProviderStatus = available ? 'OPERATIONAL' : 'DEGRADED';

      return {
        provider: provider.type,
        name: provider.name,
        status,
        latencyMs,
        lastCheckedAt: new Date().toISOString(),
      };
    } catch (err: any) {
      const latencyMs = Date.now() - start;
      return {
        provider: provider.type,
        name: provider.name,
        status: 'DOWN',
        latencyMs,
        lastCheckedAt: new Date().toISOString(),
        details: { error: err.message || String(err) },
      };
    }
  }

  public async checkHealthAll(): Promise<Record<string, ProviderHealth>> {
    const results: Record<string, ProviderHealth> = {};
    const checks = Array.from(this.providers.keys()).map(async (key) => {
      const health = await this.checkHealth(key);
      results[health.name] = health;
    });
    await Promise.allSettled(checks);
    return results;
  }
}

export const registry = ProviderRegistry.getInstance();
