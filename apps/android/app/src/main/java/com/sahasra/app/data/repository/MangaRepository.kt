package com.sahasra.app.data.repository

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class MangaRepository(
    private val api: AniListApi,
    private val animeDao: AnimeDao
) {

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

    suspend fun getMangaDetails(id: Int): Result<Manga> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.MANGA_DETAILS,
                variables = mapOf("id" to id)
            )
            val response = api.queryMedia(request)
            val media = response.data?.media
                ?: return Result.failure(IllegalStateException("Manga not found for id $id"))
            val isSaved = animeDao.isSaved(id).firstOrNull() ?: false
            Result.success(media.toManga().copy(isInLibrary = isSaved))
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
        val chCount = chapters ?: 10
        val sampleChapters = (1..minOf(chCount, 30)).map { chNum ->
            MangaChapter(
                id = "${id}_$chNum",
                number = chNum.toFloat(),
                title = "Chapter $chNum",
                pages = 22
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
            chaptersList = sampleChapters,
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

    suspend fun getChapterPages(mangaTitle: String, chapterNumber: Float): Result<List<String>> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val cleanTitle = mangaTitle.replace(Regex("[^a-zA-Z0-9 ]"), " ").trim()
                val encodedTitle = java.net.URLEncoder.encode(cleanTitle, "UTF-8")

                // 1. Search MangaDex for manga id
                val searchReq = okhttp3.Request.Builder()
                    .url("https://api.mangadex.org/manga?title=$encodedTitle&limit=5&order[relevance]=desc")
                    .header("User-Agent", "SAHASRA-Android/1.0")
                    .build()

                val searchResp = client.newCall(searchReq).execute()
                val searchJson = com.google.gson.JsonParser.parseString(searchResp.body?.string() ?: "{}").asJsonObject
                val mangaList = searchJson.getAsJsonArray("data")
                if (mangaList == null || mangaList.size() == 0) {
                    return@withContext Result.failure(Exception("Manga not found on MangaDex"))
                }

                val mangaId = mangaList.get(0).asJsonObject.get("id").asString

                // 2. Query available chapters
                val feedReq = okhttp3.Request.Builder()
                    .url("https://api.mangadex.org/manga/$mangaId/feed?translatedLanguage[]=en&order[chapter]=asc&limit=100")
                    .header("User-Agent", "SAHASRA-Android/1.0")
                    .build()

                val feedResp = client.newCall(feedReq).execute()
                val feedJson = com.google.gson.JsonParser.parseString(feedResp.body?.string() ?: "{}").asJsonObject
                val chapterList = feedJson.getAsJsonArray("data")
                if (chapterList == null || chapterList.size() == 0) {
                    return@withContext Result.failure(Exception("No chapters available"))
                }

                val targetChInt = chapterNumber.toInt().toString()
                var chapterId = chapterList.get(0).asJsonObject.get("id").asString
                for (item in chapterList) {
                    val obj = item.asJsonObject
                    val attrs = obj.getAsJsonObject("attributes")
                    val chStr = attrs?.get("chapter")?.asString
                    if (chStr == targetChInt || chStr == chapterNumber.toString()) {
                        chapterId = obj.get("id").asString
                        break
                    }
                }

                // 3. Query At-Home server for chapter page files
                val atHomeReq = okhttp3.Request.Builder()
                    .url("https://api.mangadex.org/at-home/server/$chapterId")
                    .header("User-Agent", "SAHASRA-Android/1.0")
                    .build()

                val atHomeResp = client.newCall(atHomeReq).execute()
                val atHomeJson = com.google.gson.JsonParser.parseString(atHomeResp.body?.string() ?: "{}").asJsonObject
                val baseUrl = atHomeJson.get("baseUrl")?.asString ?: return@withContext Result.failure(Exception("Invalid server"))
                val chapterObj = atHomeJson.getAsJsonObject("chapter") ?: return@withContext Result.failure(Exception("No chapter payload"))
                val hash = chapterObj.get("hash")?.asString ?: return@withContext Result.failure(Exception("No hash"))
                val dataArr = chapterObj.getAsJsonArray("data") ?: return@withContext Result.failure(Exception("No pages array"))

                val pages = mutableListOf<String>()
                for (pageFile in dataArr) {
                    val filename = pageFile.asString
                    pages.add("$baseUrl/data/$hash/$filename")
                }

                Result.success(pages)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
