/**
 * AniList GraphQL Queries
 */

export const MEDIA_FRAGMENT = `
  id
  idMal
  title {
    romaji
    english
    native
    userPreferred
  }
  type
  format
  status
  description(asHtml: false)
  startDate {
    year
    month
    day
  }
  endDate {
    year
    month
    day
  }
  season
  seasonYear
  episodes
  duration
  chapters
  volumes
  countryOfOrigin
  source
  coverImage {
    extraLarge
    large
    medium
    color
  }
  bannerImage
  genres
  averageScore
  meanScore
  popularity
  trending
  favourites
  isAdult
  nextAiringEpisode {
    id
    airingAt
    timeUntilAiring
    episode
  }
  trailer {
    id
    site
    thumbnail
  }
  studios(isMain: true) {
    nodes {
      id
      name
      isAnimationStudio
      siteUrl
    }
  }
`;

export const TRENDING_ANIME_QUERY = `
  query TrendingAnime($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: ANIME, sort: TRENDING_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const POPULAR_ANIME_QUERY = `
  query PopularAnime($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: ANIME, sort: POPULARITY_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const TOP_RATED_ANIME_QUERY = `
  query TopRatedAnime($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: ANIME, sort: SCORE_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const SEASONAL_ANIME_QUERY = `
  query SeasonalAnime($page: Int = 1, $perPage: Int = 20, $season: MediaSeason, $seasonYear: Int) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: ANIME, season: $season, seasonYear: $seasonYear, sort: POPULARITY_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const UPCOMING_ANIME_QUERY = `
  query UpcomingAnime($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: ANIME, status: NOT_YET_RELEASED, sort: POPULARITY_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const TRENDING_MANGA_QUERY = `
  query TrendingManga($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: MANGA, sort: TRENDING_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const POPULAR_MANGA_QUERY = `
  query PopularManga($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: MANGA, sort: POPULARITY_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const TOP_RATED_MANGA_QUERY = `
  query TopRatedManga($page: Int = 1, $perPage: Int = 20) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(type: MANGA, sort: SCORE_DESC, isAdult: false) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const SEARCH_ANIME_QUERY = `
  query SearchAnime(
    $search: String,
    $page: Int = 1,
    $perPage: Int = 20,
    $genre: [String],
    $year: Int,
    $season: MediaSeason,
    $format: [MediaFormat],
    $status: MediaStatus,
    $sort: [MediaSort] = [POPULARITY_DESC]
  ) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(
        type: ANIME,
        search: $search,
        genre_in: $genre,
        seasonYear: $year,
        season: $season,
        format_in: $format,
        status: $status,
        sort: $sort,
        isAdult: false
      ) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const SEARCH_MANGA_QUERY = `
  query SearchManga(
    $search: String,
    $page: Int = 1,
    $perPage: Int = 20,
    $genre: [String],
    $format: [MediaFormat],
    $status: MediaStatus,
    $sort: [MediaSort] = [POPULARITY_DESC]
  ) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        total
        currentPage
        lastPage
        hasNextPage
        perPage
      }
      media(
        type: MANGA,
        search: $search,
        genre_in: $genre,
        format_in: $format,
        status: $status,
        sort: $sort,
        isAdult: false
      ) {
        ${MEDIA_FRAGMENT}
      }
    }
  }
`;

export const ANIME_DETAILS_QUERY = `
  query AnimeDetails($id: Int!) {
    Media(id: $id, type: ANIME) {
      ${MEDIA_FRAGMENT}
      tags {
        id
        name
        description
        category
        rank
        isGeneralSpoiler
        isMediaSpoiler
        isAdult
      }
      relations {
        edges {
          relationType
          node {
            id
            type
            title {
              romaji
              english
              userPreferred
            }
            format
            status
            coverImage {
              large
              medium
              color
            }
            averageScore
          }
        }
      }
      characters(sort: [ROLE, RELEVANCE], perPage: 12) {
        edges {
          role
          node {
            id
            name {
              full
              native
            }
            image {
              large
              medium
            }
          }
          voiceActors(language: JAPANESE) {
            id
            name {
              full
            }
            image {
              large
              medium
            }
          }
        }
      }
      staff(sort: [RELEVANCE], perPage: 8) {
        edges {
          role
          node {
            id
            name {
              full
              native
            }
            image {
              large
              medium
            }
          }
        }
      }
      externalLinks {
        id
        url
        site
        icon
        color
        type
      }
      rankings {
        id
        rank
        type
        format
        year
        season
        allTime
        context
      }
      stats {
        scoreDistribution {
          score
          amount
        }
        statusDistribution {
          status
          amount
        }
      }
      recommendations(sort: [RATING_DESC], perPage: 10) {
        edges {
          node {
            id
            mediaRecommendation {
              id
              type
              title {
                romaji
                english
                userPreferred
              }
              format
              status
              coverImage {
                large
                medium
                color
              }
              averageScore
              bannerImage
            }
          }
        }
      }
    }
  }
`;

export const MANGA_DETAILS_QUERY = `
  query MangaDetails($id: Int!) {
    Media(id: $id, type: MANGA) {
      ${MEDIA_FRAGMENT}
      tags {
        id
        name
        description
        category
        rank
        isGeneralSpoiler
        isMediaSpoiler
        isAdult
      }
      relations {
        edges {
          relationType
          node {
            id
            type
            title {
              romaji
              english
              userPreferred
            }
            format
            status
            coverImage {
              large
              medium
              color
            }
            averageScore
          }
        }
      }
      characters(sort: [ROLE, RELEVANCE], perPage: 12) {
        edges {
          role
          node {
            id
            name {
              full
              native
            }
            image {
              large
              medium
            }
          }
        }
      }
      staff(sort: [RELEVANCE], perPage: 8) {
        edges {
          role
          node {
            id
            name {
              full
              native
            }
            image {
              large
              medium
            }
          }
        }
      }
      externalLinks {
        id
        url
        site
        icon
        color
        type
      }
      rankings {
        id
        rank
        type
        format
        year
        season
        allTime
        context
      }
      recommendations(sort: [RATING_DESC], perPage: 10) {
        edges {
          node {
            id
            mediaRecommendation {
              id
              type
              title {
                romaji
                english
                userPreferred
              }
              format
              status
              coverImage {
                large
                medium
                color
              }
              averageScore
              bannerImage
            }
          }
        }
      }
    }
  }
`;

export const AIRING_SCHEDULE_QUERY = `
  query AiringSchedule($airingAt_greater: Int, $airingAt_lesser: Int, $page: Int = 1, $perPage: Int = 50) {
    Page(page: $page, perPage: $perPage) {
      pageInfo {
        hasNextPage
        total
      }
      airingSchedules(
        airingAt_greater: $airingAt_greater,
        airingAt_lesser: $airingAt_lesser,
        sort: TIME
      ) {
        id
        airingAt
        timeUntilAiring
        episode
        media {
          ${MEDIA_FRAGMENT}
        }
      }
    }
  }
`;
