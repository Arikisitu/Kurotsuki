package com.sahasra.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sahasra.app.SahasraApplication
import com.sahasra.app.ui.detail.AnimeDetailScreen
import com.sahasra.app.ui.detail.AnimeDetailViewModel
import com.sahasra.app.ui.detail.MangaDetailScreen
import com.sahasra.app.ui.explore.ExploreScreen
import com.sahasra.app.ui.explore.ExploreViewModel
import com.sahasra.app.ui.home.HomeScreen
import com.sahasra.app.ui.home.HomeViewModel
import com.sahasra.app.ui.library.LibraryScreen
import com.sahasra.app.ui.library.LibraryViewModel
import com.sahasra.app.ui.player.PlayerScreen
import com.sahasra.app.ui.reader.MangaReaderScreen
import com.sahasra.app.ui.schedule.ScheduleScreen
import com.sahasra.app.ui.search.SearchScreen
import com.sahasra.app.ui.search.SearchViewModel
import com.sahasra.app.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun SahasraNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val app = SahasraApplication.instance

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(paddingValues),
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Screen.Home.route) {
            val homeViewModel = remember { HomeViewModel(app.animeRepository, app.mangaRepository) }
            HomeScreen(
                viewModel = homeViewModel,
                onAnimeClick = { anime ->
                    navController.navigate(Screen.AnimeDetail.createRoute(anime.id))
                },
                onMangaClick = { manga ->
                    navController.navigate(Screen.MangaDetail.createRoute(manga.id))
                },
                onWatchClick = { anime ->
                    navController.navigate(Screen.Player.createRoute(anime.id, anime.title.userPreferredTitle()))
                },
                onReadClick = { manga ->
                    navController.navigate(Screen.MangaReader.createRoute(manga.id, 1f))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onSeeAllClick = {
                    navController.navigate(Screen.Explore.route)
                }
            )
        }

        composable(Screen.Explore.route) {
            val exploreViewModel = remember {
                ExploreViewModel(app.animeRepository, app.mangaRepository)
            }
            ExploreScreen(
                viewModel = exploreViewModel,
                onAnimeClick = { anime ->
                    navController.navigate(Screen.AnimeDetail.createRoute(anime.id))
                },
                onMangaClick = { manga ->
                    navController.navigate(Screen.MangaDetail.createRoute(manga.id))
                }
            )
        }

        composable(Screen.Search.route) {
            val searchViewModel = remember {
                SearchViewModel(app.searchAnimeUseCase)
            }
            SearchScreen(
                viewModel = searchViewModel,
                onAnimeClick = { anime ->
                    navController.navigate(Screen.AnimeDetail.createRoute(anime.id))
                }
            )
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen(
                repository = app.animeRepository,
                onAnimeClick = { anime ->
                    navController.navigate(Screen.AnimeDetail.createRoute(anime.id))
                }
            )
        }

        composable(Screen.Library.route) {
            val libraryViewModel = remember {
                LibraryViewModel(app.animeRepository, app.mangaRepository)
            }
            LibraryScreen(
                viewModel = libraryViewModel,
                onAnimeClick = { anime ->
                    navController.navigate(Screen.AnimeDetail.createRoute(anime.id))
                },
                onMangaClick = { manga ->
                    navController.navigate(Screen.MangaDetail.createRoute(manga.id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AnimeDetail.route,
            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getInt("animeId") ?: return@composable
            val detailViewModel = remember(animeId) {
                AnimeDetailViewModel(animeId, app.animeRepository)
            }
            AnimeDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onPlayEpisode = { anime, episode ->
                    navController.navigate(
                        Screen.Player.createRoute(
                            mediaId = anime.id,
                            title = "${anime.title.userPreferredTitle()} - Ep ${episode.number}",
                            streamUrl = episode.streamUrl
                        )
                    )
                },
                onRelatedClick = { relId ->
                    navController.navigate(Screen.AnimeDetail.createRoute(relId))
                }
            )
        }

        composable(
            route = Screen.MangaDetail.route,
            arguments = listOf(navArgument("mangaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getInt("mangaId") ?: return@composable
            MangaDetailScreen(
                mangaId = mangaId,
                repository = app.mangaRepository,
                onBackClick = { navController.popBackStack() },
                onReadChapter = { manga, chapter ->
                    navController.navigate(Screen.MangaReader.createRoute(manga.id, chapter.number))
                }
            )
        }

        composable(
            route = Screen.MangaReader.route,
            arguments = listOf(
                navArgument("mangaId") { type = NavType.IntType },
                navArgument("chapterNumber") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getInt("mangaId") ?: return@composable
            val chapterNumber = backStackEntry.arguments?.getFloat("chapterNumber") ?: 1f
            com.sahasra.app.ui.reader.MangaReaderScreen(
                mangaId = mangaId,
                chapterNumber = chapterNumber,
                repository = app.mangaRepository,
                onBackClick = { navController.popBackStack() },
                onChapterChange = { nextCh ->
                    navController.navigate(Screen.MangaReader.createRoute(mangaId, nextCh)) {
                        popUpTo(Screen.MangaReader.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("mediaId") { type = NavType.IntType },
                navArgument("title") { type = NavType.StringType },
                navArgument("streamUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val rawTitle = backStackEntry.arguments?.getString("title") ?: "Player"
            val rawUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val title = runCatching { URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString()) }.getOrDefault(rawTitle)
            val streamUrl = runCatching { URLDecoder.decode(rawUrl, StandardCharsets.UTF_8.toString()) }.getOrNull()

            PlayerScreen(
                title = title,
                streamUrl = streamUrl,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
