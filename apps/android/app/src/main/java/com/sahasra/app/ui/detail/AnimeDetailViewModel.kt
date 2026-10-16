package com.sahasra.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.domain.model.Anime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnimeDetailUiState(
    val anime: Anime? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AnimeDetailViewModel(
    private val animeId: Int,
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
        observeLibraryStatus()
    }

    private fun observeLibraryStatus() {
        repository.isAnimeSaved(animeId)
            .onEach { saved ->
                _uiState.update { it.copy(isSaved = saved) }
            }
            .launchIn(viewModelScope)
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.getAnimeDetails(animeId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        anime = result.getOrNull(),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load anime details"
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val currentAnime = _uiState.value.anime ?: return
        viewModelScope.launch {
            repository.toggleSaveAnime(currentAnime)
        }
    }
}
