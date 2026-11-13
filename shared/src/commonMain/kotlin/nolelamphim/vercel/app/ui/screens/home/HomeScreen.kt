package nolelamphim.vercel.app.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.layout.ContentScale
import nolelamphim.shared.generated.resources.Res
import nolelamphim.shared.generated.resources.nolelamphim_logo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.BackdropMovieRow
import nolelamphim.vercel.app.ui.components.ContinueWatchingRow
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.HeroBanner
import nolelamphim.vercel.app.ui.components.HeroBannerCarousel
import nolelamphim.vercel.app.ui.components.HomeSkeletonScreen
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.components.SpotlightMovieCard
import nolelamphim.vercel.app.ui.components.TopRankingRow
import nolelamphim.vercel.app.ui.screens.catalog.CatalogType
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    onSeeAll: (String) -> Unit,
    onContinueClick: (WatchEntry) -> Unit,
    onOpenSearch: (() -> Unit)? = null,
    onOpenFilter: (() -> Unit)? = null,
    onSearchSubmit: ((String) -> Unit)? = null,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository, AppContainer.localLibrary)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    when {
        state.isLoading -> HomeSkeletonScreen()

        state.error != null && state.latest.isEmpty() -> ErrorView(
            message = state.error.orEmpty(),
            onRetry = viewModel::load,
        )

        state.hero == null && state.latest.isEmpty() -> EmptyView(
            message = "Chưa có phim để hiển thị",
            actionLabel = "Tải lại",
            onAction = viewModel::load,
        )

        else -> {
            val listState = rememberLazyListState()
            val isScrolled by remember {
                derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 30 }
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinemaColors.Background),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = if (isTv) 32.dp else 96.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isTv) 24.dp else 20.dp),
                    ) {
                        // 1. Showcase Hero Banner Carousel (At the very top, full bleed)
                        val heroMovies = (listOfNotNull(state.hero) + state.latest.take(5)).distinctBy { it.id }
                        if (heroMovies.isNotEmpty()) {
                            item {
                                HeroBannerCarousel(
                                    movies = heroMovies,
                                    onMovieClick = onMovieClick,
                                )
                            }
                        }

                        // 2. Quick Category Filter Hub Row
                        if (!isTv) {
                            item {
                                QuickCategoryFilterRow(onSelectCategory = onSeeAll)
                            }
                        }

                        // 3. Continue Watching (16:9 cards)
                        if (continueWatching.isNotEmpty()) {
                            item {
                                ContinueWatchingRow(
                                    entries = continueWatching,
                                    onEntryClick = onContinueClick,
                                )
                            }
                        }

                        // 4. Netflix-style Top 10 Ranking Row
                        val topMovies = (if (state.cinema.isNotEmpty()) state.cinema else state.latest).take(10)
                        if (topMovies.isNotEmpty()) {
                            item {
                                TopRankingRow(
                                    title = "Top 10 Thịnh Hành Hôm Nay",
                                    movies = topMovies,
                                    onMovieClick = onMovieClick,
                                )
                            }
                        }

                        // 5. Phim Chiếu Rạp Đỉnh Cao (2:3 Vertical Cards)
                        if (state.cinema.isNotEmpty()) {
                            item {
                                MovieRow(
                                    title = "Phim chiếu rạp đỉnh cao",
                                    movies = state.cinema,
                                    onMovieClick = onMovieClick,
                                    onSeeAll = { onSeeAll(CatalogType.CINEMA) },
                                )
                            }
                        }

                        // 6. Mid-Feed Spotlight Feature Banner
                        val spotlight = state.singles.firstOrNull() ?: state.latest.getOrNull(2)
                        if (spotlight != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = if (isTv) 24.dp else 16.dp),
                                ) {
                                    SpotlightMovieCard(
                                        movie = spotlight,
                                        onClick = { onMovieClick(spotlight) },
                                    )
                                }
                            }
                        }

                        // 7. Phim Bộ Mới Nhất (16:9 Landscape Scene Shots)
                        if (state.series.isNotEmpty()) {
                            item {
                                BackdropMovieRow(
                                    title = "Phim bộ chọn lọc đặc sắc",
                                    movies = state.series,
                                    onMovieClick = onMovieClick,
                                    onSeeAll = { onSeeAll(CatalogType.SERIES) },
                                )
                            }
                        }

                        // 8. Phim Mới Cập Nhật (2:3 Vertical Cards)
                        if (state.latest.isNotEmpty()) {
                            item {
                                MovieRow(
                                    title = "Phim mới cập nhật",
                                    movies = state.latest,
                                    onMovieClick = onMovieClick,
                                    onSeeAll = { onSeeAll(CatalogType.LATEST) },
                                )
                            }
                        }

                        // 9. Phim Lẻ Đặc Sắc (2:3 Vertical Cards)
                        if (state.singles.isNotEmpty()) {
                            item {
                                MovieRow(
                                    title = "Phim lẻ đặc sắc",
                                    movies = state.singles,
                                    onMovieClick = onMovieClick,
                                    onSeeAll = { onSeeAll(CatalogType.SINGLES) },
                                )
                            }
                        }
                    }

                    // Header: TV Top Bar or Mobile Pinned Header
                    if (isTv) {
                        TvTopBar(
                            onSearch = { isSearchExpanded = !isSearchExpanded },
                            onFilter = onOpenFilter,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    } else {
                        MobileBrandHeader(
                            isScrolled = isScrolled,
                            onSearch = { isSearchExpanded = !isSearchExpanded },
                            onFilter = onOpenFilter,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }

                    // Scrim when search dropdown is open
                    if (isSearchExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .clickable { isSearchExpanded = false },
                        )
                    }

                    // Floating Web-like Search Dropdown Panel
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .then(
                                if (!isTv) Modifier.statusBarsPadding().padding(top = 58.dp)
                                else Modifier.padding(top = 80.dp),
                            )
                            .padding(horizontal = if (isTv) 32.dp else 16.dp),
                    ) {
                        HomeSearchDropdown(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            searchHistory = searchHistory,
                            onSearch = { q ->
                                val cleanQuery = q.trim()
                                    .removePrefix("🔥 ").removePrefix("🍿 ")
                                    .removePrefix("🎬 ").removePrefix("⚡ ")
                                    .removePrefix("✨ ").removePrefix("💕 ")
                                    .removePrefix("💥 ").removePrefix("👻 ")
                                if (cleanQuery.isNotBlank()) {
                                    isSearchExpanded = false
                                    if (onSearchSubmit != null) {
                                        onSearchSubmit(cleanQuery)
                                    } else {
                                        onOpenSearch?.invoke()
                                    }
                                }
                            },
                            onClose = { isSearchExpanded = false },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSearchDropdown(
    query: String,
    onQueryChange: (String) -> Unit,
    searchHistory: List<String>,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
) {
    val trendingTags = listOf(
        "🔥 Chiếu rạp",
        "🍿 Phim bộ",
        "🎬 Phim lẻ",
        "⚡ Mới nhất",
        "✨ Anime",
        "💕 Tình cảm",
        "💥 Hành động",
        "👻 Kinh dị",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .shadow(24.dp, RoundedCornerShape(18.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xF5141420))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CinemaColors.Primary),
                    )
                    Text(
                        text = "Tìm kiếm phim",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Input Field with Action Button
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Nhập tên phim, diễn viên, đạo diễn...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaColors.TextMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm",
                        tint = CinemaColors.Primary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 6.dp),
                    ) {
                        if (query.isNotBlank()) {
                            IconButton(
                                onClick = { onQueryChange("") },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa",
                                    tint = CinemaColors.TextMuted,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CinemaColors.Primary)
                                .clickable { onSearch(query) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF0F0F17),
                    unfocusedContainerColor = Color(0xFF0F0F17),
                    focusedBorderColor = CinemaColors.Primary,
                    unfocusedBorderColor = CinemaColors.BorderSubtle,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = CinemaColors.Primary,
                ),
            )

            // Trending Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gợi ý thịnh hành",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 2.dp),
                ) {
                    items(trendingTags) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.07f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .clickable { onSearch(tag) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // Search History
            if (searchHistory.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tìm kiếm gần đây",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 2.dp),
                    ) {
                        items(searchHistory.take(6)) { historyQuery ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF1B1B26))
                                    .border(1.dp, CinemaColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                    .clickable { onSearch(historyQuery) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = CinemaColors.Primary,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text = historyQuery,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCategoryFilterRow(
    onSelectCategory: (String) -> Unit,
) {
    val categories = listOf(
        CategoryHubItem("🔥 Chiếu rạp", CatalogType.CINEMA, CinemaColors.Primary),
        CategoryHubItem("🍿 Phim bộ", CatalogType.SERIES, CinemaColors.NeonCyan),
        CategoryHubItem("🎬 Phim lẻ", CatalogType.SINGLES, CinemaColors.NeonViolet),
        CategoryHubItem("⚡ Mới nhất", CatalogType.LATEST, CinemaColors.NeonEmerald),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(categories.size) { index ->
            val item = categories[index]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF161622))
                    .border(1.dp, item.accentColor.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                    .cinemaFocusable(
                        onClick = { onSelectCategory(item.type) },
                        shape = RoundedCornerShape(22.dp),
                        scaleFocused = 1.06f,
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private data class CategoryHubItem(
    val label: String,
    val type: String,
    val accentColor: Color,
)

@Composable
private fun MobileBrandHeader(
    isScrolled: Boolean,
    onSearch: (() -> Unit)?,
    onFilter: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isScrolled) Color(0xF50D0D11) else Color.Transparent,
        animationSpec = tween(280),
        label = "header-bg",
    )
    val borderBottomColor by animateColorAsState(
        targetValue = if (isScrolled) Color.White.copy(alpha = 0.08f) else Color.Transparent,
        animationSpec = tween(280),
        label = "header-border",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .drawBehind {
                if (isScrolled) {
                    drawLine(
                        color = borderBottomColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF14141E))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.nolelamphim_logo),
                        contentDescription = "Nô Lệ Làm Phim",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }
                Column {
                    Text(
                        text = "NÔ LỆ LÀM PHIM",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = "Cinema Streaming",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onSearch != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = onSearch, modifier = Modifier.size(38.dp)) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                if (onFilter != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = onFilter, modifier = Modifier.size(38.dp)) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Bộ lọc",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvTopBar(
    onSearch: (() -> Unit)?,
    onFilter: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.85f),
                    0.6f to Color.Black.copy(alpha = 0.4f),
                    1f to Color.Transparent,
                ),
            )
            .padding(horizontal = 32.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF14141E))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nolelamphim_logo),
                    contentDescription = "Nô Lệ Làm Phim",
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            Column {
                Text(
                    text = "NÔ LỆ LÀM PHIM",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "Cinema Streaming for TV",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = CinemaColors.Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onSearch != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .cinemaFocusable(onClick = onSearch, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onSearch, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            if (onFilter != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .cinemaFocusable(onClick = onFilter, shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onFilter, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bộ lọc",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}

