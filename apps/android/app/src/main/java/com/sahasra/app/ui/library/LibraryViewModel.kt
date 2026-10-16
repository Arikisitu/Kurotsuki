package com.sahasra.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryFilter {
    ALL,
    ANIME,
    MANGA
}

data class LibraryUiState(
    val selectedFilter: LibraryFilter = LibraryFilter.ALL,
    val savedAnime: List<Anime> = emptyList(),
    val savedManga: List<Manga> = emptyList()
) {
    val totalCount: Int
        get() = savedAnime.size + savedManga.size
}

class LibraryViewModel(
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LibraryFilter.ALL)

    val uiState: StateFlow<LibraryUiState> = combine(
        _selectedFilter,
        animeRepository.getLibraryAnime(),
        mangaRepository.getLibraryManga()
    ) { filter, animeList, mangaList ->
        LibraryUiState(
            selectedFilter = filter,
            savedAnime = animeList,
            savedManga = mangaList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun setFilter(filter: LibraryFilter) {
        _selectedFilter.value = filter
    }

    fun removeAnime(id: Int) {
        viewModelScope.launch {
            animeRepository.removeAnime(id)
        }
    }
}
