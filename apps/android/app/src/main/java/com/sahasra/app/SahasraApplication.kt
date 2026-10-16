package com.sahasra.app

import android.app.Application
import com.sahasra.app.data.local.SahasraDatabase
import com.sahasra.app.data.remote.AniListApi
import com.sahasra.app.data.repository.AnimeRepository
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.usecase.GetTrendingAnimeUseCase
import com.sahasra.app.domain.usecase.SearchAnimeUseCase

class SahasraApplication : Application() {

    lateinit var api: AniListApi
        private set

    lateinit var database: SahasraDatabase
        private set

    lateinit var animeRepository: AnimeRepository
        private set

    lateinit var mangaRepository: MangaRepository
        private set

    lateinit var getTrendingAnimeUseCase: GetTrendingAnimeUseCase
        private set

    lateinit var searchAnimeUseCase: SearchAnimeUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        api = AniListApi.create()
        database = SahasraDatabase.getInstance(this)
        animeRepository = AnimeRepository(api, database.animeDao())
        mangaRepository = MangaRepository(api, database.animeDao())
        getTrendingAnimeUseCase = GetTrendingAnimeUseCase(animeRepository)
        searchAnimeUseCase = SearchAnimeUseCase(animeRepository)
    }

    companion object {
        lateinit var instance: SahasraApplication
            private set
    }
}
