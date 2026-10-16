/**
 * @file media.ts
 * @description Canonical base media models, artwork, relations, and cross-platform identifiers.
 */

import { FuzzyDate, RankItem, ScoreDistribution, StatusDistribution } from './common.js';

/**
 * Fundamental media classification.
 */
export type MediaType = 'ANIME' | 'MANGA';

/**
 * Localized titles across canonical naming standards.
 */
export interface MediaTitle {
  /** Romanized Japanese title */
  romaji?: string | null;
  /** Official or common English localized title */
  english?: string | null;
  /** Original native language title (Japanese Kanji/Kana, Korean Hangul, Chinese Hanzi) */
  native?: string | null;
  /** User preferred display title based on localization profile settings */
  userPreferred?: string | null;
}

/**
 * Multi-resolution cover image assets.
 */
export interface MediaCoverImage {
  /** Extra-large high-definition artwork URL */
  extraLarge?: string | null;
  /** Large artwork standard poster URL */
  large?: string | null;
  /** Medium thumbnail artwork URL */
  medium?: string | null;
  /** Dominant vibrant accent color extracted from image (Hex code, e.g. #E64B55) */
  color?: string | null;
}

/**
 * Comprehensive visual artwork assets for rich media presentation.
 */
export interface MediaArtwork {
  /** Primary cover poster with multi-density variants */
  cover: MediaCoverImage;
  /** High-resolution wide hero banner image URL */
  banner?: string | null;
  /** Curated collection of high-res screenshot or wallpaper URLs */
  screenshots?: string[];
  /** Color palette extracted from key art */
  dominantColor?: string | null;
}

/**
 * Cross-platform identifier and database mapping.
 */
export interface ExternalIdentifier {
  /** Source provider name */
  source: 'ANILIST' | 'MAL' | 'KITSU' | 'ANIDB' | 'MANGAUPDATES' | 'ANIME_PLANET' | 'IMDB' | 'TVDB';
  /** External database unique identifier */
  id: string | number;
  /** Direct link to canonical external page */
  url?: string | null;
}

/**
 * Descriptive categorization tag with community verification details.
 */
export interface MediaTag {
  /** Numeric tag ID */
  id: number;
  /** Tag display name */
  name: string;
  /** In-depth description of the trope or motif */
  description?: string | null;
  /** Category taxonomy grouping (e.g. Theme, Cast, Setting) */
  category?: string | null;
  /** Tag relevance rank score percentage (0-100) */
  rank?: number | null;
  /** Whether the tag spoiled broader story elements */
  isGeneralSpoiler?: boolean;
  /** Whether the tag is a spoiler for this specific title */
  isMediaSpoiler?: boolean;
  /** Whether this tag denotes adult/mature themes */
  isAdult?: boolean;
}

/**
 * External trailer video reference.
 */
export interface MediaTrailer {
  /** Video platform host ID (e.g. YouTube video ID) */
  id?: string | null;
  /** Hosting provider site name */
  site?: 'youtube' | 'dailymotion' | string | null;
  /** Video thumbnail preview URL */
  thumbnail?: string | null;
}

/**
 * Narrative or publication relation taxonomy.
 */
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

/**
 * Minimal summary node representation for related media.
 */
export interface RelatedMediaNode {
  /** Internal or AniList unique ID */
  id: number;
  /** Media type (Anime or Manga) */
  type: MediaType;
  /** Multilingual titles */
  title: MediaTitle;
  /** Format of related item */
  format?: string | null;
  /** Current release status */
  status?: string | null;
  /** Cover poster assets */
  coverImage?: MediaCoverImage | null;
  /** Community average score */
  averageScore?: number | null;
}

/**
 * Directed relational edge connecting two media entities.
 */
export interface MediaRelationEdge {
  /** Relationship type connecting this media to the node */
  relationType: MediaRelationType;
  /** Related media entity */
  node: RelatedMediaNode;
}

/**
 * Character voice actor or staff credit role.
 */
export interface CharacterNode {
  /** Character unique identifier */
  id: number;
  /** Character full name */
  name: {
    first?: string | null;
    middle?: string | null;
    last?: string | null;
    full?: string | null;
    native?: string | null;
    userPreferred?: string | null;
  };
  /** Character avatar image URL */
  image?: {
    large?: string | null;
    medium?: string | null;
  } | null;
  /** Character importance in narrative */
  role: 'MAIN' | 'SUPPORTING' | 'BACKGROUND';
  /** Voice actors in localized dubs */
  voiceActors?: StaffNode[];
}

/**
 * Production staff / creative contributor node.
 */
export interface StaffNode {
  /** Staff member unique ID */
  id: number;
  /** Staff member full name */
  name: {
    first?: string | null;
    last?: string | null;
    full?: string | null;
    native?: string | null;
    userPreferred?: string | null;
  };
  /** Headshot photo URL */
  image?: {
    large?: string | null;
    medium?: string | null;
  } | null;
  /** Role played in production (e.g. Director, Character Design, Author) */
  role?: string | null;
}

/**
 * Animation production studio or publishing organization.
 */
export interface StudioNode {
  /** Studio identifier */
  id: number;
  /** Studio legal or common name */
  name: string;
  /** Whether this studio is the lead animation/publishing entity */
  isMain?: boolean;
  /** Studio official or external profile URL */
  siteUrl?: string | null;
}

/**
 * Official external streaming, reading, or social links.
 */
export interface ExternalLink {
  /** Unique link record ID */
  id: number;
  /** Destination hyperlink */
  url: string;
  /** Streaming / reading service or community name (e.g. Crunchyroll, Netflix) */
  site: string;
  /** Visual icon or badge URL */
  icon?: string | null;
  /** Brand color associated with external site */
  color?: string | null;
  /** Classification of link: STREAMING, SOCIAL, INFO */
  type?: 'STREAMING' | 'SOCIAL' | 'INFO' | string | null;
}

/**
 * Shared foundation for Anime and Manga domain entities.
 */
export interface BaseMedia {
  /** Canonical ID (AniList ID or SAHASRA master ID) */
  id: number;
  /** Legacy MyAnimeList cross-reference ID */
  idMal?: number | null;
  /** Comprehensive localized titles */
  title: MediaTitle;
  /** Canonical media classification */
  type: MediaType;
  /** Narrative description / synopsis (supports sanitized markdown or HTML) */
  description?: string | null;
  /** Earliest release or publication date */
  startDate?: FuzzyDate | null;
  /** Final release or conclusion date */
  endDate?: FuzzyDate | null;
  /** Visual assets including multi-size covers and hero banners */
  artwork: MediaArtwork;
  /** Genres list (e.g. Action, Fantasy, Sci-Fi) */
  genres: string[];
  /** Detailed content and demographic tags */
  tags: MediaTag[];
  /** Normalized average community rating out of 100 */
  averageScore?: number | null;
  /** Mathematical mean community score out of 100 */
  meanScore?: number | null;
  /** Total count of community members tracking this media */
  popularity?: number;
  /** Current popularity trend delta index */
  trending?: number;
  /** Total user favorite count */
  favourites?: number;
  /** Adult / mature 18+ content flag */
  isAdult: boolean;
  /** Country of origin code (JP, KR, CN, etc.) */
  countryOfOrigin?: string | null;
  /** Promotional video or trailer */
  trailer?: MediaTrailer | null;
  /** Cross-platform external database mappings */
  externalIdentifiers: ExternalIdentifier[];
  /** Official streaming and community links */
  externalLinks?: ExternalLink[];
  /** Related works in the broader franchise */
  relations?: MediaRelationEdge[];
  /** Key character cast */
  characters?: CharacterNode[];
  /** Key production staff */
  staff?: StaffNode[];
  /** Global community ranking achievements */
  rankings?: RankItem[];
  /** Score histogram */
  scoreDistribution?: ScoreDistribution[];
  /** Member status distribution */
  statusDistribution?: StatusDistribution[];
  /** Last updated timestamp (ISO string or unix ms) */
  updatedAt?: number | string | null;
}
