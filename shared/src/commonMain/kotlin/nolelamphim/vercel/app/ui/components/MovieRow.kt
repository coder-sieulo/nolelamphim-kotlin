package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.model.Movie

@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    if (movies.isEmpty()) return

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem((listState.firstVisibleItemIndex - 3).coerceAtLeast(0))
                    }
                },
                enabled = listState.firstVisibleItemIndex > 0,
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Cuộn sang trái")
            }
            IconButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(
                            (listState.firstVisibleItemIndex + 3).coerceAtMost(movies.lastIndex),
                        )
                    }
                },
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Cuộn sang phải")
            }
            if (onSeeAll != null) {
                TextButton(onClick = onSeeAll) {
                    Text("Xem tất cả")
                }
            }
        }
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    modifier = Modifier.width(132.dp),
                )
            }
        }
    }
}
