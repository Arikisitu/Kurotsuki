package com.sahasra.app.data.repository

import com.sahasra.app.data.local.dao.AnimeDao
import com.sahasra.app.data.local.entity.AnimeEntity
import com.sahasra.app.data.remote.AniListApi
import com.sahasra.app.data.remote.dto.AniListCoverImageDto
import com.sahasra.app.data.remote.dto.AniListGraphQLResponse
import com.sahasra.app.data.remote.dto.AniListMediaContainer
import com.sahasra.app.data.remote.dto.AniListMediaDto
import com.sahasra.app.data.remote.dto.AniListPageContainer
import com.sahasra.app.data.remote.dto.AniListPageDto
import com.sahasra.app.data.remote.dto.AniListTitleDto
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.AnimeFormat
import com.sahasra.app.domain.model.AnimeStatus
import com.sahasra.app.domain.model.MediaTitle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnimeRepositoryTest {

    private lateinit var api: AniListApi
    private lateinit var animeDao: AnimeDao
    private lateinit var repository: AnimeRepository

    @Before
    fun setUp() {
        api = mockk(relaxed = true)
        animeDao = mockk(relaxed = true)
        repository = AnimeRepository(api, animeDao)
    }

    @Test
    fun getTrendingAnime_returnsMappedList_onSuccess() = runTest {
        val mockMediaDto = AniListMediaDto(
            id = 154587,
            title = AniListTitleDto(
                english = "Frieren: Beyond Journey's End",
                romaji = "Sousou no Frieren",
                userPreferred = "Frieren: Beyond Journey's End"
            ),
            format = "TV",
            status = "FINISHED",
            description = "An elf mage re-evaluates the meaning of life after a decade-long quest.",
            episodes = 28,
            averageScore = 91,
            genres = listOf("Adventure", "Drama", "Fantasy"),
            coverImage = AniListCoverImageDto(
                extraLarge = "https://example.com/frieren.jpg"
            )
        )

        val mockPageContainer = AniListPageContainer(
            page = AniListPageDto(media = listOf(mockMediaDto))
        )

        coEvery { api.queryPage(any()) } returns AniListGraphQLResponse(data = mockPageContainer)

        val result = repository.getTrendingAnime(page = 1, perPage = 20)

        assertTrue(result.isSuccess)
        val animeList = result.getOrNull()
        assertEquals(1, animeList?.size)
        assertEquals(154587, animeList?.first()?.id)
        assertEquals("Frieren: Beyond Journey's End", animeList?.first()?.title?.english)
        assertEquals(AnimeFormat.TV, animeList?.first()?.format)
        assertEquals(AnimeStatus.FINISHED, animeList?.first()?.status)
        assertEquals(91, animeList?.first()?.averageScore)
    }

    @Test
    fun getAnimeDetails_returnsAnimeWithLibrarySavedState() = runTest {
        val mockMediaDto = AniListMediaDto(
            id = 142838,
            title = AniListTitleDto(english = "Solo Leveling"),
            format = "TV",
            status = "FINISHED",
            averageScore = 84
        )

        coEvery { api.queryMedia(any()) } returns AniListGraphQLResponse(
            data = AniListMediaContainer(media = mockMediaDto)
        )
        every { animeDao.isSaved(142838) } returns flowOf(true)

        val result = repository.getAnimeDetails(142838)

        assertTrue(result.isSuccess)
        val anime = result.getOrNull()
        assertEquals(142838, anime?.id)
        assertTrue(anime?.isInLibrary == true)
    }

    @Test
    fun toggleSaveAnime_insertsWhenNotSaved() = runTest {
        val anime = Anime(
            id = 1001,
            title = MediaTitle(english = "Test Anime"),
            format = AnimeFormat.TV,
            status = AnimeStatus.RELEASING,
            averageScore = 85
        )

        every { animeDao.isSaved(1001) } returns flowOf(false)
        coEvery { animeDao.insert(any()) } returns Unit

        repository.toggleSaveAnime(anime)

        coVerify(exactly = 1) { animeDao.insert(match { it.id == 1001 }) }
        coVerify(exactly = 0) { animeDao.deleteById(any()) }
    }

    @Test
    fun toggleSaveAnime_deletesWhenAlreadySaved() = runTest {
        val anime = Anime(
            id = 1002,
            title = MediaTitle(english = "Test Anime 2"),
            format = AnimeFormat.TV,
            status = AnimeStatus.FINISHED
        )

        every { animeDao.isSaved(1002) } returns flowOf(true)
        coEvery { animeDao.deleteById(1002) } returns Unit

        repository.toggleSaveAnime(anime)

        coVerify(exactly = 1) { animeDao.deleteById(1002) }
        coVerify(exactly = 0) { animeDao.insert(any()) }
    }
}
