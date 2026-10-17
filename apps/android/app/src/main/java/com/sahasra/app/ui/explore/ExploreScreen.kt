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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MediaType
import com.sahasra.app.ui.components.AnimeCard
import com.sahasra.app.ui.components.AnimeCardSkeleton
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.theme.KurotsukiBg
import com.sahasra.app.ui.theme.KurotsukiBorder
import com.sahasra.app.ui.theme.KurotsukiElevated
import com.sahasra.app.ui.theme.KurotsukiRed
import com.sahasra.app.ui.theme.KurotsukiSurface
import com.sahasra.app.ui.theme.KurotsukiTextPrimary
import com.sahasra.app.ui.theme.KurotsukiTextSecondary

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onAnimeClick: (Anime) -> Unit,
    onMangaClick: (Manga) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mangaSearchQuery by remember { mutableStateOf("") }
    var selectedAnimeCategory by remember { mutableStateOf("Popular") }
    val animeCategories = listOf("Popular", "Top Airing", "Upcoming", "Genres")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurotsukiBg)
            .statusBarsPadding()
    ) {
        // Top Switcher: [ Anime | Manga ]
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(KurotsukiSurface)
                .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                .padding(3.dp)
        ) {
            val isAnime = uiState.mediaType == MediaType.ANIME
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isAnime) KurotsukiRed else KurotsukiSurface)
                    .clickable { viewModel.setMediaType(MediaType.ANIME) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Anime",
                    fontWeight = FontWeight.Bold,
                    color = if (isAnime) KurotsukiTextPrimary else KurotsukiTextSecondary,
                    fontSize = 13.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (!isAnime) KurotsukiRed else KurotsukiSurface)
                    .clickable { viewModel.setMediaType(MediaType.MANGA) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Manga",
                    fontWeight = FontWeight.Bold,
                    color = if (!isAnime) KurotsukiTextPrimary else KurotsukiTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        if (uiState.mediaType == MediaType.ANIME) {
            // ANIME EXPLORE (Screen 3 in Blueprint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Anime",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = KurotsukiTextPrimary
                    )
                )

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = KurotsukiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sub-tabs: Popular, Top Airing, Upcoming, Genres
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(animeCategories) { cat ->
                    val isSelected = selectedAnimeCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) KurotsukiRed else KurotsukiSurface)
                            .border(1.dp, if (isSelected) KurotsukiRed else KurotsukiBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedAnimeCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) KurotsukiTextPrimary else KurotsukiTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 3-Column Poster Grid
            when {
                uiState.isLoading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(9) {
                            AnimeCardSkeleton(cardWidth = 105.dp)
                        }
                    }
                }

                uiState.errorMessage != null && uiState.animeList.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Failed to fetch anime",
                        onRetry = { viewModel.loadData() }
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
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
            }
        } else {
            // MANGA EXPLORE (Screen 8 in Blueprint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = mangaSearchQuery,
                    onValueChange = { mangaSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search manga...",
                            color = KurotsukiTextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = KurotsukiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = KurotsukiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = KurotsukiSurface,
                        unfocusedContainerColor = KurotsukiSurface,
                        focusedBorderColor = KurotsukiRed,
                        unfocusedBorderColor = KurotsukiBorder,
                        focusedTextColor = KurotsukiTextPrimary,
                        unfocusedTextColor = KurotsukiTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vertical Rich List with Chapter Counts
            val filteredManga = remember(uiState.mangaList, mangaSearchQuery) {
                if (mangaSearchQuery.isBlank()) uiState.mangaList
                else uiState.mangaList.filter {
                    it.title.userPreferredTitle().contains(mangaSearchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredManga, key = { it.id }) { manga ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(KurotsukiSurface)
                            .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                            .clickable { onMangaClick(manga) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KurotsukiElevated)
                        ) {
                            AsyncImage(
                                model = manga.coverImage.bestImageUrl(),
                                contentDescription = manga.title.userPreferredTitle(),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = manga.title.userPreferredTitle(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KurotsukiTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Manga • ${manga.genres.take(2).joinToString(" • ")}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = KurotsukiTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${manga.chapters ?: 150} chapters",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = KurotsukiRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
