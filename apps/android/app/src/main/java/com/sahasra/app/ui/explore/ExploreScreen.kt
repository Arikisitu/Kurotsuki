package com.sahasra.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MediaType
import com.sahasra.app.ui.components.AnimeCard
import com.sahasra.app.ui.components.AnimeCardSkeleton
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onAnimeClick: (Anime) -> Unit,
    onMangaClick: (Manga) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
            .statusBarsPadding()
    ) {
        // Screen Header
        Text(
            text = "Explore & Discover",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SahasraTextPrimary
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Type Segmented Control (Anime vs Manga)
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SahasraSurface)
                .border(1.dp, SahasraBorder, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val isAnime = uiState.mediaType == MediaType.ANIME
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isAnime) SahasraAccent else SahasraSurface)
                    .clickable { viewModel.setMediaType(MediaType.ANIME) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Anime",
                    fontWeight = FontWeight.Bold,
                    color = if (isAnime) SahasraBg else SahasraTextSecondary,
                    fontSize = 14.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isAnime) SahasraAccent else SahasraSurface)
                    .clickable { viewModel.setMediaType(MediaType.MANGA) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Manga",
                    fontWeight = FontWeight.Bold,
                    color = if (!isAnime) SahasraBg else SahasraTextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Genres Chip Bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.availableGenres) { genre ->
                val isSelected = (genre == "All" && uiState.selectedGenre == null) ||
                        (uiState.selectedGenre == genre)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SahasraElevated else SahasraSurface)
                        .border(
                            1.dp,
                            if (isSelected) SahasraAccent else SahasraBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.selectGenre(genre) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = genre,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSelected) SahasraAccent else SahasraTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Grid
        when {
            uiState.isLoading -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(9) {
                        AnimeCardSkeleton(cardWidth = 110.dp)
                    }
                }
            }

            uiState.errorMessage != null && uiState.animeList.isEmpty() && uiState.mangaList.isEmpty() -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Failed to fetch items",
                    onRetry = { viewModel.loadData() }
                )
            }

            uiState.mediaType == MediaType.ANIME -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.animeList, key = { it.id }) { anime ->
                        AnimeCard(
                            anime = anime,
                            cardWidth = 110.dp,
                            onClick = { onAnimeClick(anime) }
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(110.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.mangaList, key = { it.id }) { manga ->
                        // Re-use card presentation for manga
                        AnimeCard(
                            anime = Anime(
                                id = manga.id,
                                title = manga.title,
                                format = com.sahasra.app.domain.model.AnimeFormat.UNKNOWN,
                                coverImage = manga.coverImage,
                                averageScore = manga.averageScore,
                                genres = manga.genres
                            ),
                            cardWidth = 110.dp,
                            onClick = { onMangaClick(manga) }
                        )
                    }
                }
            }
        }
    }
}
