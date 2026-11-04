package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.model.Movie

@Composable
fun HeroBanner(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = movie.thumbUrl ?: movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xB31B1B1F),
                            Color(0xFF1B1B1F),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(
                    movie.originName.takeIf { it.isNotBlank() },
                    movie.year?.toString(),
                    "★ ${movie.rating}".takeIf { movie.rating > 0 },
                ).joinToString("  •  "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onClick,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("Xem ngay")
            }
        }
    }
}
