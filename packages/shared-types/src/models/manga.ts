/**
 * @file manga.ts
 * @description Comprehensive Manga domain entities, formats, publication statuses, and serialization metadata.
 */

import { BaseMedia, StaffNode } from './media.js';

/**
 * Printed or digital literary format categories.
 */
export type MangaFormat =
  | 'MANGA'
  | 'NOVEL'
  | 'ONE_SHOT';

/**
 * Current publication or serialization status.
 */
export type MangaStatus =
  | 'FINISHED'
  | 'RELEASING'
  | 'NOT_YET_RELEASED'
  | 'CANCELLED'
  | 'HIATUS';

/**
 * Individual chapter descriptor.
 */
export interface MangaChapter {
  /** Chapter unique identifier */
  id: string | number;
  /** Numerical chapter index (float supported for .5 bonus chapters) */
  number: number;
  /** Tankobon volume index chapter belongs to */
  volume?: number | null;
  /** Chapter title */
  title?: string | null;
  /** Total page count */
  pages?: number | null;
  /** Publication or release timestamp */
  releaseDate?: string | null;
  /** Scanlation or translation team credit */
  scanlationGroup?: string | null;
  /** Canonical reader or legal distributor URL */
  url?: string | null;
}

/**
 * Published book volume metadata.
 */
export interface MangaVolume {
  /** Numerical volume index */
  number: number;
  /** Volume specific title */
  title?: string | null;
  /** Volume cover artwork URL */
  coverImage?: string | null;
  /** Release date */
  releaseDate?: string | null;
  /** Number of chapters contained */
  chaptersCount?: number | null;
  /** ISBN-10 or ISBN-13 identifier */
  isbn?: string | null;
}

/**
 * Primary comprehensive Manga entity.
 */
export interface Manga extends BaseMedia {
  /** Explicit media discriminator */
  type: 'MANGA';
  /** Literary publication format */
  format: MangaFormat;
  /** Current serialization status */
  status: MangaStatus;
  /** Total published chapters (null if ongoing) */
  chapters?: number | null;
  /** Total published collected volumes (null if ongoing) */
  volumes?: number | null;
  /** Magazine or serialization imprint name (e.g. Weekly Shonen Jump) */
  serialization?: string | null;
  /** Story writer credits */
  authors: StaffNode[];
  /** Illustrators / key artist credits */
  artists: StaffNode[];
  /** Available chapters catalog */
  chaptersList?: MangaChapter[];
  /** Collected volumes list */
  volumesList?: MangaVolume[];
}
