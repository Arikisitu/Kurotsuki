/**
 * @file stream.ts
 * @description Canonical streaming models, quality tiers, audio/subtitle tracks, and resolution contracts.
 */

export type StreamQuality = '1080p' | '720p' | '480p' | '360p' | 'auto';

export type SupportedAudioLanguage = 'en' | 'hi' | 'ja' | 'multi';

export type AudioType = 'sub' | 'dub';

export interface SubtitleTrack {
  id: string;
  language: string;
  label: string;
  url: string;
  isDefault?: boolean;
}

export interface StreamSource {
  id: string;
  providerId: string;
  providerName: string;
  url: string;
  headers?: Record<string, string>;
  quality: StreamQuality;
  resolutionHeight?: number;
  isHls: boolean;
  isDash: boolean;
  audioLanguage: SupportedAudioLanguage;
  audioType: AudioType;
  subtitles?: SubtitleTrack[];
  ttfbMs?: number;
  bitrate?: number;
  serverName?: string;
  status: 'active' | 'degraded' | 'error';
}

export interface StreamResolutionResult {
  animeId?: number;
  animeTitle: string;
  episodeNumber: number;
  preferredLanguage: 'en' | 'hi' | 'ja';
  preferredAudioType?: AudioType;
  primary: StreamSource;
  fallbackQueue: StreamSource[];
  allWorkingSources: StreamSource[];
  resolvedAt: number;
}

export interface ExtensionSourceSummary {
  id: string;
  name: string;
  lang: string;
  baseUrl: string;
  versionId?: number;
}

export interface ExtensionRepoEntry {
  name: string;
  pkg: string;
  apk: string;
  lang: string;
  code: number;
  version: string;
  nsfw: number;
  hasReadme?: number;
  hasChangelog?: number;
  sources?: ExtensionSourceSummary[];
  iconUrl?: string;
}

export interface ExtensionRepoIndex {
  repoName: string;
  repoUrl: string;
  totalExtensions: number;
  filteredCount: number;
  allowedLanguages: string[];
  extensions: ExtensionRepoEntry[];
}
