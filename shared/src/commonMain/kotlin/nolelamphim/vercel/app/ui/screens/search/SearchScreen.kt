package nolelamphim.vercel.app.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    onOpenFilter: () -> Unit,
    viewModel: SearchViewModel = viewModel {
        SearchViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tìm kiếm",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tên phim...") },
                singleLine = true,
            )
            TextButton(onClick = onOpenFilter) {
                Text("Bộ lọc")
            }
        }

        when {
            state.isLoading && state.results.isEmpty() -> LoadingView()

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = { viewModel.onQueryChange(state.query) },
            )

            state.query.isBlank() -> EmptyView("Nhập tên phim để tìm kiếm")

            state.results.isEmpty() -> EmptyView("Không tìm thấy phim phù hợp")

            else -> MovieGrid(
                movies = state.results,
                onMovieClick = onMovieClick,
                modifier = Modifier.weight(1f),
                onLoadMore = viewModel::loadMore,
            )
        }
    }
}
