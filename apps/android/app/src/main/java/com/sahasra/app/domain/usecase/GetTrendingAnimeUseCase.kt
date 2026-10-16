package com.sahasra.app.domain.usecase

import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.domain.model.Anime

class GetTrendingAnimeUseCase(
    private val repository: AnimeRepository
) {
    suspend operator fun invoke(page: Int = 1, perPage: Int = 20): Result<List<Anime>> {
        return repository.getTrendingAnime(page = page, perPage = perPage)
    }
}
