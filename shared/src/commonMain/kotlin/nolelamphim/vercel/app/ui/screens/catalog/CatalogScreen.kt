package nolelamphim.vercel.app.ui.screens.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieGrid

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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("← Quay lại") }
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        when {
            state.isLoading -> LoadingView()

            state.error != null && state.movies.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::loadFirstPage,
            )

            state.movies.isEmpty() -> EmptyView("Chưa có phim")

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
