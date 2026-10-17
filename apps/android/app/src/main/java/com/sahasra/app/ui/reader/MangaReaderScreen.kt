package com.sahasra.app.ui.reader

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Futon-Standard Reader Layout Modes
 */
enum class ReaderMode {
    WEBTOON,    // Continuous seamless vertical strip (Manhwa standard)
    PAGED_RTL,  // Right-to-Left paged reader (Traditional Japanese Manga standard)
    PAGED_LTR   // Left-to-Right paged reader (Western comics standard)
}

@Composable
fun MangaReaderScreen(
    mangaId: Int,
    chapterNumber: Float,
    repository: MangaRepository,
    onBackClick: () -> Unit,
    onChapterChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var manga by remember { mutableStateOf<Manga?>(null) }
    var chapterPages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isPagesLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var readerMode by remember { mutableStateOf(ReaderMode.WEBTOON) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var modeNotice by remember { mutableStateOf<String?>(null) }

    val totalPages = chapterPages.size

    // Scroll states for Webtoon and Paged modes
    val listState = rememberLazyListState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (totalPages > 0) totalPages else 1 }
    )

    // Current page tracking
    val currentPageIndex by remember(readerMode, totalPages) {
        derivedStateOf {
            if (totalPages == 0) 0
            else when (readerMode) {
                ReaderMode.WEBTOON -> listState.firstVisibleItemIndex.coerceIn(0, totalPages - 1)
                ReaderMode.PAGED_RTL, ReaderMode.PAGED_LTR -> pagerState.currentPage.coerceIn(0, totalPages - 1)
            }
        }
    }

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    LaunchedEffect(currentPageIndex) {
        if (!isDraggingSlider) {
            sliderPosition = currentPageIndex.toFloat()
        }
    }

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
                    // Default to Webtoon mode for Korean Manhwa / Webtoons
                    if (item.isManhwa()) {
                        readerMode = ReaderMode.WEBTOON
                    }

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

    // Notice timeout
    LaunchedEffect(modeNotice) {
        if (modeNotice != null) {
            delay(1500)
            modeNotice = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060709))
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
                            CircularProgressIndicator(color = SahasraAccent, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Fetching genuine chapter pages from MangaDex...",
                                color = SahasraTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (chapterPages.isEmpty()) {
                    // Unavailability State
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
                                text = "Chapter Not Available",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "No public scanlation manifest is indexed for ${item.title.userPreferredTitle()} Chapter ${chapterNumber.toInt()} on open online catalogues.",
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
                                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
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
                } else {
                    // MAIN READER CONTAINER
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { areControlsVisible = !areControlsVisible }
                                )
                            }
                    ) {
                        when (readerMode) {
                            ReaderMode.WEBTOON -> {
                                // 1. WEBTOON MODE (Continuous Vertical Strip with Zero Gap)
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(chapterPages.size, key = { it }) { pageIdx ->
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(chapterPages[pageIdx])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Page ${pageIdx + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF060709))
                                        )
                                    }

                                    item {
                                        ChapterEndSection(
                                            chapterNumber = chapterNumber,
                                            totalChapters = item.chapters ?: 50,
                                            onNext = { onChapterChange(chapterNumber + 1) },
                                            onPrev = { onChapterChange((chapterNumber - 1).coerceAtLeast(1f)) },
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(100.dp))
                                    }
                                }
                            }

                            ReaderMode.PAGED_RTL -> {
                                // 2. PAGED RTL (Traditional Japanese Manga Right-to-Left)
                                HorizontalPager(
                                    state = pagerState,
                                    reverseLayout = true,
                                    modifier = Modifier.fillMaxSize()
                                ) { pageIdx ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(chapterPages[pageIdx])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Page ${pageIdx + 1}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            ReaderMode.PAGED_LTR -> {
                                // 3. PAGED LTR (Western Left-to-Right)
                                HorizontalPager(
                                    state = pagerState,
                                    reverseLayout = false,
                                    modifier = Modifier.fillMaxSize()
                                ) { pageIdx ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(chapterPages[pageIdx])
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Page ${pageIdx + 1}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Mode Change Notification Pill
                    modeNotice?.let { notice ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xDD111318))
                                .border(1.dp, SahasraAccent, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = notice,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = SahasraTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // TOP OVERLAY BAR (HUD)
                    AnimatedVisibility(
                        visible = areControlsVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xEE111318))
                                .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = SahasraTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title.userPreferredTitle(),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SahasraTextPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Chapter ${chapterNumber.toInt()}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SahasraAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = " • MangaDex HQ",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SahasraTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Reader Mode Switcher Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x338585F7))
                                    .clickable {
                                        readerMode = when (readerMode) {
                                            ReaderMode.WEBTOON -> {
                                                modeNotice = "Switched to Paged (RTL) Mode"
                                                ReaderMode.PAGED_RTL
                                            }
                                            ReaderMode.PAGED_RTL -> {
                                                modeNotice = "Switched to Paged (LTR) Mode"
                                                ReaderMode.PAGED_LTR
                                            }
                                            ReaderMode.PAGED_LTR -> {
                                                modeNotice = "Switched to Webtoon Mode"
                                                ReaderMode.WEBTOON
                                            }
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (readerMode) {
                                            ReaderMode.WEBTOON -> Icons.Rounded.SwapVert
                                            ReaderMode.PAGED_RTL -> Icons.AutoMirrored.Rounded.MenuBook
                                            ReaderMode.PAGED_LTR -> Icons.Rounded.ViewCarousel
                                        },
                                        contentDescription = "Reader Mode",
                                        tint = SahasraAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (readerMode) {
                                            ReaderMode.WEBTOON -> "Webtoon"
                                            ReaderMode.PAGED_RTL -> "RTL"
                                            ReaderMode.PAGED_LTR -> "LTR"
                                        },
                                        color = SahasraAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // BOTTOM OVERLAY BAR (Seekbar, Page count & Chapter nav)
                    AnimatedVisibility(
                        visible = areControlsVisible && totalPages > 0,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xEE111318))
                                .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            // Top Row: Page indicator pill & Chapter jump buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Chapter Button
                                IconButton(
                                    onClick = {
                                        if (chapterNumber > 1f) {
                                            onChapterChange((chapterNumber - 1).coerceAtLeast(1f))
                                        }
                                    },
                                    enabled = chapterNumber > 1f,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronLeft,
                                        contentDescription = "Prev Chapter",
                                        tint = if (chapterNumber > 1f) SahasraAccent else SahasraTextSecondary.copy(alpha = 0.3f)
                                    )
                                }

                                // Current Page Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x338585F7))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Page ${currentPageIndex + 1} of $totalPages",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = SahasraAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                // Next Chapter Button
                                IconButton(
                                    onClick = { onChapterChange(chapterNumber + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = "Next Chapter",
                                        tint = SahasraAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Scrubbable Page Slider Seekbar
                            if (totalPages > 1) {
                                Slider(
                                    value = sliderPosition,
                                    onValueChange = { newVal ->
                                        isDraggingSlider = true
                                        sliderPosition = newVal
                                    },
                                    onValueChangeFinished = {
                                        isDraggingSlider = false
                                        val targetPage = sliderPosition.toInt().coerceIn(0, totalPages - 1)
                                        scope.launch {
                                            when (readerMode) {
                                                ReaderMode.WEBTOON -> listState.scrollToItem(targetPage)
                                                ReaderMode.PAGED_RTL, ReaderMode.PAGED_LTR -> pagerState.scrollToPage(targetPage)
                                            }
                                        }
                                    },
                                    valueRange = 0f..(totalPages - 1).toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = SahasraAccent,
                                        activeTrackColor = SahasraAccent,
                                        inactiveTrackColor = SahasraBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
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
