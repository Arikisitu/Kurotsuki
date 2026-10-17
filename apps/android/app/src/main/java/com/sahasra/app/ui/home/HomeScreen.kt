package com.sahasra.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahasra.app.R
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MangaFormat
import com.sahasra.app.ui.components.AnimeCard
import com.sahasra.app.ui.components.MangaCard
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.components.HeroSection
import com.sahasra.app.ui.components.MangaHeroSection
import com.sahasra.app.ui.components.HomeSkeleton
import com.sahasra.app.ui.components.MediaCarousel
import com.sahasra.app.ui.anime.AnikkuAnimeSection
import com.sahasra.app.ui.manga.FutonMangaSection
import com.sahasra.app.ui.theme.AnikkuAccent
import com.sahasra.app.ui.theme.AnikkuCanvasDark
import com.sahasra.app.ui.theme.AnikkuTextPrimary
import com.sahasra.app.ui.theme.FutonMangaGold
import com.sahasra.app.ui.theme.FutonVelvetBlack
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (Anime) -> Unit,
    onMangaClick: (Manga) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onReadClick: (Manga) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSeeAllClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFormat by remember { mutableStateOf("All") }
    val formatFilters = listOf("All", "Manga", "Manhwa", "Manhua", "Light Novel")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
    ) {
        when {
            uiState.isLoading && uiState.trending.isEmpty() -> {
                HomeSkeleton()
            }
            uiState.errorMessage != null && uiState.trending.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Failed to connect to AniList",
                    onRetry = { viewModel.loadHomeData() }
                )
            }
            else -> {
                if (uiState.selectedTab == HomeTab.MANGA) {
                    FutonMangaSection(
                        trendingManga = uiState.trendingManga,
                        popularManga = uiState.popularManga,
                        onMangaClick = onMangaClick,
                        onReadClick = onReadClick,
                        onBookmarkClick = { viewModel.toggleMangaBookmark(it) },
                        onSearchClick = onSearchClick
                    )
                } else {
                    AnikkuAnimeSection(
                        featuredAnime = uiState.featuredAnime,
                        trendingAnime = uiState.trending,
                        seasonalAnime = uiState.popularSeason,
                        topRatedAnime = uiState.topRated,
                        onAnimeClick = onAnimeClick,
                        onWatchClick = onWatchClick,
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        onSearchClick = onSearchClick,
                        onSettingsClick = onSettingsClick
                    )
                }

                // Fixed Top Floating Bar: Kurotsuki Logo + Centered (Anime | Manga) Switcher + Search & Settings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kurotsuki Brand Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEE0A0D14))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kurotsuki_logo),
                            contentDescription = "Kurotsuki Katana Logo",
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "KUROTSUKI",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AnikkuTextPrimary,
                                    letterSpacing = 1.sp,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Centered Segmented Switcher: Anime (Anikku) | Manga (Futon)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xDD111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAnime = uiState.selectedTab == HomeTab.ANIME
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAnime) AnikkuAccent else Color.Transparent)
                                .clickable { viewModel.setTab(HomeTab.ANIME) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Tv,
                                    contentDescription = null,
                                    tint = if (isAnime) AnikkuCanvasDark else SahasraTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Anime",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnime) AnikkuCanvasDark else SahasraTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isAnime) FutonMangaGold else Color.Transparent)
                                .clickable { viewModel.setTab(HomeTab.MANGA) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Book,
                                    contentDescription = null,
                                    tint = if (!isAnime) FutonVelvetBlack else SahasraTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manga",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isAnime) FutonVelvetBlack else SahasraTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Search & Settings Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xDD111318))
                                .border(1.dp, SahasraBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = SahasraAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xDD111318))
                                .border(1.dp, SahasraBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = SahasraTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
