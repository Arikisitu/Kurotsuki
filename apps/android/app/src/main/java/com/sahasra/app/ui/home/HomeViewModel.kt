package com.sahasra.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.domain.model.Anime
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val trending: List<Anime> = emptyList(),
    val popularSeason: List<Anime> = emptyList(),
    val topRated: List<Anime> = emptyList(),
    val upcoming: List<Anime> = emptyList(),
    val errorMessage: String? = null
) {
    val featured: Anime?
        get() = trending.firstOrNull()
}

class HomeViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val trendingDeferred = async { repository.getTrendingAnime(page = 1, perPage = 15) }
            val popularDeferred = async { repository.getPopularThisSeason(page = 1, perPage = 15) }
            val topRatedDeferred = async { repository.getTopRatedAnime(page = 1, perPage = 15) }
            val upcomingDeferred = async { repository.getUpcomingAnime(page = 1, perPage = 15) }

            val trendingRes = trendingDeferred.await()
            val popularRes = popularDeferred.await()
            val topRatedRes = topRatedDeferred.await()
            val upcomingRes = upcomingDeferred.await()

            if (trendingRes.isSuccess || popularRes.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trending = trendingRes.getOrDefault(emptyList()),
                        popularSeason = popularRes.getOrDefault(emptyList()),
                        topRated = topRatedRes.getOrDefault(emptyList()),
                        upcoming = upcomingRes.getOrDefault(emptyList()),
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

    fun toggleBookmark(anime: Anime) {
        viewModelScope.launch {
            repository.toggleSaveAnime(anime)
        }
    }
}
