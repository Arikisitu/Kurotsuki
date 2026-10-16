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
import com.sahasra.app.data.remote.dto.AniListStudioNodeDto
import com.sahasra.app.data.remote.dto.GraphQLRequestBody
import com.sahasra.app.domain.model.AiringEpisode
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.AnimeEpisode
import com.sahasra.app.domain.model.AnimeFormat
import com.sahasra.app.domain.model.AnimeSeason
import com.sahasra.app.domain.model.AnimeStatus
import com.sahasra.app.domain.model.CharacterNode
import com.sahasra.app.domain.model.ExternalLinkItem
import com.sahasra.app.domain.model.FuzzyDate
import com.sahasra.app.domain.model.MediaCoverImage
import com.sahasra.app.domain.model.MediaTag
import com.sahasra.app.domain.model.MediaTitle
import com.sahasra.app.domain.model.MediaTrailer
import com.sahasra.app.domain.model.MediaType
import com.sahasra.app.domain.model.RelatedMediaNode
import com.sahasra.app.domain.model.StaffNode
import com.sahasra.app.domain.model.StudioNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class AnimeRepository(
    private val api: AniListApi,
    private val animeDao: AnimeDao
) {

    suspend fun getTrendingAnime(page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.TRENDING_ANIME,
                variables = mapOf("page" to page, "perPage" to perPage)
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPopularThisSeason(
        page: Int = 1,
        perPage: Int = 20,
        season: String? = null,
        seasonYear: Int? = null
    ): Result<List<Anime>> {
        return try {
            val calendar = Calendar.getInstance()
            val year = seasonYear ?: calendar.get(Calendar.YEAR)
            val computedSeason = season ?: when (calendar.get(Calendar.MONTH)) {
                in 0..2 -> "WINTER"
                in 3..5 -> "SPRING"
                in 6..8 -> "SUMMER"
                else -> "FALL"
            }

            val request = GraphQLRequestBody(
                query = AniListQueries.POPULAR_THIS_SEASON,
                variables = mapOf(
                    "page" to page,
                    "perPage" to perPage,
                    "season" to computedSeason,
                    "seasonYear" to year
                )
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopRatedAnime(page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.TOP_RATED_ANIME,
                variables = mapOf("page" to page, "perPage" to perPage)
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingAnime(page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.UPCOMING_ANIME,
                variables = mapOf("page" to page, "perPage" to perPage)
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnimeDetails(id: Int): Result<Anime> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.ANIME_DETAILS,
                variables = mapOf("id" to id)
            )
            val response = api.queryMedia(request)
            val media = response.data?.media
                ?: return Result.failure(IllegalStateException("Media not found for id $id"))
            val isSaved = animeDao.isSaved(id).firstOrNull() ?: false
            Result.success(media.toAnime().copy(isInLibrary = isSaved))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchAnime(
        query: String,
        page: Int = 1,
        perPage: Int = 20,
        genre: String? = null
    ): Result<List<Anime>> {
        return try {
            val vars = mutableMapOf<String, Any?>("page" to page, "perPage" to perPage)
            if (query.isNotBlank()) vars["search"] = query
            if (!genre.isNullOrBlank()) vars["genre"] = genre

            val request = GraphQLRequestBody(
                query = AniListQueries.SEARCH_ANIME,
                variables = vars
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAiringSchedule(page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        return try {
            val request = GraphQLRequestBody(
                query = AniListQueries.AIRING_SCHEDULE,
                variables = mapOf("page" to page, "perPage" to perPage)
            )
            val response = api.queryPage(request)
            val mediaList = response.data?.page?.media ?: emptyList()
            Result.success(mediaList.map { it.toAnime() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Local library management
    fun getLibraryAnime(): Flow<List<Anime>> {
        return animeDao.getLibraryByMediaType("ANIME").map { entities ->
            entities.map { entity -> entity.toAnimeDomain() }
        }
    }

    fun isAnimeSaved(id: Int): Flow<Boolean> {
        return animeDao.isSaved(id)
    }

    suspend fun toggleSaveAnime(anime: Anime) {
        val exists = animeDao.isSaved(anime.id).firstOrNull() ?: false
        if (exists) {
            animeDao.deleteById(anime.id)
        } else {
            animeDao.insert(anime.toEntity())
        }
    }

    suspend fun saveAnime(anime: Anime) {
        animeDao.insert(anime.toEntity())
    }

    suspend fun removeAnime(id: Int) {
        animeDao.deleteById(id)
    }

    private fun Anime.toEntity(): AnimeEntity {
        return AnimeEntity(
            id = id,
            titleEnglish = title.english ?: title.userPreferred,
            titleRomaji = title.romaji,
            coverImageUrl = coverImage.bestImageUrl(),
            bannerImageUrl = bannerImage,
            format = format.name,
            status = status.name,
            averageScore = averageScore,
            episodes = episodes,
            genres = genres.joinToString(","),
            synopsis = description,
            mediaType = "ANIME",
            libraryStatus = "PLANNING"
        )
    }

    private fun AnimeEntity.toAnimeDomain(): Anime {
        return Anime(
            id = id,
            title = MediaTitle(
                english = titleEnglish,
                romaji = titleRomaji,
                userPreferred = titleEnglish ?: titleRomaji
            ),
            format = AnimeFormat.fromString(format),
            status = AnimeStatus.fromString(status),
            description = synopsis,
            episodes = episodes,
            averageScore = averageScore,
            genres = if (genres.isBlank()) emptyList() else genres.split(","),
            coverImage = MediaCoverImage(extraLarge = coverImageUrl, large = coverImageUrl),
            bannerImage = bannerImageUrl,
            isInLibrary = true
        )
    }

    private fun AniListMediaDto.toAnime(): Anime {
        val epCount = episodes ?: 12
        val generatedEpisodes = (1..minOf(epCount, 24)).map { epNum ->
            AnimeEpisode(
                id = "${id}_$epNum",
                number = epNum,
                title = "Episode $epNum",
                thumbnail = bannerImage ?: coverImage?.large,
                duration = duration ?: 24,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )
        }

        return Anime(
            id = id,
            idMal = idMal,
            title = MediaTitle(
                romaji = title?.romaji,
                english = title?.english,
                native = title?.native,
                userPreferred = title?.userPreferred
            ),
            format = AnimeFormat.fromString(format),
            status = AnimeStatus.fromString(status),
            description = description,
            startDate = startDate?.let { FuzzyDate(it.year, it.month, it.day) },
            endDate = endDate?.let { FuzzyDate(it.year, it.month, it.day) },
            season = season?.let { AnimeSeason.fromString(it) },
            seasonYear = seasonYear,
            episodes = episodes,
            duration = duration,
            source = source,
            coverImage = MediaCoverImage(
                extraLarge = coverImage?.extraLarge,
                large = coverImage?.large,
                medium = coverImage?.medium,
                color = coverImage?.color
            ),
            bannerImage = bannerImage ?: coverImage?.extraLarge,
            genres = genres ?: emptyList(),
            tags = tags?.map { tagDto ->
                MediaTag(
                    id = tagDto.id,
                    name = tagDto.name,
                    description = tagDto.description,
                    category = tagDto.category,
                    rank = tagDto.rank,
                    isAdult = tagDto.isAdult ?: false
                )
            } ?: emptyList(),
            averageScore = averageScore,
            meanScore = meanScore,
            popularity = popularity,
            trending = trending,
            favourites = favourites,
            isAdult = isAdult ?: false,
            countryOfOrigin = countryOfOrigin ?: "JP",
            trailer = trailer?.let { MediaTrailer(it.id, it.site, it.thumbnail) },
            nextAiringEpisode = nextAiringEpisode?.let {
                AiringEpisode(it.episode ?: 1, it.airingAt ?: 0L, it.timeUntilAiring ?: 0L)
            },
            studios = studios?.nodes?.map { it.toStudio() } ?: emptyList(),
            characters = characters?.edges?.mapNotNull { it.toCharacter() } ?: emptyList(),
            staff = staff?.edges?.mapNotNull { it.toStaff() } ?: emptyList(),
            relations = relations?.edges?.mapNotNull { it.toRelation() } ?: emptyList(),
            externalLinks = externalLinks?.map { it.toExternalLink() } ?: emptyList(),
            episodesList = generatedEpisodes,
            isInLibrary = false
        )
    }

    private fun AniListStudioNodeDto.toStudio(): StudioNode {
        return StudioNode(
            id = id,
            name = name,
            isMain = isAnimationStudio ?: false,
            siteUrl = siteUrl
        )
    }

    private fun AniListCharacterEdgeDto.toCharacter(): CharacterNode? {
        val characterNode = node ?: return null
        val va = voiceActors?.firstOrNull()
        return CharacterNode(
            id = characterNode.id,
            name = characterNode.name?.full ?: "Unknown Character",
            role = role ?: "MAIN",
            imageUrl = characterNode.image?.large ?: characterNode.image?.medium,
            voiceActorName = va?.name?.full,
            voiceActorImageUrl = va?.image?.large ?: va?.image?.medium
        )
    }

    private fun AniListStaffEdgeDto.toStaff(): StaffNode? {
        val staffNode = node ?: return null
        return StaffNode(
            id = staffNode.id,
            name = staffNode.name?.full ?: "Unknown Staff",
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
