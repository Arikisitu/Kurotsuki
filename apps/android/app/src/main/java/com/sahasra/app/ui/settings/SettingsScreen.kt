package com.sahasra.app.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import com.sahasra.app.R
import com.sahasra.app.ui.theme.KurotsukiBg
import com.sahasra.app.ui.theme.KurotsukiBorder
import com.sahasra.app.ui.theme.KurotsukiElevated
import com.sahasra.app.ui.theme.KurotsukiRed
import com.sahasra.app.ui.theme.KurotsukiSurface
import com.sahasra.app.ui.theme.KurotsukiTextPrimary
import com.sahasra.app.ui.theme.KurotsukiTextSecondary

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAnimeSourcesClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("kurotsuki_prefs", Context.MODE_PRIVATE) }

    var selectedTheme by remember { mutableStateOf("Dark (AMOLED)") }
    var selectedLanguage by remember { mutableStateOf("English (US)") }
    var selectedQuality by remember { mutableStateOf("Auto (1080p)") }
    var selectedReaderMode by remember { mutableStateOf("Vertical Webtoon") }
    var selectedMangaSource by remember { mutableStateOf("MangaDex HQ") }
    var cropBorders by remember { mutableStateOf("Enabled") }
    var highQualityImages by remember { mutableStateOf("32-bit (Full)") }
    var cacheNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurotsukiBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = KurotsukiTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KurotsukiTextPrimary
                )
            )
        }

        // Kurotsuki Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(KurotsukiSurface)
                .border(1.dp, KurotsukiBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.kurotsuki_logo),
                contentDescription = "Kurotsuki Katana Logo",
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "KUROTSUKI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = KurotsukiTextPrimary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Version 1.0.0 • Anikku + Futon Core",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KurotsukiTextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Anikku Engine & Futon Reader Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF10B981),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Group 1: General
        SettingsGroupHeader(title = "General")
        SettingsCard {
            SettingsNavRow(
                icon = Icons.Rounded.Palette,
                title = "Appearance",
                value = selectedTheme,
                onClick = {
                    val next = if (selectedTheme == "Dark (AMOLED)") "OLED Midnight" else "Dark (AMOLED)"
                    selectedTheme = next
                }
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Language,
                title = "Language",
                value = selectedLanguage,
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Sync,
                title = "Tracking Accounts",
                value = "AniList & MAL",
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Download,
                title = "Downloads Storage",
                value = "Internal (48.2 GB free)",
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Backup,
                title = "Backup & Restore",
                value = "Local Backup",
                onClick = {}
            )
        }

        // Group 2: Anime (Anikku Engine)
        SettingsGroupHeader(title = "Anime (Anikku Engine)")
        SettingsCard {
            SettingsNavRow(
                icon = Icons.Rounded.CloudDownload,
                title = "Anime Sources",
                value = "7 sources installed",
                highlightValue = true,
                onClick = onAnimeSourcesClick
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.PlayCircle,
                title = "Video Player Engine",
                value = "Media3 ExoPlayer (HW Accel)",
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Subtitles,
                title = "Preferred Audio & Subtitles",
                value = "English, Hindi, Japanese",
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.VideoSettings,
                title = "Default Stream Quality",
                value = selectedQuality,
                onClick = {
                    val next = when (selectedQuality) {
                        "Auto (1080p)" -> "1080p FHD"
                        "1080p FHD" -> "720p HD"
                        else -> "Auto (1080p)"
                    }
                    selectedQuality = next
                }
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Download,
                title = "Anime Downloads",
                value = "720p Preferred",
                onClick = {}
            )
        }

        // Group 3: Manga (Futon Engine)
        SettingsGroupHeader(title = "Manga (Futon Engine)")
        SettingsCard {
            SettingsNavRow(
                icon = Icons.Rounded.CloudDownload,
                title = "Manga Catalogues",
                value = selectedMangaSource,
                highlightValue = true,
                onClick = {
                    val next = when (selectedMangaSource) {
                        "MangaDex HQ" -> "Kotatsu Mirror"
                        "Kotatsu Mirror" -> "Webtoons HQ"
                        else -> "MangaDex HQ"
                    }
                    selectedMangaSource = next
                }
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = "Default Reader Mode",
                value = selectedReaderMode,
                onClick = {
                    val next = when (selectedReaderMode) {
                        "Vertical Webtoon" -> "Paged (Right to Left)"
                        "Paged (Right to Left)" -> "Paged (Left to Right)"
                        else -> "Vertical Webtoon"
                    }
                    selectedReaderMode = next
                }
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Crop,
                title = "Crop Page Borders",
                value = cropBorders,
                onClick = {
                    cropBorders = if (cropBorders == "Enabled") "Disabled" else "Enabled"
                }
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.AutoFixHigh,
                title = "Image Quality",
                value = highQualityImages,
                onClick = {
                    highQualityImages = if (highQualityImages == "32-bit (Full)") "16-bit (Low Memory)" else "32-bit (Full)"
                }
            )
        }

        // Group 4: Advanced & Storage
        SettingsGroupHeader(title = "Advanced & Storage")
        SettingsCard {
            SettingsNavRow(
                icon = Icons.Rounded.Wifi,
                title = "Network & DNS",
                value = "DNS-over-HTTPS (DoH)",
                onClick = {}
            )
            DividerLine()
            SettingsNavRow(
                icon = Icons.Rounded.Storage,
                title = "Clear Media Cache",
                value = "Purge images & temp files",
                onClick = {
                    Coil.imageLoader(context).apply {
                        memoryCache?.clear()
                        diskCache?.clear()
                    }
                    cacheNotice = "Disk & memory cache cleared successfully"
                    Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        cacheNotice?.let { notice ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x3310B981))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = KurotsukiRed,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(KurotsukiSurface)
            .border(1.dp, KurotsukiBorder, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    highlightValue: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KurotsukiTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = KurotsukiTextPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (highlightValue) KurotsukiRed else KurotsukiTextSecondary,
                    fontWeight = if (highlightValue) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = KurotsukiTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        color = KurotsukiBorder.copy(alpha = 0.6f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
