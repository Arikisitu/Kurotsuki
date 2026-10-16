package com.sahasra.app.domain.model

enum class AnimeFormat {
    TV,
    TV_SHORT,
    MOVIE,
    SPECIAL,
    OVA,
    ONA,
    MUSIC,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AnimeFormat {
            return when (value?.uppercase()) {
                "TV" -> TV
                "TV_SHORT" -> TV_SHORT
                "MOVIE" -> MOVIE
                "SPECIAL" -> SPECIAL
                "OVA" -> OVA
                "ONA" -> ONA
                "MUSIC" -> MUSIC
                else -> UNKNOWN
            }
        }
    }
}

enum class AnimeStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AnimeStatus {
            return when (value?.uppercase()) {
                "FINISHED" -> FINISHED
                "RELEASING" -> RELEASING
                "NOT_YET_RELEASED" -> NOT_YET_RELEASED
                "CANCELLED" -> CANCELLED
                "HIATUS" -> HIATUS
                else -> UNKNOWN
            }
        }
    }
}

enum class AnimeSeason {
    WINTER,
    SPRING,
    SUMMER,
    FALL,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): AnimeSeason {
            return when (value?.uppercase()) {
                "WINTER" -> WINTER
                "SPRING" -> SPRING
                "SUMMER" -> SUMMER
                "FALL" -> FALL
                else -> UNKNOWN
            }
        }
    }
}

data class AiringEpisode(
    val episode: Int,
    val airingAt: Long,
    val timeUntilAiring: Long
) {
    fun humanTimeRemaining(): String {
        val days = timeUntilAiring / 86400
        val hours = (timeUntilAiring % 86400) / 3600
        return when {
            days > 0 -> "Ep $episode in ${days}d ${hours}h"
            hours > 0 -> "Ep $episode in ${hours}h"
            else -> "Ep $episode airing soon"
        }
    }
}

data class AnimeEpisode(
    val id: String,
    val number: Int,
    val title: String? = null,
    val thumbnail: String? = null,
    val duration: Int? = null,
    val airDate: String? = null,
    val summary: String? = null,
    val streamUrl: String? = null,
    val isFiller: Boolean = false
)

data class Anime(
    val id: Int,
    val idMal: Int? = null,
    val title: MediaTitle,
    val format: AnimeFormat = AnimeFormat.TV,
    val status: AnimeStatus = AnimeStatus.UNKNOWN,
    val description: String? = null,
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,
    val season: AnimeSeason? = null,
    val seasonYear: Int? = null,
    val episodes: Int? = null,
    val duration: Int? = null, // in minutes
    val source: String? = null,
    val coverImage: MediaCoverImage = MediaCoverImage(),
    val bannerImage: String? = null,
    val genres: List<String> = emptyList(),
    val tags: List<MediaTag> = emptyList(),
    val averageScore: Int? = null, // 0 - 100
    val meanScore: Int? = null,
    val popularity: Int? = null,
    val trending: Int? = null,
    val favourites: Int? = null,
    val isAdult: Boolean = false,
    val countryOfOrigin: String? = "JP",
    val trailer: MediaTrailer? = null,
    val nextAiringEpisode: AiringEpisode? = null,
    val studios: List<StudioNode> = emptyList(),
    val characters: List<CharacterNode> = emptyList(),
    val staff: List<StaffNode> = emptyList(),
    val relations: List<RelatedMediaNode> = emptyList(),
    val externalLinks: List<ExternalLinkItem> = emptyList(),
    val episodesList: List<AnimeEpisode> = emptyList(),
    val isInLibrary: Boolean = false
) {
    fun displayScore(): String {
        return averageScore?.let { "${it}%" } ?: "N/A"
    }

    fun displayScoreOutOfTen(): String {
        return averageScore?.let { String.format("%.1f", it / 10.0) } ?: "—"
    }

    fun cleanDescription(): String {
        if (description.isNullOrBlank()) return "No description available."
        return description.replace(Regex("<[^>]*>"), "")
    }

    fun studioName(): String {
        return studios.firstOrNull { it.isMain }?.name
            ?: studios.firstOrNull()?.name
            ?: "Studio TBA"
    }
}
