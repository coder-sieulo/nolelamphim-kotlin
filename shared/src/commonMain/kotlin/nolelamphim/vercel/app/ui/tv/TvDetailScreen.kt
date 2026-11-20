package nolelamphim.vercel.app.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.EpisodeProgress
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.sameEpisode
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.DetailSkeletonScreen
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.screens.detail.DetailUiState
import nolelamphim.vercel.app.ui.theme.CinemaColors

@Composable
fun TvDetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPlay: (String, Int, Int) -> Unit,
    onMovieClick: (String) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PlatformBackHandler(onBack = onBack)
    when (state) {
        DetailUiState.Loading -> DetailSkeletonScreen()
        is DetailUiState.Error -> ErrorView(state.message, onRetry)
        is DetailUiState.Success -> {
            val history by AppContainer.localLibrary.history.collectAsStateWithLifecycle()
            val progress by AppContainer.localLibrary.episodeProgress.collectAsStateWithLifecycle()
            TvMovieDetails(state, history.firstOrNull { it.slug == state.movie.slug }, progress,
                onPlay, onMovieClick, onToggleFavorite)
        }
    }
}

private data class StartTarget(val server: Int, val episode: Int, val resume: WatchEntry? = null)

private fun startTarget(movie: MovieDetail, saved: WatchEntry?): StartTarget? {
    if (saved != null) {
        val source = movie.servers.indexOfFirst { it.name.trim().equals(saved.serverName.trim(), ignoreCase = true) }
        if (source >= 0) {
            val episode = movie.servers[source].episodes.indexOfFirst { sameEpisode(it.slug, it.name, saved.episodeSlug, saved.episodeName) }
            if (episode >= 0 && movie.servers[source].episodes[episode].let { it.embedUrl.isNotBlank() || !it.m3u8Url.isNullOrBlank() }) {
                return StartTarget(source, episode, saved)
            }
        }
    }
    movie.servers.forEachIndexed { source, server ->
        val episode = server.episodes.indexOfFirst { it.embedUrl.isNotBlank() || !it.m3u8Url.isNullOrBlank() }
        if (episode >= 0) return StartTarget(source, episode)
    }
    return null
}

private fun episodeLabel(name: String, index: Int): String = when {
    name.isBlank() -> "Tập ${index + 1}"
    name.all(Char::isDigit) -> "Tập $name"
    else -> name
}

@Composable
private fun TvMovieDetails(
    state: DetailUiState.Success,
    saved: WatchEntry?,
    progress: List<EpisodeProgress>,
    onPlay: (String, Int, Int) -> Unit,
    onMovieClick: (String) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val movie = state.movie
    val target = remember(movie, saved) { startTarget(movie, saved) }
    val contentFocus = remember { FocusRequester() }
    val episodesFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var episodePanel by rememberSaveable(movie.slug) { mutableStateOf(false) }
    var returnToEpisodes by remember { mutableStateOf(false) }
    var descriptionExpanded by rememberSaveable(movie.slug) { mutableStateOf(false) }
    LaunchedEffect(movie.slug) { withFrameNanos { }; contentFocus.requestFocus() }
    LaunchedEffect(episodePanel) {
        if (!episodePanel && returnToEpisodes) {
            withFrameNanos { }
            episodesFocus.requestFocus()
            returnToEpisodes = false
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().focusRequester(contentFocus).focusRestorer().focusGroup(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "overview") {
            Box(Modifier.fillMaxWidth().heightIn(min = 338.dp)) {
                AsyncImage(movie.posterUrl, null, contentScale = ContentScale.Crop,
                    modifier = Modifier.align(Alignment.CenterEnd).width(440.dp).fillMaxSize())
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(CinemaColors.TvBackground, CinemaColors.TvBackground.copy(alpha = .93f), CinemaColors.TvBackground.copy(alpha = .35f)))))
                Column(Modifier.padding(start = 48.dp, end = 48.dp, top = 32.dp, bottom = 24.dp).widthIn(max = 730.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(movie.name, style = MaterialTheme.typography.headlineLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val metadata = listOfNotNull(movie.year?.toString(), movie.duration?.takeIf(String::isNotBlank), movie.lang?.takeIf(String::isNotBlank), movie.quality?.takeIf(String::isNotBlank))
                    if (metadata.isNotEmpty()) Text(metadata.joinToString(" · "), maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
                    if (movie.description.isNotBlank()) Text(movie.description, maxLines = 3, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge, color = CinemaColors.TvMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val completed = target?.resume?.let { it.durationMs > 0L && it.positionMs.toDouble() / it.durationMs >= .95 } == true
                        val label = when {
                            completed -> "Xem lại"
                            target?.resume != null -> "Tiếp tục ${episodeLabel(movie.servers[target.server].episodes[target.episode].name, target.episode)}"
                            else -> "Xem ngay"
                        }
                        TvActionButton(label, { target?.let { onPlay(movie.slug, it.server, it.episode) } },
                            modifier = Modifier.widthIn(max = 300.dp), primary = true, enabled = target != null, icon = Icons.Default.PlayArrow)
                        TvActionButton("Tập phim", { episodePanel = true }, Modifier.focusRequester(episodesFocus), enabled = target != null)
                        TvActionButton(if (state.isFavorite) "Đã lưu" else "Lưu", onToggleFavorite,
                            icon = if (state.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder)
                    }
                    if (target == null) Text("Phim chưa có nguồn phát khả dụng.", color = CinemaColors.TvMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (movie.description.isNotBlank()) {
            item(key = "description") {
                Column(Modifier.padding(horizontal = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Nội dung phim", style = MaterialTheme.typography.titleLarge)
                    Text(movie.description, style = MaterialTheme.typography.bodyLarge, color = CinemaColors.TvMuted,
                        maxLines = if (descriptionExpanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis)
                    TvActionButton(if (descriptionExpanded) "Thu gọn" else "Đọc thêm", { descriptionExpanded = !descriptionExpanded })
                }
            }
        }
        item(key = "metadata") {
            Column(Modifier.padding(horizontal = 48.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (movie.categories.isNotEmpty()) Text("Thể loại: ${movie.categories.joinToString(" · ")}", style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
                if (movie.directors.isNotEmpty()) Text("Đạo diễn: ${movie.directors.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
                if (movie.actors.isNotEmpty()) Text("Diễn viên: ${movie.actors.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium, color = CinemaColors.TvMuted)
            }
        }
        if (state.related.isNotEmpty()) item(key = "related") {
            MovieRow("Có thể bạn muốn xem", state.related, { onMovieClick(it.slug) }, Modifier.padding(horizontal = 24.dp))
        }
    }
    if (episodePanel) {
        TvDetailEpisodes(movie, target, progress,
            onDismiss = { returnToEpisodes = true; episodePanel = false },
            onSelect = { source, episode -> episodePanel = false; onPlay(movie.slug, source, episode) })
    }
}

@Composable
private fun TvDetailEpisodes(
    movie: MovieDetail,
    target: StartTarget?,
    progress: List<EpisodeProgress>,
    onDismiss: () -> Unit,
    onSelect: (Int, Int) -> Unit,
) {
    var source by rememberSaveable { mutableIntStateOf(target?.server ?: 0) }
    var group by rememberSaveable { mutableIntStateOf((target?.episode ?: 0) / 20) }
    val server = movie.servers.getOrNull(source)
    val episodes = server?.episodes.orEmpty()
    val visibleEpisodes = episodes.drop(group * 20).take(20)
    val focus = remember { FocusRequester() }
    val closeFocus = remember { FocusRequester() }
    val preferred = target?.episode?.takeIf { target.server == source && it / 20 == group }
    val active = preferred?.takeIf { episodes.getOrNull(it)?.let { ep -> ep.embedUrl.isNotBlank() || !ep.m3u8Url.isNullOrBlank() } == true }
        ?: visibleEpisodes.indexOfFirst { it.embedUrl.isNotBlank() || !it.m3u8Url.isNullOrBlank() }
            .takeIf { it >= 0 }?.plus(group * 20)
    val grid = rememberLazyGridState()
    LaunchedEffect(source, group) {
        if (active != null) grid.scrollToItem((active - group * 20).coerceAtLeast(0))
        withFrameNanos { }
        if (active != null) focus.requestFocus() else closeFocus.requestFocus()
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.widthIn(max = 864.dp).heightIn(max = 480.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CinemaColors.TvSurface).padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tập phim", style = MaterialTheme.typography.headlineMedium)
                TvActionButton("Đóng", onDismiss, Modifier.focusRequester(closeFocus))
            }
            if (movie.servers.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(movie.servers) { index, item ->
                    TvActionButton(item.name.ifBlank { "Nguồn ${index + 1}" }, { source = index; group = 0 }, primary = source == index)
                }
            }
            if (episodes.size > 20) LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items((episodes.size + 19) / 20) { range ->
                    TvActionButton("${range * 20 + 1}–${minOf((range + 1) * 20, episodes.size)}", { group = range }, primary = range == group)
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(5), state = grid, modifier = Modifier.weight(1f, fill = false).heightIn(min = 80.dp, max = 220.dp).focusRestorer().focusGroup(),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(visibleEpisodes, key = { index, episode -> "$source-$group-${episode.slug}-$index" }) { index, episode ->
                    val absoluteIndex = group * 20 + index
                    val savedProgress = progress.firstOrNull { it.movieSlug == movie.slug && it.serverName == server?.name && sameEpisode(it.episodeSlug, it.episodeName, episode.slug, episode.name) }
                    val label = episodeLabel(episode.name, absoluteIndex) + if (savedProgress?.isCompleted == true) " ✓" else ""
                    TvActionButton(label, { onSelect(source, absoluteIndex) },
                        modifier = if (absoluteIndex == active) Modifier.focusRequester(focus) else Modifier,
                        primary = target?.server == source && target.episode == absoluteIndex,
                        enabled = episode.embedUrl.isNotBlank() || !episode.m3u8Url.isNullOrBlank())
                }
            }
        }
    }
}
