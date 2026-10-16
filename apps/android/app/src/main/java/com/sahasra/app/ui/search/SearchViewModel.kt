package com.sahasra.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.usecase.SearchAnimeUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val searchAnimeUseCase: SearchAnimeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .debounce(400)
            .distinctUntilChanged()
            .onEach { q ->
                if (q.isBlank()) {
                    _uiState.update { it.copy(results = emptyList(), isLoading = false, hasSearched = false) }
                } else {
                    executeSearch(q)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        queryFlow.value = newQuery
    }

    fun clearQuery() {
        onQueryChange("")
    }

    fun searchNow() {
        val q = _uiState.value.query.trim()
        if (q.isNotBlank()) {
            executeSearch(q)
        }
    }

    private fun executeSearch(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasSearched = true, errorMessage = null) }
            val result = searchAnimeUseCase(query = query, page = 1, perPage = 30)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = result.getOrDefault(emptyList()),
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Search failed"
                    )
                }
            }
        }
    }
}
