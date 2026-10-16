package com.sahasra.app.domain.model

enum class MangaFormat {
    MANGA,
    NOVEL,
    ONE_SHOT,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MangaFormat {
            return when (value?.uppercase()) {
                "MANGA" -> MANGA
                "NOVEL" -> NOVEL
                "ONE_SHOT" -> ONE_SHOT
                else -> UNKNOWN
            }
        }
    }
}

enum class MangaStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED,
    HIATUS,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): MangaStatus {
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

data class MangaChapter(
    val id: String,
    val number: Float,
    val volume: Int? = null,
    val title: String? = null,
    val pages: Int? = null,
    val releaseDate: String? = null,
    val scanlationGroup: String? = null,
    val readerUrl: String? = null
)

data class Manga(
    val id: Int,
    val idMal: Int? = null,
    val title: MediaTitle,
    val format: MangaFormat = MangaFormat.MANGA,
    val status: MangaStatus = MangaStatus.UNKNOWN,
    val description: String? = null,
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val serialization: String? = null,
    val coverImage: MediaCoverImage = MediaCoverImage(),
    val bannerImage: String? = null,
    val genres: List<String> = emptyList(),
    val tags: List<MediaTag> = emptyList(),
    val averageScore: Int? = null,
    val meanScore: Int? = null,
    val popularity: Int? = null,
    val trending: Int? = null,
    val favourites: Int? = null,
    val isAdult: Boolean = false,
    val countryOfOrigin: String? = "JP",
    val authors: List<StaffNode> = emptyList(),
    val artists: List<StaffNode> = emptyList(),
    val characters: List<CharacterNode> = emptyList(),
    val relations: List<RelatedMediaNode> = emptyList(),
    val externalLinks: List<ExternalLinkItem> = emptyList(),
    val chaptersList: List<MangaChapter> = emptyList(),
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

    fun authorNames(): String {
        val names = authors.map { it.name }.filter { it.isNotBlank() }
        return if (names.isNotEmpty()) names.joinToString(", ") else "Author TBA"
    }
}
