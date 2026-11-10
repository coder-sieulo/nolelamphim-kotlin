package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun ContinueWatchingRow(
    entries: List<WatchEntry>,
    onEntryClick: (WatchEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val isTv = LocalIsTv.current
    val listState = rememberLazyListState()
    val cardWidth = if (isTv) 240.dp else 190.dp
    val horizontalPadding = if (isTv) 24.dp else 16.dp
    val cardShape = RoundedCornerShape(12.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CinemaColors.Primary),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Tiếp tục xem",
                style = MaterialTheme.typography.titleMedium,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(if (isTv) 16.dp else 12.dp),
        ) {
            items(
                items = entries,
                key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" },
            ) { entry ->
                Column(
                    modifier = Modifier
                        .width(cardWidth)
                        .cinemaFocusable(
                            onClick = { onEntryClick(entry) },
                            shape = cardShape,
                            scaleFocused = 1.06f,
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(cardShape)
                            .background(CinemaColors.SurfaceCard),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = entry.posterUrl,
                            contentDescription = entry.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Subtle dark tint
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                        )

                        // Play circle icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Phát",
                                tint = CinemaColors.Primary,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // Episode badge on bottom left
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = entry.episodeName.ifBlank { "Tập ${entry.episodeIndex + 1}" },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = CinemaColors.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        // Orange bottom progress bar indicator (simulated continue watching line)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color.White.copy(alpha = 0.2f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(3.dp)
                                    .background(CinemaColors.Primary),
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = entry.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = entry.serverName.ifBlank { "Đang xem dở" },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = CinemaColors.TextMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
