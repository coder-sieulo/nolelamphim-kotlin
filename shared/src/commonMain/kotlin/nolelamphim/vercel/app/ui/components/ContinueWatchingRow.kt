package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.toMovie
import kotlinx.coroutines.launch

@Composable
fun ContinueWatchingRow(
    entries: List<WatchEntry>,
    onEntryClick: (WatchEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = "Xem tiếp",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
            IconButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem((listState.firstVisibleItemIndex - 2).coerceAtLeast(0))
                    }
                },
                enabled = listState.firstVisibleItemIndex > 0,
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Cuộn sang trái")
            }
            IconButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(
                            (listState.firstVisibleItemIndex + 2).coerceAtMost(entries.lastIndex),
                        )
                    }
                },
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Cuộn sang phải")
            }
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = entries,
                key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" },
            ) { entry ->
                Column(modifier = Modifier.width(132.dp)) {
                    MovieCard(
                        movie = entry.toMovie(),
                        onClick = { onEntryClick(entry) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = entry.episodeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
