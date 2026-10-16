package com.sahasra.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GraphQLRequestBody(
    @SerializedName("query") val query: String,
    @SerializedName("variables") val variables: Map<String, Any?> = emptyMap()
)

data class AniListGraphQLResponse<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<AniListGraphQLError>? = null
)

data class AniListGraphQLError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class AniListPageContainer(
    @SerializedName("Page") val page: AniListPageDto? = null
)

data class AniListMediaContainer(
    @SerializedName("Media") val media: AniListMediaDto? = null
)

data class AniListPageDto(
    @SerializedName("pageInfo") val pageInfo: AniListPageInfoDto? = null,
    @SerializedName("media") val media: List<AniListMediaDto>? = null
)

data class AniListPageInfoDto(
    @SerializedName("total") val total: Int? = null,
    @SerializedName("perPage") val perPage: Int? = null,
    @SerializedName("currentPage") val currentPage: Int? = null,
    @SerializedName("lastPage") val lastPage: Int? = null,
    @SerializedName("hasNextPage") val hasNextPage: Boolean? = null
)

data class AniListTitleDto(
    @SerializedName("romaji") val romaji: String? = null,
    @SerializedName("english") val english: String? = null,
    @SerializedName("native") val native: String? = null,
    @SerializedName("userPreferred") val userPreferred: String? = null
)

data class AniListCoverImageDto(
    @SerializedName("extraLarge") val extraLarge: String? = null,
    @SerializedName("large") val large: String? = null,
    @SerializedName("medium") val medium: String? = null,
    @SerializedName("color") val color: String? = null
)

data class AniListFuzzyDateDto(
    @SerializedName("year") val year: Int? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("day") val day: Int? = null
)

data class AniListTrailerDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("site") val site: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null
)

data class AniListNextAiringEpisodeDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("airingAt") val airingAt: Long? = null,
    @SerializedName("timeUntilAiring") val timeUntilAiring: Long? = null,
    @SerializedName("episode") val episode: Int? = null
)

data class AniListTagDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("rank") val rank: Int? = null,
    @SerializedName("isAdult") val isAdult: Boolean? = null
)

data class AniListStudiosDto(
    @SerializedName("nodes") val nodes: List<AniListStudioNodeDto>? = null
)

data class AniListStudioNodeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("isAnimationStudio") val isAnimationStudio: Boolean? = null,
    @SerializedName("siteUrl") val siteUrl: String? = null
)

data class AniListCharactersDto(
    @SerializedName("edges") val edges: List<AniListCharacterEdgeDto>? = null
)

data class AniListCharacterEdgeDto(
    @SerializedName("role") val role: String? = null,
    @SerializedName("node") val node: AniListCharacterNodeDto? = null,
    @SerializedName("voiceActors") val voiceActors: List<AniListVoiceActorDto>? = null
)

data class AniListCharacterNodeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: AniListNameDto? = null,
    @SerializedName("image") val image: AniListImageDto? = null
)

data class AniListNameDto(
    @SerializedName("full") val full: String? = null,
    @SerializedName("native") val native: String? = null
)

data class AniListImageDto(
    @SerializedName("large") val large: String? = null,
    @SerializedName("medium") val medium: String? = null
)

data class AniListVoiceActorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: AniListNameDto? = null,
    @SerializedName("image") val image: AniListImageDto? = null
)

data class AniListStaffDto(
    @SerializedName("edges") val edges: List<AniListStaffEdgeDto>? = null
)

data class AniListStaffEdgeDto(
    @SerializedName("role") val role: String? = null,
    @SerializedName("node") val node: AniListStaffNodeDto? = null
)

data class AniListStaffNodeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: AniListNameDto? = null,
    @SerializedName("image") val image: AniListImageDto? = null
)

data class AniListRelationsDto(
    @SerializedName("edges") val edges: List<AniListRelationEdgeDto>? = null
)

data class AniListRelationEdgeDto(
    @SerializedName("relationType") val relationType: String? = null,
    @SerializedName("node") val node: AniListRelatedMediaNodeDto? = null
)

data class AniListRelatedMediaNodeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: AniListTitleDto? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("coverImage") val coverImage: AniListCoverImageDto? = null,
    @SerializedName("averageScore") val averageScore: Int? = null
)

data class AniListExternalLinkDto(
    @SerializedName("id") val id: Int,
    @SerializedName("url") val url: String,
    @SerializedName("site") val site: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null
)

data class AniListMediaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("idMal") val idMal: Int? = null,
    @SerializedName("title") val title: AniListTitleDto? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("startDate") val startDate: AniListFuzzyDateDto? = null,
    @SerializedName("endDate") val endDate: AniListFuzzyDateDto? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("seasonYear") val seasonYear: Int? = null,
    @SerializedName("episodes") val episodes: Int? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("chapters") val chapters: Int? = null,
    @SerializedName("volumes") val volumes: Int? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("coverImage") val coverImage: AniListCoverImageDto? = null,
    @SerializedName("bannerImage") val bannerImage: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("averageScore") val averageScore: Int? = null,
    @SerializedName("meanScore") val meanScore: Int? = null,
    @SerializedName("popularity") val popularity: Int? = null,
    @SerializedName("trending") val trending: Int? = null,
    @SerializedName("favourites") val favourites: Int? = null,
    @SerializedName("tags") val tags: List<AniListTagDto>? = null,
    @SerializedName("isAdult") val isAdult: Boolean? = null,
    @SerializedName("countryOfOrigin") val countryOfOrigin: String? = null,
    @SerializedName("trailer") val trailer: AniListTrailerDto? = null,
    @SerializedName("nextAiringEpisode") val nextAiringEpisode: AniListNextAiringEpisodeDto? = null,
    @SerializedName("studios") val studios: AniListStudiosDto? = null,
    @SerializedName("characters") val characters: AniListCharactersDto? = null,
    @SerializedName("staff") val staff: AniListStaffDto? = null,
    @SerializedName("relations") val relations: AniListRelationsDto? = null,
    @SerializedName("externalLinks") val externalLinks: List<AniListExternalLinkDto>? = null
)
