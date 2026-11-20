package nolelamphim.vercel.app.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import nolelamphim.vercel.app.ui.tv.TvBrowseMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    type: String,
    title: String,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: CatalogViewModel = viewModel {
        CatalogViewModel(AppContainer.movieRepository, type, title)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    if (isTv) {
        TvCatalogScreen(type, state, viewModel, onBack, onMovieClick)
        return
    }
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTv) Color.Transparent else CinemaColors.Background),
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CinemaColors.SurfaceCard)
                    .border(1.dp, CinemaColors.BorderSubtle, CircleShape)
                    .cinemaFocusable(onClick = onBack, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column {
                Text(
                    text = state.title,
                    style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                )
                if (state.movies.isNotEmpty()) {
                    Text(
                        text = "Danh sách tổng hợp",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.Primary,
                    )
                }
            }
        }

        when {
            state.isLoading -> LoadingView(message = "Đang tải danh mục ${state.title}...")

            state.error != null && state.movies.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::loadFirstPage,
            )

            state.movies.isEmpty() -> EmptyView("Chưa có phim trong danh mục này")

            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                MovieGrid(
                    movies = state.movies,
                    onMovieClick = onMovieClick,
                    onLoadMore = viewModel::loadMore,
                )
            }
        }
    }
}

@Composable
private fun TvCatalogScreen(
    type: String,
    state: CatalogUiState,
    viewModel: CatalogViewModel,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
) {
    val backRequester = remember { FocusRequester() }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            TvBrowseButton("Quay lại", onBack, Modifier.focusRequester(backRequester), navigateUpToMenu = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(state.title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
                Text(
                    if (state.isRefreshing) "Đang cập nhật danh sách…" else "${state.movies.size} phim đã tải",
                    fontSize = 15.sp, color = CinemaColors.TextSecondary,
                )
            }
            TvBrowseButton("Tải lại", viewModel::refresh, enabled = !state.isRefreshing && !state.isLoading, navigateUpToMenu = true)
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when {
                state.isLoading && state.movies.isEmpty() -> TvBrowseMessage("Đang tải phim", "Danh sách sẽ xuất hiện tại đây.")
                state.error != null && state.movies.isEmpty() -> TvBrowseMessage("Chưa tải được danh sách", "Kiểm tra kết nối rồi thử lại.", "Thử lại", viewModel::loadFirstPage, requestFocus = true)
                state.movies.isEmpty() -> TvBrowseMessage("Danh mục chưa có phim", "Thử khám phá một danh mục khác.", "Quay lại", onBack, requestFocus = true)
                else -> MovieGrid(
                    movies = state.movies,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.fillMaxSize(),
                    onLoadMore = if (state.error == null) viewModel::loadMore else null,
                    focusKey = "catalog:$type",
                    requestInitialFocus = true,
                    topFocusRequester = backRequester,
                )
            }
        }
        if (state.error != null && state.movies.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa cập nhật được danh sách", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.weight(1f))
                TvBrowseButton("Thử lại", if (state.hasMore) viewModel::loadMore else viewModel::refresh)
            }
        }
        if (state.isLoadingMore) Text("Đang tải thêm…", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
    }
}
