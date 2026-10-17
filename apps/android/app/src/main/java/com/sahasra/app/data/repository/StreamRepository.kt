package com.sahasra.app.data.repository

import android.util.Log
import com.sahasra.app.domain.model.StreamResolutionResult
import com.sahasra.app.domain.model.StreamSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class StreamRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8000, TimeUnit.MILLISECONDS)
        .readTimeout(8000, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {
    companion object {
        private const val TAG = "StreamRepository"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36"
        private const val FLIX_BASE = "https://reanime.to"
        private const val FLIX_CLOUD = "https://flixcloud.cc"
        private const val ENC_DEC_API = "https://enc-dec.app/api"
        private const val ANIMEGG_BASE = "https://www.animegg.org"
    }

    /**
     * Measure Time to First Byte (TTFB) in milliseconds.
     */
    suspend fun pingStream(url: String, headers: Map<String, String> = emptyMap()): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val reqBuilder = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", USER_AGENT)
            headers.forEach { (k, v) -> reqBuilder.header(k, v) }

            client.newCall(reqBuilder.build()).execute().use { response ->
                if (response.isSuccessful || response.code < 400) {
                    System.currentTimeMillis() - start
                } else {
                    // Try ranged GET if HEAD returned 405 Method Not Allowed
                    val rangedReq = Request.Builder()
                        .url(url)
                        .header("Range", "bytes=0-1024")
                        .header("User-Agent", USER_AGENT)
                    headers.forEach { (k, v) -> rangedReq.header(k, v) }
                    client.newCall(rangedReq.build()).execute().use { rRes ->
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

    /**
     * Resolves genuine media stream sources dynamically by inspecting real extension providers.
     * Never returns dummy/mock media.
     */
    suspend fun resolveStreams(
        title: String,
        episode: Int,
        preferredLanguage: String = "en",
        preferredAudioType: String = "dub",
        mediaId: Int = 0
    ): StreamResolutionResult = withContext(Dispatchers.IO) {
        val cleanTitle = extractCleanTitle(title)
        val targetEpisode = extractEpisode(title, episode)
        Log.d(TAG, "Resolving streams for '$cleanTitle', Ep: $targetEpisode (mediaId: $mediaId, pref: $preferredLanguage/$preferredAudioType)")

        val candidates = mutableListOf<StreamSource>()

        // 1. Try FlixCloud / ReAnime Provider (primary fast provider)
        try {
            val flixStreams = resolveFlixStreams(cleanTitle, targetEpisode, mediaId, preferredAudioType)
            candidates.addAll(flixStreams)
        } catch (e: Exception) {
            Log.w(TAG, "FlixCloud resolution failed: ${e.message}")
        }

        // 2. Try AnimeGG Provider (backup real stream scraper)
        if (candidates.isEmpty()) {
            try {
                val ggStreams = resolveAnimeGgStreams(cleanTitle, targetEpisode, preferredAudioType)
                candidates.addAll(ggStreams)
            } catch (e: Exception) {
                Log.w(TAG, "AnimeGG resolution failed: ${e.message}")
            }
        }

        // 3. Try Local Backend Orchestration Service (if running on host or emulator)
        if (candidates.isEmpty()) {
            try {
                val backendStreams = resolveFromBackend(cleanTitle, targetEpisode, mediaId, preferredLanguage, preferredAudioType)
                candidates.addAll(backendStreams)
            } catch (e: Exception) {
                Log.w(TAG, "Backend stream resolver failed: ${e.message}")
            }
        }

        if (candidates.isEmpty()) {
            throw IllegalStateException("No active streams found for \"$cleanTitle\" Episode $targetEpisode. The episode may not have aired yet or sources are currently blocked.")
        }

        // Ping candidates concurrently to verify liveness and measure TTFB latency
        val pinged = candidates.map { source ->
            async {
                val ping = pingStream(source.url, source.headers)
                source.copy(ttfbMs = ping)
            }
        }.awaitAll()

        // Filter out dead links (latency >= 3000ms usually signifies failed/timed out)
        val working = pinged.filter { it.ttfbMs < 3000L }.ifEmpty { pinged }

        // Rank by user requirement hierarchy:
        // 1. Matching audio language
        // 2. Matching Sub/Dub preference
        // 3. Lowest TTFB latency
        // 4. Quality (1080p > 720p > 480p)
        val ranked = working.sortedWith(
            compareByDescending<StreamSource> { it.audioLanguage.equals(preferredLanguage, ignoreCase = true) }
                .thenByDescending { it.audioType.equals(preferredAudioType, ignoreCase = true) }
                .thenBy { it.ttfbMs }
                .thenByDescending {
                    when (it.quality.lowercase()) {
                        "1080p" -> 100
                        "720p" -> 75
                        "480p" -> 50
                        else -> 25
                    }
                }
        )

        val primary = ranked.first()
        val fallbackQueue = if (ranked.size > 1) ranked.subList(1, ranked.size) else emptyList()

        StreamResolutionResult(
            animeTitle = cleanTitle,
            episodeNumber = targetEpisode,
            preferredLanguage = preferredLanguage,
            primary = primary,
            fallbackQueue = fallbackQueue,
            allWorkingSources = ranked
        )
    }

    /**
     * Resolves streams using the ReAnime/FlixCloud provider.
     */
    private suspend fun resolveFlixStreams(
        cleanTitle: String,
        episode: Int,
        mediaId: Int,
        preferredAudioType: String
    ): List<StreamSource> = withContext(Dispatchers.IO) {
        val anilistId = if (mediaId > 0) {
            mediaId
        } else {
            lookupAnilistId(cleanTitle)
        }

        if (anilistId <= 0) return@withContext emptyList()

        val flixUrl = "$FLIX_BASE/api/flix/$anilistId/$episode"
        val flixReq = Request.Builder()
            .url(flixUrl)
            .header("User-Agent", USER_AGENT)
            .header("Referer", "$FLIX_BASE/watch/anime?ep=$episode")
            .header("Accept", "application/json, text/plain, */*")
            .build()

        val flixJson = client.newCall(flixReq).execute().use { res ->
            if (!res.isSuccessful) return@withContext emptyList()
            JSONObject(res.body?.string() ?: "{}")
        }

        val servers = flixJson.optJSONArray("servers") ?: return@withContext emptyList()
        val sources = mutableListOf<StreamSource>()

        // Order servers: test matching dataType (sub/dub) first
        val serverList = mutableListOf<JSONObject>()
        for (i in 0 until servers.length()) {
            val s = servers.getJSONObject(i)
            serverList.add(s)
        }
        serverList.sortByDescending { it.optString("dataType").equals(preferredAudioType, ignoreCase = true) }

        for (s in serverList) {
            val dataLink = s.optString("dataLink")
            val serverName = s.optString("serverName", "HD-1")
            val dataType = s.optString("dataType", "sub") // sub or dub

            if (dataLink.isBlank()) continue

            try {
                // Fetch flixcloud embed page
                val embedReq = Request.Builder()
                    .url(dataLink)
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "$FLIX_CLOUD/")
                    .build()

                val embedHtml = client.newCall(embedReq).execute().use { it.body?.string() ?: "" }
                val match = Regex("""type:\s*"data",\s*data:\s*(\{[\s\S]*?\})\s*,\s*uses:""").find(embedHtml)
                    ?: continue

                val rawData = match.groupValues[1]
                // Convert Svelte unquoted object to standard JSON
                val fixedJsonStr = rawData.replace(Regex("""(?<=[{,])\s*([a-zA-Z0-9_]+)\s*:"""), "\"$1\":")
                val dataObj = JSONObject(fixedJsonStr)
                dataObj.remove("subtitles")

                // Step 1: Token request to enc-dec.app
                val tokenReqBody = JSONObject().put("data", dataObj).toString()
                    .toRequestBody("application/json".toMediaType())
                val tokenReq = Request.Builder()
                    .url("$ENC_DEC_API/dec-flixcloud?type=token")
                    .header("User-Agent", USER_AGENT)
                    .post(tokenReqBody)
                    .build()

                val tokenResJson = client.newCall(tokenReq).execute().use { res ->
                    if (!res.isSuccessful) return@use null
                    JSONObject(res.body?.string() ?: "{}")
                } ?: continue

                if (tokenResJson.optInt("status") != 200) continue
                val tokenResult = tokenResJson.getJSONObject("result")
                val token = tokenResult.getString("token")
                val context = tokenResult.getJSONObject("context")

                // Step 2: Fetch encrypted stream from flixcloud
                val streamReq = Request.Builder()
                    .url("$FLIX_CLOUD/api/m3u8/$token")
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", "$FLIX_CLOUD/")
                    .build()

                val streamResJson = client.newCall(streamReq).execute().use { res ->
                    if (!res.isSuccessful) return@use null
                    JSONObject(res.body?.string() ?: "{}")
                } ?: continue

                // Step 3: Decrypt stream via enc-dec.app
                val decPayload = JSONObject().apply {
                    put("data", JSONObject().apply {
                        put("context", context)
                        put("stream_response", streamResJson)
                    })
                }
                val decReq = Request.Builder()
                    .url("$ENC_DEC_API/dec-flixcloud?type=stream")
                    .header("User-Agent", USER_AGENT)
                    .post(decPayload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val decResJson = client.newCall(decReq).execute().use { res ->
                    if (!res.isSuccessful) return@use null
                    JSONObject(res.body?.string() ?: "{}")
                } ?: continue

                if (decResJson.optInt("status") != 200) continue
                val streamResult = decResJson.getJSONObject("result")
                val rawStreamUrl = streamResult.getString("stream")
                val wPayload = streamResult.getJSONObject("context").getString("w_payload")

                // Final parsed manifest URL with multi-audio & multi-quality tracks
                val manifestUrl = "$ENC_DEC_API/parse-flixcloud?url=" +
                        URLEncoder.encode(rawStreamUrl, "UTF-8") +
                        "&w_payload=" + URLEncoder.encode(wPayload, "UTF-8")

                val isSub = dataType.equals("sub", ignoreCase = true)
                val lang = if (isSub) "ja" else "en"

                sources.add(
                    StreamSource(
                        id = "flix-$dataType-$serverName-$episode",
                        providerId = "reanime-flix",
                        providerName = "FlixCloud $serverName (${if (isSub) "Subbed" else "English Dub"})",
                        url = manifestUrl,
                        quality = "1080p",
                        isHls = true,
                        audioLanguage = lang,
                        audioType = if (isSub) "sub" else "dub",
                        serverName = "FlixCloud $serverName",
                        headers = mapOf("Referer" to "$FLIX_CLOUD/")
                    )
                )

                // If we got 2 working sources, that's enough for primary + fallback
                if (sources.size >= 2) break
            } catch (e: Exception) {
                Log.w(TAG, "Error decrypting Flix server $serverName: ${e.message}")
            }
        }

        sources
    }

    /**
     * Look up AniList ID from title via ReAnime search API.
     */
    private fun lookupAnilistId(cleanTitle: String): Int {
        try {
            val searchUrl = "$FLIX_BASE/api/v1/search?q=" + URLEncoder.encode(cleanTitle, "UTF-8") + "&limit=5"
            val req = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", USER_AGENT)
                .header("Referer", "$FLIX_BASE/")
                .build()

            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) return 0
                val json = JSONObject(res.body?.string() ?: "{}")
                val results = json.optJSONArray("results") ?: return 0
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val aid = item.optInt("anilist_id", 0)
                    if (aid > 0) return aid
                }
            }
        } catch (_: Exception) {}
        return 0
    }

    /**
     * Resolves streams from AnimeGG mirror.
     */
    private suspend fun resolveAnimeGgStreams(
        cleanTitle: String,
        episode: Int,
        preferredAudioType: String
    ): List<StreamSource> = withContext(Dispatchers.IO) {
        val sources = mutableListOf<StreamSource>()
        try {
            val searchUrl = "$ANIMEGG_BASE/search/?q=" + URLEncoder.encode(cleanTitle, "UTF-8")
            val sReq = Request.Builder().url(searchUrl).header("User-Agent", USER_AGENT).build()
            val sHtml = client.newCall(sReq).execute().use { it.body?.string() ?: "" }

            val seriesRegex = Regex("""<a[^>]+href="([^"]+)"[^>]*class="mse"[^>]*>[\s\S]*?<h2>([^<]+)</h2>""", RegexOption.IGNORE_CASE)
            val seriesMatch = seriesRegex.find(sHtml) ?: return@withContext emptyList()
            val seriesHref = seriesMatch.groupValues[1]
            val fullSeriesUrl = if (seriesHref.startsWith("http")) seriesHref else "$ANIMEGG_BASE$seriesHref"

            val seriesReq = Request.Builder().url(fullSeriesUrl).header("User-Agent", USER_AGENT).build()
            val seriesHtml = client.newCall(seriesReq).execute().use { it.body?.string() ?: "" }

            val epRegex = Regex("""<a[^>]+href="([^"]+episode-$episode)"""", RegexOption.IGNORE_CASE)
            val epMatch = epRegex.find(seriesHtml) ?: return@withContext emptyList()
            val epHref = epMatch.groupValues[1]
            val fullEpUrl = if (epHref.startsWith("http")) epHref else "$ANIMEGG_BASE$epHref"

            val epReq = Request.Builder().url(fullEpUrl).header("User-Agent", USER_AGENT).build()
            val epHtml = client.newCall(epReq).execute().use { it.body?.string() ?: "" }

            val iframeRegex = Regex("""<iframe[^>]+src="([^"]+)"""", RegexOption.IGNORE_CASE)
            val iframes = iframeRegex.findAll(epHtml).map { it.groupValues[1] }.toList()

            for (ifLink in iframes) {
                val embedUrl = if (ifLink.startsWith("http")) ifLink else "$ANIMEGG_BASE$ifLink"
                val emReq = Request.Builder()
                    .url(embedUrl)
                    .header("User-Agent", USER_AGENT)
                    .header("Referer", fullEpUrl)
                    .build()
                val emHtml = client.newCall(emReq).execute().use { it.body?.string() ?: "" }
                val vsIdx = emHtml.indexOf("var videoSources =")
                if (vsIdx != -1) {
                    val rawArray = emHtml.substring(vsIdx + 18).substringBefore(";")
                    val fixedJson = rawArray.replace(Regex("""(?<=[{,])\s*([a-zA-Z0-9_]+)\s*:"""), "\"$1\":")
                    val arr = JSONArray(fixedJson)
                    for (j in 0 until arr.length()) {
                        val vObj = arr.getJSONObject(j)
                        val file = vObj.optString("file")
                        val label = vObj.optString("label", "720p")
                        if (file.isNotBlank()) {
                            val vUrl = if (file.startsWith("http")) file else "$ANIMEGG_BASE$file"
                            sources.add(
                                StreamSource(
                                    id = "animegg-$label-$episode",
                                    providerId = "animegg",
                                    providerName = "AnimeGG Mirror ($label)",
                                    url = vUrl,
                                    quality = label,
                                    isHls = file.contains(".m3u8"),
                                    audioLanguage = "en",
                                    audioType = preferredAudioType,
                                    serverName = "AnimeGG Edge",
                                    headers = mapOf("Referer" to "$ANIMEGG_BASE/")
                                )
                            )
                        }
                    }
                    if (sources.isNotEmpty()) break
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "AnimeGG scraping error: ${e.message}")
        }
        sources
    }

    /**
     * Resolves streams from local backend orchestrator if available.
     */
    private suspend fun resolveFromBackend(
        cleanTitle: String,
        episode: Int,
        mediaId: Int,
        preferredLanguage: String,
        preferredAudioType: String
    ): List<StreamSource> = withContext(Dispatchers.IO) {
        val candidateHosts = listOf("10.0.2.2:4000", "127.0.0.1:4000", "localhost:4000")
        for (host in candidateHosts) {
            try {
                val url = "http://$host/api/v1/stream/resolve?title=" +
                        URLEncoder.encode(cleanTitle, "UTF-8") +
                        "&episode=$episode&mediaId=$mediaId&lang=$preferredLanguage&audioType=$preferredAudioType"
                val req = Request.Builder().url(url).build()
                client.newCall(req).execute().use { res ->
                    if (res.isSuccessful) {
                        val json = JSONObject(res.body?.string() ?: "{}")
                        val workingArray = json.optJSONArray("allWorkingSources") ?: return@use
                        val list = mutableListOf<StreamSource>()
                        for (i in 0 until workingArray.length()) {
                            val item = workingArray.getJSONObject(i)
                            list.add(
                                StreamSource(
                                    id = item.optString("id"),
                                    providerId = item.optString("providerId"),
                                    providerName = item.optString("providerName"),
                                    url = item.optString("url"),
                                    quality = item.optString("quality", "1080p"),
                                    isHls = item.optBoolean("isHls", true),
                                    audioLanguage = item.optString("audioLanguage", preferredLanguage),
                                    audioType = item.optString("audioType", preferredAudioType),
                                    ttfbMs = item.optLong("ttfbMs", 50L),
                                    serverName = item.optString("serverName", "Backend Mirror")
                                )
                            )
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (_: Exception) {}
        }
        emptyList()
    }

    private fun extractEpisode(title: String, defaultEp: Int): Int {
        val match = Regex("""(?:(?:Ep|Episode|E)\s*(\d+))""", RegexOption.IGNORE_CASE).find(title)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: defaultEp
    }

    private fun extractCleanTitle(title: String): String {
        return title.replace(Regex("""-\s*(?:Ep|Episode|E)\s*\d+.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(.*?\)\s*"""), "")
            .trim()
    }
}
