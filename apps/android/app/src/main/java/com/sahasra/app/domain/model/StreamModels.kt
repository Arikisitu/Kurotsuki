package com.sahasra.app.domain.model

data class StreamSource(
    val id: String,
    val providerId: String,
    val providerName: String,
    val url: String,
    val quality: String = "1080p",
    val isHls: Boolean = true,
    val audioLanguage: String = "en", // "en", "hi", "ja"
    val audioType: String = "dub", // "sub", "dub"
    val ttfbMs: Long = 50L,
    val serverName: String = "Primary Edge",
    val status: String = "active"
)

data class StreamResolutionResult(
    val animeTitle: String,
    val episodeNumber: Int,
    val preferredLanguage: String,
    val primary: StreamSource,
    val fallbackQueue: List<StreamSource>,
    val allWorkingSources: List<StreamSource>
)
