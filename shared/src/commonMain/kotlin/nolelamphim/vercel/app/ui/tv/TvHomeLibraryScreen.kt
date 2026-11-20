package nolelamphim.vercel.app.ui.tv

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.ContinueWatchingRow
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.HomeSkeletonScreen
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.screens.catalog.CatalogType
import nolelamphim.vercel.app.ui.screens.home.HomeUiState
import nolelamphim.vercel.app.ui.theme.CinemaColors

/** Library-first TV home: the first viewport is usable content, without a competing hero. */
@Composable
fun TvHomeLibraryScreen(
    state: HomeUiState,
    entries: List<WatchEntry>,
    onMovieClick: (Movie) -> Unit,
    onContinueClick: (WatchEntry) -> Unit,
    onSeeAll: (String) -> Unit,
    onRetry: () -> Unit,
) {
    if (state.isLoading && state.latest.isEmpty() && entries.isEmpty()) { HomeSkeletonScreen(); return }
    if (state.error != null && state.latest.isEmpty() && entries.isEmpty()) { ErrorView(state.error, onRetry); return }
    if (state.latest.isEmpty() && entries.isEmpty() && state.series.isEmpty() && state.singles.isEmpty()) {
        EmptyView("Chưa có phim để hiển thị", actionLabel = "Tải lại", onAction = onRetry); return
    }
    val listState = rememberLazyListState()
    var lastRowKey by rememberSaveable { mutableStateOf<String?>(null) }
    val rowRequesters = remember {
        listOf("continue", "latest", "series", "singles", "cinema", "all-latest")
            .associateWith { FocusRequester() }
    }
    val readyRows = remember { mutableStateMapOf<String, Boolean>() }
    val rowKeys = buildList {
        if (entries.isNotEmpty()) add("continue")
        if (state.latest.isNotEmpty()) add("latest")
        if (state.series.isNotEmpty()) add("series")
        if (state.singles.isNotEmpty()) add("singles")
        if (state.cinema.isNotEmpty()) add("cinema")
        add("all-latest")
    }
    val itemKeys = buildList {
        add("home-title")
        if (entries.isNotEmpty()) add("continue")
        if (state.error != null) add("refresh-error")
        addAll(rowKeys.filter { it != "continue" })
    }
    val restoreRow = lastRowKey?.takeIf { it in rowKeys } ?: rowKeys.first()
    val navigation = LocalTvNavigation.current
    val latestNavigation by rememberUpdatedState(navigation)
    LaunchedEffect(Unit) {
        // Navigation disposes the old focus nodes, so persist row identity as well as scroll.
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it > 0 }
        if (listState.layoutInfo.visibleItemsInfo.none { it.key == restoreRow }) {
            listState.scrollToItem(itemKeys.indexOf(restoreRow).coerceAtLeast(0))
        }
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.key == restoreRow } }.first { it }
        if (restoreRow != "all-latest") snapshotFlow { readyRows[restoreRow] == true }.first { it }
        withFrameNanos { }
        if (!latestNavigation.isNavigationFocused) rowRequesters.getValue(restoreRow).requestFocus()
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().focusRestorer(fallback = rowRequesters.getValue(restoreRow)).focusGroup(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "home-title") {
            Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Phim của bạn", style = MaterialTheme.typography.headlineMedium)
                Text(if (entries.isEmpty()) "Tìm một câu chuyện cho hôm nay." else "Tiếp tục câu chuyện đang xem.",
                    style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
            }
        }
        if (entries.isNotEmpty()) {
            item(key = "continue", contentType = "continue") {
                ContinueWatchingRow(entries, onContinueClick,
                    rowFocusRequester = rowRequesters.getValue("continue"), onRowFocused = { lastRowKey = "continue" },
                    onRowReady = { readyRows["continue"] = true })
            }
        }
        if (state.error != null) {
            item(key = "refresh-error") {
                Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Chưa cập nhật được phim mới. Nội dung đã tải vẫn có thể xem.",
                        style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
                    TvActionButton("Tải lại", onRetry)
                }
            }
        }
        if (state.latest.isNotEmpty()) {
            item(key = "latest", contentType = "poster-row") {
                MovieRow("Mới cập nhật", state.latest, onMovieClick,
                    onSeeAll = if (entries.isNotEmpty()) ({ onSeeAll(CatalogType.LATEST) }) else null,
                    rowFocusRequester = rowRequesters.getValue("latest"), onRowFocused = { lastRowKey = "latest" },
                    onRowReady = { readyRows["latest"] = true })
            }
        }
        if (state.series.isNotEmpty()) {
            item(key = "series", contentType = "poster-row") {
                MovieRow("Phim bộ", state.series, onMovieClick, onSeeAll = { onSeeAll(CatalogType.SERIES) },
                    rowFocusRequester = rowRequesters.getValue("series"), onRowFocused = { lastRowKey = "series" },
                    onRowReady = { readyRows["series"] = true })
            }
        }
        if (state.singles.isNotEmpty()) {
            item(key = "singles", contentType = "poster-row") {
                MovieRow("Phim lẻ", state.singles, onMovieClick, onSeeAll = { onSeeAll(CatalogType.SINGLES) },
                    rowFocusRequester = rowRequesters.getValue("singles"), onRowFocused = { lastRowKey = "singles" },
                    onRowReady = { readyRows["singles"] = true })
            }
        }
        if (state.cinema.isNotEmpty()) {
            item(key = "cinema", contentType = "poster-row") {
                MovieRow("Phim chiếu rạp", state.cinema, onMovieClick, onSeeAll = { onSeeAll(CatalogType.CINEMA) },
                    rowFocusRequester = rowRequesters.getValue("cinema"), onRowFocused = { lastRowKey = "cinema" },
                    onRowReady = { readyRows["cinema"] = true })
            }
        }
        item(key = "all-latest") {
            TvActionButton("Tất cả phim mới cập nhật", { onSeeAll(CatalogType.LATEST) },
                Modifier.padding(horizontal = 24.dp).focusRequester(rowRequesters.getValue("all-latest"))
                    .onFocusChanged { if (it.hasFocus) lastRowKey = "all-latest" })
        }
    }
}
