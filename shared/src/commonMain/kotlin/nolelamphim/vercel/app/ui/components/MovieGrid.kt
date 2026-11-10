package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.LocalIsTv

@Composable
fun MovieGrid(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
) {
    val isTv = LocalIsTv.current
    val minItemSize = if (isTv) 160.dp else 105.dp
    val spacing = if (isTv) 18.dp else 12.dp
    val padding = if (isTv) 24.dp else 16.dp

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minItemSize),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = padding, end = padding, top = 12.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(if (isTv) 22.dp else 16.dp),
    ) {
        itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
            if (index >= movies.lastIndex - 3 && onLoadMore != null) {
                LaunchedEffect(index) { onLoadMore() }
            }
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
