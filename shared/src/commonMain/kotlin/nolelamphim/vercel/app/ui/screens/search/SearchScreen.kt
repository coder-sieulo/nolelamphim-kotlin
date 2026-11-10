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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    onOpenFilter: () -> Unit,
    viewModel: SearchViewModel = viewModel {
        SearchViewModel(AppContainer.movieRepository, AppContainer.localLibrary)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        // Search Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa",
                                    tint = CinemaColors.TextSecondary,
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

                // Filter button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CinemaColors.SurfaceCard)
                        .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                        .cinemaFocusable(onClick = onOpenFilter, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
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
                            text = "Lọc",
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
