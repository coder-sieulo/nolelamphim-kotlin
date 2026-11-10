package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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

/**
 * 16:9 Cinematic Landscape Backdrop Card
 */
@Composable
fun BackdropMovieCard(
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
                scaleFocused = 1.06f,
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(cardShape)
                .background(CinemaColors.SurfaceCard),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = movie.thumbUrl ?: movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Cinematic gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x66000000), Color(0xE60D0D11)),
                        ),
                    ),
            )

            // Play icon watermark
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Top-right Rating Pill
            if (movie.rating > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CinemaColors.Gold,
                        modifier = Modifier.size(10.dp),
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = "${((movie.rating * 10).toInt() / 10.0)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = CinemaColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = movie.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        if (movie.originName.isNotBlank() && movie.originName != movie.name) {
            Text(
                text = movie.originName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = CinemaColors.TextMuted,
            )
        }
    }
}

/**
 * Featured Spotlight Banner Card for middle of home feed
 */
@Composable
fun SpotlightMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(cardShape)
            .background(CinemaColors.SurfaceCard)
            .cinemaFocusable(onClick = onClick, shape = cardShape, scaleFocused = 1.04f),
    ) {
        AsyncImage(
            model = movie.thumbUrl ?: movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xF00D0D11),
                            Color(0xCC0D0D11),
                            Color(0x330D0D11),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.72f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CinemaColors.Primary)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "GỢI Ý ĐẶC BIỆT",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                )
            }

            Text(
                text = movie.name,
                style = MaterialTheme.typography.titleLarge,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CinemaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (movie.year != null && movie.year > 0) {
                    Text(
                        text = "${movie.year} · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextMuted,
                    )
                }
                if (movie.rating > 0) {
                    Text(
                        text = "★ ${((movie.rating * 10).toInt() / 10.0)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.Gold,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

