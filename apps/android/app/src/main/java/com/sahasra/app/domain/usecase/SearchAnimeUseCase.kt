package com.sahasra.app.domain.usecase

import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.domain.model.Anime

class SearchAnimeUseCase(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return repository.searchAnime(query = query, page = page, perPage = perPage)
    }
}
