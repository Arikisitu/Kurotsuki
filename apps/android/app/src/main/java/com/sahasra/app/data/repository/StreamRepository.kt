package com.sahasra.app.data.repository

import com.sahasra.app.domain.model.StreamResolutionResult
import com.sahasra.app.domain.model.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class StreamRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .build()
) {
    private val hlsTestStream = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    private val mp4TestStream1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private val mp4TestStream2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private val mp4TestStream3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    suspend fun pingStream(url: String): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", "SAHASRA-Android/1.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code < 400) {
                    System.currentTimeMillis() - start
                } else {
                    // Try ranged GET if HEAD returned method not allowed
                    val rangedReq = Request.Builder()
                        .url(url)
                        .header("Range", "bytes=0-1024")
                        .header("User-Agent", "SAHASRA-Android/1.0")
                        .build()
                    client.newCall(rangedReq).execute().use { rRes ->
                        if (rRes.isSuccessful || rRes.code < 400) {
                            System.currentTimeMillis() - start
                        } else {
                            3000L
                        }
                    }
                }
            }
        } catch (_: Exception) {
            3000L
        }
    }

    fun getCandidateStreams(title: String, episode: Int): List<StreamSource> {
        return listOf(
            StreamSource(
                id = "pahe-turbo-en-1080p",
                providerId = "pahe-turbo",
                providerName = "AnimePahe Turbo CDN",
                url = hlsTestStream,
                quality = "1080p",
                isHls = true,
                audioLanguage = "en",
                audioType = "dub",
                serverName = "Cloudflare Edge",
                ttfbMs = 45L
            ),
            StreamSource(
                id = "gogo-cloud-ja-1080p",
                providerId = "gogo-cloud",
                providerName = "Gogo Stream Cloud",
                url = hlsTestStream,
                quality = "1080p",
                isHls = true,
                audioLanguage = "ja",
                audioType = "sub",
                serverName = "Tokyo Akamai",
                ttfbMs = 40L
            ),
            StreamSource(
                id = "animeworld-india-hi-1080p",
                providerId = "animeworld-india",
                providerName = "AnimeWorld India HD",
                url = mp4TestStream1,
                quality = "1080p",
                isHls = false,
                audioLanguage = "hi",
                audioType = "dub",
                serverName = "Mumbai FastCDN",
                ttfbMs = 50L
            ),
            StreamSource(
                id = "yomovies-hi-720p",
                providerId = "yomovies-hi",
                providerName = "YoMovies India",
                url = mp4TestStream2,
                quality = "720p",
                isHls = false,
                audioLanguage = "hi",
                audioType = "dub",
                serverName = "Delhi Edge",
                ttfbMs = 95L
            ),
            StreamSource(
                id = "kaa-ja-720p",
                providerId = "kaa-mirror",
                providerName = "KickAssAnime Mirror",
                url = hlsTestStream,
                quality = "720p",
                isHls = true,
                audioLanguage = "ja",
                audioType = "sub",
                serverName = "Fastly EU",
                ttfbMs = 110L
            ),
            StreamSource(
                id = "ultralite-480p",
                providerId = "ultralite",
                providerName = "UltraLite 480p",
                url = mp4TestStream3,
                quality = "480p",
                isHls = false,
                audioLanguage = "en",
                audioType = "dub",
                serverName = "Global Lite",
                ttfbMs = 135L
            )
        )
    }

    suspend fun resolveStreams(
        title: String,
        episode: Int,
        preferredLanguage: String = "en",
        preferredAudioType: String = "dub"
    ): StreamResolutionResult = withContext(Dispatchers.IO) {
        val candidates = getCandidateStreams(title, episode)

        // Ping candidates concurrently
        val pinged = candidates.map { source ->
            async {
                val ping = pingStream(source.url)
                source.copy(ttfbMs = ping)
            }
        }.awaitAll()

        // Filter strictly to allowed languages (en, hi, ja)
        val valid = pinged.filter { it.audioLanguage in listOf("en", "hi", "ja") }

        // Rank by Hierarchy: 1. Preferred language, 2. Lowest TTFB, 3. Quality tier
        val ranked = valid.sortedWith(
            compareByDescending<StreamSource> { it.audioLanguage == preferredLanguage }
                .thenByDescending { it.audioType == preferredAudioType }
                .thenBy { it.ttfbMs }
                .thenByDescending {
                    when (it.quality) {
                        "1080p" -> 100
                        "720p" -> 75
                        "480p" -> 50
                        else -> 25
                    }
                }
        )

        val primary = ranked.firstOrNull() ?: candidates.first()
        val fallback = if (ranked.size > 1) ranked.subList(1, ranked.size) else emptyList()

        StreamResolutionResult(
            animeTitle = title,
            episodeNumber = episode,
            preferredLanguage = preferredLanguage,
            primary = primary,
            fallbackQueue = fallback,
            allWorkingSources = ranked
        )
    }
}
