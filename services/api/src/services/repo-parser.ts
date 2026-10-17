/**
 * @file repo-parser.ts
 * @description In-memory parser and sync engine for Aniyomi-compatible extension repositories (JSON & Gzipped Protobuf).
 * Applies strict language filtering to ONLY allow 'en' (English), 'hi' (Hindi), 'ja' (Japanese), and 'all'.
 */

import zlib from 'zlib';
import { ExtensionRepoEntry, ExtensionRepoIndex } from '@sahasra/shared-types';

export const ALLOWED_LANGUAGES = ['en', 'hi', 'ja', 'all'] as const;

export interface RepoConfig {
  name: string;
  url: string;
  type: 'json' | 'protobuf';
}

export const REPOSITORY_ENDPOINTS: RepoConfig[] = [
  {
    name: 'Yūzōnō Anime Repository',
    url: 'https://raw.githubusercontent.com/yuzono/anime-repo/repo/index.min.json',
    type: 'json',
  },
  {
    name: 'Secozzi Aniyomi Extensions',
    url: 'https://raw.githubusercontent.com/Secozzi/aniyomi-extensions/refs/heads/repo/index.pb',
    type: 'protobuf',
  },
];

interface CacheItem {
  timestamp: number;
  data: ExtensionRepoIndex;
}

export class RepoParserService {
  private cache: Map<string, CacheItem> = new Map();
  private ttlMs = 60 * 60 * 1000; // 1 hour TTL

  /**
   * Helper to read varint from buffer.
   */
  private readVarint(buffer: Buffer, offset: number): [number, number] {
    let res = 0;
    let shift = 0;
    while (offset < buffer.length) {
      const b = buffer[offset++];
      if (b === undefined) break;
      res |= (b & 0x7f) << shift;
      if (!(b & 0x80)) break;
      shift += 7;
    }
    return [res, offset];
  }

  /**
   * Parse binary Gzipped Protocol Buffer repository manifest.
   */
  public parseProtobufIndex(compressedBuffer: Buffer, repoUrl: string): ExtensionRepoIndex {
    let buf: Buffer;
    try {
      buf = zlib.gunzipSync(compressedBuffer);
    } catch {
      buf = compressedBuffer;
    }

    let off = 0;
    let repoName = 'Secozzi Aniyomi Extensions';
    let extRawData: Buffer | null = null;

    while (off < buf.length) {
      const [tag, nextOff] = this.readVarint(buf, off);
      off = nextOff;
      const wire = tag & 0x07;
      const field = tag >> 3;

      if (wire === 2) {
        const [len, dataOff] = this.readVarint(buf, off);
        const data = buf.slice(dataOff, dataOff + len);
        off = dataOff + len;

        if (field === 1) {
          repoName = data.toString('utf8');
        } else if (field === 101) {
          extRawData = data;
        }
      } else if (wire === 0) {
        const [, valOff] = this.readVarint(buf, off);
        off = valOff;
      } else {
        break;
      }
    }

    const allExtensions: ExtensionRepoEntry[] = [];

    if (extRawData) {
      let subOff = 0;
      while (subOff < extRawData.length) {
        const [tag, nextOff] = this.readVarint(extRawData, subOff);
        subOff = nextOff;
        const wire = tag & 0x07;
        const field = tag >> 3;

        if (wire === 2 && field === 1) {
          const [len, dataOff] = this.readVarint(extRawData, subOff);
          const mData = extRawData.slice(dataOff, dataOff + len);
          subOff = dataOff + len;

          let mOff = 0;
          let name = '';
          let pkg = '';
          let apk = '';
          let lang = 'all';
          let code = 1;
          let version = '1.0';

          while (mOff < mData.length) {
            const [mTag, mNextOff] = this.readVarint(mData, mOff);
            mOff = mNextOff;
            const mWire = mTag & 0x07;
            const mField = mTag >> 3;

            if (mWire === 2) {
              const [mLen, mDataOff] = this.readVarint(mData, mOff);
              const val = mData.slice(mDataOff, mDataOff + mLen);
              mOff = mDataOff + mLen;

              if (mField === 1) name = val.toString('utf8');
              else if (mField === 2) {
                pkg = val.toString('utf8');
                // Extract language from package name if available (e.g. animeextension.all.jellyfin)
                const parts = pkg.split('.');
                if (parts.length >= 4) {
                  lang = parts[parts.length - 2] || 'all';
                }
              } else if (mField === 3) {
                apk = val.toString('utf8').replace(/[\r\n\x00-\x1F]/g, '').trim();
              } else if (mField === 6) version = val.toString('utf8');
            } else if (mWire === 0) {
              const [mVal, mValOff] = this.readVarint(mData, mOff);
              mOff = mValOff;
              if (mField === 5) code = mVal;
            }
          }

          if (name && pkg) {
            allExtensions.push({
              name,
              pkg,
              apk,
              lang: lang.toLowerCase(),
              code,
              version,
              nsfw: 0,
              sources: [{ id: pkg, name, lang: lang.toLowerCase(), baseUrl: '' }],
            });
          }
        } else {
          break;
        }
      }
    }

    const filtered = allExtensions.filter((e) =>
      ALLOWED_LANGUAGES.includes(e.lang as any)
    );

    return {
      repoName,
      repoUrl,
      totalExtensions: allExtensions.length,
      filteredCount: filtered.length,
      allowedLanguages: [...ALLOWED_LANGUAGES],
      extensions: filtered,
    };
  }

  /**
   * Parse JSON repository manifest.
   */
  public parseJsonIndex(jsonString: string, repoName: string, repoUrl: string): ExtensionRepoIndex {
    const rawList: any[] = JSON.parse(jsonString);
    const allExtensions: ExtensionRepoEntry[] = rawList.map((item) => ({
      name: item.name,
      pkg: item.pkg,
      apk: item.apk,
      lang: (item.lang || 'en').toLowerCase(),
      code: item.code || 1,
      version: item.version || '1.0',
      nsfw: item.nsfw || 0,
      hasReadme: item.hasReadme,
      hasChangelog: item.hasChangelog,
      sources: item.sources?.map((s: any) => ({
        id: String(s.id || ''),
        name: s.name || '',
        lang: (s.lang || item.lang || 'en').toLowerCase(),
        baseUrl: s.baseUrl || '',
        versionId: s.versionId,
      })),
      iconUrl: item.icon,
    }));

    // Strictly filter by ALLOWED_LANGUAGES
    const filtered = allExtensions.filter((item) =>
      ALLOWED_LANGUAGES.includes(item.lang as any)
    );

    return {
      repoName,
      repoUrl,
      totalExtensions: allExtensions.length,
      filteredCount: filtered.length,
      allowedLanguages: [...ALLOWED_LANGUAGES],
      extensions: filtered,
    };
  }

  /**
   * Sync and parse a repository with in-memory caching.
   */
  public async fetchAndParseRepo(config: RepoConfig, forceRefresh = false): Promise<ExtensionRepoIndex> {
    const cached = this.cache.get(config.url);
    if (!forceRefresh && cached && Date.now() - cached.timestamp < this.ttlMs) {
      return cached.data;
    }

    const response = await fetch(config.url, {
      headers: {
        'User-Agent': 'SAHASRA-StreamAggregator/1.0',
        Accept: config.type === 'protobuf' ? 'application/octet-stream, */*' : 'application/json, */*',
      },
    });

    if (!response.ok) {
      throw new Error(`Failed to fetch repo from ${config.url} (status: ${response.status})`);
    }

    let parsed: ExtensionRepoIndex;
    if (config.type === 'protobuf') {
      const buffer = Buffer.from(await response.arrayBuffer());
      parsed = this.parseProtobufIndex(buffer, config.url);
    } else {
      const text = await response.text();
      parsed = this.parseJsonIndex(text, config.name, config.url);
    }

    this.cache.set(config.url, { timestamp: Date.now(), data: parsed });
    return parsed;
  }

  /**
   * Sync all configured repositories concurrently and aggregate the filtered extensions.
   */
  public async syncAllRepositories(forceRefresh = false): Promise<{
    repositories: ExtensionRepoIndex[];
    totalAvailable: number;
    totalFiltered: number;
    combinedExtensions: ExtensionRepoEntry[];
  }> {
    const results = await Promise.allSettled(
      REPOSITORY_ENDPOINTS.map((cfg) => this.fetchAndParseRepo(cfg, forceRefresh))
    );

    const repositories: ExtensionRepoIndex[] = [];
    const combinedMap = new Map<string, ExtensionRepoEntry>();

    for (let i = 0; i < results.length; i++) {
      const res = results[i];
      const cfg = REPOSITORY_ENDPOINTS[i];
      if (res && res.status === 'fulfilled') {
        repositories.push(res.value);
        for (const ext of res.value.extensions) {
          const existing = combinedMap.get(ext.pkg);
          if (!existing || ext.code > existing.code) {
            combinedMap.set(ext.pkg, ext);
          }
        }
      } else if (res && res.status === 'rejected' && cfg) {
        console.warn(`[RepoParser] Error syncing ${cfg.name}:`, (res as PromiseRejectedResult).reason?.message);
      }
    }

    const combinedExtensions = Array.from(combinedMap.values());
    const totalAvailable = repositories.reduce((acc, r) => acc + r.totalExtensions, 0);

    return {
      repositories,
      totalAvailable,
      totalFiltered: combinedExtensions.length,
      combinedExtensions,
    };
  }

  /**
   * Clear in-memory cache.
   */
  public clearCache(): void {
    this.cache.clear();
  }
}

export const repoParserService = new RepoParserService();
