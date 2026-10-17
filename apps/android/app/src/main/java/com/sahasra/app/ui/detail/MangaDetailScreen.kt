package com.sahasra.app.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.domain.model.MangaChapter
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.components.HeroSkeleton
import com.sahasra.app.ui.theme.FutonEspresso
import com.sahasra.app.ui.theme.FutonMangaGold
import com.sahasra.app.ui.theme.FutonMangaGoldSecondary
import com.sahasra.app.ui.theme.FutonVelvetBlack
import com.sahasra.app.ui.theme.FutonWarmClay
import com.sahasra.app.ui.theme.FutonWarmElevated
import com.sahasra.app.ui.theme.FutonWarmIvory
import com.sahasra.app.ui.theme.FutonWarmObsidian
import com.sahasra.app.ui.theme.FutonWarmSurface
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
import kotlinx.coroutines.launch

@Composable
fun MangaDetailScreen(
    mangaId: Int,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    onReadChapter: (Manga, MangaChapter) -> Unit
) {
    var manga by remember { mutableStateOf<Manga?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var selectedSource by remember { mutableStateOf("MangaDex HQ") }
    val availableSources = listOf("MangaDex HQ", "Kotatsu Mirror", "Webtoons")
    var chapterSearchQuery by remember { mutableStateOf("") }
    var isSortAscending by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.getMangaDetails(mangaId)
            if (result.isSuccess) {
                manga = result.getOrNull()
                isLoading = false
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load manga"
                isLoading = false
            }
        }
    }

    LaunchedEffect(mangaId) {
        loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FutonVelvetBlack)
    ) {
        when {
            isLoading && manga == null -> {
                HeroSkeleton()
            }
            errorMessage != null && manga == null -> {
                ErrorState(
                    message = errorMessage ?: "Failed to load manga",
                    onRetry = { loadData() }
                )
            }
            manga != null -> {
                val item = manga!!

                val filteredChapters = remember(item.chaptersList, chapterSearchQuery, isSortAscending) {
                    val filtered = if (chapterSearchQuery.isBlank()) {
                        item.chaptersList
                    } else {
                        item.chaptersList.filter {
                            (it.title?.contains(chapterSearchQuery, ignoreCase = true) == true) ||
                            it.number.toString().contains(chapterSearchQuery)
                        }
                    }
                    if (isSortAscending) {
                        filtered.sortedBy { it.number }
                    } else {
                        filtered.sortedByDescending { it.number }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Header Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            AsyncImage(
                                model = item.bannerImage ?: item.coverImage.bestImageUrl(),
                                contentDescription = item.title.userPreferredTitle(),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x660D0D0D),
                                                Color(0xB30D0D0D),
                                                FutonVelvetBlack
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Poster Overlap & Details
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-50).dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .aspectRatio(13f / 18f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(FutonWarmObsidian)
                                    .border(1.5.dp, FutonWarmClay.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = item.coverImage.bestImageUrl(),
                                    contentDescription = item.title.userPreferredTitle(),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title.userPreferredTitle(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FutonWarmIvory
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.authorNames(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = FutonMangaGold,
                                        fontWeight = FontWeight.Medium
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (item.averageScore != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                tint = FutonMangaGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.displayScoreOutOfTen(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = FutonWarmIvory
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "• ${item.chapters ?: "?"} chapters",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = FutonWarmClay
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FutonEspresso.copy(alpha = 0.45f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.formatBadgeText(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = FutonMangaGold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Buttons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-35).dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val firstChapter = item.chaptersList.firstOrNull() ?: MangaChapter(
                                        id = "${item.id}_1",
                                        number = 1f,
                                        title = "Chapter 1"
                                    )
                                    onReadChapter(item, firstChapter)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FutonMangaGold,
                                    contentColor = FutonVelvetBlack
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = "Read",
                                    tint = FutonVelvetBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "START READING CH. 1",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FutonVelvetBlack
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        repository.toggleSaveManga(item)
                                        manga = item.copy(isInLibrary = !item.isInLibrary)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = FutonWarmSurface,
                                    contentColor = if (item.isInLibrary) FutonMangaGold else FutonWarmIvory
                                ),
                                border = BorderStroke(1.dp, FutonWarmClay.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isInLibrary) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (item.isInLibrary) FutonMangaGold else FutonWarmIvory,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (item.isInLibrary) "In Library" else "Save",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.isInLibrary) FutonMangaGold else FutonWarmIvory
                                )
                            }
                        }
                    }

                    // Synopsis
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-20).dp)
                        ) {
                            Text(
                                text = "Synopsis",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FutonWarmIvory
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.cleanDescription(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = FutonWarmClay,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }

                    // Source Selector
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .offset(y = (-10).dp)
                        ) {
                            Text(
                                text = "Catalogue Source",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FutonWarmClay
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableSources.forEach { source ->
                                    val isSelected = selectedSource == source
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) FutonMangaGold else FutonWarmSurface)
                                            .border(
                                                1.dp,
                                                if (isSelected) FutonMangaGold else FutonWarmClay.copy(alpha = 0.25f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedSource = source }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = source,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) FutonVelvetBlack else FutonWarmIvory,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chapters Header & Search
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Chapters (${filteredChapters.size})",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = FutonWarmIvory
                                    )
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FutonWarmSurface)
                                        .border(1.dp, FutonWarmClay.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                        .clickable { isSortAscending = !isSortAscending }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSortAscending) "Oldest First" else "Newest First",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = FutonMangaGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = chapterSearchQuery,
                                onValueChange = { chapterSearchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search chapters (e.g. 100, Prologue)...",
                                        color = FutonWarmClay,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = FutonWarmClay,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = FutonWarmSurface,
                                    unfocusedContainerColor = FutonWarmSurface,
                                    focusedBorderColor = FutonMangaGold,
                                    unfocusedBorderColor = FutonWarmClay.copy(alpha = 0.25f),
                                    focusedTextColor = FutonWarmIvory,
                                    unfocusedTextColor = FutonWarmIvory
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(filteredChapters, key = { it.id }) { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(FutonWarmSurface)
                                .border(1.dp, FutonWarmClay.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .clickable { onReadChapter(item, chapter) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chapter.title ?: "Chapter ${chapter.number}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = FutonWarmIvory
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${chapter.pages ?: 20} pages",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = FutonWarmClay,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = "• EN",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = FutonMangaGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "• $selectedSource",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = FutonWarmClay,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onReadChapter(item, chapter) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FutonWarmElevated)
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

                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }

                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0D0D0D))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = FutonWarmIvory,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
