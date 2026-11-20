package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

/** Compatibility entry point; new TV browsing opens Detail directly. */
@Deprecated("Open the TV detail screen directly instead of a quick-detail overlay")
@Composable
fun TvMovieDetailModal(
    movie: Movie,
    onDismiss: () -> Unit,
    onPlayClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    onDetailClick: ((Movie) -> Unit)? = null,
) {
    val actionRequester = remember { FocusRequester() }
    LaunchedEffect(movie.slug) { actionRequester.requestFocus() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = modifier.widthIn(max = 560.dp).fillMaxWidth(0.8f)
                .background(CinemaColors.TvSurface, RoundedCornerShape(10.dp)).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineMedium,
                color = CinemaColors.TvWhite,
                fontWeight = FontWeight.SemiBold,
            )
            movie.year?.takeIf { it > 0 }?.let {
                Text(it.toString(), style = MaterialTheme.typography.bodyLarge, color = CinemaColors.TvMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (onDetailClick != null) "Chi tiết" else "Xem phim",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaColors.TvWhite,
                    modifier = Modifier.focusRequester(actionRequester)
                        .cinemaFocusable(
                            onClick = { onDismiss(); (onDetailClick ?: onPlayClick)(movie) },
                            scaleFocused = 1f,
                            focusedBorderColor = CinemaColors.TvWhite,
                        ).padding(horizontal = 20.dp, vertical = 14.dp),
                )
                Text(
                    text = "Đóng",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaColors.TvMuted,
                    modifier = Modifier.cinemaFocusable(onClick = onDismiss, scaleFocused = 1f)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
        }
    }
}
