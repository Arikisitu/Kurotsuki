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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.components.HeroSkeleton
import com.sahasra.app.ui.theme.KurotsukiBg
import com.sahasra.app.ui.theme.KurotsukiBorder
import com.sahasra.app.ui.theme.KurotsukiElevated
import com.sahasra.app.ui.theme.KurotsukiRed
import com.sahasra.app.ui.theme.KurotsukiStar
import com.sahasra.app.ui.theme.KurotsukiSurface
import com.sahasra.app.ui.theme.KurotsukiTextPrimary
import com.sahasra.app.ui.theme.KurotsukiTextSecondary

@Composable
fun AnimeDetailScreen(
    viewModel: AnimeDetailViewModel,
    onBackClick: () -> Unit,
    onPlayEpisode: (Anime, AnimeEpisode) -> Unit,
    onRelatedClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Episodes", "Details", "Recommendations")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KurotsukiBg)
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
                                                KurotsukiBg
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
                                    .background(KurotsukiSurface)
                                    .border(1.5.dp, KurotsukiBorder, RoundedCornerShape(12.dp))
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
                                        color = KurotsukiTextPrimary,
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
                                                color = KurotsukiTextSecondary,
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
                                                tint = KurotsukiStar,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = anime.displayScoreOutOfTen(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = KurotsukiTextPrimary
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "• ${anime.format.name}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = KurotsukiRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )

                                    Text(
                                        text = "• ${anime.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = KurotsukiTextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons (Watch & Bookmark)
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
                                    containerColor = KurotsukiRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Watch",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Watch Ep 1",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.toggleBookmark() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = KurotsukiSurface,
                                    contentColor = if (uiState.isSaved) KurotsukiRed else KurotsukiTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Library",
                                    tint = if (uiState.isSaved) KurotsukiRed else KurotsukiTextPrimary,
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

                    // 3-Tab Header (Episodes, Details, Recommendations)
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = KurotsukiBg,
                            contentColor = KurotsukiRed,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = KurotsukiRed,
                                    height = 3.dp
                                )
                            },
                            divider = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(KurotsukiBorder)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-30).dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTab == index) KurotsukiRed else KurotsukiTextSecondary,
                                            fontSize = 14.sp
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        0 -> {
                            // EPISODES TAB
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = (-20).dp)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Episodes (${anime.episodesList.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = KurotsukiTextPrimary
                                        )
                                    )
                                }
                            }

                            items(anime.episodesList) { episode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = (-20).dp)
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(KurotsukiSurface)
                                        .border(1.dp, KurotsukiBorder, RoundedCornerShape(12.dp))
                                        .clickable { onPlayEpisode(anime, episode) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(KurotsukiElevated)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = "Play",
                                            tint = KurotsukiRed,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = episode.title ?: "Episode ${episode.number}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = KurotsukiTextPrimary
                                            )
                                        )
                                        Text(
                                            text = "${episode.duration ?: 24} mins",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = KurotsukiTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // DETAILS TAB
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = (-15).dp)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "Synopsis",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = KurotsukiTextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = anime.cleanDescription(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = KurotsukiTextSecondary,
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
                                            color = KurotsukiRed,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Genres
                                    if (anime.genres.isNotEmpty()) {
                                        Text(
                                            text = "Genres",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = KurotsukiTextPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(anime.genres) { genre ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(KurotsukiElevated)
                                                        .border(1.dp, KurotsukiBorder, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                                ) {
                                                    Text(
                                                        text = genre,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = KurotsukiTextSecondary,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // Studio & Status info
                                    Text(
                                        text = "Information",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = KurotsukiTextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KurotsukiSurface)
                                            .border(1.dp, KurotsukiBorder, RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Studio", color = KurotsukiTextSecondary, fontSize = 13.sp)
                                            Text(anime.studioName(), color = KurotsukiTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Format", color = KurotsukiTextSecondary, fontSize = 13.sp)
                                            Text(anime.format.name, color = KurotsukiTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Status", color = KurotsukiTextSecondary, fontSize = 13.sp)
                                            Text(anime.status.name, color = KurotsukiTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Season", color = KurotsukiTextSecondary, fontSize = 13.sp)
                                            Text("${anime.season?.name ?: ""} ${anime.seasonYear ?: ""}", color = KurotsukiTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // Characters
                            if (anime.characters.isNotEmpty()) {
                                item {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        Text(
                                            text = "Main Characters",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = KurotsukiTextPrimary
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
                                                            .background(KurotsukiElevated)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = char.name,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = KurotsukiTextPrimary
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
                        }

                        2 -> {
                            // RECOMMENDATIONS TAB
                            if (anime.relations.isNotEmpty()) {
                                items(anime.relations) { rel ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = (-20).dp)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KurotsukiSurface)
                                            .border(1.dp, KurotsukiBorder, RoundedCornerShape(12.dp))
                                            .clickable { onRelatedClick(rel.id) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = rel.coverImage?.bestImageUrl(),
                                            contentDescription = rel.title.userPreferredTitle(),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 50.dp, height = 70.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rel.title.userPreferredTitle(),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = KurotsukiTextPrimary
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${rel.relationType} • ${rel.format}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = KurotsukiTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No related titles found",
                                            color = KurotsukiTextSecondary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }

                // Top Navigation Bar (Back)
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
                            tint = KurotsukiTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
