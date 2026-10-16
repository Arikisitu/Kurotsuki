package com.sahasra.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Schedule : Screen("schedule")
    data object Settings : Screen("settings")

    data object AnimeDetail : Screen("anime_detail/{animeId}") {
        fun createRoute(animeId: Int): String = "anime_detail/$animeId"
    }

    data object MangaDetail : Screen("manga_detail/{mangaId}") {
        fun createRoute(mangaId: Int): String = "manga_detail/$mangaId"
    }

    data object Player : Screen("player/{mediaId}/{title}?streamUrl={streamUrl}") {
        fun createRoute(mediaId: Int, title: String, streamUrl: String? = null): String {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedUrl = streamUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""
            return "player/$mediaId/$encodedTitle?streamUrl=$encodedUrl"
        }
    }
}

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Rounded.Home),
    BottomNavItem(Screen.Explore, "Explore", Icons.Rounded.Explore),
    BottomNavItem(Screen.Search, "Search", Icons.Rounded.Search),
    BottomNavItem(Screen.Schedule, "Schedule", Icons.Rounded.CalendarMonth),
    BottomNavItem(Screen.Library, "Library", Icons.Rounded.VideoLibrary)
)
