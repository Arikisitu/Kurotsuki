package com.sahasra.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val mediaType: MediaType = MediaType.ANIME,
    val selectedGenre: String? = null,
    val animeList: List<Anime> = emptyList(),
    val mangaList: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExploreViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    val availableGenres = listOf(
        "All",
        "Action",
        "Adventure",
        "Comedy",
        "Drama",
        "Fantasy",
        "Mystery",
        "Romance",
        "Sci-Fi",
        "Slice of Life",
        "Supernatural",
        "Thriller",
        "Horror",
        "Psychological"
    )

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setMediaType(type: MediaType) {
        if (_uiState.value.mediaType != type) {
            _uiState.update { it.copy(mediaType = type) }
            loadData()
        }
    }

    fun selectGenre(genre: String) {
        val normalized = if (genre == "All") null else genre
        if (_uiState.value.selectedGenre != normalized) {
            _uiState.update { it.copy(selectedGenre = normalized) }
            loadData()
        }
    }

    fun loadData() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (state.mediaType == MediaType.ANIME) {
                val result = animeRepository.searchAnime(
                    query = "",
                    page = 1,
                    perPage = 24,
                    genre = state.selectedGenre
                )
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            animeList = result.getOrDefault(emptyList()),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load anime"
                        )
                    }
                }
            } else {
                val result = mangaRepository.searchManga(
                    query = "",
                    page = 1,
                    perPage = 24,
                    genre = state.selectedGenre
                )
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mangaList = result.getOrDefault(emptyList()),
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load manga"
                        )
                    }
                }
            }
        }
    }
}
