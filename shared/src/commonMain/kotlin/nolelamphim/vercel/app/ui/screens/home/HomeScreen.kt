package nolelamphim.vercel.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    onSeeAll: (String) -> Unit,
    onContinueClick: (WatchEntry) -> Unit,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository, AppContainer.localLibrary)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingView()

        state.error != null && state.latest.isEmpty() -> ErrorView(
            message = state.error.orEmpty(),
            onRetry = viewModel::load,
        )

        state.hero == null && state.latest.isEmpty() -> EmptyView(
            message = "Chưa có phim để hiển thị",
        )

        else -> PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    HomeBrandHeader()
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
                        title = "Mới cập nhật",
                        movies = state.latest,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.LATEST) },
                    )
                }
                item {
                    MovieRow(
                        title = "Phim bộ",
                        movies = state.series,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.SERIES) },
                    )
                }
                item {
                    MovieRow(
                        title = "Phim lẻ",
                        movies = state.singles,
                        onMovieClick = onMovieClick,
                        onSeeAll = { onSeeAll(CatalogType.SINGLES) },
                    )
                }
                item {
                    MovieRow(
                        title = "Phim chiếu rạp",
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
private fun HomeBrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AsyncImage(
            model = "https://nolelamphim.vercel.app/logo-64.png",
            contentDescription = "Logo Nô Lệ Làm Phim",
            modifier = Modifier
                .width(32.dp)
                .height(32.dp),
        )
        Text(
            text = "Nô Lệ Làm Phim",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
