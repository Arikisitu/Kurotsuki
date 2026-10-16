/**
 * @file anime.ts
 * @description Comprehensive Anime domain entities, formats, airing schedules, and streaming metadata.
 */

import { BaseMedia, StudioNode, MediaRelationType, RelatedMediaNode } from './media.js';

/**
 * Supported anime release distribution formats.
 */
export type AnimeFormat =
  | 'TV'
  | 'TV_SHORT'
  | 'MOVIE'
  | 'SPECIAL'
  | 'OVA'
  | 'ONA'
  | 'MUSIC';

/**
 * Current production or release status of an anime title.
 */
export type AnimeStatus =
  | 'FINISHED'
  | 'RELEASING'
  | 'NOT_YET_RELEASED'
  | 'CANCELLED'
  | 'HIATUS';

/**
 * Calendar release season quarter.
 */
export type AnimeSeason =
  | 'WINTER'
  | 'SPRING'
  | 'SUMMER'
  | 'FALL';

/**
 * Original source material classification from which the anime was adapted.
 */
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

/**
 * Upcoming broadcast schedule item for currently airing anime.
 */
export interface AiringSchedule {
  /** Unique schedule entry ID */
  id: number;
  /** Unix timestamp in seconds when episode airs */
  airingAt: number;
  /** Seconds remaining until airing */
  timeUntilAiring: number;
  /** Sequential episode number */
  episode: number;
  /** Target anime ID */
  mediaId: number;
}

/**
 * Detailed broadcast schedule information.
 */
export interface AnimeBroadcast {
  /** Day of week of broadcast in JST (e.g. Sunday, Monday) */
  day?: string | null;
  /** Local broadcast time (e.g. 23:00) */
  time?: string | null;
  /** Timezone name (typically Asia/Tokyo) */
  timezone?: string | null;
  /** Formatted human-readable broadcast string */
  string?: string | null;
}

/**
 * Individual episode metadata.
 */
export interface AnimeEpisode {
  /** Identifier or episode unique key */
  id: string | number;
  /** Episode index number */
  number: number;
  /** Localized episode title */
  title?: string | null;
  /** Episode preview thumbnail URL */
  thumbnail?: string | null;
  /** Approximate duration in minutes or seconds */
  duration?: number | null;
  /** Release date ISO string or fuzzy date */
  airDate?: string | null;
  /** Brief episode summary or synopsis */
  summary?: string | null;
  /** Direct link to watch on supported legal streaming provider */
  url?: string | null;
  /** Filler episode indicator */
  isFiller?: boolean;
  /** Recap episode indicator */
  isRecap?: boolean;
}

/**
 * Official streaming service catalog episode entry.
 */
export interface StreamingEpisode {
  /** Streaming provider name (e.g. Crunchyroll, Netflix) */
  site: string;
  /** Episode title */
  title?: string | null;
  /** Thumbnail image */
  thumbnail?: string | null;
  /** Legal streaming URL */
  url: string;
}

/**
 * Canonical MediaRelation export for explicit relation linking.
 */
export interface MediaRelation {
  /** Relationship type classification */
  relationType: MediaRelationType;
  /** Linked media node */
  node: RelatedMediaNode;
}

/**
 * Primary comprehensive Anime entity.
 */
export interface Anime extends BaseMedia {
  /** Explicit media discriminator */
  type: 'ANIME';
  /** Broadcast or release medium */
  format: AnimeFormat;
  /** Current airing or release status */
  status: AnimeStatus;
  /** Calendar quarter season of premiere */
  season?: AnimeSeason | null;
  /** Calendar premiere year */
  seasonYear?: number | null;
  /** Total count of produced episodes (null if unannounced or ongoing) */
  episodes?: number | null;
  /** Average duration per episode in minutes */
  duration?: number | null;
  /** Original creative material type */
  source?: AnimeSource | null;
  /** Next upcoming scheduled episode (if currently releasing) */
  nextAiringEpisode?: AiringSchedule | null;
  /** Scheduled recurring broadcast slot */
  broadcast?: AnimeBroadcast | null;
  /** Animation production studios */
  studios: StudioNode[];
  /** Detailed episode guide or recent episodes */
  episodesList?: AnimeEpisode[];
  /** Licensed streaming links */
  streamingEpisodes?: StreamingEpisode[];
}
