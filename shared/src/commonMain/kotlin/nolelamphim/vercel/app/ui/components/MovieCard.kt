package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .cinemaFocusable(
                onClick = onClick,
                shape = cardShape,
                scaleFocused = 1.08f,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(cardShape)
                .background(CinemaColors.SurfaceCard),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = movie.posterUrl ?: movie.thumbUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Bottom gradient overlay on poster
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CinemaColors.PosterOverlayGradient),
            )

            // Top-left: Year pill
            if (movie.year != null && movie.year > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = movie.year.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Top-right: Rating pill
            if (movie.rating > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xE61E1B18))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CinemaColors.Gold,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(Modifier.size(3.dp))
                    Text(
                        text = "${((movie.rating * 10).toInt() / 10.0)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CinemaColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Title
        Text(
            text = movie.name,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
        )

        // Subtitle / Origin name
        if (movie.originName.isNotBlank() && movie.originName != movie.name) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = movie.originName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = CinemaColors.TextMuted,
            )
        }
    }
}
