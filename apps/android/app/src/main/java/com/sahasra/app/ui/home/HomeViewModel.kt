package com.sahasra.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab {
    ANIME,
    MANGA
}

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.ANIME,
    val isLoading: Boolean = true,
    val trending: List<Anime> = emptyList(),
    val popularSeason: List<Anime> = emptyList(),
    val topRated: List<Anime> = emptyList(),
    val upcoming: List<Anime> = emptyList(),
    val trendingManga: List<Manga> = emptyList(),
    val popularManga: List<Manga> = emptyList(),
    val errorMessage: String? = null
) {
    val featuredAnime: Anime?
        get() = trending.firstOrNull()

    val featuredManga: Manga?
        get() = trendingManga.firstOrNull()
}

class HomeViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun setTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == HomeTab.MANGA && _uiState.value.trendingManga.isEmpty()) {
            loadMangaData()
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val trendingDeferred = async { animeRepository.getTrendingAnime(page = 1, perPage = 15) }
            val popularDeferred = async { animeRepository.getPopularThisSeason(page = 1, perPage = 15) }
            val topRatedDeferred = async { animeRepository.getTopRatedAnime(page = 1, perPage = 15) }
            val upcomingDeferred = async { animeRepository.getUpcomingAnime(page = 1, perPage = 15) }
            val mangaTrendingDeferred = async { mangaRepository.getTrendingManga(page = 1, perPage = 15) }

            val trendingRes = trendingDeferred.await()
            val popularRes = popularDeferred.await()
            val topRatedRes = topRatedDeferred.await()
            val upcomingRes = upcomingDeferred.await()
            val mangaTrendingRes = mangaTrendingDeferred.await()

            if (trendingRes.isSuccess || popularRes.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trending = trendingRes.getOrDefault(emptyList()),
                        popularSeason = popularRes.getOrDefault(emptyList()),
                        topRated = topRatedRes.getOrDefault(emptyList()),
                        upcoming = upcomingRes.getOrDefault(emptyList()),
                        trendingManga = mangaTrendingRes.getOrDefault(emptyList()),
                        errorMessage = null
                    )
                }
            } else {
                val error = trendingRes.exceptionOrNull()?.localizedMessage
                    ?: popularRes.exceptionOrNull()?.localizedMessage
                    ?: "Network connection error"
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    fun loadMangaData() {
        viewModelScope.launch {
            val trendingRes = mangaRepository.getTrendingManga(page = 1, perPage = 15)
            val popularRes = mangaRepository.searchManga(query = "", page = 1, perPage = 15)
            _uiState.update {
                it.copy(
                    trendingManga = trendingRes.getOrDefault(emptyList()),
                    popularManga = popularRes.getOrDefault(emptyList())
                )
            }
        }
    }

    fun toggleBookmark(anime: Anime) {
        viewModelScope.launch {
            animeRepository.toggleSaveAnime(anime)
        }
    }

    fun toggleMangaBookmark(manga: Manga) {
        viewModelScope.launch {
            mangaRepository.toggleSaveManga(manga)
        }
    }
}
