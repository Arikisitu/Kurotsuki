package com.sahasra.app.ui.anime

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.sahasra.app.domain.model.Anime
import com.sahasra.app.domain.model.AnimeFormat
import com.sahasra.app.ui.theme.AnikkuAccent
import com.sahasra.app.ui.theme.AnikkuAccentSecondary
import com.sahasra.app.ui.theme.AnikkuAmber
import com.sahasra.app.ui.theme.AnikkuBloodMoon
import com.sahasra.app.ui.theme.AnikkuBorder
import com.sahasra.app.ui.theme.AnikkuCanvasDark
import com.sahasra.app.ui.theme.AnikkuEmerald
import com.sahasra.app.ui.theme.AnikkuSurface
import com.sahasra.app.ui.theme.AnikkuSurfaceElevated
import com.sahasra.app.ui.theme.AnikkuTextMuted
import com.sahasra.app.ui.theme.AnikkuTextPrimary
import com.sahasra.app.ui.theme.AnikkuTextSecondary

enum class AnikkuTab(val title: String) {
    SUGGESTIONS("Suggestions"),
    POPULAR("Popular"),
    SEASONAL("Seasonal"),
    UPDATES("Updates"),
    SOURCES("Catalogues")
}

data class AnikkuStreamCatalogue(
    val id: String,
    val name: String,
    val version: String,
    val language: String,
    val isOnline: Boolean,
    val latencyMs: Int,
    val supportedTypes: String,
    val quality: String
)

val DefaultAnikkuCatalogues = listOf(
    AnikkuStreamCatalogue(
        id = "allanime",
        name = "AllAnime HQ (Multi-Source)",
        version = "v1.5.0",
        language = "EN / Multi",
        isOnline = true,
        latencyMs = 88,
        supportedTypes = "Anime, OVAs, Movies",
        quality = "1080p / 720p HLS"
    ),
    AnikkuStreamCatalogue(
        id = "gogo",
        name = "Gogoanime Fast CDN",
        version = "v2.0.4",
        language = "EN Sub / Dub",
        isOnline = true,
        latencyMs = 74,
        supportedTypes = "Episodic Streams",
        quality = "1080p / 720p / 480p"
    ),
    AnikkuStreamCatalogue(
        id = "hianime",
        name = "HiAnime / Zoro (1080p Ultra)",
        version = "v1.3.2",
        language = "EN Multi",
        isOnline = true,
        latencyMs = 95,
        supportedTypes = "Sub & Dub Fast CDN",
        quality = "1080p Softsub"
    ),
    AnikkuStreamCatalogue(
        id = "pahe",
        name = "AnimePahe Lightweight",
        version = "v1.1.8",
        language = "EN Sub",
        isOnline = true,
        latencyMs = 112,
        supportedTypes = "Kwikt Small Files",
        quality = "720p / 1080p"
    ),
    AnikkuStreamCatalogue(
        id = "yuzono",
        name = "Yūzōnō Stream Vault",
        version = "v1.4.1",
        language = "Multi",
        isOnline = true,
        latencyMs = 105,
        supportedTypes = "Community Scrapers",
        quality = "Multi-Server CDN"
    )
)

@Composable
fun AnikkuAnimeSection(
    featuredAnime: Anime?,
    trendingAnime: List<Anime>,
    seasonalAnime: List<Anime>,
    topRatedAnime: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onBookmarkClick: (Anime) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AnikkuTab.SUGGESTIONS) }
    var activeSource by remember { mutableStateOf(DefaultAnikkuCatalogues.first()) }
    var showSourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AnikkuCanvasDark)
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // Anikku Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Anikku Brand Mark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnikkuAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = AnikkuCanvasDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "KUROTSUKI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = AnikkuTextPrimary,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "ANIKKU CORE • 黒月",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnikkuAccent,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Active Source Switcher Pill & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AnikkuSurface)
                        .border(1.dp, AnikkuBorder, RoundedCornerShape(20.dp))
                        .clickable { showSourceDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AnikkuEmerald)
                    )
                    Text(
                        text = activeSource.name.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnikkuAccent,
                            fontSize = 11.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = "Switch Catalogue",
                        tint = AnikkuTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AnikkuSurface)
                        .border(1.dp, AnikkuBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = AnikkuAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Sub-Tabs Bar: Suggestions, Popular, Seasonal, Updates, Catalogues
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AnikkuTab.values()) { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AnikkuAccent else AnikkuSurface)
                        .border(
                            1.dp,
                            if (isSelected) AnikkuAccent else AnikkuBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) AnikkuCanvasDark else AnikkuTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tab Content
        when (selectedTab) {
            AnikkuTab.SUGGESTIONS -> {
                AnikkuSuggestionsTab(
                    featuredAnime = featuredAnime,
                    trendingAnime = trendingAnime,
                    seasonalAnime = seasonalAnime,
                    onAnimeClick = onAnimeClick,
                    onWatchClick = onWatchClick,
                    onBookmarkClick = onBookmarkClick
                )
            }
            AnikkuTab.POPULAR -> {
                AnikkuPopularTab(
                    popularAnime = if (topRatedAnime.isNotEmpty()) topRatedAnime else trendingAnime,
                    onAnimeClick = onAnimeClick,
                    onWatchClick = onWatchClick,
                    onBookmarkClick = onBookmarkClick
                )
            }
            AnikkuTab.SEASONAL -> {
                AnikkuSeasonalTab(
                    seasonalAnime = seasonalAnime,
                    onAnimeClick = onAnimeClick,
                    onWatchClick = onWatchClick,
                    onBookmarkClick = onBookmarkClick
                )
            }
            AnikkuTab.UPDATES -> {
                AnikkuUpdatesTab(
                    animeList = trendingAnime,
                    onWatchClick = onWatchClick,
                    onAnimeClick = onAnimeClick
                )
            }
            AnikkuTab.SOURCES -> {
                AnikkuSourcesTab(
                    catalogues = DefaultAnikkuCatalogues,
                    activeCatalogue = activeSource,
                    onSelect = { activeSource = it }
                )
            }
        }
    }

    if (showSourceDialog) {
        AnikkuCatalogueSourceDialog(
            catalogues = DefaultAnikkuCatalogues,
            activeCatalogue = activeSource,
            onSelect = {
                activeSource = it
                showSourceDialog = false
            },
            onDismiss = { showSourceDialog = false }
        )
    }
}

/**
 * Anikku Suggestions Tab: Signature Auto-Recommendations Engine
 */
@Composable
private fun AnikkuSuggestionsTab(
    featuredAnime: Anime?,
    trendingAnime: List<Anime>,
    seasonalAnime: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onBookmarkClick: (Anime) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Anikku Panorama Cover / Hero Banner
        featuredAnime?.let { anime ->
            item(key = "anikku_hero_${anime.id}") {
                AnikkuPanoramaHero(
                    anime = anime,
                    onWatchClick = { onWatchClick(anime) },
                    onDetailsClick = { onAnimeClick(anime) },
                    onBookmarkClick = { onBookmarkClick(anime) }
                )
            }
        }

        // Section: Anikku Anime Suggestions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ANIME SUGGESTIONS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnikkuTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AnikkuAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ANIKKU AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AnikkuAccent,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = "Source-website recommendations & community picks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AnikkuTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trendingAnime, key = { "suggestion_${it.id}" }) { anime ->
                    AnikkuMediaCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) },
                        onBookmark = { onBookmarkClick(anime) },
                        modifier = Modifier.width(135.dp)
                    )
                }
            }
        }

        // Section: Trending Anime Masterpieces
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRENDING MASTERPIECES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AnikkuTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(seasonalAnime, key = { "trending_${it.id}" }) { anime ->
                    AnikkuMediaCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) },
                        onBookmark = { onBookmarkClick(anime) },
                        modifier = Modifier.width(135.dp)
                    )
                }
            }
        }
    }
}

/**
 * Anikku Panorama Hero Banner (Anikku's signature wide artwork layout)
 */
@Composable
private fun AnikkuPanoramaHero(
    anime: Anime,
    onWatchClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AnikkuSurface)
            .border(1.dp, AnikkuBorder, RoundedCornerShape(16.dp))
            .clickable { onDetailsClick() }
    ) {
        AsyncImage(
            model = anime.bannerImage ?: anime.coverImage.bestImageUrl(),
            contentDescription = anime.title.userPreferredTitle(),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x990A0D14),
                            AnikkuCanvasDark
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AnikkuAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.format.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnikkuCanvasDark,
                            fontSize = 10.sp
                        )
                    )
                }

                anime.averageScore?.let { score ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x99101522))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AnikkuAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", score / 10.0),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnikkuTextPrimary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                anime.nextAiringEpisode?.let { airing ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AnikkuEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = airing.humanTimeRemaining(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnikkuEmerald,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = anime.title.userPreferredTitle(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = AnikkuTextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${anime.studioName()} • ${anime.genres.take(3).joinToString(" • ")}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AnikkuTextSecondary,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWatchClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnikkuAccent,
                        contentColor = AnikkuCanvasDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Watch",
                        tint = AnikkuCanvasDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WATCH NOW • EP 1",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onBookmarkClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AnikkuSurface,
                        contentColor = if (anime.isInLibrary) AnikkuBloodMoon else AnikkuTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AnikkuBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = if (anime.isInLibrary) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (anime.isInLibrary) AnikkuBloodMoon else AnikkuTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Anikku Media Card (Standard 2:3 ratio, 14dp corners, with score and audio badges)
 */
@Composable
private fun AnikkuMediaCard(
    anime: Anime,
    onClick: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .background(AnikkuSurface)
                .border(1.dp, AnikkuBorder, RoundedCornerShape(14.dp))
        ) {
            AsyncImage(
                model = anime.coverImage.bestImageUrl(),
                contentDescription = anime.title.userPreferredTitle(),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD0A0D14))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = anime.format.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnikkuAccent,
                            fontSize = 9.sp
                        )
                    )
                }

                if (anime.averageScore != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xDD0A0D14))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AnikkuAmber,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = anime.displayScoreOutOfTen(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnikkuTextPrimary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Bottom Audio Indicator: SUB / DUB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xEE101522))
                    .border(0.5.dp, AnikkuAccent.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SUB • DUB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = AnikkuAccent,
                        fontSize = 8.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = anime.title.userPreferredTitle(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = AnikkuTextPrimary
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${anime.seasonYear ?: ""} • ${anime.episodes?.let { "$it eps" } ?: "Ongoing"}",
            style = MaterialTheme.typography.labelSmall.copy(
                color = AnikkuTextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Anikku Popular Tab (Full Grid View)
 */
@Composable
private fun AnikkuPopularTab(
    popularAnime: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onBookmarkClick: (Anime) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(popularAnime, key = { "grid_${it.id}" }) { anime ->
            AnikkuMediaCard(
                anime = anime,
                onClick = { onAnimeClick(anime) },
                onBookmark = { onBookmarkClick(anime) }
            )
        }
    }
}

/**
 * Anikku Seasonal Tab
 */
@Composable
private fun AnikkuSeasonalTab(
    seasonalAnime: List<Anime>,
    onAnimeClick: (Anime) -> Unit,
    onWatchClick: (Anime) -> Unit,
    onBookmarkClick: (Anime) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(seasonalAnime, key = { "season_${it.id}" }) { anime ->
            AnikkuMediaCard(
                anime = anime,
                onClick = { onAnimeClick(anime) },
                onBookmark = { onBookmarkClick(anime) }
            )
        }
    }
}

/**
 * Anikku Updates Tab (Latest Episode Releases Feed with Timestamps & Play Action)
 */
@Composable
private fun AnikkuUpdatesTab(
    animeList: List<Anime>,
    onWatchClick: (Anime) -> Unit,
    onAnimeClick: (Anime) -> Unit
) {
    val times = listOf("25m ago", "1h ago", "2h ago", "4h ago", "6h ago", "Today", "Yesterday")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "LATEST EPISODE RELEASES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AnikkuTextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
        }

        items(animeList) { anime ->
            val randomTime = times[Math.abs(anime.id.hashCode()) % times.size]
            val epNumber = (Math.abs(anime.id.hashCode()) % 24) + 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AnikkuSurface)
                    .border(1.dp, AnikkuBorder, RoundedCornerShape(14.dp))
                    .clickable { onAnimeClick(anime) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnikkuSurfaceElevated)
                ) {
                    AsyncImage(
                        model = anime.coverImage.bestImageUrl(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xDD0A0D14))
                            .padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = "EP $epNumber",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AnikkuAccent,
                                fontSize = 8.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = anime.title.userPreferredTitle(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AnikkuTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Episode $epNumber • Sub & Dub",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AnikkuAccentSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3310B981))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "1080p HD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AnikkuEmerald,
                                    fontSize = 9.sp
                                )
                            )
                        }

                        Text(
                            text = "• $randomTime",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AnikkuTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Quick Play Action
                IconButton(
                    onClick = { onWatchClick(anime) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AnikkuSurfaceElevated)
                        .border(1.dp, AnikkuBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play Episode",
                        tint = AnikkuAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Anikku Sources / Catalogues Tab
 */
@Composable
private fun AnikkuSourcesTab(
    catalogues: List<AnikkuStreamCatalogue>,
    activeCatalogue: AnikkuStreamCatalogue,
    onSelect: (AnikkuStreamCatalogue) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "STREAMING EXTENSION SOURCES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AnikkuTextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            )
        }

        items(catalogues) { catalogue ->
            val isCurrent = catalogue.id == activeCatalogue.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AnikkuSurface)
                    .border(
                        1.dp,
                        if (isCurrent) AnikkuAccent else AnikkuBorder,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(catalogue) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = catalogue.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) AnikkuAccent else AnikkuTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x331F273B))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = catalogue.version,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AnikkuTextSecondary,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${catalogue.supportedTypes} • ${catalogue.quality}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AnikkuTextSecondary,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AnikkuEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${catalogue.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AnikkuEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Text(
                            text = "• ${catalogue.language}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AnikkuTextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AnikkuAccent)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = AnikkuCanvasDark,
                                fontSize = 10.sp
                            )
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelect(catalogue) },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AnikkuBorder),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "Use",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AnikkuTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Catalogue Picker Dialog
 */
@Composable
private fun AnikkuCatalogueSourceDialog(
    catalogues: List<AnikkuStreamCatalogue>,
    activeCatalogue: AnikkuStreamCatalogue,
    onSelect: (AnikkuStreamCatalogue) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AnikkuSurfaceElevated)
                .border(1.dp, AnikkuBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "Select Streaming Catalogue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AnikkuTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Powered by Anikku Extension Engine",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AnikkuTextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            catalogues.forEach { catalogue ->
                val isSelected = catalogue.id == activeCatalogue.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AnikkuAccent.copy(alpha = 0.15f) else AnikkuSurface)
                        .border(
                            1.dp,
                            if (isSelected) AnikkuAccent else AnikkuBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(catalogue) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = catalogue.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AnikkuAccent else AnikkuTextPrimary
                            )
                        )
                        Text(
                            text = "${catalogue.version} • ${catalogue.latencyMs}ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AnikkuTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Selected",
                            tint = AnikkuAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
