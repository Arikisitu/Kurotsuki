package com.sahasra.app.domain.model

enum class MediaType {
    ANIME,
    MANGA
}

data class MediaTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
    val userPreferred: String? = null
) {
    fun userPreferredTitle(): String {
        return english?.takeIf { it.isNotBlank() }
            ?: romaji?.takeIf { it.isNotBlank() }
            ?: userPreferred?.takeIf { it.isNotBlank() }
            ?: native?.takeIf { it.isNotBlank() }
            ?: "Unknown Title"
    }
}

data class MediaCoverImage(
    val extraLarge: String? = null,
    val large: String? = null,
    val medium: String? = null,
    val color: String? = null
) {
    fun bestImageUrl(): String {
        return extraLarge?.takeIf { it.isNotBlank() }
            ?: large?.takeIf { it.isNotBlank() }
            ?: medium?.takeIf { it.isNotBlank() }
            ?: ""
    }
}

data class FuzzyDate(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null
) {
    fun formatted(): String {
        if (year == null) return "TBA"
        val m = month?.let { String.format("%02d", it) }
        val d = day?.let { String.format("%02d", it) }
        return when {
            m != null && d != null -> "$year-$m-$d"
            m != null -> "$year-$m"
            else -> "$year"
        }
    }
}

data class MediaTrailer(
    val id: String? = null,
    val site: String? = null,
    val thumbnail: String? = null
) {
    fun youtubeUrl(): String? {
        return if (site?.equals("youtube", ignoreCase = true) == true && !id.isNullOrBlank()) {
            "https://www.youtube.com/watch?v=$id"
        } else null
    }
}

data class MediaTag(
    val id: Int,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val rank: Int? = null,
    val isAdult: Boolean = false
)

data class CharacterNode(
    val id: Int,
    val name: String,
    val role: String, // MAIN, SUPPORTING, BACKGROUND
    val imageUrl: String? = null,
    val voiceActorName: String? = null,
    val voiceActorImageUrl: String? = null
)

data class StaffNode(
    val id: Int,
    val name: String,
    val role: String? = null,
    val imageUrl: String? = null
)

data class StudioNode(
    val id: Int,
    val name: String,
    val isMain: Boolean = false,
    val siteUrl: String? = null
)

data class RelatedMediaNode(
    val id: Int,
    val type: MediaType,
    val title: MediaTitle,
    val format: String? = null,
    val status: String? = null,
    val coverImage: MediaCoverImage? = null,
    val averageScore: Int? = null,
    val relationType: String = "RELATED"
)

data class ExternalLinkItem(
    val id: Int,
    val url: String,
    val site: String,
    val icon: String? = null,
    val color: String? = null
)
