package com.sahasra.app.ui.reader

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.sahasra.app.ui.theme.SahasraElevated
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
    val context = LocalContext.current
    var manga by remember { mutableStateOf<Manga?>(null) }
    var chapterPages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isPagesLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var readerMode by remember { mutableStateOf(ReaderMode.VERTICAL) }
    var areControlsVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val totalPages = chapterPages.size
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (totalPages > 0) totalPages else 1 }
    )

    fun loadContent() {
        scope.launch {
            isLoading = true
            isPagesLoading = true
            errorMessage = null

            val mangaResult = repository.getMangaDetails(mangaId)
            if (mangaResult.isSuccess) {
                val item = mangaResult.getOrNull()
                manga = item
                isLoading = false

                if (item != null) {
                    val title = item.title.userPreferredTitle()
                    val pagesResult = repository.getChapterPages(title, chapterNumber)
                    if (pagesResult.isSuccess) {
                        chapterPages = pagesResult.getOrDefault(emptyList())
                    } else {
                        chapterPages = emptyList()
                    }
                }
                isPagesLoading = false
            } else {
                errorMessage = mangaResult.exceptionOrNull()?.localizedMessage ?: "Failed to load manga"
                isLoading = false
                isPagesLoading = false
            }
        }
    }

    LaunchedEffect(mangaId, chapterNumber) {
        loadContent()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SahasraAccent)
                }
            }

            errorMessage != null && manga == null -> {
                ErrorState(
                    message = errorMessage ?: "Failed to load manga reader",
                    onRetry = { loadContent() }
                )
            }

            manga != null -> {
                val item = manga!!

                if (isPagesLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SahasraAccent, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Fetching genuine chapter pages from MangaDex...",
                                color = SahasraTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (chapterPages.isEmpty()) {
                    // Honest Unavailability State (Never fake pages or repeat covers!)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SahasraSurface)
                                .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(SahasraCrimson.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "Not Available",
                                    tint = SahasraCrimson,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Scanlation Not Indexed",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "No public scanlation manifest is available for ${item.title.userPreferredTitle()} Chapter ${chapterNumber.toInt()} on open community networks.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SahasraTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (item.externalLinks.isNotEmpty()) {
                                Text(
                                    text = "Read on Licensed Platforms:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SahasraAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                item.externalLinks.take(3).forEach { link ->
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(text = link.site, color = SahasraTextPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.OpenInNew,
                                            contentDescription = null,
                                            tint = SahasraAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SahasraAccent,
                                    contentColor = SahasraBg
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Back to Details", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (readerMode == ReaderMode.VERTICAL) {
                    // Genuine Vertical Webtoon Scroll
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { areControlsVisible = !areControlsVisible }
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }

                        items(chapterPages.size) { pageIdx ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SahasraCardBg)
                            ) {
                                AsyncImage(
                                    model = chapterPages[pageIdx],
                                    contentDescription = "Page ${pageIdx + 1}",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xCC08090B))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pageIdx + 1} / ${chapterPages.size}",
                                        color = SahasraTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        item {
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
                    // Genuine Horizontal Pager Mode
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
                                AsyncImage(
                                    model = chapterPages[pageIdx],
                                    contentDescription = "Page ${pageIdx + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xCC08090B))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Page ${pageIdx + 1} of ${chapterPages.size}",
                                        color = SahasraAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
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
                            .padding(12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEE111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = SahasraTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

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
                                text = "Chapter ${chapterNumber.toInt()} • ${if (chapterPages.isNotEmpty()) "${chapterPages.size} Pages" else "MangaDex"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SahasraAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Toggle Mode Button (Vertical vs Horizontal)
                        IconButton(
                            onClick = {
                                readerMode = if (readerMode == ReaderMode.VERTICAL) {
                                    ReaderMode.HORIZONTAL
                                } else {
                                    ReaderMode.VERTICAL
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (readerMode == ReaderMode.VERTICAL) {
                                    Icons.Rounded.ViewCarousel
                                } else {
                                    Icons.Rounded.SwapVert
                                },
                                contentDescription = "Switch Reader Mode",
                                tint = SahasraAccent
                            )
                        }
                    }
                }
            }
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
