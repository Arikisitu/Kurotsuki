/**
 * @file types.ts
 * @description Base contract interface for all SAHASRA upstream anime/manga providers.
 */

import { Anime, Manga, SearchResult } from '../models';
import { ProviderCapability, ProviderType } from '../models/provider';

export interface AnimeProvider {
  name: string;
  type: ProviderType;
  capabilities: ProviderCapability[];
  isAvailable(): Promise<boolean>;
  searchAnime(query: string, page?: number): Promise<SearchResult<Anime>>;
  searchManga(query: string, page?: number): Promise<SearchResult<Manga>>;
  getAnimeById(id: string): Promise<Anime | null>;
  getMangaById(id: string): Promise<Manga | null>;
  getTrending(page?: number): Promise<SearchResult<Anime>>;
  getPopular(page?: number): Promise<SearchResult<Anime>>;
  getSeasonal(year: number, season: string, page?: number): Promise<SearchResult<Anime>>;
}
