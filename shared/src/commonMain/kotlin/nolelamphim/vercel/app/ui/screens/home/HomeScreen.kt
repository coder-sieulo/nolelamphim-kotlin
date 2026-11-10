package nolelamphim.vercel.app.ui.screens.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import nolelamphim.vercel.app.ui.components.ContinueWatchingRow
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.HeroBanner
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow
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
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository, AppContainer.localLibrary)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current

    when {
        state.isLoading -> LoadingView(message = "Đang tải trang chủ...")

        state.error != null && state.latest.isEmpty() -> ErrorView(
            message = state.error.orEmpty(),
            onRetry = viewModel::load,
        )

        state.hero == null && state.latest.isEmpty() -> EmptyView(
            message = "Chưa có phim để hiển thị",
            actionLabel = "Tải lại",
            onAction = viewModel::load,
        )

        else -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .background(CinemaColors.Background),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (isTv) 32.dp else 90.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTv) 20.dp else 16.dp),
            ) {
                if (!isTv) {
                    item {
                        MobileBrandHeader(
                            onSearch = onOpenSearch,
                            onFilter = onOpenFilter,
                        )
                    }
                } else {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                state.hero?.let { hero ->
                    item {
                        HeroBanner(
                            movie = hero,
                            onClick = { onMovieClick(hero) },
                        )
                    }
                }

                if (continueWatching.isNotEmpty()) {
                    item {
                        ContinueWatchingRow(
                            entries = continueWatching,
                            onEntryClick = onContinueClick,
                        )
                    }
                }

                item {
                    MovieRow(
                        title = "Phim mới cập nhật",
                        movies = state.latest,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.LATEST) },
                    )
                }

                item {
                    MovieRow(
                        title = "Phim bộ chọn lọc",
                        movies = state.series,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.SERIES) },
                    )
                }

                item {
                    MovieRow(
                        title = "Phim lẻ đặc sắc",
                        movies = state.singles,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.SINGLES) },
                    )
                }

                item {
                    MovieRow(
                        title = "Phim chiếu rạp đỉnh cao",
                        movies = state.cinema,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.CINEMA) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileBrandHeader(
    onSearch: (() -> Unit)?,
    onFilter: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CinemaColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = "NÔ LỆ LÀM PHIM",
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "Cinema Streaming",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = CinemaColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onSearch != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CinemaColors.SurfaceElevated)
                        .border(1.dp, CinemaColors.BorderSubtle, CircleShape)
                        .cinemaFocusable(onClick = onSearch, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onSearch, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm",
                            tint = CinemaColors.TextPrimary,
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
                        .background(CinemaColors.SurfaceElevated)
                        .border(1.dp, CinemaColors.BorderSubtle, CircleShape)
                        .cinemaFocusable(onClick = onFilter, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onFilter, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bộ lọc",
                            tint = CinemaColors.TextPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
