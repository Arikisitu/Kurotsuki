package com.sahasra.app.ui.player

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraCrimson
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    title: String,
    streamUrl: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Resolution streams (YouTube-style multi-rendition)
    val hlsStreamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    val mp4StreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    var selectedResolution by remember { mutableStateOf("Auto (1080p)") }
    var showResolutionDialog by remember { mutableStateOf(false) }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val activeUrl = streamUrl?.takeIf { it.isNotBlank() } ?: hlsStreamUrl

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

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    fun preparePlayer(url: String) {
        playbackError = null
        isBuffering = true
        isPlaybackReady = false
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            playbackError = "Could not initialize media item: ${e.message}"
            isBuffering = false
        }
    }

    LaunchedEffect(activeUrl) {
        preparePlayer(activeUrl)
    }

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
                playbackError = "Playback error: ${error.errorCodeName} (${error.localizedMessage ?: "Source unavailable"})"
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

    // Auto-hide controls after 4 seconds of inactivity during playback
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
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

        // Buffering Spinner
        if (isBuffering && playbackError == null) {
            CircularProgressIndicator(
                color = SahasraAccent,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
            )
        }

        // Error State Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE08090B))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SahasraSurface)
                        .border(1.dp, SahasraBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp)
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
                            contentDescription = null,
                            tint = SahasraCrimson,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Playback Interrupted",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SahasraTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = playbackError ?: "Stream connection failed",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SahasraTextSecondary,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { preparePlayer(mp4StreamUrl) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SahasraAccent,
                                contentColor = SahasraBg
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Alternative Stream")
                        }

                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SahasraElevated,
                                contentColor = SahasraTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }

        // Overlay Controls (YouTube-Style)
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
                                Color(0xCC08090B),
                                Color(0x3308090B),
                                Color(0xCC08090B)
                            )
                        )
                    )
            ) {
                // Top Header (Back button + Title + Quality/Speed buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x66111318))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = SahasraTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SahasraTextPrimary
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = if (isPlaybackReady) "Now Playing" else "Buffering...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SahasraAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Resolution Switcher (YouTube Quality button)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x88111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(8.dp))
                            .clickable { showResolutionDialog = !showResolutionDialog }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = "Quality",
                                tint = SahasraAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedResolution,
                                color = SahasraTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Playback Speed button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x88111318))
                            .border(1.dp, SahasraBorder, RoundedCornerShape(8.dp))
                            .clickable { showSpeedDialog = !showSpeedDialog }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedSpeed}x",
                            color = SahasraTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Resolution Dropdown Dialog
                if (showResolutionDialog) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 64.dp, end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SahasraSurface)
                            .border(1.dp, SahasraBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            listOf("Auto (1080p)", "1080p", "720p", "480p", "360p").forEach { res ->
                                Text(
                                    text = res,
                                    color = if (selectedResolution == res) SahasraAccent else SahasraTextPrimary,
                                    fontWeight = if (selectedResolution == res) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedResolution = res
                                            showResolutionDialog = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Speed Dropdown Dialog
                if (showSpeedDialog) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 64.dp, end = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SahasraSurface)
                            .border(1.dp, SahasraBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                Text(
                                    text = "${speed}x",
                                    color = if (selectedSpeed == speed) SahasraAccent else SahasraTextPrimary,
                                    fontWeight = if (selectedSpeed == speed) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedSpeed = speed
                                            exoPlayer.playbackParameters = PlaybackParameters(speed)
                                            showSpeedDialog = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Center Play / Pause / Replay / Skip Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66111318))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = SahasraTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SahasraAccent)
                            .clickable {
                                if (isPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = SahasraBg,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(totalDuration)
                            exoPlayer.seekTo(newPos)
                            currentPosition = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x66111318))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forward10,
                            contentDescription = "Forward 10s",
                            tint = SahasraTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Bottom Timeline & Controls Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(if (isUserSeeking) (seekProgress * totalDuration).toLong() else currentPosition),
                            color = SahasraTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = formatTime(totalDuration),
                            color = SahasraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = if (totalDuration > 0) {
                            if (isUserSeeking) seekProgress else (currentPosition.toFloat() / totalDuration.toFloat())
                        } else 0f,
                        onValueChange = { progress ->
                            isUserSeeking = true
                            seekProgress = progress
                        },
                        onValueChangeFinished = {
                            val targetMs = (seekProgress * totalDuration).toLong()
                            exoPlayer.seekTo(targetMs)
                            currentPosition = targetMs
                            isUserSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = SahasraAccent,
                            activeTrackColor = SahasraAccent,
                            inactiveTrackColor = Color(0x448585F7)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
