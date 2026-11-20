package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.tv.LocalTvNavigation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MovieGrid(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: (() -> Unit)? = null,
    focusKey: String = "movies",
    requestInitialFocus: Boolean = false,
    topFocusRequester: FocusRequester? = null,
    focusEnabled: Boolean = true,
    onMovieFocused: ((Movie) -> Unit)? = null,
) {
    val isTv = LocalIsTv.current
    val minItemSize = if (isTv) 160.dp else 105.dp
    val spacing = if (isTv) 18.dp else 12.dp
    val padding = if (isTv) 24.dp else 16.dp

    val gridState = rememberLazyGridState()
    var focusedSlug by rememberSaveable(focusKey) { mutableStateOf<String?>(null) }
    var restoredFocus by remember(focusKey) { mutableStateOf(false) }
    val requesters = remember(focusKey) { mutableMapOf<String, FocusRequester>() }
    val fallbackRequester = remember(focusKey) { FocusRequester() }
    val tvNavigation = LocalTvNavigation.current
    val entries = remember(movies) { movies.distinctBy { it.slug } }
    val fallbackSlug = focusedSlug?.takeIf { slug -> entries.any { it.slug == slug } } ?: entries.firstOrNull()?.slug

    LaunchedEffect(focusKey, entries.isNotEmpty(), requestInitialFocus, focusEnabled, tvNavigation.isNavigationFocused) {
        if (!isTv || !focusEnabled || tvNavigation.isNavigationFocused || restoredFocus || entries.isEmpty() || (!requestInitialFocus && focusedSlug == null)) return@LaunchedEffect
        val target = entries.firstOrNull { it.slug == focusedSlug } ?: entries.first()
        val targetIndex = entries.indexOf(target)
        if (gridState.layoutInfo.visibleItemsInfo.none { it.key == target.slug }) {
            gridState.scrollToItem(targetIndex)
        }
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.any { it.key == target.slug } }.first { it }
        withFrameNanos { }
        requesters[target.slug]?.requestFocus()
        restoredFocus = true
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val tvColumnCount = if (maxWidth >= 900.dp) 6 else 5
        LazyVerticalGrid(
            columns = if (isTv) GridCells.Fixed(tvColumnCount) else GridCells.Adaptive(minSize = minItemSize),
            state = gridState,
            modifier = Modifier.fillMaxWidth().then(if (isTv) Modifier.focusRestorer(fallback = fallbackRequester).focusGroup() else Modifier),
            contentPadding = PaddingValues(start = padding, end = padding, top = 12.dp, bottom = if (isTv) 32.dp else 84.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(if (isTv) 22.dp else 16.dp),
        ) {
            itemsIndexed(entries, key = { _, movie -> movie.slug }, contentType = { _, _ -> "movie" }) { index, movie ->
                if (index >= entries.lastIndex - 3 && onLoadMore != null) {
                    LaunchedEffect(movie.slug, entries.size) { onLoadMore() }
                }
                val requester = remember(focusKey, movie.slug) {
                    requesters.getOrPut(movie.slug) { FocusRequester() }
                }
                MovieCard(
                    movie = movie,
                    onClick = {
                        focusedSlug = movie.slug
                        onMovieClick(movie)
                    },
                    onFocusChanged = {
                        if (it) {
                            focusedSlug = movie.slug
                            onMovieFocused?.invoke(movie)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().then(
                        if (isTv) Modifier.focusRequester(requester)
                            .then(if (movie.slug == fallbackSlug) Modifier.focusRequester(fallbackRequester) else Modifier)
                            .onPreviewKeyEvent { event ->
                            if (index < tvColumnCount && event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp) {
                                if (topFocusRequester != null) topFocusRequester.requestFocus()
                                else tvNavigation.focusNavigation()
                                true
                            } else false
                        } else Modifier,
                    ),
                )
            }
        }
    }
}
