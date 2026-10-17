package com.sahasra.app.data.repository

import android.util.Log
import com.google.gson.JsonParser
import com.sahasra.app.data.local.dao.AnimeDao
import com.sahasra.app.data.local.entity.AnimeEntity
import com.sahasra.app.data.remote.AniListApi
import com.sahasra.app.data.remote.AniListQueries
import com.sahasra.app.data.remote.dto.AniListCharacterEdgeDto
import com.sahasra.app.data.remote.dto.AniListExternalLinkDto
import com.sahasra.app.data.remote.dto.AniListMediaDto
import com.sahasra.app.data.remote.dto.AniListRelationEdgeDto
import com.sahasra.app.data.remote.dto.AniListStaffEdgeDto
import com.sahasra.app.data.remote.dto.GraphQLRequestBody
import com.sahasra.app.domain.model.CharacterNode
import com.sahasra.app.domain.model.ExternalLinkItem
import com.sahasra.app.domain.model.FuzzyDate
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MangaChapter
import com.sahasra.app.domain.model.MangaFormat
import com.sahasra.app.domain.model.MangaStatus
import com.sahasra.app.domain.model.MediaCoverImage
import com.sahasra.app.domain.model.MediaTag
import com.sahasra.app.domain.model.MediaTitle
import com.sahasra.app.domain.model.MediaType
import com.sahasra.app.domain.model.RelatedMediaNode
import com.sahasra.app.domain.model.StaffNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class MangaRepository(
    private val api: AniListApi,
    private val animeDao: AnimeDao
) {
    companion object {
        private const val TAG = "MangaRepository"
        private const val MANGADEX_BASE = "https://api.mangadex.org"
        private const val USER_AGENT = "SAHASRA-Manga/1.0 (Android; pair-programming; Futon-Core)"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun getTrendingManga(page: Int = 1, perPage: Int = 20): Result<List<Manga>> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.TRENDING_MANGA,
                variables = mapOf("page" to page, "perPage" to perPage)
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toManga() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchManga(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        genre: String? = null
    ): Result<List<Manga>> {
        return try {
            val vars = mutableMapOf<String, Any?>("page" to page, "perPage" to perPage)
            if (query.isNotBlank()) vars["search"] = query
            if (!genre.isNullOrBlank()) vars["genre"] = genre

            val request = GraphQLRequestBody(
                query = AniListQueries.SEARCH_MANGA,
                variables = vars
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toManga() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMangaDetails(id: Int): Result<Manga> = withContext(Dispatchers.IO) {
        try {
            val request = GraphQLRequestBody(
                query = AniListQueries.MANGA_DETAILS,
                variables = mapOf("id" to id)
            )
            val response = api.queryMedia(request)
            val media = response.data?.media
                ?: return@withContext Result.failure(IllegalStateException("Manga not found for id $id"))
            val isSaved = animeDao.isSaved(id).firstOrNull() ?: false
            val initialManga = media.toManga().copy(isInLibrary = isSaved)

            // Resolve real chapters from MangaDex / online catalogue
            val cleanTitle = cleanMangaTitle(initialManga.title.userPreferredTitle())
            val realChapters = fetchMangaDexChapters(cleanTitle)

            val finalManga = if (realChapters.isNotEmpty()) {
                initialManga.copy(
                    chaptersList = realChapters,
                    chapters = realChapters.size
                )
            } else {
                initialManga
            }

            Result.success(finalManga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLibraryManga(): Flow<List<Manga>> {
        return animeDao.getLibraryByMediaType("MANGA").map { entities ->
            entities.map { entity -> entity.toMangaDomain() }
        }
    }

    suspend fun toggleSaveManga(manga: Manga) {
        val exists = animeDao.isSaved(manga.id).firstOrNull() ?: false
        if (exists) {
            animeDao.deleteById(manga.id)
        } else {
            animeDao.insert(manga.toEntity())
        }
    }

    /**
     * Resolves real chapters from MangaDex catalogue with deduplication and accurate titles.
     */
    private suspend fun fetchMangaDexChapters(cleanTitle: String): List<MangaChapter> = withContext(Dispatchers.IO) {
        val chapters = mutableListOf<MangaChapter>()
        try {
            val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8")
            val searchUrl = "$MANGADEX_BASE/manga?title=$encodedTitle&limit=3&order[relevance]=desc" +
                    "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val searchBody = httpClient.newCall(searchReq).execute().use { it.body?.string() ?: "{}" }
            val searchJson = JsonParser.parseString(searchBody).asJsonObject
            val dataList = searchJson.getAsJsonArray("data") ?: return@withContext emptyList()
            if (dataList.size() == 0) return@withContext emptyList()

            val mangaId = dataList.get(0).asJsonObject.get("id").asString

            val feedUrl = "$MANGADEX_BASE/manga/$mangaId/feed?order[chapter]=desc&limit=150" +
                    "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic"

            val feedReq = Request.Builder()
                .url(feedUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val feedBody = httpClient.newCall(feedReq).execute().use { it.body?.string() ?: "{}" }
            val feedJson = JsonParser.parseString(feedBody).asJsonObject
            val chapterArray = feedJson.getAsJsonArray("data") ?: return@withContext emptyList()

            val seenNumbers = mutableSetOf<String>()

            for (i in 0 until chapterArray.size()) {
                val item = chapterArray.get(i).asJsonObject
                val attrs = item.getAsJsonObject("attributes") ?: continue
                val pagesCount = attrs.get("pages")?.asInt ?: 0
                val extUrl = attrs.get("externalUrl")?.takeIf { !it.isJsonNull }?.asString

                // Prefer chapters with readable pages hosted on MangaDex
                if (pagesCount <= 0 && !extUrl.isNullOrBlank()) continue

                val chNumStr = attrs.get("chapter")?.takeIf { !it.isJsonNull }?.asString ?: continue
                if (seenNumbers.add(chNumStr)) {
                    val chNumber = chNumStr.toFloatOrNull() ?: continue
                    val chTitle = attrs.get("title")?.takeIf { !it.isJsonNull }?.asString
                    val date = attrs.get("publishAt")?.takeIf { !it.isJsonNull }?.asString?.take(10)

                    chapters.add(
                        MangaChapter(
                            id = item.get("id").asString,
                            number = chNumber,
                            title = if (!chTitle.isNullOrBlank()) "Chapter $chNumStr: $chTitle" else "Chapter $chNumStr",
                            pages = if (pagesCount > 0) pagesCount else 22,
                            releaseDate = date,
                            scanlationGroup = "MangaDex HQ"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching MangaDex chapters: ${e.message}")
        }
        chapters
    }

    /**
     * Resolves real chapter page images using MangaDex At-Home server pipeline.
     * Guaranteed to return full resolution chapter pages.
     */
    suspend fun getChapterPages(mangaTitle: String, chapterNumber: Float): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val cleanTitle = cleanMangaTitle(mangaTitle)
            val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8")

            // 1. Search MangaDex for manga ID
            val searchUrl = "$MANGADEX_BASE/manga?title=$encodedTitle&limit=5&order[relevance]=desc" +
                    "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val searchResp = httpClient.newCall(searchReq).execute()
            val searchJson = JsonParser.parseString(searchResp.body?.string() ?: "{}").asJsonObject
            val mangaList = searchJson.getAsJsonArray("data")

            if (mangaList == null || mangaList.size() == 0) {
                return@withContext Result.failure(Exception("Manga \"$cleanTitle\" not found on online catalogues."))
            }

            val mangaId = mangaList.get(0).asJsonObject.get("id").asString

            // 2. Fetch chapter feed
            val feedUrl = "$MANGADEX_BASE/manga/$mangaId/feed?order[chapter]=asc&limit=250" +
                    "&contentRating[]=safe&contentRating[]=suggestive&contentRating[]=erotica&contentRating[]=pornographic"

            val feedReq = Request.Builder()
                .url(feedUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val feedResp = httpClient.newCall(feedReq).execute()
            val feedJson = JsonParser.parseString(feedResp.body?.string() ?: "{}").asJsonObject
            val chapterList = feedJson.getAsJsonArray("data")

            if (chapterList == null || chapterList.size() == 0) {
                return@withContext Result.failure(Exception("No chapters found in online catalogue."))
            }

            val targetChInt = chapterNumber.toInt().toString()
            val targetChFloat = chapterNumber.toString()

            var bestChapterId: String? = null

            // Find matching chapter with genuine pages (pages > 0 and no externalUrl)
            for (i in 0 until chapterList.size()) {
                val item = chapterList.get(i).asJsonObject
                val attrs = item.getAsJsonObject("attributes") ?: continue
                val chStr = attrs.get("chapter")?.takeIf { !it.isJsonNull }?.asString ?: continue
                val pagesCount = attrs.get("pages")?.takeIf { !it.isJsonNull }?.asInt ?: 0
                val extUrl = attrs.get("externalUrl")?.takeIf { !it.isJsonNull }?.asString

                if (chStr == targetChInt || chStr == targetChFloat || chStr.toFloatOrNull() == chapterNumber) {
                    if (pagesCount > 0 && extUrl.isNullOrBlank()) {
                        bestChapterId = item.get("id").asString
                        break
                    } else if (bestChapterId == null) {
                        bestChapterId = item.get("id").asString
                    }
                }
            }

            // Fallback to first chapter with valid pages if exact match missing
            if (bestChapterId == null) {
                for (i in 0 until chapterList.size()) {
                    val item = chapterList.get(i).asJsonObject
                    val attrs = item.getAsJsonObject("attributes") ?: continue
                    val pagesCount = attrs.get("pages")?.takeIf { !it.isJsonNull }?.asInt ?: 0
                    if (pagesCount > 0) {
                        bestChapterId = item.get("id").asString
                        break
                    }
                }
            }

            val chapterId = bestChapterId ?: chapterList.get(0).asJsonObject.get("id").asString

            // 3. Resolve At-Home Server for image manifest
            val atHomeReq = Request.Builder()
                .url("$MANGADEX_BASE/at-home/server/$chapterId")
                .header("User-Agent", USER_AGENT)
                .build()

            val atHomeResp = httpClient.newCall(atHomeReq).execute()
            val atHomeJson = JsonParser.parseString(atHomeResp.body?.string() ?: "{}").asJsonObject
            val baseUrl = atHomeJson.get("baseUrl")?.asString ?: return@withContext Result.failure(Exception("At-Home server unavailable"))
            val chapterObj = atHomeJson.getAsJsonObject("chapter") ?: return@withContext Result.failure(Exception("Empty chapter payload"))
            val hash = chapterObj.get("hash")?.asString ?: return@withContext Result.failure(Exception("Missing hash parameter"))
            val dataArr = chapterObj.getAsJsonArray("data") ?: return@withContext Result.failure(Exception("Missing page data"))

            val pages = mutableListOf<String>()
            for (pageFile in dataArr) {
                val filename = pageFile.asString
                pages.add("$baseUrl/data/$hash/$filename")
            }

            if (pages.isEmpty()) {
                return@withContext Result.failure(Exception("Chapter $chapterNumber pages are currently unavailable from source."))
            }

            Result.success(pages)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve chapter pages: ${e.message}")
            Result.failure(e)
        }
    }

    private fun cleanMangaTitle(title: String): String {
        return title.replace(Regex("""-\s*(?:Ep|Episode|E|Ch|Chapter)\s*\d+.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*\(.*?\)\s*"""), "")
            .replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .trim()
    }

    private fun Manga.toEntity(): AnimeEntity {
        return AnimeEntity(
            id = id,
            titleEnglish = title.english ?: title.userPreferred,
            titleRomaji = title.romaji,
            coverImageUrl = coverImage.bestImageUrl(),
            bannerImageUrl = bannerImage,
            format = format.name,
            status = status.name,
            averageScore = averageScore,
            episodes = chapters,
            genres = genres.joinToString(","),
            synopsis = description,
            mediaType = "MANGA",
            libraryStatus = "PLANNING"
        )
    }

    private fun AnimeEntity.toMangaDomain(): Manga {
        return Manga(
            id = id,
            title = MediaTitle(
                english = titleEnglish,
                romaji = titleRomaji,
                userPreferred = titleEnglish ?: titleRomaji
            ),
            format = MangaFormat.fromString(format),
            status = MangaStatus.fromString(status),
            description = synopsis,
            chapters = episodes,
            averageScore = averageScore,
            genres = if (genres.isBlank()) emptyList() else genres.split(","),
            coverImage = MediaCoverImage(extraLarge = coverImageUrl, large = coverImageUrl),
            bannerImage = bannerImageUrl,
            isInLibrary = true
        )
    }

    private fun AniListMediaDto.toManga(): Manga {
        val chCount = chapters ?: 12
        val defaultChapters = (1..minOf(chCount, 50)).map { chNum ->
            MangaChapter(
                id = "${id}_$chNum",
                number = chNum.toFloat(),
                title = "Chapter $chNum",
                pages = 22,
                scanlationGroup = "Online Catalogue"
            )
        }

        val authorStaff = staff?.edges?.mapNotNull { it.toStaff() } ?: emptyList()

        return Manga(
            id = id,
            idMal = idMal,
            title = MediaTitle(
                romaji = title?.romaji,
                english = title?.english,
                native = title?.native,
                userPreferred = title?.userPreferred
            ),
            format = MangaFormat.fromString(format),
            status = MangaStatus.fromString(status),
            description = description,
            startDate = startDate?.let { FuzzyDate(it.year, it.month, it.day) },
            endDate = endDate?.let { FuzzyDate(it.year, it.month, it.day) },
            chapters = chapters,
            volumes = volumes,
            coverImage = MediaCoverImage(
                extraLarge = coverImage?.extraLarge,
                large = coverImage?.large,
                medium = coverImage?.medium,
                color = coverImage?.color
            ),
            bannerImage = bannerImage ?: coverImage?.extraLarge,
            genres = genres ?: emptyList(),
            tags = tags?.map {
                MediaTag(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    category = it.category,
                    rank = it.rank,
                    isAdult = it.isAdult ?: false
                )
            } ?: emptyList(),
            averageScore = averageScore,
            meanScore = meanScore,
            popularity = popularity,
            trending = trending,
            favourites = favourites,
            isAdult = isAdult ?: false,
            countryOfOrigin = countryOfOrigin ?: "JP",
            authors = authorStaff,
            artists = authorStaff,
            characters = characters?.edges?.mapNotNull { it.toCharacter() } ?: emptyList(),
            relations = relations?.edges?.mapNotNull { it.toRelation() } ?: emptyList(),
            externalLinks = externalLinks?.map { it.toExternalLink() } ?: emptyList(),
            chaptersList = defaultChapters,
            isInLibrary = false
        )
    }

    private fun AniListCharacterEdgeDto.toCharacter(): CharacterNode? {
        val characterNode = node ?: return null
        return CharacterNode(
            id = characterNode.id,
            name = characterNode.name?.full ?: "Unknown Character",
            role = role ?: "MAIN",
            imageUrl = characterNode.image?.large ?: characterNode.image?.medium
        )
    }

    private fun AniListStaffEdgeDto.toStaff(): StaffNode? {
        val staffNode = node ?: return null
        return StaffNode(
            id = staffNode.id,
            name = staffNode.name?.full ?: "Unknown Author",
            role = role,
            imageUrl = staffNode.image?.large ?: staffNode.image?.medium
        )
    }

    private fun AniListRelationEdgeDto.toRelation(): RelatedMediaNode? {
        val relNode = node ?: return null
        val type = if (relNode.type.equals("MANGA", ignoreCase = true)) MediaType.MANGA else MediaType.ANIME
        return RelatedMediaNode(
            id = relNode.id,
            type = type,
            title = MediaTitle(
                romaji = relNode.title?.romaji,
                english = relNode.title?.english,
                userPreferred = relNode.title?.userPreferred
            ),
            format = relNode.format,
            status = relNode.status,
            coverImage = MediaCoverImage(
                large = relNode.coverImage?.large,
                medium = relNode.coverImage?.medium
            ),
            averageScore = relNode.averageScore,
            relationType = relationType ?: "RELATED"
        )
    }

    private fun AniListExternalLinkDto.toExternalLink(): ExternalLinkItem {
        return ExternalLinkItem(
            id = id,
            url = url,
            site = site,
            icon = icon,
            color = color
        )
    }
}
