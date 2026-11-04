package nolelamphim.vercel.app.ui.screens.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import nolelamphim.vercel.app.data.Taxonomy
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieGrid

private val filterYears = (2026 downTo 1990).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: FilterViewModel = viewModel {
        FilterViewModel(AppContainer.movieRepository)
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
                text = "Bộ lọc phim",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        ChipSection(
            title = "Thể loại",
            items = state.genres,
            selectedSlug = state.selectedGenre,
            onSelect = viewModel::selectGenre,
        )
        ChipSection(
            title = "Quốc gia",
            items = state.countries,
            selectedSlug = state.selectedCountry,
            onSelect = viewModel::selectCountry,
        )

        Text(
            text = "Năm phát hành",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filterYears, key = { it }) { year ->
                FilterChip(
                    selected = state.selectedYear == year,
                    onClick = { viewModel.selectYear(year) },
                    label = { Text(year.toString()) },
                )
            }
        }

        Text(
            text = "Sắp xếp",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SortOption.entries, key = { it.name }) { option ->
                FilterChip(
                    selected = state.sort == option,
                    onClick = { viewModel.selectSort(option) },
                    label = { Text(option.label) },
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = viewModel::apply,
                modifier = Modifier.weight(1f),
            ) {
                Text("Áp dụng bộ lọc")
            }
            TextButton(onClick = viewModel::reset) {
                Text("Đặt lại")
            }
        }

        when {
            state.isLoading && state.results.isEmpty() -> LoadingView()

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::apply,
            )

            state.results.isEmpty() -> EmptyView("Chọn bộ lọc rồi bấm Áp dụng")

            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::apply,
                modifier = Modifier.weight(1f),
            ) {
                MovieGrid(
                    movies = sortMovies(state.results, state.sort),
                    onMovieClick = onMovieClick,
                    onLoadMore = viewModel::loadMore,
                )
            }
        }
    }
}

@Composable
private fun ChipSection(
    title: String,
    items: List<Taxonomy>,
    selectedSlug: String?,
    onSelect: (String?) -> Unit,
) {
    if (items.isEmpty()) return

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.slug }) { item ->
            FilterChip(
                selected = selectedSlug == item.slug,
                onClick = { onSelect(item.slug) },
                label = { Text(item.name) },
            )
        }
    }
}
