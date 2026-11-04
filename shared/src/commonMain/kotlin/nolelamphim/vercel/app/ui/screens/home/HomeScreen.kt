package nolelamphim.vercel.app.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
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
                    HomeIntro(onLatestClick = { onSeeAll(CatalogType.LATEST) })
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
private fun HomeIntro(onLatestClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "home-intro")
    val lightOffset by transition.animateFloat(
        initialValue = -260f,
        targetValue = 620f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "home-intro-light",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF97316).copy(alpha = 0.18f),
                        Color(0xFF26262B).copy(alpha = 0.92f),
                        Color(0xFF1B1B1F),
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = lightOffset }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF97316), Color(0xFFFB923C).copy(alpha = 0.2f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = "NÔ LỆ LÀM PHIM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFB923C),
                )
                Text(
                    text = "Chọn phim cho tối nay",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "Những câu chuyện đáng để ngồi lại",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f),
                )
            }
            TextButton(onClick = onLatestClick) {
                Text(
                    text = "Mới nhất",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
