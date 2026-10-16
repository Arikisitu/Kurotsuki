package com.sahasra.app.ui.reader

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahasra.app.data.repository.MangaRepository
import com.sahasra.app.domain.model.Manga
import com.sahasra.app.ui.components.ErrorState
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraCardBg
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary
import kotlinx.coroutines.launch

enum class ReaderMode {
    VERTICAL,
    HORIZONTAL
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MangaReaderScreen(
    mangaId: Int,
    chapterNumber: Float,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    onChapterChange: (Float) -> Unit
) {
    var manga by remember { mutableStateOf<Manga?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var readerMode by remember { mutableStateOf(ReaderMode.VERTICAL) }
    var areControlsVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val totalPages = 18
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { totalPages })

    fun loadManga() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.getMangaDetails(mangaId)
            if (result.isSuccess) {
                manga = result.getOrNull()
                isLoading = false
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to load chapter"
                isLoading = false
            }
        }
    }

    LaunchedEffect(mangaId) {
        loadManga()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
    ) {
        when {
            isLoading && manga == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SahasraAccent)
                }
            }
            errorMessage != null && manga == null -> {
                ErrorState(
                    message = errorMessage ?: "Failed to load manga reader",
                    onRetry = { loadManga() }
                )
            }
            manga != null -> {
                val item = manga!!
                val coverUrl = item.coverImage.bestImageUrl()

                // Reader Content Canvas
                if (readerMode == ReaderMode.VERTICAL) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { areControlsVisible = !areControlsVisible }
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }

                        items(totalPages) { pageIdx ->
                            MangaPagePlaceholder(
                                mangaTitle = item.title.userPreferredTitle(),
                                chapter = chapterNumber,
                                page = pageIdx + 1,
                                totalPages = totalPages,
                                coverUrl = coverUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        item {
                            // Chapter Navigation Footer
                            ChapterEndSection(
                                chapterNumber = chapterNumber,
                                totalChapters = item.chapters ?: 30,
                                onNext = { onChapterChange(chapterNumber + 1) },
                                onPrev = { onChapterChange((chapterNumber - 1).coerceAtLeast(1f)) },
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                } else {
                    // Horizontal Pager Mode
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { areControlsVisible = !areControlsVisible }
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIdx ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                MangaPagePlaceholder(
                                    mangaTitle = item.title.userPreferredTitle(),
                                    chapter = chapterNumber,
                                    page = pageIdx + 1,
                                    totalPages = totalPages,
                                    coverUrl = coverUrl,
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .aspectRatio(0.68f)
                                )
                            }
                        }
                    }
                }

                // Top Header Overlay Bar
                if (areControlsVisible) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEE111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SahasraSurface)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = SahasraTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title.userPreferredTitle(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Chapter ${chapterNumber.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SahasraAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Mode Switcher Toggle
                        IconButton(
                            onClick = {
                                readerMode = if (readerMode == ReaderMode.VERTICAL) {
                                    ReaderMode.HORIZONTAL
                                } else {
                                    ReaderMode.VERTICAL
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SahasraSurface)
                        ) {
                            Icon(
                                imageVector = if (readerMode == ReaderMode.VERTICAL) {
                                    Icons.Rounded.ViewCarousel
                                } else {
                                    Icons.Rounded.SwapVert
                                },
                                contentDescription = "Toggle Reading Mode",
                                tint = SahasraAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Bottom Floating Progress Pill
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xF0111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { onChapterChange((chapterNumber - 1).coerceAtLeast(1f)) },
                            enabled = chapterNumber > 1f,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Prev Chapter",
                                tint = if (chapterNumber > 1f) SahasraTextPrimary else SahasraTextSecondary.copy(alpha = 0.4f)
                            )
                        }

                        Text(
                            text = if (readerMode == ReaderMode.HORIZONTAL) {
                                "Pg ${pagerState.currentPage + 1} / $totalPages"
                            } else {
                                "Ch ${chapterNumber.toInt()}"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SahasraTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        IconButton(
                            onClick = { onChapterChange(chapterNumber + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Next Chapter",
                                tint = SahasraTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MangaPagePlaceholder(
    mangaTitle: String,
    chapter: Float,
    page: Int,
    totalPages: Int,
    coverUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SahasraCardBg)
            .border(1.dp, SahasraBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, SahasraAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = mangaTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mangaTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SahasraTextPrimary,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Chapter ${chapter.toInt()} — Page $page of $totalPages",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SahasraAccent,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SAHASRA Inbuilt Manga Reader",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SahasraTextSecondary,
                    fontSize = 11.sp
                )
            )
        }

        // Page Indicator Corner Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xCC08090B))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$page / $totalPages",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SahasraTextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun ChapterEndSection(
    chapterNumber: Float,
    totalChapters: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SahasraSurface)
            .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "End of Chapter ${chapterNumber.toInt()}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SahasraTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Progress saved to your personal library",
            style = MaterialTheme.typography.bodySmall.copy(color = SahasraTextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chapterNumber > 1f) {
                Button(
                    onClick = onPrev,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SahasraCardBg,
                        contentColor = SahasraTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Prev Chapter")
                }
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SahasraAccent,
                    contentColor = SahasraBg
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Next Chapter", fontWeight = FontWeight.Bold)
            }
        }
    }
}
