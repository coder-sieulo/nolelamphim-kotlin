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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
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
 * Universal Hero Showcase:
 * - On TV: Cinefy Big-Screen Cinema Hero (matching cinefy-tv.html) with wide backdrop,
 *   eyebrow pill, large Space Grotesk headline, dual primary/secondary action buttons & indicator pips.
 * - On Mobile: 3D CoverFlow Carousel with vertical posters and centered "Xem ngay" button.
 */
@Composable
fun HeroBannerCarousel(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onInfoClick: ((Movie) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return
    val isTv = LocalIsTv.current

    if (isTv) {
        TvCinefyHeroBanner(
            movies = movies,
            onMovieClick = onMovieClick,
            onInfoClick = onInfoClick,
            modifier = modifier,
        )
    } else {
        MobileCoverFlowHeroBanner(
            movies = movies,
            onMovieClick = onMovieClick,
            modifier = modifier,
        )
    }
}

/**
 * Cinefy TV Big Screen Hero Canvas (matching cinefy-tv.html .hero section)
 */
@Composable
private fun TvCinefyHeroBanner(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onInfoClick: ((Movie) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val featuredMovies = remember(movies) { movies.take(6) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { featuredMovies.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
            .background(Color(0xFF0D0E12)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val movie = featuredMovies[page]

            Box(modifier = Modifier.fillMaxSize()) {
                // Wide Cinematic Backdrop
                AsyncImage(
                    model = movie.thumbUrl ?: movie.posterUrl,
                    contentDescription = movie.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Left-to-right cinematic gradient (matching cinefy-tv.html)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xF509090D),
                                    0.36f to Color(0xD909090D),
                                    0.78f to Color(0x1809090D),
                                    1.0f to Color.Transparent,
                                ),
                            ),
                        ),
                )

                // Bottom vignette gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1.0f to Color(0x9908090C),
                                ),
                            ),
                        ),
                )

                // Left Content Copy
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(0.56f)
                        .padding(start = 40.dp, end = 16.dp),
                ) {
                    // Eyebrow pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xC9251910))
                            .border(1.dp, Color(0x5CFF9B5C), RoundedCornerShape(30.dp))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF812B),
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "NỔI BẬT · CINEFY ORIGINALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFC18F),
                            letterSpacing = 1.sp,
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Title
                    Text(
                        text = movie.name.uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            lineHeight = 44.sp,
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(10.dp))

                    // Synopsis / Subtitle
                    val subtitle = if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        "${movie.originName} • Năm ${movie.year ?: 2024} ${if (movie.rating > 0) "• ★ ${movie.rating}" else ""}"
                    } else {
                        "Trải nghiệm phim chiếu rạp đỉnh cao với hình ảnh chất lượng cao trên màn hình lớn."
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = Color(0xFFDDDDDD),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 1. Primary "Xem ngay" button
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .cinemaFocusable(
                                    onClick = { onMovieClick(movie) },
                                    shape = RoundedCornerShape(30.dp),
                                    scaleFocused = 1.05f,
                                    focusedBorderColor = Color(0xFFFFD0A9),
                                )
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFFB16D), Color(0xFFFF812B)),
                                    ),
                                )
                                .padding(horizontal = 26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF211108),
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "Xem ngay",
                                    color = Color(0xFF211108),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                        }

                        // 2. Secondary "Chi tiết" button
                        Box(
                            modifier = Modifier
                                .height(50.dp)
                                .cinemaFocusable(
                                    onClick = {
                                        if (onInfoClick != null) {
                                            onInfoClick(movie)
                                        } else {
                                            onMovieClick(movie)
                                        }
                                    },
                                    shape = RoundedCornerShape(30.dp),
                                    scaleFocused = 1.05f,
                                    focusedBorderColor = Color(0xFFFFD0A9),
                                )
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(30.dp))
                                .padding(horizontal = 26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Chi tiết",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Indicator Pips (Matching cinefy-tv.html .hero-pips)
        if (featuredMovies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(featuredMovies.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 7.dp,
                        animationSpec = tween(250),
                        label = "tv-pip-width",
                    )
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFFF812B) else Color.White.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}

/**
 * Mobile 3D CoverFlow Carousel
 */
@Composable
private fun MobileCoverFlowHeroBanner(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
) {
    val featuredMovies = remember(movies) { movies.take(10) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { featuredMovies.size },
    )
    val scope = rememberCoroutineScope()

    // Auto-scroll on mobile
    LaunchedEffect(pagerState.pageCount) {
        if (featuredMovies.size <= 1) return@LaunchedEffect
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
            .statusBarsPadding()
            .padding(top = 52.dp),
    ) {
        val screenWidth = maxWidth

        val posterWidth = when {
            screenWidth < 360.dp -> 190.dp
            screenWidth < 600.dp -> 215.dp
            else -> 250.dp
        }
        val posterHeight = posterWidth * 1.48f
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
                                scaleFocused = 1.02f,
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
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .width(195.dp)
                            .height(42.dp)
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
                                scaleFocused = 1.03f,
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
