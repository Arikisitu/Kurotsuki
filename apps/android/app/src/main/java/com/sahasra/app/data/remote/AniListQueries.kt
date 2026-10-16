package com.sahasra.app.data.remote

object AniListQueries {

    private const val MEDIA_BASIC_FIELDS = """
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
        description
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
        countryOfOrigin
        trailer {
            id
            site
            thumbnail
        }
        nextAiringEpisode {
            id
            airingAt
            timeUntilAiring
            episode
        }
    """

    private const val MEDIA_DETAIL_FIELDS = """
        $MEDIA_BASIC_FIELDS
        tags {
            id
            name
            description
            category
            rank
            isAdult
        }
        studios {
            nodes {
                id
                name
                isAnimationStudio
                siteUrl
            }
        }
        characters(sort: ROLE, perPage: 12) {
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
        staff(perPage: 8) {
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
                    }
                    averageScore
                }
            }
        }
        externalLinks {
            id
            url
            site
            icon
            color
        }
    """

    val TRENDING_ANIME = """
        query TrendingAnime(${'$'}page: Int = 1, ${'$'}perPage: Int = 20) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: ANIME, sort: TRENDING_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val POPULAR_THIS_SEASON = """
        query PopularThisSeason(${'$'}page: Int = 1, ${'$'}perPage: Int = 20, ${'$'}season: MediaSeason, ${'$'}seasonYear: Int) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: ANIME, season: ${'$'}season, seasonYear: ${'$'}seasonYear, sort: POPULARITY_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val TOP_RATED_ANIME = """
        query TopRatedAnime(${'$'}page: Int = 1, ${'$'}perPage: Int = 20) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: ANIME, sort: SCORE_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val UPCOMING_ANIME = """
        query UpcomingAnime(${'$'}page: Int = 1, ${'$'}perPage: Int = 20) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: ANIME, status: NOT_YET_RELEASED, sort: POPULARITY_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val SEARCH_ANIME = """
        query SearchAnime(${'$'}search: String, ${'$'}page: Int = 1, ${'$'}perPage: Int = 20, ${'$'}genre: String) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: ANIME, search: ${'$'}search, genre: ${'$'}genre, sort: POPULARITY_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val ANIME_DETAILS = """
        query AnimeDetails(${'$'}id: Int) {
            Media(id: ${'$'}id, type: ANIME) {
                $MEDIA_DETAIL_FIELDS
            }
        }
    """.trimIndent()

    val TRENDING_MANGA = """
        query TrendingManga(${'$'}page: Int = 1, ${'$'}perPage: Int = 20) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: MANGA, sort: TRENDING_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val SEARCH_MANGA = """
        query SearchManga(${'$'}search: String, ${'$'}page: Int = 1, ${'$'}perPage: Int = 20, ${'$'}genre: String) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    perPage
                    currentPage
                    lastPage
                    hasNextPage
                }
                media(type: MANGA, search: ${'$'}search, genre: ${'$'}genre, sort: POPULARITY_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()

    val MANGA_DETAILS = """
        query MangaDetails(${'$'}id: Int) {
            Media(id: ${'$'}id, type: MANGA) {
                $MEDIA_DETAIL_FIELDS
            }
        }
    """.trimIndent()

    val AIRING_SCHEDULE = """
        query AiringSchedule(${'$'}page: Int = 1, ${'$'}perPage: Int = 20) {
            Page(page: ${'$'}page, perPage: ${'$'}perPage) {
                pageInfo {
                    total
                    hasNextPage
                }
                media(type: ANIME, status: RELEASING, sort: POPULARITY_DESC, isAdult: false) {
                    $MEDIA_BASIC_FIELDS
                }
            }
        }
    """.trimIndent()
}
