package nolelamphim.vercel.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable
import kotlin.math.absoluteValue

@Composable
fun HeroBannerCarousel(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return

    val isTv = LocalIsTv.current
    val featuredMovies = movies.take(6)
    val pagerState = rememberPagerState(pageCount = { featuredMovies.size })
    val bannerHeight = if (isTv) 460.dp else 400.dp
    val bannerShape = if (isTv) RoundedCornerShape(20.dp) else RoundedCornerShape(0.dp)

    // Auto-scroll effect
    LaunchedEffect(pagerState.pageCount) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .then(if (isTv) Modifier.padding(horizontal = 24.dp, vertical = 8.dp) else Modifier)
            .clip(bannerShape),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val movie = featuredMovies[page]
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val scale = lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
            val alpha = lerp(0.65f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
            ) {
                // Movie Backdrop
                AsyncImage(
                    model = movie.thumbUrl ?: movie.posterUrl,
                    contentDescription = movie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Multi-directional cinematic gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CinemaColors.HeroSideGradient),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CinemaColors.HeroOverlayGradient),
                )

                // Movie Info & Actions
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(if (isTv) 0.65f else 0.94f)
                        .padding(horizontal = if (isTv) 32.dp else 20.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Badges row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CinemaColors.Primary)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "NỔI BẬT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                            )
                        }

                        if (movie.year != null && movie.year > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = movie.year.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaColors.TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        if (movie.rating > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, CinemaColors.Gold.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CinemaColors.Gold,
                                    modifier = Modifier.size(13.dp),
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${((movie.rating * 10).toInt() / 10.0)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaColors.Gold,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    // Title
                    Text(
                        text = movie.name,
                        style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = if (isTv) 40.sp else 30.sp,
                    )

                    // Origin Name Subtitle
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        Text(
                            text = movie.originName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onMovieClick(movie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaColors.Primary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.cinemaFocusable(
                                onClick = { onMovieClick(movie) },
                                shape = RoundedCornerShape(12.dp),
                                scaleFocused = 1.08f,
                            ),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Xem ngay", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onMovieClick(movie) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.25f)),
                            ),
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .cinemaFocusable(
                                    onClick = { onMovieClick(movie) },
                                    shape = RoundedCornerShape(12.dp),
                                    scaleFocused = 1.08f,
                                ),
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Chi tiết", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Dynamic Animated Indicator Dots
        if (featuredMovies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = if (isTv) 36.dp else 20.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(featuredMovies.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "pill-width",
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBanner(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeroBannerCarousel(
        movies = listOf(movie),
        onMovieClick = { onClick() },
        modifier = modifier,
    )
}
