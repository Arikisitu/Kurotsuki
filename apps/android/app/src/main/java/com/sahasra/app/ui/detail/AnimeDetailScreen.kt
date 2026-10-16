package com.sahasra.app.ui.detail

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.AnimeEpisode
import com.sahasra.app.ui.components.AnimeCard
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.components.HeroSkeleton
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraCardBg
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraScoreGold
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary

@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel,
    onBackClick: () -> Unit,
    onPlayEpisode: (Anime, AnimeEpisode) -> Unit,
    onRelatedClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
    ) {
        when {
            uiState.isLoading && uiState.anime == null -> {
                HeroSkeleton()
            }

            uiState.errorMessage != null && uiState.anime == null -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Failed to load anime details",
                    onRetry = { viewModel.loadDetails() }
                )
            }

            uiState.anime != null -> {
                val anime = uiState.anime!!

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Header Banner with Gradient Scrim
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            AsyncImage(
                                model = anime.bannerImage ?: anime.coverImage.bestImageUrl(),
                                contentDescription = anime.title.userPreferredTitle(),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x6608090B),
                                                Color(0xB308090B),
                                                SahasraBg
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Poster Overlap & Title Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-60).dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Poster Card
                            Box(
                                modifier = Modifier
                                    .width(115.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SahasraCardBg)
                                    .border(1.5.dp, SahasraBorder, RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = anime.coverImage.bestImageUrl(),
                                    contentDescription = anime.title.userPreferredTitle(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Header details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = anime.title.userPreferredTitle(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SahasraTextPrimary,
                                        lineHeight = 24.sp
                                    ),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                anime.title.romaji?.let { romaji ->
                                    if (romaji != anime.title.userPreferredTitle()) {
                                        Text(
                                            text = romaji,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = SahasraTextSecondary,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (anime.averageScore != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                tint = SahasraScoreGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = anime.displayScoreOutOfTen(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SahasraTextPrimary
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "• ${anime.format.name}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SahasraAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    Text(
                                        text = "• ${anime.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SahasraTextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons (Play & Bookmark)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-45).dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val firstEpisode = anime.episodesList.firstOrNull() ?: AnimeEpisode(
                                        id = "${anime.id}_1",
                                        number = 1,
                                        title = "Episode 1",
                                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                    )
                                    onPlayEpisode(anime, firstEpisode)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SahasraAccent,
                                    contentColor = SahasraBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Watch",
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Watch Episode 1",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.toggleBookmark() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = SahasraSurface,
                                    contentColor = if (uiState.isSaved) SahasraCrimson else SahasraTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Library",
                                    tint = if (uiState.isSaved) SahasraCrimson else SahasraTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isSaved) "In Library" else "Save",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Synopsis / Storyline
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-35).dp)
                        ) {
                            Text(
                                text = "Synopsis",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = anime.cleanDescription(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SahasraTextSecondary,
                                    lineHeight = 22.sp
                                ),
                                maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 4,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .animateContentSize()
                                    .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            )

                            Text(
                                text = if (isDescriptionExpanded) "Show Less" else "Read More",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SahasraAccent,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            )
                        }
                    }

                    // Genre Pills
                    item {
                        if (anime.genres.isNotEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.offset(y = (-20).dp)
                            ) {
                                items(anime.genres) { genre ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SahasraElevated)
                                            .border(1.dp, SahasraBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SahasraTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Episodes Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Episodes (${anime.episodesList.size})",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                )
                            )
                        }
                    }

                    items(anime.episodesList) { episode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SahasraSurface)
                                .border(1.dp, SahasraBorder, RoundedCornerShape(12.dp))
                                .clickable { onPlayEpisode(anime, episode) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SahasraElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play",
                                    tint = SahasraAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = episode.title ?: "Episode ${episode.number}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = SahasraTextPrimary
                                    )
                                )
                                Text(
                                    text = "${episode.duration ?: 24} mins",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SahasraTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Characters Section
                    if (anime.characters.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "Main Characters",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SahasraTextPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(anime.characters) { char ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(80.dp)
                                        ) {
                                            AsyncImage(
                                                model = char.imageUrl,
                                                contentDescription = char.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .background(SahasraElevated)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = char.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = SahasraTextPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }

                // Top Navigation Bar (Back & Share/Bookmark)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x9908090B))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = SahasraTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
