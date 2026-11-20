package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
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
    rowFocusRequester: FocusRequester? = null,
    onRowFocused: (() -> Unit)? = null,
    onRowReady: (() -> Unit)? = null,
) {
    if (entries.isEmpty()) return

    val isTv = LocalIsTv.current
    if (isTv) {
        TvContinueWatchingRow(entries, onEntryClick, modifier, rowFocusRequester, onRowFocused, onRowReady)
        return
    }
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
                    .height(if (isTv) 21.dp else 18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isTv) CinemaColors.TvOrange else CinemaColors.Primary),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Tiếp tục xem",
                style = MaterialTheme.typography.titleMedium,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (isTv) {
                Text(
                    text = "Xem tiếp tập dở",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color(0xFF96949A),
                )
            }
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
                            scaleFocused = 1.05f,
                            focusedBorderColor = if (isTv) CinemaColors.TvOrangeHighlight else CinemaColors.Primary,
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(cardShape)
                            .background(CinemaColors.SurfaceCard)
                            .then(
                                if (isTv) Modifier.border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.12f),
                                    cardShape,
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = entry.posterUrl,
                            contentDescription = entry.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Subtle left-to-right cinema gradient overlay matching cinefy-tv mockup
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(
                                            Color(0xE608090C),
                                            Color(0x6608090C),
                                            Color.Transparent,
                                        ),
                                    ),
                                ),
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
                                tint = if (isTv) CinemaColors.TvOrange else CinemaColors.Primary,
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

                        // Unknown duration has no meaningful progress fraction.
                        if (entry.durationMs > 0L) Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(Color.White.copy(alpha = 0.25f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((entry.positionMs.toDouble() / entry.durationMs).toFloat().coerceIn(0f, 1f))
                                    .height(3.5.dp)
                                    .background(if (isTv) CinemaColors.TvOrange else CinemaColors.Primary),
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

@Composable
private fun TvContinueWatchingRow(
    entries: List<WatchEntry>,
    onEntryClick: (WatchEntry) -> Unit,
    modifier: Modifier = Modifier,
    rowFocusRequester: FocusRequester? = null,
    onRowFocused: (() -> Unit)? = null,
    onRowReady: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    var focusedSlug by rememberSaveable { mutableStateOf(entries.first().slug) }
    val restoreSlug = focusedSlug.takeIf { slug -> entries.any { it.slug == slug } } ?: entries.first().slug
    val defaultRequester = remember { FocusRequester() }
    val fallbackRequester = rowFocusRequester ?: defaultRequester
    LaunchedEffect(rowFocusRequester) {
        if (rowFocusRequester == null) return@LaunchedEffect
        val index = entries.indexOfFirst { it.slug == restoreSlug }.coerceAtLeast(0)
        val entry = entries[index]
        val itemKey = "${entry.slug}-${entry.serverIndex}-${entry.episodeIndex}"
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it > 0 }
        if (listState.layoutInfo.visibleItemsInfo.none { it.key == itemKey }) listState.scrollToItem(index)
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.key == itemKey } }.first { it }
        withFrameNanos { }
        onRowReady?.invoke()
    }
    Column(modifier = modifier.fillMaxWidth()
        .onFocusChanged { if (it.hasFocus) onRowFocused?.invoke() }.focusGroup()) {
        Text(
            text = "Tiếp tục xem",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
            color = CinemaColors.TvWhite,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
        )
        LazyRow(
            state = listState,
            modifier = Modifier.focusRestorer(fallback = fallbackRequester).focusGroup(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(entries, key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" }, contentType = { "tv_continue" }) { entry ->
                TvContinueWatchingCard(
                    entry = entry,
                    onClick = { onEntryClick(entry) },
                    onFocusChanged = { focused -> if (focused) focusedSlug = entry.slug },
                    modifier = Modifier
                        .width(264.dp)
                        .then(if (entry.slug == restoreSlug) Modifier.focusRequester(fallbackRequester) else Modifier),
                )
            }
        }
    }
}

/** Shared by Home and the TV library; unknown duration never shows a fabricated bar. */
@Composable
fun TvContinueWatchingCard(
    entry: WatchEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocusChanged: ((Boolean) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    val position = entry.positionMs.coerceAtLeast(0L)
    val progress = if (entry.durationMs > 0L) {
        (position.toDouble() / entry.durationMs).toFloat().coerceIn(0f, 1f)
    } else null
    val episode = entry.episodeName.trim().let { name ->
        when {
            name.isBlank() -> "Tập ${entry.episodeIndex + 1}"
            name.all { it.isDigit() } -> "Tập $name"
            else -> name
        }
    }
    val remainingMs = (entry.durationMs - position).coerceAtLeast(0L)
    val remainingMinutes = remainingMs / 60_000L + if (remainingMs % 60_000L > 0L) 1L else 0L
    val progressLabel = when {
        progress != null -> "Còn $remainingMinutes phút"
        position > 0L -> "Đã xem ${position / 60_000L}:${((position / 1000L) % 60L).toString().padStart(2, '0')}"
        else -> "Xem tiếp"
    }
    Column(
        modifier = modifier
            .cinemaFocusable(
                onClick = onClick,
                shape = shape,
                scaleFocused = 1f,
                focusedBorderColor = CinemaColors.TvWhite,
                onFocusChanged = onFocusChanged,
            )
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape)
                .background(CinemaColors.TvPosterBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(entry.name.take(1), style = MaterialTheme.typography.headlineMedium, color = CinemaColors.TvMuted)
            AsyncImage(
                model = entry.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (progress != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Black.copy(alpha = 0.65f)),
                ) {
                    Box(Modifier.fillMaxWidth(progress).height(4.dp).background(CinemaColors.TvOrange))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 17.sp, lineHeight = 23.sp),
            color = CinemaColors.TvWhite,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$episode · $progressLabel",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp, lineHeight = 21.sp),
            color = CinemaColors.TvMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
