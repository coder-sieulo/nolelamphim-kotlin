package nolelamphim.vercel.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieGrid
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable
import nolelamphim.vercel.app.ui.tv.TvBrowseButton
import nolelamphim.vercel.app.ui.tv.TvBrowseConfirmation
import nolelamphim.vercel.app.ui.tv.TvBrowseMessage
import nolelamphim.vercel.app.ui.tv.LocalTvNavigation

@Composable
fun SearchScreen(
    initialQuery: String? = null,
    onMovieClick: (Movie) -> Unit,
    onOpenFilter: () -> Unit,
    viewModel: SearchViewModel = viewModel(key = "search_${initialQuery.orEmpty()}") {
        SearchViewModel(
            repository = AppContainer.movieRepository,
            localLibrary = AppContainer.localLibrary,
            initialQuery = initialQuery,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    if (isTv) {
        TvSearchScreen(state, searchHistory, viewModel, onMovieClick, onOpenFilter)
        return
    }
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTv) Color.Transparent else CinemaColors.Background),
    ) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Tìm kiếm phim",
                style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            // Search Bar Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Nhập tên phim, diễn viên, anime...",
                            color = CinemaColors.TextMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CinemaColors.Primary,
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = CinemaColors.TextSecondary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaColors.Primary)
                                    .cinemaFocusable(
                                        onClick = { viewModel.submitSearch() },
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .clickable { viewModel.submitSearch() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Tìm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CinemaColors.SurfaceElevated,
                        unfocusedContainerColor = CinemaColors.SurfaceCard,
                        focusedBorderColor = CinemaColors.Primary,
                        unfocusedBorderColor = CinemaColors.BorderSubtle,
                        focusedTextColor = CinemaColors.TextPrimary,
                        unfocusedTextColor = CinemaColors.TextPrimary,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() }),
                )

                // Filter button (Navigates to Explore / Filter screen)
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CinemaColors.SurfaceCard)
                        .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                        .cinemaFocusable(onClick = onOpenFilter, shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenFilter)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bộ lọc",
                            tint = CinemaColors.Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Bộ lọc",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        // Content Area
        when {
            state.isLoading && state.results.isEmpty() && state.query.isNotBlank() ->
                LoadingView(message = "Đang tìm kiếm '${state.query}'...")

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::retry,
            )

            state.query.isBlank() -> RecentSearchesView(
                history = searchHistory,
                onQueryClick = viewModel::onHistoryClick,
                onClear = viewModel::clearHistory,
                horizontalPadding = horizontalPadding,
            )

            state.results.isEmpty() -> EmptyView(
                message = "Không tìm thấy phim phù hợp với '${state.query}'",
                actionLabel = "Mở bộ lọc nâng cao",
                onAction = onOpenFilter,
            )

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 4.dp),
                ) {
                    Text(
                        text = "Tìm thấy ${state.results.size} kết quả",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextMuted,
                    )
                }
                MovieGrid(
                    movies = state.results,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.weight(1f),
                    onLoadMore = viewModel::loadMore,
                )
            }
        }
    }
}

@Composable
private fun TvSearchScreen(
    state: SearchUiState,
    history: List<String>,
    viewModel: SearchViewModel,
    onMovieClick: (Movie) -> Unit,
    onOpenFilter: () -> Unit,
) {
    val inputRequester = remember { FocusRequester() }
    val clearHistoryRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val navigation = LocalTvNavigation.current
    var restoreResults by rememberSaveable { mutableStateOf(false) }
    var focusResults by remember { mutableStateOf(false) }
    var showClearHistory by remember { mutableStateOf(false) }
    var inputFocusedInitially by remember { mutableStateOf(false) }
    fun submit() {
        if (state.query.isBlank()) return
        focusResults = true
        keyboard?.hide()
        viewModel.submitSearch()
    }
    LaunchedEffect(navigation.isNavigationFocused) {
        if (!restoreResults && !inputFocusedInitially && !navigation.isNavigationFocused) {
            withFrameNanos { }
            inputRequester.requestFocus()
            inputFocusedInitially = true
        }
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Tìm kiếm", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = {
                        restoreResults = false
                        focusResults = false
                        viewModel.onQueryChange(it)
                    },
                    placeholder = { Text("Nhập tên phim", fontSize = 18.sp, color = CinemaColors.TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = CinemaColors.TextSecondary) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).focusRequester(inputRequester)
                        .onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionUp) {
                                keyboard?.hide()
                                navigation.focusNavigation()
                                true
                            } else false
                        },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = CinemaColors.BorderSubtle,
                        focusedContainerColor = CinemaColors.SurfaceCard,
                        unfocusedContainerColor = CinemaColors.SurfaceCard,
                        focusedTextColor = CinemaColors.TextPrimary,
                        unfocusedTextColor = CinemaColors.TextPrimary,
                    ),
                )
                TvBrowseButton("Tìm", ::submit, primary = true, enabled = state.query.isNotBlank(), navigateUpToMenu = true)
                TvBrowseButton("Bộ lọc", onOpenFilter, navigateUpToMenu = true)
            }
            Text(
                when {
                    state.isLoading -> "Đang tìm “${state.query.trim()}”…"
                    state.query.isBlank() -> "Dùng bàn phím trên TV để nhập tên phim."
                    state.results.isNotEmpty() -> "${state.results.size} phim đã tải cho “${state.resultsQuery}”"
                    else -> "Kết quả tìm kiếm"
                },
                fontSize = 15.sp, color = CinemaColors.TextSecondary,
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.query.isBlank() -> Column(
                    Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Tìm gần đây", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = CinemaColors.TextPrimary, modifier = Modifier.weight(1f))
                        if (history.isNotEmpty()) TvBrowseButton("Xóa lịch sử tìm", { showClearHistory = true }, Modifier.focusRequester(clearHistoryRequester))
                    }
                    if (history.isEmpty()) {
                        Text("Tên phim bạn tìm sẽ được lưu ở đây.", fontSize = 16.sp, color = CinemaColors.TextSecondary)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(4.dp)) {
                            items(history, key = { it }) { query ->
                                TvBrowseButton(query, {
                                    keyboard?.hide()
                                    focusResults = true
                                    viewModel.onHistoryClick(query)
                                })
                            }
                        }
                    }
                }
                state.isLoading && state.results.isEmpty() -> TvBrowseMessage("Đang tìm phim", "Kết quả sẽ xuất hiện tại đây.")
                state.error != null && state.results.isEmpty() -> TvBrowseMessage(
                    "Chưa tìm được phim", "Kiểm tra kết nối rồi thử lại.", "Thử lại", viewModel::retry,
                    requestFocus = focusResults,
                )
                state.results.isEmpty() -> TvBrowseMessage(
                    "Không tìm thấy “${state.query.trim()}”", "Thử tên ngắn hơn hoặc kiểm tra chính tả.",
                    "Sửa từ khóa", { inputRequester.requestFocus() }, requestFocus = focusResults,
                )
                else -> key(state.resultsQuery) {
                    MovieGrid(
                        movies = state.results,
                        onMovieClick = {
                            restoreResults = true
                            keyboard?.hide()
                            onMovieClick(it)
                        },
                        modifier = Modifier.fillMaxSize(),
                        onLoadMore = if (state.error == null && !state.isLoading) viewModel::loadMore else null,
                        focusKey = "search:${state.resultsQuery}",
                        requestInitialFocus = restoreResults || (focusResults && !state.isLoading && state.resultsQuery == state.query.trim()),
                        topFocusRequester = inputRequester,
                        focusEnabled = !showClearHistory,
                    )
                }
            }
        }
        if (state.error != null && state.results.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa cập nhật được kết quả", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.weight(1f))
                TvBrowseButton("Thử lại", viewModel::retry)
            }
        }
        if (state.isLoadingMore) Text("Đang tải thêm…", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
    }
    if (showClearHistory) {
        TvBrowseConfirmation(
            "Xóa lịch sử tìm kiếm?", "Các từ khóa đã tìm trên thiết bị này sẽ được xóa.", "Xóa lịch sử",
            onDismiss = {
                showClearHistory = false
                scope.launch { withFrameNanos { }; clearHistoryRequester.requestFocus() }
            },
            onConfirm = {
                showClearHistory = false
                viewModel.clearHistory()
                scope.launch { withFrameNanos { }; inputRequester.requestFocus() }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesView(
    history: List<String>,
    onQueryClick: (String) -> Unit,
    onClear: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = CinemaColors.Primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Tìm kiếm gần đây",
                        style = MaterialTheme.typography.titleMedium,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                TextButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = CinemaColors.TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Xoá tất cả",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextMuted,
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                history.forEach { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(8.dp))
                            .cinemaFocusable(
                                onClick = { onQueryClick(item) },
                                shape = RoundedCornerShape(8.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaColors.TextSecondary,
                        )
                    }
                }
            }
        }

        // Suggestions section
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Gợi ý thể loại phổ biến",
            style = MaterialTheme.typography.titleSmall,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )

        val trendingTags = listOf(
            "Hành Động", "Chiếu Rạp", "Anime", "Tình Cảm", "Kinh Dị",
            "Cổ Trang", "Khoa Học Viễn Tưởng", "Hài Hước", "Võ Thuật",
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            trendingTags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaColors.SurfaceElevated)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .cinemaFocusable(
                            onClick = { onQueryClick(tag) },
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaColors.TextPrimary,
                    )
                }
            }
        }
    }
}
