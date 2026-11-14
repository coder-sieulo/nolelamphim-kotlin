package nolelamphim.vercel.app.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.DetailSkeletonScreen
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.bounceClick
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (slug: String, serverIndex: Int, episodeIndex: Int) -> Unit,
    onMovieClick: (slug: String) -> Unit,
    viewModel: DetailViewModel = viewModel {
        DetailViewModel(AppContainer.movieRepository, AppContainer.localLibrary, slug)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        when (val s = state) {
            DetailUiState.Loading -> DetailSkeletonScreen()

            is DetailUiState.Error -> ErrorView(
                message = s.message,
                onRetry = viewModel::load,
            )

            is DetailUiState.Success -> DetailContent(
                movie = s.movie,
                related = s.related,
                isFavorite = s.isFavorite,
                onBack = onBack,
                onPlay = onPlay,
                onMovieClick = onMovieClick,
                onToggleFavorite = viewModel::toggleFavorite,
                onShowMessage = { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }
}

@Composable
private fun DetailContent(
    movie: MovieDetail,
    related: List<Movie>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlay: (slug: String, serverIndex: Int, episodeIndex: Int) -> Unit,
    onMovieClick: (slug: String) -> Unit,
    onToggleFavorite: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val isTv = LocalIsTv.current
    var selectedServerIndex by remember { mutableIntStateOf(0) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showStickyBar by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 380 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (isTv) 32.dp else 96.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // Hero / Header Section
            item {
                if (isTv) {
                    TvDetailHeader(
                        movie = movie,
                        isFavorite = isFavorite,
                        onBack = onBack,
                        onPlayFirst = { onPlay(movie.slug, 0, 0) },
                        onToggleFavorite = onToggleFavorite,
                    )
                } else {
                    MobileDetailHeader(
                        movie = movie,
                        isFavorite = isFavorite,
                        onBack = onBack,
                        onPlayFirst = { onPlay(movie.slug, selectedServerIndex, 0) },
                        onToggleFavorite = onToggleFavorite,
                        onShowMessage = onShowMessage,
                    )
                }
            }

            // Interactive Genre Chips Section
            if (movie.categories.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = if (isTv) 32.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SectionHeader(title = "Thể loại")
                        GenreChipsRow(categories = movie.categories)
                    }
                }
            }

            // Synopsis Section with smooth expand/collapse
            if (movie.description.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = if (isTv) 32.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionHeader(title = "Nội dung phim")
                        Text(
                            text = movie.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaColors.TextSecondary,
                            lineHeight = 22.sp,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize(),
                        )
                        Text(
                            text = if (isDescriptionExpanded) "Thu gọn ▲" else "Xem thêm ▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaColors.Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(vertical = 4.dp),
                        )
                    }
                }
            }

            // Native Cast & Directors Section (Avatars instead of plain text table)
            if (movie.directors.isNotEmpty() || movie.actors.isNotEmpty()) {
                item {
                    CastAndCrewSection(
                        directors = movie.directors,
                        actors = movie.actors,
                        isTv = isTv,
                    )
                }
            }

            // Native Episodes Hub
            item {
                EpisodeSelectorSection(
                    movie = movie,
                    selectedServerIndex = selectedServerIndex,
                    onSelectServer = { selectedServerIndex = it },
                    onSelectEpisode = { serverIdx, epIdx ->
                        onPlay(movie.slug, serverIdx, epIdx)
                    },
                    isTv = isTv,
                )
            }

            // Related Movies
            if (related.isNotEmpty()) {
                item {
                    MovieRow(
                        title = "Có thể bạn muốn xem",
                        movies = related,
                        onMovieClick = { onMovieClick(it.slug) },
                    )
                }
            }
        }

        // Sticky Bottom Play Bar on Mobile
        if (!isTv) {
            AnimatedVisibility(
                visible = showStickyBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                StickyPlayBar(
                    movie = movie,
                    onPlay = { onPlay(movie.slug, selectedServerIndex, 0) },
                )
            }
        }
    }
}

@Composable
private fun StickyPlayBar(
    movie: MovieDetail,
    onPlay: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFA111118))
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 34.dp, height = 46.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
                Column {
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = movie.episodeCurrent?.let { "Đang phát: $it" } ?: "Bấm xem ngay",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.bounceClick(onClick = onPlay),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Xem ngay", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MobileDetailHeader(
    movie: MovieDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlayFirst: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp),
    ) {
        // Backdrop poster with deep cinematic fade
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient blend (Top vignette + bottom cinema dark)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.8f),
                        0.22f to Color.Transparent,
                        0.5f to Color.Black.copy(alpha = 0.5f),
                        0.8f to CinemaColors.Background.copy(alpha = 0.95f),
                        1f to CinemaColors.Background,
                    ),
                ),
        )

        // Top action bar (Frosted Glass Back + Share + Favorite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .bounceClick(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Share button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .bounceClick(onClick = { onShowMessage("Đã sao chép liên kết phim!") }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Chia sẻ",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Favorite button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .bounceClick(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) CinemaColors.Primary else Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // Bottom title, badges & action row
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Badges row
            BadgeRow(movie = movie)

            // Primary Play CTA Button
            Button(
                onClick = onPlayFirst,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .bounceClick(onClick = onPlayFirst),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Xem phim ${movie.episodeCurrent?.let { "· $it" } ?: ""}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }

            // Native Quick Action Bar (4 interactive icon buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuickActionItem(
                    icon = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    label = if (isFavorite) "Đã lưu" else "Yêu thích",
                    tint = if (isFavorite) CinemaColors.Primary else Color.White,
                    onClick = onToggleFavorite,
                )
                QuickActionItem(
                    icon = Icons.Default.Star,
                    label = if (movie.rating > 0) "${((movie.rating * 10).toInt() / 10.0)}" else "Đánh giá",
                    tint = CinemaColors.Gold,
                    onClick = { onShowMessage("Điểm IMDb: ${movie.rating}/10") },
                )
                QuickActionItem(
                    icon = Icons.Default.Share,
                    label = "Chia sẻ",
                    tint = Color.White,
                    onClick = { onShowMessage("Đã sao chép liên kết!") },
                )
                QuickActionItem(
                    icon = Icons.Default.CloudDownload,
                    label = "Tải về",
                    tint = Color.White,
                    onClick = { onShowMessage("Chức năng tải về đang phát triển") },
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun TvDetailHeader(
    movie: MovieDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlayFirst: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Floating poster card
        Box(
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Details column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineLarge,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaColors.TextSecondary,
                )
            }

            BadgeRow(movie = movie)

            // CTA Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Play CTA (Liquid Glass Primary)
                Box(
                    modifier = Modifier
                        .cinemaFocusable(
                            onClick = onPlayFirst,
                            shape = RoundedCornerShape(12.dp),
                            scaleFocused = 1.08f,
                            focusedBorderColor = Color.White,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(CinemaColors.Primary, Color(0xFFB70606)),
                            ),
                        )
                        .border(1.dp, Color(0xFFFF6666).copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Xem ngay", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }

                // Favorite CTA (Liquid Glass Frosted)
                Box(
                    modifier = Modifier
                        .cinemaFocusable(
                            onClick = onToggleFavorite,
                            shape = RoundedCornerShape(12.dp),
                            scaleFocused = 1.08f,
                            focusedBorderColor = CinemaColors.Primary,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) CinemaColors.Primary else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isFavorite) "Đã yêu thích" else "Thêm yêu thích",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }

                // Back CTA (Liquid Glass Frosted)
                Box(
                    modifier = Modifier
                        .cinemaFocusable(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            scaleFocused = 1.08f,
                            focusedBorderColor = Color.White,
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Quay lại", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeRow(movie: MovieDetail) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (movie.rating > 0) {
            PillBadge(
                text = "★ ${((movie.rating * 10).toInt() / 10.0)}",
                bgColor = Color(0xE61E1B18),
                textColor = CinemaColors.Gold,
                borderColor = CinemaColors.Gold.copy(alpha = 0.35f),
                isBold = true,
            )
        }
        movie.year?.let {
            PillBadge(text = it.toString())
        }
        movie.quality?.takeIf { it.isNotBlank() }?.let {
            PillBadge(
                text = it,
                bgColor = CinemaColors.BlueBadge.copy(alpha = 0.15f),
                textColor = CinemaColors.BlueBadge,
                borderColor = CinemaColors.BlueBadge.copy(alpha = 0.35f),
                isBold = true,
            )
        }
        movie.lang?.takeIf { it.isNotBlank() }?.let {
            PillBadge(
                text = it,
                bgColor = CinemaColors.GreenBadge.copy(alpha = 0.15f),
                textColor = CinemaColors.GreenBadge,
                borderColor = CinemaColors.GreenBadge.copy(alpha = 0.35f),
            )
        }
        movie.duration?.takeIf { it.isNotBlank() }?.let {
            PillBadge(text = it)
        }
        movie.episodeCurrent?.takeIf { it.isNotBlank() }?.let {
            PillBadge(text = it)
        }
    }
}

@Composable
private fun PillBadge(
    text: String,
    bgColor: Color = Color.White.copy(alpha = 0.08f),
    textColor: Color = CinemaColors.TextPrimary,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    isBold: Boolean = false,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun GenreChipsRow(categories: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories) { category ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CinemaColors.SurfaceCard)
                    .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun CastAndCrewSection(
    directors: List<String>,
    actors: List<String>,
    isTv: Boolean,
) {
    val horizontalPadding = if (isTv) 32.dp else 16.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
            SectionHeader(title = "Diễn viên & Đạo diễn")
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Directors first
            items(directors) { director ->
                PersonAvatarCard(name = director, role = "Đạo diễn")
            }
            // Then actors
            items(actors) { actor ->
                PersonAvatarCard(name = actor, role = "Diễn viên")
            }
        }
    }
}

@Composable
private fun PersonAvatarCard(name: String, role: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.width(76.dp),
    ) {
        // Avatar circle with subtle gradient & person initial
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2C2C3A), Color(0xFF1B1B26)),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val initial = name.trim().take(1).uppercase()
            if (initial.isNotBlank() && initial.first().isLetter()) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CinemaColors.TextMuted,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )

        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = CinemaColors.TextMuted,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EpisodeSelectorSection(
    movie: MovieDetail,
    selectedServerIndex: Int,
    onSelectServer: (Int) -> Unit,
    onSelectEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
    isTv: Boolean,
) {
    val horizontalPadding = if (isTv) 32.dp else 16.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(title = "Danh sách tập phim")

            val currentServer = movie.servers.getOrNull(selectedServerIndex) ?: movie.servers.firstOrNull()
            val epCount = currentServer?.episodes?.size ?: 0
            if (epCount > 0) {
                Text(
                    text = "$epCount tập",
                    style = MaterialTheme.typography.labelMedium,
                    color = CinemaColors.TextMuted,
                )
            }
        }

        if (movie.servers.isEmpty()) {
            Text(
                text = "Phim chưa có tập phát sóng",
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaColors.TextMuted,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
            return
        }

        // Server switcher chips
        if (movie.servers.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(movie.servers) { idx, server ->
                    val isSelected = idx == selectedServerIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CinemaColors.Primary else CinemaColors.SurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) CinemaColors.Primary else CinemaColors.BorderSubtle,
                                RoundedCornerShape(8.dp),
                            )
                            .then(
                                if (isTv) {
                                    Modifier.cinemaFocusable(
                                        onClick = { onSelectServer(idx) },
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                } else {
                                    Modifier.bounceClick { onSelectServer(idx) }
                                },
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = server.name.ifBlank { "Nguồn ${idx + 1}" },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // Episodes Grid or Flow
        val currentServer = movie.servers.getOrNull(selectedServerIndex) ?: movie.servers.first()
        val episodes = currentServer.episodes

        if (!isTv && episodes.size <= 40) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                episodes.forEachIndexed { epIdx, ep ->
                    val epTitle = ep.name.ifBlank { "${epIdx + 1}" }
                    val displayLabel = if (epTitle.startsWith("Tập", ignoreCase = true)) epTitle else "Tập $epTitle"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(8.dp))
                            .bounceClick { onSelectEpisode(selectedServerIndex, epIdx) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(
                    items = episodes,
                    key = { epIdx, ep -> "${selectedServerIndex}_${ep.slug}_$epIdx" },
                ) { epIdx, ep ->
                    val epTitle = ep.name.ifBlank { "${epIdx + 1}" }
                    val displayLabel = if (epTitle.startsWith("Tập", ignoreCase = true)) epTitle else "Tập $epTitle"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(8.dp))
                            .then(
                                if (isTv) {
                                    Modifier.cinemaFocusable(
                                        onClick = { onSelectEpisode(selectedServerIndex, epIdx) },
                                        shape = RoundedCornerShape(8.dp),
                                        scaleFocused = 1.08f,
                                    )
                                } else {
                                    Modifier.bounceClick { onSelectEpisode(selectedServerIndex, epIdx) }
                                },
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
        )
    }
}
