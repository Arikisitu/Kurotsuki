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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.Coil
import com.sahasra.app.R
import com.sahasra.app.ui.theme.SahasraAccent
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraBorder
import com.sahasra.app.ui.theme.SahasraElevated
import com.sahasra.app.ui.theme.SahasraSuccess
import com.sahasra.app.ui.theme.SahasraSurface
import com.sahasra.app.ui.theme.SahasraTextPrimary
import com.sahasra.app.ui.theme.SahasraTextSecondary

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sahasra_prefs", Context.MODE_PRIVATE) }

    var isAmoledEnabled by remember {
        mutableStateOf(prefs.getBoolean("amoled_theme", true))
    }
    var selectedQuality by remember {
        mutableStateOf(prefs.getString("streaming_quality", "Auto (1080p)") ?: "Auto (1080p)")
    }
    var selectedSpeed by remember {
        mutableStateOf(prefs.getString("playback_speed", "1.0x") ?: "1.0x")
    }
    var isAutoplayEnabled by remember {
        mutableStateOf(prefs.getBoolean("autoplay_next", true))
    }
    var defaultReaderMode by remember {
        mutableStateOf(prefs.getString("default_reader_mode", "Vertical Webtoon") ?: "Vertical Webtoon")
    }
    var cacheNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SahasraBg)
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
                    tint = SahasraTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SahasraTextPrimary
                )
            )
        }

        // App Card Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SahasraSurface)
                .border(1.dp, SahasraBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.sahasra_logo),
                contentDescription = "SAHASRA Logo",
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "SAHASRA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = SahasraTextPrimary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Version 0.2.0 • Release",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SahasraTextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = SahasraSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AniList & MangaDex Connected",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SahasraSuccess,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Appearance
        SettingsCategory(title = "Appearance & Interface") {
            SettingsToggleRow(
                icon = Icons.Rounded.Palette,
                title = "AMOLED Pitch Black Theme",
                subtitle = "Optimized for OLED displays with true blacks",
                checked = isAmoledEnabled,
                onCheckedChange = {
                    isAmoledEnabled = it
                    prefs.edit().putBoolean("amoled_theme", it).apply()
                }
            )
        }

        // Playback
        SettingsCategory(title = "Video Player") {
            SettingsOptionRow(
                icon = Icons.Rounded.HighQuality,
                title = "Preferred Video Quality",
                value = selectedQuality,
                onClick = {
                    val next = when (selectedQuality) {
                        "Auto (1080p)" -> "1080p"
                        "1080p" -> "720p"
                        "720p" -> "480p"
                        else -> "Auto (1080p)"
                    }
                    selectedQuality = next
                    prefs.edit().putString("streaming_quality", next).apply()
                }
            )

            HorizontalDivider(color = SahasraBorder, thickness = 0.5.dp)

            SettingsOptionRow(
                icon = Icons.Rounded.Speed,
                title = "Default Playback Speed",
                value = selectedSpeed,
                onClick = {
                    val next = when (selectedSpeed) {
                        "1.0x" -> "1.25x"
                        "1.25x" -> "1.5x"
                        "1.5x" -> "2.0x"
                        else -> "1.0x"
                    }
                    selectedSpeed = next
                    prefs.edit().putString("playback_speed", next).apply()
                }
            )

            HorizontalDivider(color = SahasraBorder, thickness = 0.5.dp)

            SettingsToggleRow(
                icon = Icons.Rounded.Wifi,
                title = "Auto-play Next Episode",
                subtitle = "Advance to the next episode automatically",
                checked = isAutoplayEnabled,
                onCheckedChange = {
                    isAutoplayEnabled = it
                    prefs.edit().putBoolean("autoplay_next", it).apply()
                }
            )
        }

        // Manga Reader Settings
        SettingsCategory(title = "Manga Reader") {
            SettingsOptionRow(
                icon = Icons.Rounded.MenuBook,
                title = "Default Reading Mode",
                value = defaultReaderMode,
                onClick = {
                    val next = if (defaultReaderMode == "Vertical Webtoon") "Horizontal Paged" else "Vertical Webtoon"
                    defaultReaderMode = next
                    prefs.edit().putString("default_reader_mode", next).apply()
                }
            )
        }

        // Data & Cache
        SettingsCategory(title = "Storage & Cache") {
            SettingsActionRow(
                icon = Icons.Rounded.DeleteOutline,
                title = "Clear Image & HTTP Cache",
                subtitle = cacheNotice ?: "Frees up temporary downloaded images and scan files",
                onClick = {
                    try {
                        Coil.imageLoader(context).memoryCache?.clear()
                        Coil.imageLoader(context).diskCache?.clear()
                        context.cacheDir.deleteRecursively()
                        cacheNotice = "Cache successfully cleared"
                        Toast.makeText(context, "Storage cache cleared", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        cacheNotice = "Cleared memory cache"
                    }
                }
            )
        }

        // About & Legal
        SettingsCategory(title = "About & Licenses") {
            SettingsActionRow(
                icon = Icons.Rounded.Info,
                title = "About SAHASRA",
                subtitle = "Unified Anime, Manga & Discovery Platform. Open source compliance.",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SahasraAccent,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SahasraSurface)
                .border(1.dp, SahasraBorder, RoundedCornerShape(14.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SahasraAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SahasraTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SahasraTextSecondary,
                    fontSize = 11.sp
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SahasraAccent,
                checkedTrackColor = SahasraElevated,
                uncheckedThumbColor = SahasraTextSecondary,
                uncheckedTrackColor = SahasraSurface
            )
        )
    }
}

@Composable
private fun SettingsOptionRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SahasraAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SahasraTextPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SahasraElevated)
                .border(1.dp, SahasraBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                color = SahasraAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SahasraAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SahasraTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SahasraTextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
