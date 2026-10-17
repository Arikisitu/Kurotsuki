package com.sahasra.app.ui.downloads

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahasra.app.ui.theme.KurotsukiBg
import com.sahasra.app.ui.theme.KurotsukiBorder
import com.sahasra.app.ui.theme.KurotsukiElevated
import com.sahasra.app.ui.theme.KurotsukiRed
import com.sahasra.app.ui.theme.KurotsukiSurface
import com.sahasra.app.ui.theme.KurotsukiTextPrimary
import com.sahasra.app.ui.theme.KurotsukiTextSecondary

enum class DownloadType {
    ANIME,
    MANGA
}

data class DownloadItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val progress: Float, // 0f to 1f
    val isCompleted: Boolean,
    val sizeText: String,
    val posterUrl: String
)

@Composable
fun DownloadsScreen(
    onItemClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DownloadType.ANIME) }

    val animeDownloads = remember {
        listOf(
            DownloadItem(
                id = "solo_leveling",
                title = "Solo Leveling",
                subtitle = "3 episodes • 720p",
                progress = 0.60f,
                isCompleted = false,
                sizeText = "2.1 GB / 3.5 GB",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx151807-m1gjdHJrE0Tk.jpg"
            ),
            DownloadItem(
                id = "jjk",
                title = "Jujutsu Kaisen",
                subtitle = "5 episodes • 720p",
                progress = 0.0f,
                isCompleted = false,
                sizeText = "Queued",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx113415-bbBWj4pEFseh.jpg"
            ),
            DownloadItem(
                id = "demon_slayer",
                title = "Demon Slayer: Kimetsu no Yaiba",
                subtitle = "11 episodes • 1080p",
                progress = 1.0f,
                isCompleted = true,
                sizeText = "4.2 GB",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx101922-PEn1CTyeNhKt.png"
            )
        )
    }

    val mangaDownloads = remember {
        listOf(
            DownloadItem(
                id = "berserk",
                title = "Berserk",
                subtitle = "12 chapters",
                progress = 1.0f,
                isCompleted = true,
                sizeText = "1.2 GB",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx30002-7729221.jpg"
            ),
            DownloadItem(
                id = "chainsaw_man",
                title = "Chainsaw Man",
                subtitle = "10 chapters",
                progress = 0.0f,
                isCompleted = false,
                sizeText = "Queued",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx105778-9Yp6Xk6aHspZ.jpg"
            ),
            DownloadItem(
                id = "solo_leveling_manga",
                title = "Solo Leveling (Manhwa)",
                subtitle = "25 chapters",
                progress = 1.0f,
                isCompleted = true,
                sizeText = "850 MB",
                posterUrl = "https://s4.anilist.co/file/anilistcdn/media/manga/cover/large/bx105398-b67312.jpg"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurotsukiBg)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KurotsukiTextPrimary
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search Downloads",
                        tint = KurotsukiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = KurotsukiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Segmented Switcher: [ Anime | Manga ]
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KurotsukiSurface)
                .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                .padding(3.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val isAnime = selectedTab == DownloadType.ANIME
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isAnime) KurotsukiRed else Color.Transparent)
                    .clickable { selectedTab = DownloadType.ANIME }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Anime",
                    fontWeight = FontWeight.Bold,
                    color = if (isAnime) KurotsukiTextPrimary else KurotsukiTextSecondary,
                    fontSize = 13.sp
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (!isAnime) KurotsukiRed else Color.Transparent)
                    .clickable { selectedTab = DownloadType.MANGA }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Manga",
                    fontWeight = FontWeight.Bold,
                    color = if (!isAnime) KurotsukiTextPrimary else KurotsukiTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Downloads List
        val currentItems = if (selectedTab == DownloadType.ANIME) animeDownloads else mangaDownloads

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(currentItems, key = { it.id }) { item ->
                var isPaused by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KurotsukiSurface)
                        .border(1.dp, KurotsukiBorder, RoundedCornerShape(14.dp))
                        .clickable { onItemClick(item.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KurotsukiElevated)
                    ) {
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KurotsukiTextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KurotsukiTextSecondary,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (!item.isCompleted) {
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = KurotsukiRed,
                                trackColor = KurotsukiElevated
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = item.sizeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isCompleted) Color(0xFF10B981) else KurotsukiTextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        IconButton(
                            onClick = { isPaused = !isPaused },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(KurotsukiElevated)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                contentDescription = if (isPaused) "Resume" else "Pause",
                                tint = KurotsukiTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Storage Footer Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KurotsukiSurface)
                .border(1.dp, KurotsukiBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Internal Storage: 48.2 GB free of 128 GB",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = KurotsukiTextSecondary,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "7.5 GB used",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KurotsukiRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
