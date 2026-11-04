package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.toMovie

@Composable
fun ContinueWatchingRow(
    entries: List<WatchEntry>,
    onEntryClick: (WatchEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Xem tiếp",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyRow(
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
