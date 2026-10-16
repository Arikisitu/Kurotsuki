/**
 * @file user.ts
 * @description User accounts, profiles, watch & reading progress, and media collection lists.
 */

import { MediaType } from './media.js';
import { FuzzyDate } from './common.js';

/**
 * Standard media tracking collection statuses.
 */
export type UserMediaListStatus =
  | 'CURRENT'
  | 'PLANNING'
  | 'COMPLETED'
  | 'DROPPED'
  | 'PAUSED'
  | 'REPEATING';

/**
 * User visual avatar image references.
 */
export interface UserAvatar {
  /** High-resolution avatar URL */
  large?: string | null;
  /** Medium thumbnail avatar URL */
  medium?: string | null;
}

/**
 * User preference settings.
 */
export interface UserPreferences {
  /** Preferred display language for Japanese titles */
  titleLanguage: 'ROMAJI' | 'ENGLISH' | 'NATIVE' | 'ROMAJI_STYLISED' | 'ENGLISH_STYLISED';
  /** Flag allowing 18+ adult content in discovery and queries */
  displayAdultContent: boolean;
  /** Primary accent color theme override */
  accentColor?: string | null;
  /** Notification email alerts */
  notificationsEnabled?: boolean;
}

/**
 * User consumption statistics for anime.
 */
export interface UserAnimeStats {
  /** Total distinct anime titles in user list */
  count: number;
  /** User's arithmetic mean score for rated anime */
  meanScore: number;
  /** Total calculated minutes spent watching anime */
  minutesWatched: number;
  /** Total episodes logged */
  episodesWatched: number;
  /** Genre breakdown distribution */
  genres?: Array<{ genre: string; count: number; meanScore: number; minutesWatched: number }>;
}

/**
 * User consumption statistics for manga.
 */
export interface UserMangaStats {
  /** Total distinct manga titles in user list */
  count: number;
  /** User's arithmetic mean score for rated manga */
  meanScore: number;
  /** Total chapters logged */
  chaptersRead: number;
  /** Total volumes logged */
  volumesRead: number;
  /** Genre breakdown distribution */
  genres?: Array<{ genre: string; count: number; meanScore: number; chaptersRead: number }>;
}

/**
 * Aggregated profile statistics.
 */
export interface UserStatistics {
  /** Anime tracking statistics */
  anime: UserAnimeStats;
  /** Manga tracking statistics */
  manga: UserMangaStats;
}

/**
 * Primary user profile entity.
 */
export interface UserProfile {
  /** Unique user ID */
  id: number | string;
  /** Public handle/username */
  username: string;
  /** Registered email address (private) */
  email?: string | null;
  /** User avatar photo variants */
  avatar?: UserAvatar | null;
  /** Profile header banner image URL */
  bannerImage?: string | null;
  /** Custom bio / about me text (markdown) */
  bio?: string | null;
  /** User display preferences */
  preferences?: UserPreferences;
  /** Aggregated viewing & reading stats */
  statistics?: UserStatistics;
  /** Account creation timestamp */
  createdAt?: number | string | null;
  /** Last active timestamp */
  updatedAt?: number | string | null;
}

/**
 * Granular individual media tracking entry in user's library.
 */
export interface UserListEntry {
  /** Unique entry identifier */
  id: string | number;
  /** User profile ID who owns this entry */
  userId: string | number;
  /** Media identifier (Anime or Manga ID) */
  mediaId: number;
  /** Media type (ANIME or MANGA) */
  mediaType: MediaType;
  /** Current collection status (e.g. CURRENT, COMPLETED) */
  status: UserMediaListStatus;
  /** User rating score (0 - 100 or 0 - 10 based on system score mode) */
  score?: number | null;
  /** Watched episode count or read chapter count */
  progress: number;
  /** Read volume count for manga */
  progressVolumes?: number | null;
  /** Number of times this media has been rewatched/reread */
  repeat?: number;
  /** Priority ordering in user's queue (0 = Low, 1 = Medium, 2 = High) */
  priority?: number;
  /** Whether this list entry is private to the user */
  private?: boolean;
  /** Personal journal notes or review snippet */
  notes?: string | null;
  /** User started date */
  startedAt?: FuzzyDate | null;
  /** User completed date */
  completedAt?: FuzzyDate | null;
  /** Timestamp entry was last updated */
  updatedAt?: number | string | null;
  /** Timestamp entry was first created */
  createdAt?: number | string | null;
}

/**
 * Real-time video playback watch progress.
 */
export interface WatchProgress {
  /** Target Anime ID */
  mediaId: number;
  /** Episode number currently being watched */
  episodeNumber: number;
  /** Current playback playback timestamp in seconds */
  currentTimeSeconds: number;
  /** Total video duration in seconds */
  durationSeconds: number;
  /** Whether the episode was watched to completion (e.g. > 90%) */
  completed: boolean;
  /** ISO timestamp or unix ms of last playback update */
  lastWatchedAt: string | number;
  /** Source player or provider service if external */
  provider?: string | null;
}

/**
 * Real-time literary reading progress.
 */
export interface ReadingProgress {
  /** Target Manga ID */
  mediaId: number;
  /** Chapter number currently being read */
  chapterNumber: number;
  /** Volume number if applicable */
  volumeNumber?: number | null;
  /** Current page index within chapter */
  currentPage: number;
  /** Total pages in current chapter */
  totalPages: number;
  /** Whether chapter has been completely finished */
  completed: boolean;
  /** ISO timestamp or unix ms of last read update */
  lastReadAt: string | number;
  /** Reader source provider name */
  provider?: string | null;
}
