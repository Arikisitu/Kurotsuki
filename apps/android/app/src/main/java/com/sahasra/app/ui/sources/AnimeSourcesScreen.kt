package com.sahasra.app.ui.sources

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahasra.app.ui.theme.KurotsukiBg
import com.sahasra.app.ui.theme.KurotsukiBorder
import com.sahasra.app.ui.theme.KurotsukiElevated
import com.sahasra.app.ui.theme.KurotsukiRed
import com.sahasra.app.ui.theme.KurotsukiSurface
import com.sahasra.app.ui.theme.KurotsukiTextPrimary
import com.sahasra.app.ui.theme.KurotsukiTextSecondary

data class AnimeRepoMeta(
    val name: String,
    val updated: String,
    val count: Int,
    val iconColor: Color
)

data class InstalledSourceItem(
    val id: String,
    val name: String,
    val lang: String,
    val version: String,
    val defaultEnabled: Boolean,
    val tagColor: Color
)

@Composable
fun AnimeSourcesScreen(
    onBackClick: () -> Unit
) {
    val repositories = remember {
        listOf(
            AnimeRepoMeta(
                name = "Yūzōnō Repo",
                updated = "Last updated: 2 days ago",
                count = 48,
                iconColor = Color(0xFFEF4444)
            ),
            AnimeRepoMeta(
                name = "Secozzi Extensions",
                updated = "Last updated: 3 days ago",
                count = 92,
                iconColor = Color(0xFF6366F1)
            )
        )
    }

    val installedSources = remember {
        listOf(
            InstalledSourceItem("gogo", "Gogoanime", "EN/HIN/JP", "v14.2", true, Color(0xFFFEA628)),
            InstalledSourceItem("zoro", "Zoro", "EN/HIN/JP", "v13.1", true, Color(0xFFF43F5E)),
            InstalledSourceItem("pahe", "AnimePahe", "EN/JP", "v16.0", true, Color(0xFFEF4444)),
            InstalledSourceItem("miruro", "Miruro", "EN/HIN", "v1.7.4", true, Color(0xFF8B5CF6)),
            InstalledSourceItem("aniwave", "AniWave", "EN/HIN", "v3.1.2", true, Color(0xFF3B82F6)),
            InstalledSourceItem("yugen", "Yugen", "EN/JP", "v1.5.0", true, Color(0xFF10B981)),
            InstalledSourceItem("kickass", "KickAssAnime", "EN", "v2.0.1", true, Color(0xFFF59E0B))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurotsukiBg)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = KurotsukiTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Anime Sources",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = KurotsukiTextPrimary
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = KurotsukiTextSecondary
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = KurotsukiTextSecondary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Repositories Section
            item {
                Text(
                    text = "REPOSITORIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KurotsukiTextSecondary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(repositories) { repo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KurotsukiSurface)
                        .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(repo.iconColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            tint = repo.iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = repo.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KurotsukiTextPrimary
                            )
                        )
                        Text(
                            text = "${repo.updated} • ${repo.count} sources",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KurotsukiTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = "Sync",
                            tint = KurotsukiTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Installed Sources Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "INSTALLED SOURCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KurotsukiTextSecondary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(installedSources) { source ->
                var isEnabled by remember { mutableStateOf(source.defaultEnabled) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KurotsukiSurface)
                        .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(source.tagColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = source.name.take(2).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = source.tagColor,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${source.name} (${source.lang})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KurotsukiTextPrimary
                            )
                        )
                        Text(
                            text = source.version,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = KurotsukiTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KurotsukiTextPrimary,
                            checkedTrackColor = KurotsukiRed,
                            uncheckedThumbColor = KurotsukiTextSecondary,
                            uncheckedTrackColor = KurotsukiElevated
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Source Settings",
                            tint = KurotsukiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
