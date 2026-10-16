package com.sahasra.app.ui.library

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.AnimeFormat
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.ui.components.AnimeCard
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraAccentGlow
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SahasraTextPrimary
                    )
                )
                Text(
                    text = "${uiState.totalCount} saved titles",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SahasraTextSecondary
                    )
                )
            }
        }

        // Filter Pills
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LibraryFilter.entries.forEach { filter ->
                val isSelected = uiState.selectedFilter == filter
                val count = when (filter) {
                    LibraryFilter.ALL -> uiState.totalCount
                    LibraryFilter.ANIME -> uiState.savedAnime.size
                    LibraryFilter.MANGA -> uiState.savedManga.size
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SahasraElevated else SahasraSurface)
                        .border(
                            1.dp,
                            if (isSelected) SahasraAccent else SahasraBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.setFilter(filter) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${filter.name.lowercase().replaceFirstChar { it.uppercase() }} ($count)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSelected) SahasraAccent else SahasraTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display list based on selected filter
        val displayAnime = when (uiState.selectedFilter) {
            LibraryFilter.ALL, LibraryFilter.ANIME -> uiState.savedAnime
            LibraryFilter.MANGA -> emptyList()
        }

        val displayManga = when (uiState.selectedFilter) {
            LibraryFilter.ALL, LibraryFilter.MANGA -> uiState.savedManga
            LibraryFilter.ANIME -> emptyList()
        }

        val totalDisplayed = displayAnime.size + displayManga.size

        if (totalDisplayed == 0) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SahasraAccentGlow)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        tint = SahasraAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your library is empty",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SahasraTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Save your favorite anime and manga using the bookmark icon on any title.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SahasraTextSecondary,
                        textAlign = TextAlign.Center
                    )
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(110.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayAnime, key = { "anime_${it.id}" }) { anime ->
                    AnimeCard(
                        anime = anime,
                        cardWidth = 110.dp,
                        onClick = { onAnimeClick(anime) }
                    )
                }

                items(displayManga, key = { "manga_${it.id}" }) { manga ->
                    AnimeCard(
                        anime = Anime(
                            id = manga.id,
                            title = manga.title,
                            format = AnimeFormat.UNKNOWN,
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
