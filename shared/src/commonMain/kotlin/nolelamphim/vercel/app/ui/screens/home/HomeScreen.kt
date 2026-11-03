package nolelamphim.vercel.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.HeroBanner
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow

@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit,
    viewModel: HomeViewModel = viewModel {
        HomeViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingView()

        state.error != null -> ErrorView(
            message = state.error.orEmpty(),
            onRetry = viewModel::load,
        )

        state.hero == null && state.latest.isEmpty() -> EmptyView(
            message = "Chưa có phim để hiển thị",
        )

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "Nolelamphim",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                state.hero?.let { hero ->
                    item {
                        HeroBanner(
                            movie = hero,
                            onClick = { onMovieClick(hero) },
                        )
                    }
                }
                item {
                    MovieRow(
                        title = "Mới cập nhật",
                        movies = state.latest,
                        onMovieClick = onMovieClick,
                    )
                }
                item {
                    MovieRow(
                        title = "Phim bộ",
                        movies = state.series,
                        onMovieClick = onMovieClick,
                    )
                }
                item {
                    MovieRow(
                        title = "Phim lẻ",
                        movies = state.singles,
                        onMovieClick = onMovieClick,
                    )
                }
                item {
                    MovieRow(
                        title = "Phim chiếu rạp",
                        movies = state.cinema,
                        onMovieClick = onMovieClick,
                    )
                }
            }
        }
    }
}
