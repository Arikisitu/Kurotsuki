/**
 * Raw AniList GraphQL API Response Types
 */

export interface AniListPageInfo {
  total: number;
  perPage: number;
  currentPage: number;
  lastPage: number;
  hasNextPage: boolean;
}

export interface AniListTitle {
  romaji: string | null;
  english: string | null;
  native: string | null;
  userPreferred: string | null;
}

export interface AniListCoverImage {
  extraLarge: string | null;
  large: string | null;
  medium: string | null;
  color: string | null;
}

export interface AniListFuzzyDate {
  year: number | null;
  month: number | null;
  day: number | null;
}

export interface AniListTrailer {
  id: string | null;
  site: string | null;
  thumbnail: string | null;
}

export interface AniListNextAiringEpisode {
  id?: number;
  airingAt: number;
  timeUntilAiring: number;
  episode: number;
}

export interface AniListStudioNode {
  id: number;
  name: string;
  isAnimationStudio?: boolean;
  siteUrl?: string;
}

export interface AniListTag {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  rank: number | null;
  isGeneralSpoiler: boolean | null;
  isMediaSpoiler: boolean | null;
  isAdult: boolean | null;
}

export interface AniListExternalLink {
  id: number;
  url: string;
  site: string;
  icon: string | null;
  color: string | null;
  type: string | null;
}

export interface AniListScoreDistribution {
  score: number;
  amount: number;
}

export interface AniListStatusDistribution {
  status: string;
  amount: number;
}

export interface AniListRanking {
  id: number;
  rank: number;
  type: 'RATED' | 'POPULAR';
  format: string;
  year: number | null;
  season: string | null;
  allTime: boolean;
  context: string;
}

export interface AniListCharacterEdge {
  role: 'MAIN' | 'SUPPORTING' | 'BACKGROUND';
  node: {
    id: number;
    name: {
      full: string | null;
      native: string | null;
    };
    image: {
      large: string | null;
      medium: string | null;
    } | null;
  };
  voiceActors?: {
    id: number;
    name: {
      full: string | null;
    };
    image: {
      large: string | null;
      medium: string | null;
    } | null;
  }[];
}

export interface AniListStaffEdge {
  role: string;
  node: {
    id: number;
    name: {
      full: string | null;
      native: string | null;
    };
    image: {
      large: string | null;
      medium: string | null;
    } | null;
  };
}

export interface AniListRelationEdge {
  relationType: string;
  node: {
    id: number;
    type: 'ANIME' | 'MANGA';
    title: AniListTitle;
    format: string | null;
    status: string | null;
    coverImage: AniListCoverImage | null;
    averageScore: number | null;
  };
}

export interface AniListRecommendationEdge {
  node: {
    id: number;
    mediaRecommendation: {
      id: number;
      type: 'ANIME' | 'MANGA';
      title: AniListTitle;
      format: string | null;
      status: string | null;
      coverImage: AniListCoverImage | null;
      averageScore: number | null;
      bannerImage: string | null;
    } | null;
  };
}

export interface AniListMedia {
  id: number;
  idMal: number | null;
  title: AniListTitle;
  type: 'ANIME' | 'MANGA';
  format: string | null;
  status: string | null;
  description: string | null;
  startDate: AniListFuzzyDate | null;
  endDate: AniListFuzzyDate | null;
  season: string | null;
  seasonYear: number | null;
  episodes: number | null;
  duration: number | null;
  chapters: number | null;
  volumes: number | null;
  countryOfOrigin: string | null;
  source: string | null;
  coverImage: AniListCoverImage | null;
  bannerImage: string | null;
  genres: string[];
  synonyms: string[];
  averageScore: number | null;
  meanScore: number | null;
  popularity: number | null;
  trending: number | null;
  favourites: number | null;
  tags: AniListTag[];
  relations?: {
    edges: AniListRelationEdge[];
  };
  characters?: {
    edges: AniListCharacterEdge[];
  };
  staff?: {
    edges: AniListStaffEdge[];
  };
  studios?: {
    nodes: AniListStudioNode[];
  };
  isAdult: boolean;
  nextAiringEpisode: AniListNextAiringEpisode | null;
  trailer: AniListTrailer | null;
  externalLinks?: AniListExternalLink[];
  rankings?: AniListRanking[];
  stats?: {
    scoreDistribution?: AniListScoreDistribution[];
    statusDistribution?: AniListStatusDistribution[];
  };
  recommendations?: {
    edges: AniListRecommendationEdge[];
  };
}

export interface AniListPageData {
  pageInfo: AniListPageInfo;
  media: AniListMedia[];
}

export interface AniListGraphQLResponse<T> {
  data?: T;
  errors?: {
    message: string;
    status?: number;
    locations?: { line: number; column: number }[];
  }[];
}
