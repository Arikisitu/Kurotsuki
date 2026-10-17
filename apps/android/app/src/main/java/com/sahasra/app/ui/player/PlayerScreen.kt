package com.sahasra.app.ui.player

import android.content.Context
import android.net.Uri
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.sahasra.app.data.repository.StreamRepository
import com.sahasra.app.domain.model.StreamSource
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    title: String,
    streamUrl: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val streamRepo = remember { StreamRepository() }
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("sahasra_prefs", Context.MODE_PRIVATE)
    }

    // Language Preferences (Strictly Hindi, English, Japanese)
    var selectedLanguage by remember {
        mutableStateOf(sharedPrefs.getString("playback_language", "en") ?: "en")
    }
    var selectedAudioType by remember {
        mutableStateOf(sharedPrefs.getString("playback_audio_type", "dub") ?: "dub")
    }

    // Stream Aggregator State
    var activeSource by remember { mutableStateOf<StreamSource?>(null) }
    var fallbackQueue by remember { mutableStateOf<List<StreamSource>>(emptyList()) }
    var allWorkingSources by remember { mutableStateOf<List<StreamSource>>(emptyList()) }
    var isResolving by remember { mutableStateOf(true) }
    var failoverNotice by remember { mutableStateOf<String?>(null) }

    // Dialog & Dropdown visibility
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var showResolutionDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    var selectedResolution by remember { mutableStateOf("Auto (1080p)") }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var subtitlesEnabled by remember { mutableStateOf(true) }

    // Player state
    var isPlaying by remember { mutableStateOf(false) }
    var isPlaybackReady by remember { mutableStateOf(false) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(true) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // Double-tap visual feedback
    var doubleTapAction by remember { mutableStateOf<String?>(null) }

    // High performance tuned ExoPlayer instance with custom LoadControl
    val exoPlayer = remember(context) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                15000, // Min buffer (15s)
                50000, // Max buffer (50s)
                1000,  // Buffer for playback (1s fast start)
                2000   // Buffer for playback after rebuffer (2s)
            )
            .build()
        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    fun prepareSource(source: StreamSource, startPosition: Long = 0L) {
        playbackError = null
        isBuffering = true
        isPlaybackReady = false
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(source.url))
            exoPlayer.setMediaItem(mediaItem)
            if (startPosition > 0L) {
                exoPlayer.seekTo(startPosition)
            }
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            playbackError = "Could not initialize stream: ${e.message}"
            isBuffering = false
        }
    }

    // Seamless failover logic to next fastest stream
    fun triggerAutoFailover() {
        if (fallbackQueue.isNotEmpty()) {
            val nextSource = fallbackQueue.first()
            val remaining = if (fallbackQueue.size > 1) fallbackQueue.subList(1, fallbackQueue.size) else emptyList()
            val savedPos = exoPlayer.currentPosition.coerceAtLeast(0L)

            activeSource = nextSource
            fallbackQueue = remaining
            failoverNotice = "Switched to backup: ${nextSource.providerName} (${nextSource.ttfbMs}ms)"

            prepareSource(nextSource, savedPos)
        } else {
            playbackError = "All streams for this episode are currently unavailable."
        }
    }

    // Initial stream resolution
    fun resolveAndPlay(lang: String, audioType: String) {
        isResolving = true
        playbackError = null
        scope.launch {
            try {
                val res = streamRepo.resolveStreams(
                    title = title,
                    episode = 1,
                    preferredLanguage = lang,
                    preferredAudioType = audioType
                )
                activeSource = res.primary
                fallbackQueue = res.fallbackQueue
                allWorkingSources = res.allWorkingSources
                prepareSource(res.primary, 0L)
            } catch (e: Exception) {
                playbackError = "Failed to resolve sources: ${e.message}"
            } finally {
                isResolving = false
            }
        }
    }

    LaunchedEffect(title, selectedLanguage, selectedAudioType) {
        if (!streamUrl.isNullOrBlank()) {
            val directSource = StreamSource(
                id = "custom-direct",
                providerId = "direct",
                providerName = "Direct Stream",
                url = streamUrl,
                audioLanguage = selectedLanguage
            )
            activeSource = directSource
            prepareSource(directSource, 0L)
            isResolving = false
        } else {
            resolveAndPlay(selectedLanguage, selectedAudioType)
        }
    }

    // ExoPlayer event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    isPlaybackReady = true
                    playbackError = null
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                isPlaybackReady = false
                isPlaying = false
                // Seamlessly trigger auto-failover instead of crashing or showing dead screen
                triggerAutoFailover()
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Auto-update playback progress every 500ms
    LaunchedEffect(isPlaying, isUserSeeking) {
        while (isPlaying && !isUserSeeking) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDuration = exoPlayer.duration.coerceAtLeast(0L)
            bufferedPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
            delay(500)
        }
    }

    // Buffer stall detection: failover if buffering continuously for > 3.5s
    LaunchedEffect(isBuffering, isPlaying) {
        if (isBuffering && isPlaying) {
            delay(3500)
            if (isBuffering && isPlaying && fallbackQueue.isNotEmpty()) {
                triggerAutoFailover()
            }
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Clear failover banner after 4 seconds
    LaunchedEffect(failoverNotice) {
        if (failoverNotice != null) {
            delay(4000)
            failoverNotice = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 2) {
                            val target = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(target)
                            currentPosition = target
                            doubleTapAction = "-10s"
                        } else {
                            val target = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(target)
                            currentPosition = target
                            doubleTapAction = "+10s"
                        }
                    },
                    onTap = {
                        areControlsVisible = !areControlsVisible
                    }
                )
            }
    ) {
        // Media3 ExoPlayer View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading or Resolving Indicator
        if ((isResolving || isBuffering) && playbackError == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = SahasraAccent,
                    modifier = Modifier.size(54.dp),
                    strokeWidth = 4.dp
                )
                if (isResolving) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Resolving fastest streams & testing latency...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SahasraTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Double Tap Visual Ripple Feedback
        doubleTapAction?.let { action ->
            LaunchedEffect(action) {
                delay(600)
                doubleTapAction = null
            }
            Box(
                modifier = Modifier
                    .align(if (action == "-10s") Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 48.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x9908090B)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Failover Notification Chip
        failoverNotice?.let { notice ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEE111318))
                    .border(1.dp, SahasraAccent, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SahasraTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Error State Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE608090B))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Error",
                        tint = SahasraCrimson,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Stream Playback Error",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SahasraTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = playbackError ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SahasraTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { resolveAndPlay(selectedLanguage, selectedAudioType) },
                            colors = ButtonDefaults.buttonColors(containerColor = SahasraAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = SahasraBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Fastest", color = SahasraBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        if (fallbackQueue.isNotEmpty()) {
                            Button(
                                onClick = { triggerAutoFailover() },
                                colors = ButtonDefaults.buttonColors(containerColor = SahasraElevated),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Next Source", color = SahasraTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // In-Player YouTube-Style Overlay Controls
        AnimatedVisibility(
            visible = areControlsVisible && playbackError == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xB3000000),
                                Color.Transparent,
                                Color(0xE6000000)
                            )
                        )
                    )
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = SahasraTextPrimary
                            )
                        }
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SahasraTextPrimary
                                ),
                                maxLines = 1
                            )
                            activeSource?.let { src ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${src.providerName} • ${src.quality}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SahasraAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "${src.ttfbMs}ms",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF4ADE80),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Top Right Actions: Language, Sources, Speed, Quality
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Language Selector
                        IconButton(onClick = { showLanguageDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Translate,
                                contentDescription = "Language",
                                tint = SahasraAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Sources Switcher
                        IconButton(onClick = { showSourceDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = "Sources",
                                tint = SahasraTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Speed Switcher
                        IconButton(onClick = { showSpeedDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = "Speed",
                                tint = SahasraTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Quality Switcher
                        IconButton(onClick = { showResolutionDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = "Quality",
                                tint = SahasraTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Center Controls: Rewind 10s, Play/Pause, Forward 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x66191C22))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = SahasraTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SahasraAccent)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SahasraBg,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x66191C22))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = SahasraTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Timeline Scrubber and Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(if (isUserSeeking) (seekProgress * totalDuration).toLong() else currentPosition),
                            style = MaterialTheme.typography.labelSmall.copy(color = SahasraTextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = formatDuration(totalDuration),
                            style = MaterialTheme.typography.labelSmall.copy(color = SahasraTextSecondary)
                        )
                    }

                    Slider(
                        value = if (isUserSeeking) seekProgress else {
                            if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f
                        },
                        onValueChange = {
                            isUserSeeking = true
                            seekProgress = it
                        },
                        onValueChangeFinished = {
                            val targetPos = (seekProgress * totalDuration).toLong()
                            exoPlayer.seekTo(targetPos)
                            currentPosition = targetPos
                            isUserSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = SahasraAccent,
                            activeTrackColor = SahasraAccent,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Language & Dub Selection Dialog (Strictly Hindi, English, Japanese)
        if (showLanguageDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showLanguageDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SahasraSurface)
                        .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Playback Audio & Language",
                        style = MaterialTheme.typography.titleMedium.copy(color = SahasraTextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val options = listOf(
                        Triple("en", "dub", "English (Dub)"),
                        Triple("hi", "dub", "Hindi (Dub)"),
                        Triple("ja", "sub", "Japanese (Sub)")
                    )

                    options.forEach { (lang, type, label) ->
                        val isSel = selectedLanguage == lang && selectedAudioType == type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0x338585F7) else Color.Transparent)
                                .clickable {
                                    selectedLanguage = lang
                                    selectedAudioType = type
                                    sharedPrefs.edit().putString("playback_language", lang).putString("playback_audio_type", type).apply()
                                    showLanguageDialog = false
                                    resolveAndPlay(lang, type)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSel) SahasraAccent else SahasraTextPrimary,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSel) {
                                Text("Active", color = SahasraAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sources Switcher Dialog with Speed Tags
        if (showSourceDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showSourceDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SahasraSurface)
                        .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Speed-Ranked Sources",
                        style = MaterialTheme.typography.titleMedium.copy(color = SahasraTextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allWorkingSources) { src ->
                            val isCurrent = src.id == activeSource?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) Color(0x338585F7) else SahasraElevated)
                                    .clickable {
                                        activeSource = src
                                        showSourceDialog = false
                                        val cur = exoPlayer.currentPosition
                                        prepareSource(src, cur)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = src.providerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isCurrent) SahasraAccent else SahasraTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${src.quality} • ${src.audioLanguage.uppercase()} ${src.audioType.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SahasraTextSecondary, fontSize = 10.sp)
                                    )
                                }
                                Text(
                                    text = "${src.ttfbMs}ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF4ADE80),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quality Resolution Dialog
        if (showResolutionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showResolutionDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SahasraSurface)
                        .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Video Quality",
                        style = MaterialTheme.typography.titleMedium.copy(color = SahasraTextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf("Auto (1080p)", "1080p", "720p", "480p", "360p").forEach { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedResolution == res) Color(0x338585F7) else Color.Transparent)
                                .clickable {
                                    selectedResolution = res
                                    showResolutionDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = res,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedResolution == res) SahasraAccent else SahasraTextPrimary,
                                    fontWeight = if (selectedResolution == res) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        // Playback Speed Dialog
        if (showSpeedDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showSpeedDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SahasraSurface)
                        .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Playback Speed",
                        style = MaterialTheme.typography.titleMedium.copy(color = SahasraTextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedSpeed == speed) Color(0x338585F7) else Color.Transparent)
                                .clickable {
                                    selectedSpeed = speed
                                    exoPlayer.playbackParameters = PlaybackParameters(speed)
                                    showSpeedDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedSpeed == speed) SahasraAccent else SahasraTextPrimary,
                                    fontWeight = if (selectedSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0L) return "00:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
