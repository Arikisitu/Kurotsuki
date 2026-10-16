/**
 * Normalizer utilities to transform AniList GraphQL responses into canonical SAHASRA domain models.
 */

import {
  Anime,
  Manga,
  BaseMedia,
  AnimeFormat,
  AnimeStatus,
  AnimeSeason,
  MangaFormat,
  MangaStatus,
  MediaRelationEdge,
  CharacterNode,
  StaffNode,
  StudioNode,
  ExternalLink,
  RankItem,
  MediaRelationType,
} from '@sahasra/shared-types';
import { AniListMedia, AniListCharacterEdge, AniListStaffEdge, AniListRelationEdge } from './types';

/**
 * Clean up HTML tags and entities from synopses
 */
export function sanitizeDescription(desc: string | null | undefined): string {
  if (!desc) return '';
  return desc
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<i>(.*?)<\/i>/gi, '$1')
    .replace(/<b>(.*?)<\/b>/gi, '$1')
    .replace(/&quot;/g, '"')
    .replace(/&amp;/g, '&')
    .replace(/&#039;/g, "'")
    .replace(/<[^>]+>/g, '')
    .trim();
}

/**
 * Normalize base media properties common to Anime and Manga
 */
export function normalizeBaseMedia(raw: AniListMedia): BaseMedia {
  const characters: CharacterNode[] = (raw.characters?.edges || []).map((edge: AniListCharacterEdge) => ({
    id: edge.node.id,
    name: {
      full: edge.node.name.full || 'Unknown',
      native: edge.node.name.native || null,
      userPreferred: edge.node.name.full || null,
    },
    image: edge.node.image
      ? {
          large: edge.node.image.large || null,
          medium: edge.node.image.medium || null,
        }
      : null,
    role: edge.role || 'SUPPORTING',
    voiceActors: edge.voiceActors?.map((va) => ({
      id: va.id,
      name: { full: va.name.full || 'Unknown' },
      image: va.image ? { large: va.image.large || null, medium: va.image.medium || null } : null,
      role: 'Voice Actor',
    })),
  }));

  const staff: StaffNode[] = (raw.staff?.edges || []).map((edge: AniListStaffEdge) => ({
    id: edge.node.id,
    name: {
      full: edge.node.name.full || 'Unknown',
      native: edge.node.name.native || null,
      userPreferred: edge.node.name.full || null,
    },
    image: edge.node.image
      ? {
          large: edge.node.image.large || null,
          medium: edge.node.image.medium || null,
        }
      : null,
    role: edge.role,
  }));

  const relations: MediaRelationEdge[] = (raw.relations?.edges || []).map((edge: AniListRelationEdge) => ({
    relationType: (edge.relationType as MediaRelationType) || 'OTHER',
    node: {
      id: edge.node.id,
      type: edge.node.type,
      title: {
        romaji: edge.node.title.romaji,
        english: edge.node.title.english,
        userPreferred: edge.node.title.userPreferred || edge.node.title.romaji,
      },
      format: edge.node.format,
      status: edge.node.status,
      coverImage: edge.node.coverImage,
      averageScore: edge.node.averageScore,
    },
  }));

  const externalLinks: ExternalLink[] = (raw.externalLinks || []).map((link) => ({
    id: link.id,
    url: link.url,
    site: link.site,
    icon: link.icon,
    color: link.color,
    type: link.type,
  }));

  const rankings: RankItem[] = (raw.rankings || []).map((rank) => ({
    rank: rank.rank,
    type: rank.type,
    allTime: rank.allTime,
    year: rank.year,
    season: rank.season,
  }));

  return {
    id: raw.id,
    idMal: raw.idMal,
    title: {
      romaji: raw.title.romaji,
      english: raw.title.english,
      native: raw.title.native,
      userPreferred: raw.title.userPreferred || raw.title.english || raw.title.romaji,
    },
    type: raw.type,
    description: sanitizeDescription(raw.description),
    startDate: raw.startDate
      ? {
          year: raw.startDate.year,
          month: raw.startDate.month,
          day: raw.startDate.day,
        }
      : null,
    endDate: raw.endDate
      ? {
          year: raw.endDate.year,
          month: raw.endDate.month,
          day: raw.endDate.day,
        }
      : null,
    artwork: {
      cover: {
        extraLarge: raw.coverImage?.extraLarge || raw.coverImage?.large,
        large: raw.coverImage?.large,
        medium: raw.coverImage?.medium,
        color: raw.coverImage?.color || '#8585F7',
      },
      banner: raw.bannerImage || null,
      dominantColor: raw.coverImage?.color || '#8585F7',
    },
    genres: raw.genres || [],
    tags: (raw.tags || []).map((t) => ({
      id: t.id,
      name: t.name,
      description: t.description,
      category: t.category,
      rank: t.rank,
      isGeneralSpoiler: t.isGeneralSpoiler || false,
      isMediaSpoiler: t.isMediaSpoiler || false,
      isAdult: t.isAdult || false,
    })),
    averageScore: raw.averageScore,
    meanScore: raw.meanScore,
    popularity: raw.popularity || 0,
    trending: raw.trending || 0,
    favourites: raw.favourites || 0,
    isAdult: Boolean(raw.isAdult),
    countryOfOrigin: raw.countryOfOrigin,
    trailer: raw.trailer
      ? {
          id: raw.trailer.id,
          site: raw.trailer.site,
          thumbnail: raw.trailer.thumbnail,
        }
      : null,
    externalIdentifiers: [
      { source: 'ANILIST', id: raw.id, url: `https://anilist.co/${raw.type.toLowerCase()}/${raw.id}` },
      ...(raw.idMal
        ? [{ source: 'MAL' as const, id: raw.idMal, url: `https://myanimelist.net/${raw.type.toLowerCase()}/${raw.idMal}` }]
        : []),
    ],
    externalLinks,
    relations,
    characters,
    staff,
    rankings,
    scoreDistribution: raw.stats?.scoreDistribution || [],
    statusDistribution: raw.stats?.statusDistribution || [],
  };
}

/**
 * Normalize AniList raw data to Canonical Anime entity
 */
export function normalizeAnime(raw: AniListMedia): Anime {
  const base = normalizeBaseMedia(raw);

  const studios: StudioNode[] = (raw.studios?.nodes || []).map((s) => ({
    id: s.id,
    name: s.name,
    isMain: s.isAnimationStudio || true,
    siteUrl: s.siteUrl,
  }));

  return {
    ...base,
    type: 'ANIME',
    format: (raw.format as AnimeFormat) || 'TV',
    status: (raw.status as AnimeStatus) || 'FINISHED',
    season: (raw.season as AnimeSeason) || null,
    seasonYear: raw.seasonYear || null,
    episodes: raw.episodes || null,
    duration: raw.duration || null,
    source: (raw.source as any) || null,
    nextAiringEpisode: raw.nextAiringEpisode
      ? {
          id: raw.nextAiringEpisode.id || 0,
          airingAt: raw.nextAiringEpisode.airingAt,
          timeUntilAiring: raw.nextAiringEpisode.timeUntilAiring,
          episode: raw.nextAiringEpisode.episode,
          mediaId: raw.id,
        }
      : null,
    studios,
  };
}

/**
 * Normalize AniList raw data to Canonical Manga entity
 */
export function normalizeManga(raw: AniListMedia): Manga {
  const base = normalizeBaseMedia(raw);

  // Group staff into authors and artists
  const authors: StaffNode[] = base.staff?.filter((s) =>
    /story|author|creator|original creator/i.test(s.role || '')
  ) || [];

  const artists: StaffNode[] = base.staff?.filter((s) =>
    /art|illustration|illustrator/i.test(s.role || '')
  ) || [];

  return {
    ...base,
    type: 'MANGA',
    format: (raw.format as MangaFormat) || 'MANGA',
    status: (raw.status as MangaStatus) || 'FINISHED',
    chapters: raw.chapters || null,
    volumes: raw.volumes || null,
    serialization: null,
    authors: authors.length > 0 ? authors : (base.staff || []).slice(0, 2),
    artists: artists.length > 0 ? artists : (base.staff || []).slice(0, 2),
  };
}
