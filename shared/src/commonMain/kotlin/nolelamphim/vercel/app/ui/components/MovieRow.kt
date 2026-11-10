package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null,
) {
    if (movies.isEmpty()) return

    val isTv = LocalIsTv.current
    val listState = rememberLazyListState()
    val cardWidth = if (isTv) 175.dp else 136.dp
    val horizontalPadding = if (isTv) 24.dp else 16.dp

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Orange accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CinemaColors.Primary),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (onSeeAll != null) {
                Row(
                    modifier = Modifier
                        .cinemaFocusable(
                            onClick = onSeeAll,
                            shape = RoundedCornerShape(8.dp),
                            scaleFocused = 1.05f,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Xem tất cả",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Xem tất cả",
                        tint = CinemaColors.Primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(if (isTv) 16.dp else 12.dp),
        ) {
            items(movies, key = { it.id }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    modifier = Modifier.width(cardWidth),
                )
            }
        }
    }
}
