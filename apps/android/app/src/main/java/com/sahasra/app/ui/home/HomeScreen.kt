package com.sahasra.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.components.HeroSection
import com.sahasra.app.ui.components.HomeSkeleton
import com.sahasra.app.ui.components.MediaCarousel
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAnimeClick: (Anime) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Hero Section
                    uiState.featured?.let { featured ->
                        item(key = "hero_${featured.id}") {
                            HeroSection(
                                anime = featured,
                                onWatchClick = onWatchClick,
                                onDetailsClick = onAnimeClick,
                                onBookmarkClick = { viewModel.toggleBookmark(it) }
                            )
                        }
                    }

                    // Trending Section
                    if (uiState.trending.isNotEmpty()) {
                        item(key = "section_trending") {
                            MediaCarousel(
                                title = "Trending Now",
                                items = uiState.trending,
                                onSeeAllClick = { onSeeAllClick("trending") },
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // Popular This Season
                    if (uiState.popularSeason.isNotEmpty()) {
                        item(key = "section_popular") {
                            MediaCarousel(
                                title = "Popular This Season",
                                items = uiState.popularSeason,
                                onSeeAllClick = { onSeeAllClick("popular") },
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // Top Rated
                    if (uiState.topRated.isNotEmpty()) {
                        item(key = "section_top_rated") {
                            MediaCarousel(
                                title = "Top Rated All-Time",
                                items = uiState.topRated,
                                onSeeAllClick = { onSeeAllClick("top_rated") },
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // Upcoming Next Season
                    if (uiState.upcoming.isNotEmpty()) {
                        item(key = "section_upcoming") {
                            MediaCarousel(
                                title = "Anticipated Upcoming",
                                items = uiState.upcoming,
                                onSeeAllClick = { onSeeAllClick("upcoming") },
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Floating Brand Header Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xCC08090B))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sahasra_logo),
                            contentDescription = "SAHASRA Logo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = "SAHASRA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SahasraTextPrimary,
                                letterSpacing = 2.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC111318))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = SahasraAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
