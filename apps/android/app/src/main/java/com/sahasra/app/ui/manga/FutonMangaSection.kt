package com.sahasra.app.ui.manga

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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapVert
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
import coil.compose.AsyncImage
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MangaFormat
import com.sahasra.app.ui.theme.FutonEspresso
import com.sahasra.app.ui.theme.FutonMangaGold
import com.sahasra.app.ui.theme.FutonMangaGoldSecondary
import com.sahasra.app.ui.theme.FutonVelvetBlack
import com.sahasra.app.ui.theme.FutonWarmClay
import androidx.compose.ui.window.Dialog
import com.sahasra.app.ui.theme.FutonWarmElevated
import com.sahasra.app.ui.theme.FutonWarmIvory
import com.sahasra.app.ui.theme.FutonWarmObsidian
import com.sahasra.app.ui.theme.FutonWarmSurface

enum class FutonTab(val title: String) {
    POPULAR("Popular"),
    LATEST("Latest"),
    WEBTOONS("Webtoons"),
    SOURCES("Catalogues")
}

data class FutonCatalogue(
    val id: String,
    val name: String,
    val version: String,
    val language: String,
    val isOnline: Boolean,
    val latencyMs: Int,
    val supportedTypes: String
)

val DefaultFutonCatalogues = listOf(
    FutonCatalogue(
        id = "kotatsu",
        name = "Kotatsu Multi-Catalogue Mirror",
        version = "v2.1.0",
        language = "EN / Multi",
        isOnline = true,
        latencyMs = 74,
        supportedTypes = "Manga, Manhwa, Webtoons (1200+ Sources)"
    ),
    FutonCatalogue(
        id = "mangasee",
        name = "MangaSee Scans Mirror",
        version = "v1.2.0",
        language = "EN",
        isOnline = true,
        latencyMs = 86,
        supportedTypes = "High Quality Manga Releases"
    ),
    FutonCatalogue(
        id = "asura",
        name = "Asura Scans HQ",
        version = "v1.4.0",
        language = "EN",
        isOnline = true,
        latencyMs = 95,
        supportedTypes = "Action, Fantasy, Manhwa"
    ),
    FutonCatalogue(
        id = "webtoons",
        name = "Webtoons HQ (Naver/Line)",
        version = "v1.0.8",
        language = "EN",
        isOnline = true,
        latencyMs = 84,
        supportedTypes = "Korean Webtoons, Long Strip"
    ),
    FutonCatalogue(
        id = "flame",
        name = "Flame Comics",
        version = "v1.3.2",
        language = "EN",
        isOnline = true,
        latencyMs = 112,
        supportedTypes = "Manhua, Cultivation, Webtoons"
    ),
    FutonCatalogue(
        id = "bato",
        name = "Bato.to Community",
        version = "v1.3.1",
        language = "Multi",
        isOnline = true,
        latencyMs = 120,
        supportedTypes = "Scanlations, Romance, Shoujo"
    )
)

@Composable
fun FutonMangaSection(
    trendingManga: List<Manga>,
    popularManga: List<Manga>,
    onMangaClick: (Manga) -> Unit,
    onReadClick: (Manga) -> Unit,
    onBookmarkClick: (Manga) -> Unit,
    onSearchClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(FutonTab.POPULAR) }
    var activeSource by remember { mutableStateOf(DefaultFutonCatalogues.first()) }
    var showSourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FutonVelvetBlack)
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // Futon Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Futon Brand Title & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FutonMangaGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = FutonVelvetBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "FUTON",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = FutonWarmIvory,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "MANGA READER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FutonMangaGold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Active Source Pill & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Source Switcher Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FutonWarmSurface)
                        .border(1.dp, FutonWarmClay.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { showSourceDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (activeSource.isOnline) Color(0xFF4ADE80) else Color.Red)
                    )
                    Text(
                        text = activeSource.name.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FutonWarmIvory,
                            fontSize = 11.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = "Switch Source",
                        tint = FutonMangaGold,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FutonWarmSurface)
                        .border(1.dp, FutonWarmClay.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = FutonWarmIvory,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Futon Sub-Tabs (Popular, Latest, Webtoons, Catalogues)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FutonTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FutonMangaGold else FutonWarmSurface)
                        .border(
                            1.dp,
                            if (isSelected) FutonMangaGold else FutonWarmClay.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) FutonVelvetBlack else FutonWarmClay,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Tab Content
        when (selectedTab) {
            FutonTab.POPULAR -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Futon Hero Spotlight
                    val heroManga = trendingManga.firstOrNull()
                    if (heroManga != null) {
                        item(key = "futon_hero_${heroManga.id}") {
                            FutonHeroCard(
                                manga = heroManga,
                                onReadClick = onReadClick,
                                onDetailsClick = onMangaClick,
                                onBookmarkClick = onBookmarkClick
                            )
                        }
                    }

                    // Popular Manga Grid Section
                    item(key = "futon_popular_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Trending & Popular",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FutonWarmIvory
                                )
                            )
                            Text(
                                text = "${trendingManga.size} titles",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FutonWarmClay
                                )
                            )
                        }
                    }

                    // 2-column Grid of Media Cards
                    val itemsToDisplay = trendingManga.drop(1)
                    val chunked = itemsToDisplay.chunked(2)
                    items(chunked) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (manga in pair) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FutonMediaCard(
                                        manga = manga,
                                        onClick = { onMangaClick(manga) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }

            FutonTab.LATEST -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Latest Chapter Releases",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FutonWarmIvory
                                )
                            )
                            Text(
                                text = "Source: ${activeSource.name.split(" ").first()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = FutonMangaGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    items(popularManga) { manga ->
                        FutonLatestChapterRow(
                            manga = manga,
                            sourceName = activeSource.name.split(" ").first(),
                            onMangaClick = { onMangaClick(manga) },
                            onReadClick = { onReadClick(manga) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }

            FutonTab.WEBTOONS -> {
                val webtoonsList = remember(trendingManga, popularManga) {
                    val combined = (trendingManga + popularManga).distinctBy { it.id }
                    combined.filter { it.isManhwa() || it.isManhua() || it.format == MangaFormat.MANGA }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Webtoons & Manhwa",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FutonWarmIvory
                                )
                            )
                            Text(
                                text = "Continuous vertical scroll format for webcomics & Korean serializations",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = FutonWarmClay,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    val chunked = webtoonsList.chunked(2)
                    items(chunked) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (manga in pair) {
                                Box(modifier = Modifier.weight(1f)) {
                                    FutonMediaCard(
                                        manga = manga,
                                        badgeOverride = "WEBTOON",
                                        onClick = { onMangaClick(manga) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }

            FutonTab.SOURCES -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = "Installed Catalogue Parsers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = FutonWarmIvory
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Powered by Kotatsu and Keiyoushi open-source parser ecosystem",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = FutonWarmClay,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    items(DefaultFutonCatalogues) { catalogue ->
                        FutonCatalogueCard(
                            catalogue = catalogue,
                            isActive = activeSource.id == catalogue.id,
                            onSelect = {
                                activeSource = catalogue
                                selectedTab = FutonTab.POPULAR
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }
    }

    // Source Selector Dialog
    if (showSourceDialog) {
        Dialog(onDismissRequest = { showSourceDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FutonWarmObsidian)
                    .border(1.dp, FutonWarmClay.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Select Active Catalogue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FutonWarmIvory
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DefaultFutonCatalogues.forEach { cat ->
                        val isSelected = activeSource.id == cat.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FutonWarmElevated else Color.Transparent)
                                .clickable {
                                    activeSource = cat
                                    showSourceDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) FutonMangaGold else FutonWarmIvory
                                    )
                                )
                                Text(
                                    text = "${cat.version} • ${cat.latencyMs}ms",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = FutonWarmClay,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Active",
                                    tint = FutonMangaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FutonHeroCard(
    manga: Manga,
    onReadClick: (Manga) -> Unit,
    onDetailsClick: (Manga) -> Unit,
    onBookmarkClick: (Manga) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable { onDetailsClick(manga) }
    ) {
        // Banner Artwork
        val imageUrl = manga.bannerImage ?: manga.coverImage.bestImageUrl()
        AsyncImage(
            model = imageUrl,
            contentDescription = manga.title.userPreferredTitle(),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay to Velvet Black
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x330D0D0D),
                            Color(0xB30D0D0D),
                            FutonVelvetBlack
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Futon Spotlight Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FutonMangaGold)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "FUTON SPOTLIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = FutonVelvetBlack,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FutonWarmSurface)
                        .border(1.dp, FutonWarmClay.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = manga.formatBadgeText(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FutonWarmIvory,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = manga.title.userPreferredTitle(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FutonWarmIvory,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (manga.averageScore != null && manga.averageScore > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(FutonWarmSurface)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = FutonMangaGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = manga.displayScoreOutOfTen(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = FutonWarmIvory
                            )
                        )
                    }
                }

                Text(
                    text = "${manga.chapters ?: "?"} chapters",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = FutonWarmClay,
                        fontWeight = FontWeight.Medium
                    )
                )

                if (manga.genres.isNotEmpty()) {
                    Text(
                        text = "• ${manga.genres.first()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = FutonMangaGoldSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Flagship Manga Gold Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onReadClick(manga) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FutonMangaGold,
                        contentColor = FutonVelvetBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = "Read",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "START READING",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                OutlinedButton(
                    onClick = { onDetailsClick(manga) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FutonWarmSurface,
                        contentColor = FutonWarmIvory
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { onBookmarkClick(manga) },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FutonWarmSurface)
                        .border(1.dp, FutonWarmClay.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (manga.isInLibrary) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (manga.isInLibrary) FutonMangaGold else FutonWarmIvory
                    )
                }
            }
        }
    }
}

@Composable
fun FutonMediaCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    badgeOverride: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Strict 18:13 cover aspect ratio with 16dp rounded corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(13f / 18f)
                .clip(RoundedCornerShape(16.dp))
                .background(FutonWarmObsidian)
                .border(1.dp, FutonWarmClay.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = manga.coverImage.bestImageUrl(),
                contentDescription = manga.title.userPreferredTitle(),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Format Badge (Top Right)
            val badgeText = badgeOverride ?: manga.formatBadgeText()
            if (badgeText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD121110))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutonMangaGold
                        )
                    )
                }
            }

            // Score Badge (Top Left)
            if (manga.averageScore != null && manga.averageScore > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD0D0D0D))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = FutonMangaGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = manga.displayScoreOutOfTen(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutonWarmIvory
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title (Warm Ivory, 2 lines max with ellipsis)
        Text(
            text = manga.title.userPreferredTitle(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = FutonWarmIvory,
                lineHeight = 18.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Subtitle (Warm Clay)
        Text(
            text = "${manga.chapters ?: "?"} chapters • Futon Engine",
            style = MaterialTheme.typography.bodySmall.copy(
                color = FutonWarmClay,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FutonLatestChapterRow(
    manga: Manga,
    sourceName: String,
    onMangaClick: () -> Unit,
    onReadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(FutonWarmSurface)
            .border(1.dp, FutonWarmClay.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onMangaClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        AsyncImage(
            model = manga.coverImage.bestImageUrl(),
            contentDescription = manga.title.userPreferredTitle(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 46.dp, height = 62.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FutonWarmObsidian)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Latest Chapter Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = manga.title.userPreferredTitle(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = FutonWarmIvory
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Chapter ${manga.chapters ?: 1} • Just now",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = FutonMangaGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            )

            Text(
                text = "$sourceName • English Scanlation",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = FutonWarmClay,
                    fontSize = 11.sp
                )
            )
        }

        IconButton(
            onClick = onReadClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FutonEspresso.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = "Read",
                tint = FutonMangaGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FutonCatalogueCard(
    catalogue: FutonCatalogue,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) FutonWarmElevated else FutonWarmSurface)
            .border(
                1.dp,
                if (isActive) FutonMangaGold else FutonWarmClay.copy(alpha = 0.25f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = catalogue.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) FutonMangaGold else FutonWarmIvory
                    )
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(FutonMangaGold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = FutonVelvetBlack,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${catalogue.version} • ${catalogue.language} • ${catalogue.supportedTypes}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = FutonWarmClay,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (catalogue.isOnline) Color(0xFF4ADE80) else Color.Red)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (catalogue.isOnline) "Operational • ${catalogue.latencyMs}ms" else "Offline",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (catalogue.isOnline) Color(0xFF4ADE80) else Color.Red,
                        fontSize = 10.sp
                    )
                )
            }
        }

        OutlinedButton(
            onClick = onSelect,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isActive) FutonMangaGold else Color.Transparent,
                contentColor = if (isActive) FutonVelvetBlack else FutonWarmIvory
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isActive) "Current" else "Switch",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
