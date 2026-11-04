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

@Composable
fun MovieGrid(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 112.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(movies, key = { _, movie -> movie.id }) { index, movie ->
            if (index == movies.lastIndex && onLoadMore != null) {
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
