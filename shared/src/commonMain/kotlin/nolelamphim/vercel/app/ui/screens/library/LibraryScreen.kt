package nolelamphim.vercel.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.toMovie
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.MovieGrid

@Composable
fun LibraryScreen(
    onMovieClick: (String) -> Unit,
    onWatchClick: (WatchEntry) -> Unit,
    viewModel: LibraryViewModel = viewModel {
        LibraryViewModel(AppContainer.localLibrary)
    },
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Thư viện",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Yêu thích (${favorites.size})") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Đã xem (${history.size})") },
            )
        }

        if (selectedTab == 0) {
            if (favorites.isEmpty()) {
                EmptyView("Chưa có phim yêu thích.\nNhấn ♥ ở trang phim để lưu.")
            } else {
                MovieGrid(
                    movies = favorites.map { it.toMovie() },
                    onMovieClick = { movie -> onMovieClick(movie.slug) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            if (history.isEmpty()) {
                EmptyView("Chưa có lịch sử xem")
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = viewModel::clearHistory) {
                            Text("Xoá tất cả")
                        }
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = history,
                            key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" },
                        ) { entry ->
                            HistoryRow(entry = entry, onClick = { onWatchClick(entry) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: WatchEntry,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = entry.posterUrl,
            contentDescription = entry.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(56.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF24242B)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(
                    entry.episodeName.takeIf { it.isNotBlank() },
                    entry.serverName.takeIf { it.isNotBlank() },
                ).joinToString("  •  "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
