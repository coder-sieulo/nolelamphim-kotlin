package nolelamphim.vercel.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.roundToInt

/**
 * 3D CoverFlow Carousel for the Top Film Showcase
 * Matching cinema ticketing & streaming hero designs:
 * - Center vertical poster in sharp focus, flanked by symmetrical peeking side posters
 * - 3D scaling (0.82x) and dimming (0.50x) on non-active posters
 * - Centered Movie Title & Time/Meta directly below active poster
 * - Prominent warm coral "Xem ngay" (Book Now style) CTA button
 * - Native TV D-pad navigation + auto-advance on mobile
 */
@Composable
fun HeroBannerCarousel(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return

    val isTv = LocalIsTv.current
    val featuredMovies = remember(movies) { movies.take(10) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { featuredMovies.size },
    )
    val scope = rememberCoroutineScope()

    // Auto-scroll on mobile (disabled on TV to prevent stealing D-pad focus)
    LaunchedEffect(pagerState.pageCount, isTv) {
        if (isTv || featuredMovies.size <= 1) return@LaunchedEffect
        while (true) {
            delay(6000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isTv) Modifier.statusBarsPadding().padding(top = 52.dp)
                else Modifier.padding(top = 24.dp)
            ),
    ) {
        val screenWidth = maxWidth

        // Calculate card dimensions based on screen type
        val posterWidth = when {
            isTv -> 250.dp
            screenWidth < 360.dp -> 190.dp
            screenWidth < 600.dp -> 215.dp
            else -> 250.dp
        }
        val posterHeight = posterWidth * 1.48f // ~ 2:3 vertical poster ratio
        val peekPadding = ((screenWidth - posterWidth) / 2).coerceAtLeast(16.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 3D CoverFlow Pager with Ambient Glow Behind
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(posterHeight + 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Subtle ambient glow centered behind the active poster
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(posterHeight)
                        .drawBehind {
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFE87A50).copy(alpha = 0.18f),
                                        Color(0xFFEF4444).copy(alpha = 0.05f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width * 0.75f,
                                ),
                            )
                        },
                )

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = peekPadding),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(posterHeight),
                ) { page ->
                    val movie = featuredMovies[page]
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val fraction = (1f - pageOffset.coerceIn(0f, 1f))

                    val scale = lerp(0.82f, 1.0f, fraction)
                    val alpha = lerp(0.50f, 1.0f, fraction)
                    val isCenter = page == pagerState.currentPage

                    Box(
                        modifier = Modifier
                            .width(posterWidth)
                            .height(posterHeight)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                shape = RoundedCornerShape(16.dp)
                                clip = true
                                if (fraction > 0.85f) {
                                    shadowElevation = (14.dp * fraction).toPx()
                                }
                            }
                            .cinemaFocusable(
                                onClick = {
                                    if (isCenter) {
                                        onMovieClick(movie)
                                    } else {
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                page = page,
                                                animationSpec = tween(350, easing = FastOutSlowInEasing),
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                scaleFocused = if (isTv) 1.05f else 1.02f,
                                focusedBorderColor = Color(0xFFE87A50),
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(CinemaColors.SurfaceElevated),
                    ) {
                        AsyncImage(
                            model = movie.posterUrl ?: movie.thumbUrl,
                            contentDescription = movie.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Subtle border highlighting edges
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.dp,
                                    color = if (isCenter) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp),
                                ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Active Movie Info & CTA Button (Centered, matching the mockup)
            val currentMovie = featuredMovies.getOrNull(pagerState.currentPage) ?: featuredMovies.first()

            AnimatedContent(
                targetState = currentMovie,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(140))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { movie ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Movie Title
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTv) 23.sp else 20.sp,
                            letterSpacing = (-0.3).sp,
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )

                    Spacer(Modifier.height(4.dp))

                    // Subtitle / Meta (Formatted clean like "Time: 100mins" in mockup)
                    val metaText = remember(movie) {
                        val parts = mutableListOf<String>()
                        if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                            parts.add(movie.originName)
                        }
                        if (movie.year != null && movie.year > 0) {
                            parts.add("${movie.year}")
                        }
                        if (movie.rating > 0) {
                            val rounded = ((movie.rating * 10).roundToInt() / 10.0)
                            parts.add("★ $rounded")
                        }
                        if (parts.isEmpty()) "Chiếu rạp • Vietsub" else parts.joinToString(" • ")
                    }

                    Text(
                        text = metaText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = if (isTv) 14.5.sp else 13.sp,
                        ),
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )

                    Spacer(Modifier.height(16.dp))

                    // CTA Button: "Xem ngay" in warm terracotta / coral orange (#E87A50)
                    Box(
                        modifier = Modifier
                            .width(if (isTv) 220.dp else 195.dp)
                            .height(if (isTv) 46.dp else 42.dp)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionLeft -> {
                                            if (pagerState.currentPage > 0) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                }
                                                true
                                            } else false
                                        }
                                        Key.DirectionRight -> {
                                            if (pagerState.currentPage < featuredMovies.size - 1) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                }
                                                true
                                            } else false
                                        }
                                        else -> false
                                    }
                                } else false
                            }
                            .cinemaFocusable(
                                onClick = { onMovieClick(movie) },
                                shape = RoundedCornerShape(14.dp),
                                scaleFocused = if (isTv) 1.08f else 1.03f,
                                focusedBorderColor = Color.White,
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE87A50)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Xem ngay",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                ),
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            // Indicator Dots below the button
            if (featuredMovies.size > 1) {
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(featuredMovies.size) { index ->
                        val isSelected = index == pagerState.currentPage
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 18.dp else 5.dp,
                            animationSpec = tween(250),
                            label = "dot-width",
                        )
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(pillWidth)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFE87A50) else Color.White.copy(alpha = 0.22f)),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for single movie hero or carousel
 */
@Composable
fun HeroBanner(
    movie: Movie,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    HeroBannerCarousel(
        movies = listOf(movie),
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}
