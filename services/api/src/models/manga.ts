/**
 * @file manga.ts
 * @description Comprehensive Manga domain entities, formats, and serialization metadata.
 */

import { BaseMedia, StaffNode } from './anime';

export type MangaFormat =
  | 'MANGA'
  | 'NOVEL'
  | 'ONE_SHOT';

export type MangaStatus =
  | 'FINISHED'
  | 'RELEASING'
  | 'NOT_YET_RELEASED'
  | 'CANCELLED'
  | 'HIATUS';

export interface MangaChapter {
  id: string | number;
  number: number;
  volume?: number | null;
  title?: string | null;
  pages?: number | null;
  releaseDate?: string | null;
  scanlationGroup?: string | null;
  url?: string | null;
}

export interface MangaVolume {
  number: number;
  title?: string | null;
  coverImage?: string | null;
  releaseDate?: string | null;
  chaptersCount?: number | null;
  isbn?: string | null;
}

export interface Manga extends BaseMedia {
  type: 'MANGA';
  format: MangaFormat;
  status: MangaStatus;
  chapters?: number | null;
  volumes?: number | null;
  serialization?: string | null;
  authors: StaffNode[];
  artists: StaffNode[];
  chaptersList?: MangaChapter[];
  volumesList?: MangaVolume[];
}
