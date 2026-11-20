package nolelamphim.vercel.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.FavoriteMovie
import nolelamphim.vercel.app.data.local.toMovie
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.MovieGrid
import nolelamphim.vercel.app.ui.components.TvContinueWatchingCard
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable
import nolelamphim.vercel.app.ui.tv.TvBrowseButton
import nolelamphim.vercel.app.ui.tv.TvBrowseConfirmation
import nolelamphim.vercel.app.ui.tv.TvBrowseMessage
import nolelamphim.vercel.app.ui.tv.LocalTvNavigation

@Composable
fun LibraryScreen(
    onMovieClick: (String) -> Unit,
    onWatchClick: (WatchEntry) -> Unit,
    onExploreClick: (() -> Unit)? = null,
    viewModel: LibraryViewModel = viewModel {
        LibraryViewModel(AppContainer.localLibrary)
    },
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val isTv = LocalIsTv.current
    if (isTv) {
        TvLibraryScreen(favorites, history, viewModel, onMovieClick, onWatchClick, onExploreClick)
        return
    }
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTv) Color.Transparent else CinemaColors.Background),
    ) {
        // Library Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Thư viện cá nhân",
                style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            // Segmented Pills
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CinemaColors.SurfaceCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LibraryTabPill(
                    label = "Yêu thích",
                    count = favorites.size,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                )
                LibraryTabPill(
                    label = "Lịch sử xem",
                    count = history.size,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                )
            }
        }

        // Tab Content
        if (selectedTab == 0) {
            if (favorites.isEmpty()) {
                EmptyView(
                    message = "Chưa có phim trong danh sách yêu thích.\nNhấn nút ♥ trên bất kỳ trang chi tiết phim nào để lưu lại.",
                    actionLabel = "Khám phá phim ngay",
                    onAction = onExploreClick,
                )
            } else {
                MovieGrid(
                    movies = favorites.map { it.toMovie() },
                    onMovieClick = { movie -> onMovieClick(movie.slug) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            if (history.isEmpty()) {
                EmptyView(
                    message = "Chưa có lịch sử xem phim.\nNhững bộ phim bạn đã xem sẽ tự động lưu tại đây.",
                    actionLabel = "Bắt đầu xem phim",
                    onAction = onExploreClick,
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 6.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = viewModel::clearHistory) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = CinemaColors.TextMuted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Xoá toàn bộ lịch sử",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.TextMuted,
                            )
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            bottom = if (isTv) 32.dp else 80.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = history,
                            key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" },
                        ) { entry ->
                            HistoryCard(entry = entry, onClick = { onWatchClick(entry) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvLibraryScreen(
    favorites: List<FavoriteMovie>,
    history: List<WatchEntry>,
    viewModel: LibraryViewModel,
    onMovieClick: (String) -> Unit,
    onWatchClick: (WatchEntry) -> Unit,
    onExploreClick: (() -> Unit)?,
) {
    val continuing = history.filter { it.durationMs <= 0L || it.positionMs.toDouble() / it.durationMs < 0.95 }
    var tab by rememberSaveable { mutableIntStateOf(if (continuing.isNotEmpty()) 0 else 1) }
    var continuingSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var favoriteSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var historySlug by rememberSaveable { mutableStateOf<String?>(null) }
    var contextSlug by remember { mutableStateOf<String?>(null) }
    var removePending by remember { mutableStateOf<String?>(null) }
    var clearHistoryOpen by remember { mutableStateOf(false) }
    val tabRequesters = remember { List(3) { FocusRequester() } }
    val optionsRequester = remember { FocusRequester() }
    val clearRequester = remember { FocusRequester() }
    val tabStates = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    val hasItems = when (tab) { 0 -> continuing.isNotEmpty(); 1 -> favorites.isNotEmpty(); else -> history.isNotEmpty() }
    val modalOpen = contextSlug != null || removePending != null || clearHistoryOpen
    fun restoreTo(requester: FocusRequester) {
        scope.launch { withFrameNanos { }; requester.requestFocus() }
    }
    fun closeContext() {
        contextSlug = null
        restoreTo(if (hasItems) optionsRequester else tabRequesters[tab])
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Thư viện", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary, modifier = Modifier.weight(1f))
                if (tab == 2 && history.isNotEmpty()) {
                    TvBrowseButton("Xóa lịch sử", { clearHistoryOpen = true }, Modifier.focusRequester(clearRequester), navigateUpToMenu = true)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val counts = listOf(continuing.size, favorites.size, history.size)
                listOf("Tiếp tục xem", "Đã lưu", "Lịch sử").forEachIndexed { index, label ->
                    TvBrowseButton("$label (${counts[index]})", { tab = index }, Modifier.focusRequester(tabRequesters[index]), isSelected = tab == index, navigateUpToMenu = true)
                }
                Spacer(Modifier.weight(1f))
                if (hasItems) {
                    TvBrowseButton("Tùy chọn", {
                        contextSlug = when (tab) {
                            0 -> continuingSlug?.takeIf { slug -> continuing.any { it.slug == slug } } ?: continuing.firstOrNull()?.slug
                            1 -> favoriteSlug?.takeIf { slug -> favorites.any { it.slug == slug } } ?: favorites.firstOrNull()?.slug
                            else -> historySlug?.takeIf { slug -> history.any { it.slug == slug } } ?: history.firstOrNull()?.slug
                        }
                    }, Modifier.focusRequester(optionsRequester), navigateUpToMenu = true)
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            tabStates.SaveableStateProvider(tab) {
                when {
                    !hasItems -> TvBrowseMessage(
                        title = when (tab) { 0 -> "Không có phim đang xem dở"; 1 -> "Bạn chưa lưu phim nào"; else -> "Lịch sử xem đang trống" },
                        message = when (tab) { 0 -> "Phim bạn đang xem sẽ xuất hiện ở đây để tiếp tục."; 1 -> "Chọn Lưu tại trang chi tiết để thêm phim vào thư viện."; else -> "Những phim bạn đã xem trên thiết bị này sẽ xuất hiện ở đây." },
                        actionLabel = if (onExploreClick != null) "Khám phá phim" else null,
                        onAction = onExploreClick,
                    )
                    tab == 1 -> MovieGrid(
                        movies = favorites.map { it.toMovie() },
                        onMovieClick = { onMovieClick(it.slug) },
                        modifier = Modifier.fillMaxSize(),
                        focusKey = "library:saved",
                        topFocusRequester = tabRequesters[tab],
                        focusEnabled = !modalOpen,
                        onMovieFocused = { favoriteSlug = it.slug },
                    )
                    else -> TvLibraryWatchGrid(
                        entries = if (tab == 0) continuing else history,
                        historyMode = tab == 2,
                        onEntryClick = onWatchClick,
                        topRequester = tabRequesters[tab],
                        focusEnabled = !modalOpen,
                        onEntryFocused = { if (tab == 0) continuingSlug = it.slug else historySlug = it.slug },
                    )
                }
            }
        }
    }
    val selectedFavorite = favorites.firstOrNull { it.slug == contextSlug }
    val selectedEntry = history.firstOrNull { it.slug == contextSlug }
    if (contextSlug != null && (selectedFavorite != null || selectedEntry != null)) {
        val firstRequester = remember { FocusRequester() }
        Dialog(onDismissRequest = ::closeContext) {
            Column(Modifier.width(520.dp).background(CinemaColors.SurfaceElevated, RoundedCornerShape(12.dp)).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (tab == 1) selectedFavorite!!.name else selectedEntry!!.name, fontSize = 24.sp, lineHeight = 30.sp, color = CinemaColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                if (tab != 1 && selectedEntry != null) {
                    TvBrowseButton("Xem tiếp", { contextSlug = null; onWatchClick(selectedEntry) }, Modifier.fillMaxWidth().focusRequester(firstRequester), primary = true)
                }
                TvBrowseButton("Chi tiết phim", { val slug = contextSlug!!; contextSlug = null; onMovieClick(slug) }, Modifier.fillMaxWidth().then(if (tab == 1) Modifier.focusRequester(firstRequester) else Modifier))
                TvBrowseButton(if (tab == 1) "Bỏ lưu phim" else "Xóa lịch sử phim này", { removePending = contextSlug; contextSlug = null }, Modifier.fillMaxWidth())
                TvBrowseButton("Đóng", ::closeContext, Modifier.fillMaxWidth())
            }
            LaunchedEffect(Unit) { withFrameNanos { }; firstRequester.requestFocus() }
        }
    }
    if (removePending != null) {
        TvBrowseConfirmation(
            title = if (tab == 1) "Bỏ lưu phim này?" else "Xóa lịch sử phim này?",
            message = if (tab == 1) "Bạn có thể lưu lại phim từ trang chi tiết." else "Vị trí đã xem và tiến trình các tập của phim này sẽ được xóa trên thiết bị.",
            confirmLabel = if (tab == 1) "Bỏ lưu" else "Xóa lịch sử",
            onDismiss = { removePending = null; restoreTo(optionsRequester) },
            onConfirm = {
                val slug = removePending!!
                removePending = null
                if (tab == 1) viewModel.removeFavorite(slug) else viewModel.removeHistory(slug)
                restoreTo(tabRequesters[tab])
            },
        )
    }
    if (clearHistoryOpen) {
        TvBrowseConfirmation(
            "Xóa toàn bộ lịch sử?", "Vị trí đã xem của mọi phim trên thiết bị này sẽ bị xóa. Danh sách Đã lưu vẫn được giữ.", "Xóa lịch sử",
            onDismiss = { clearHistoryOpen = false; restoreTo(clearRequester) },
            onConfirm = { clearHistoryOpen = false; viewModel.clearHistory(); restoreTo(tabRequesters[tab]) },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TvLibraryWatchGrid(
    entries: List<WatchEntry>,
    historyMode: Boolean,
    onEntryClick: (WatchEntry) -> Unit,
    topRequester: FocusRequester,
    focusEnabled: Boolean,
    onEntryFocused: (WatchEntry) -> Unit,
) {
    val gridState = rememberLazyGridState()
    var focusedSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var restoredFocus by remember { mutableStateOf(false) }
    val requesters = remember { mutableMapOf<String, FocusRequester>() }
    val fallbackRequester = remember { FocusRequester() }
    val navigation = LocalTvNavigation.current
    val columns = if (historyMode) 1 else 3
    val fallbackSlug = focusedSlug.takeIf { slug -> entries.any { it.slug == slug } } ?: entries.first().slug
    LaunchedEffect(focusEnabled, navigation.isNavigationFocused) {
        if (!focusEnabled || navigation.isNavigationFocused || restoredFocus || focusedSlug == null) return@LaunchedEffect
        val index = entries.indexOfFirst { it.slug == fallbackSlug }.coerceAtLeast(0)
        if (gridState.layoutInfo.visibleItemsInfo.none { it.key == fallbackSlug }) gridState.scrollToItem(index)
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.any { it.key == fallbackSlug } }.first { it }
        withFrameNanos { }
        requesters[fallbackSlug]?.requestFocus()
        restoredFocus = true
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = gridState,
        modifier = Modifier.fillMaxSize().focusRestorer(fallback = fallbackRequester).focusGroup(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(entries, key = { it.slug }, contentType = { if (historyMode) "tv_history" else "tv_continue" }) { entry ->
            val index = entries.indexOf(entry)
            val requester = remember(entry.slug) { requesters.getOrPut(entry.slug) { FocusRequester() } }
            val itemModifier = Modifier.fillMaxWidth().focusRequester(requester)
                .then(if (entry.slug == fallbackSlug) Modifier.focusRequester(fallbackRequester) else Modifier)
                .onPreviewKeyEvent { event ->
                    if (index < columns && event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp) {
                        topRequester.requestFocus()
                        true
                    } else false
                }
            val onFocus: (Boolean) -> Unit = { if (it) { focusedSlug = entry.slug; onEntryFocused(entry) } }
            if (historyMode) {
                TvLibraryHistoryCard(entry, { onEntryClick(entry) }, itemModifier, onFocus)
            } else {
                TvContinueWatchingCard(entry, { onEntryClick(entry) }, itemModifier, onFocus)
            }
        }
    }
}

@Composable
private fun TvLibraryHistoryCard(entry: WatchEntry, onClick: () -> Unit, modifier: Modifier, onFocus: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val duration = entry.durationMs.coerceAtLeast(0L)
    val position = if (duration > 0L) entry.positionMs.coerceIn(0L, duration) else entry.positionMs.coerceAtLeast(0L)
    fun time(ms: Long): String = "${ms / 60_000}:${(ms / 1000 % 60).toString().padStart(2, '0')}"
    Row(
        modifier.cinemaFocusable(onClick = onClick, shape = shape, scaleFocused = 1f, onFocusChanged = onFocus)
            .background(CinemaColors.SurfaceCard, shape).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(entry.posterUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.width(156.dp).aspectRatio(16f / 9f).clip(shape).background(CinemaColors.SurfaceElevated))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(entry.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(entry.episodeName.ifBlank { "Tập ${entry.episodeIndex + 1}" }, fontSize = 16.sp, color = CinemaColors.TextSecondary)
            Text(if (duration > 0L) "Đã xem ${time(position)} / ${time(duration)}" else if (position > 0L) "Đã xem ${time(position)}" else "Chưa có vị trí đã xem", fontSize = 14.sp, color = CinemaColors.TextSecondary)
            if (duration > 0L) {
                Box(Modifier.fillMaxWidth().height(4.dp).background(CinemaColors.SurfaceElevated, shape)) {
                    Box(Modifier.fillMaxWidth((position.toDouble() / duration).toFloat()).height(4.dp).background(CinemaColors.Primary, shape))
                }
            }
        }
        Text(if (duration > 0L && position.toDouble() / duration >= 0.95) "Đã xem" else "Xem tiếp", fontSize = 16.sp, color = CinemaColors.TextSecondary)
    }
}

@Composable
private fun LibraryTabPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CinemaColors.Primary else Color.Transparent)
            .cinemaFocusable(onClick = onClick, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f)
                        else CinemaColors.SurfaceElevated,
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) Color.White else CinemaColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: WatchEntry,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CinemaColors.SurfaceCard)
            .border(1.dp, CinemaColors.BorderSubtle, cardShape)
            .cinemaFocusable(onClick = onClick, shape = cardShape, scaleFocused = 1.03f)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(CinemaColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = entry.posterUrl,
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CinemaColors.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleSmall,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaColors.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = entry.episodeName.ifBlank { "Tập ${entry.episodeIndex + 1}" },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = entry.serverName.ifBlank { "Đã lưu vị trí" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = CinemaColors.TextMuted,
                )
            }
        }
    }
}
