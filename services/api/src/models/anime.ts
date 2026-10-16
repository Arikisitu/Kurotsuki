/**
 * @file anime.ts
 * @description Comprehensive Anime domain entities, formats, and metadata.
 */

import { FuzzyDate, RankItem, ScoreDistribution, StatusDistribution } from './common';

export type AnimeFormat =
  | 'TV'
  | 'TV_SHORT'
  | 'MOVIE'
  | 'SPECIAL'
  | 'OVA'
  | 'ONA'
  | 'MUSIC';

export type AnimeStatus =
  | 'FINISHED'
  | 'RELEASING'
  | 'NOT_YET_RELEASED'
  | 'CANCELLED'
  | 'HIATUS';

export type AnimeSeason =
  | 'WINTER'
  | 'SPRING'
  | 'SUMMER'
  | 'FALL';

export type AnimeSource =
  | 'ORIGINAL'
  | 'MANGA'
  | 'LIGHT_NOVEL'
  | 'VISUAL_NOVEL'
  | 'VIDEO_GAME'
  | 'OTHER'
  | 'NOVEL'
  | 'DOUJINSHI'
  | 'ANIME'
  | 'COMIC'
  | 'LIVE_ACTION'
  | 'MULTIMEDIA_PROJECT'
  | 'PICTURE_BOOK';

export interface AiringSchedule {
  id: number;
  airingAt: number;
  timeUntilAiring: number;
  episode: number;
  mediaId: number;
}

export interface AnimeBroadcast {
  day?: string | null;
  time?: string | null;
  timezone?: string | null;
  string?: string | null;
}

export interface AnimeEpisode {
  id: string | number;
  number: number;
  title?: string | null;
  thumbnail?: string | null;
  duration?: number | null;
  airDate?: string | null;
  summary?: string | null;
  url?: string | null;
  isFiller?: boolean;
  isRecap?: boolean;
}

export interface StreamingEpisode {
  site: string;
  title?: string | null;
  thumbnail?: string | null;
  url: string;
}

export type MediaType = 'ANIME' | 'MANGA';

export interface MediaTitle {
  romaji?: string | null;
  english?: string | null;
  native?: string | null;
  userPreferred?: string | null;
}

export interface MediaCoverImage {
  extraLarge?: string | null;
  large?: string | null;
  medium?: string | null;
  color?: string | null;
}

export interface MediaArtwork {
  cover: MediaCoverImage;
  banner?: string | null;
  screenshots?: string[];
  dominantColor?: string | null;
}

export interface ExternalIdentifier {
  source: 'ANILIST' | 'MAL' | 'KITSU' | 'ANIDB' | 'MANGAUPDATES' | 'ANIME_PLANET' | 'IMDB' | 'TVDB';
  id: string | number;
  url?: string | null;
}

export interface MediaTag {
  id: number;
  name: string;
  description?: string | null;
  category?: string | null;
  rank?: number | null;
  isGeneralSpoiler?: boolean;
  isMediaSpoiler?: boolean;
  isAdult?: boolean;
}

export interface MediaTrailer {
  id?: string | null;
  site?: 'youtube' | 'dailymotion' | string | null;
  thumbnail?: string | null;
}

export type MediaRelationType =
  | 'ADAPTATION'
  | 'PREQUEL'
  | 'SEQUEL'
  | 'PARENT'
  | 'SIDE_STORY'
  | 'CHARACTER'
  | 'SUMMARY'
  | 'ALTERNATIVE'
  | 'SPIN_OFF'
  | 'OTHER';

export interface RelatedMediaNode {
  id: number;
  type: MediaType;
  title: MediaTitle;
  format?: string | null;
  status?: string | null;
  coverImage?: MediaCoverImage | null;
  averageScore?: number | null;
}

export interface MediaRelationEdge {
  relationType: MediaRelationType;
  node: RelatedMediaNode;
}

export interface StaffNode {
  id: number;
  name: {
    first?: string | null;
    last?: string | null;
    full?: string | null;
    native?: string | null;
    userPreferred?: string | null;
  };
  image?: {
    large?: string | null;
    medium?: string | null;
  } | null;
  role?: string | null;
}

export interface CharacterNode {
  id: number;
  name: {
    first?: string | null;
    middle?: string | null;
    last?: string | null;
    full?: string | null;
    native?: string | null;
    userPreferred?: string | null;
  };
  image?: {
    large?: string | null;
    medium?: string | null;
  } | null;
  role: 'MAIN' | 'SUPPORTING' | 'BACKGROUND';
  voiceActors?: StaffNode[];
}

export interface StudioNode {
  id: number;
  name: string;
  isMain?: boolean;
  siteUrl?: string | null;
}

export interface ExternalLink {
  id: number;
  url: string;
  site: string;
  icon?: string | null;
  color?: string | null;
  type?: 'STREAMING' | 'SOCIAL' | 'INFO' | string | null;
}

export interface BaseMedia {
  id: number;
  idMal?: number | null;
  title: MediaTitle;
  type: MediaType;
  description?: string | null;
  startDate?: FuzzyDate | null;
  endDate?: FuzzyDate | null;
  artwork: MediaArtwork;
  genres: string[];
  tags: MediaTag[];
  averageScore?: number | null;
  meanScore?: number | null;
  popularity?: number;
  trending?: number;
  favourites?: number;
  isAdult: boolean;
  countryOfOrigin?: string | null;
  trailer?: MediaTrailer | null;
  externalIdentifiers: ExternalIdentifier[];
  externalLinks?: ExternalLink[];
  relations?: MediaRelationEdge[];
  characters?: CharacterNode[];
  staff?: StaffNode[];
  rankings?: RankItem[];
  scoreDistribution?: ScoreDistribution[];
  statusDistribution?: StatusDistribution[];
  updatedAt?: number | string | null;
}

export interface Anime extends BaseMedia {
  type: 'ANIME';
  format: AnimeFormat;
  status: AnimeStatus;
  season?: AnimeSeason | null;
  seasonYear?: number | null;
  episodes?: number | null;
  duration?: number | null;
  source?: AnimeSource | null;
  nextAiringEpisode?: AiringSchedule | null;
  broadcast?: AnimeBroadcast | null;
  studios: StudioNode[];
  episodesList?: AnimeEpisode[];
  streamingEpisodes?: StreamingEpisode[];
}
