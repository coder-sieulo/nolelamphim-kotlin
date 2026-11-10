package nolelamphim.vercel.app.ui.screens.filter

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import nolelamphim.vercel.app.data.Taxonomy
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieGrid
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

private val filterYears = (2026 downTo 1995).toList()

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
    val isTv = LocalIsTv.current
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Bộ lọc phim",
                    style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Khám phá phim theo thể loại, quốc gia và năm",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaColors.TextMuted,
                )
            }
        }

        // Filter Rows
        ChipSection(
            title = "Thể loại",
            items = state.genres,
            selectedSlug = state.selectedGenre,
            onSelect = viewModel::selectGenre,
            horizontalPadding = horizontalPadding,
        )

        ChipSection(
            title = "Quốc gia",
            items = state.countries,
            selectedSlug = state.selectedCountry,
            onSelect = viewModel::selectCountry,
            horizontalPadding = horizontalPadding,
        )

        // Year section
        Text(
            text = "Năm phát hành",
            style = MaterialTheme.typography.titleSmall,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filterYears, key = { it }) { year ->
                val isSelected = state.selectedYear == year
                SelectablePill(
                    label = year.toString(),
                    isSelected = isSelected,
                    onClick = { viewModel.selectYear(year) },
                )
            }
        }

        // Sort section
        Text(
            text = "Sắp xếp theo",
            style = MaterialTheme.typography.titleSmall,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(SortOption.entries, key = { it.name }) { option ->
                val isSelected = state.sort == option
                SelectablePill(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = { viewModel.selectSort(option) },
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = viewModel::apply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .cinemaFocusable(onClick = viewModel::apply, shape = RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Áp dụng bộ lọc", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = viewModel::reset,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CinemaColors.TextSecondary,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .cinemaFocusable(onClick = viewModel::reset, shape = RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Đặt lại", fontWeight = FontWeight.SemiBold)
            }
        }

        // Results
        when {
            state.isLoading && state.results.isEmpty() ->
                LoadingView(message = "Đang áp dụng bộ lọc...")

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::apply,
            )

            state.results.isEmpty() -> EmptyView(
                message = "Chọn tiêu chí lọc bên trên rồi bấm Áp dụng bộ lọc",
            )

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
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    if (items.isEmpty()) return

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = CinemaColors.TextPrimary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 4.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.slug }) { item ->
            val isSelected = selectedSlug == item.slug
            SelectablePill(
                label = item.name,
                isSelected = isSelected,
                onClick = { onSelect(item.slug) },
            )
        }
    }
}

@Composable
private fun SelectablePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) CinemaColors.Primary else CinemaColors.SurfaceCard)
            .border(
                1.dp,
                if (isSelected) CinemaColors.Primary else CinemaColors.BorderSubtle,
                shape,
            )
            .cinemaFocusable(onClick = onClick, shape = shape, scaleFocused = 1.05f)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}
