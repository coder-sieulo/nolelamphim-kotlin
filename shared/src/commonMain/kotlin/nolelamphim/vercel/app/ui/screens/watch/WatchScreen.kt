package nolelamphim.vercel.app.ui.screens.watch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.components.VideoPlayer
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.bounceClick
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun WatchScreen(
    slug: String,
    serverIndex: Int,
    episodeIndex: Int,
    onBack: () -> Unit,
    onNextEpisode: () -> Unit = {},
    onMovieClick: (slug: String) -> Unit = {},
    viewModel: WatchViewModel = viewModel {
        WatchViewModel(
            repository = AppContainer.movieRepository,
            localLibrary = AppContainer.localLibrary,
            slug = slug,
            initialServerIndex = serverIndex,
            initialEpisodeIndex = episodeIndex,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        when (val s = state) {
            WatchUiState.Loading -> LoadingView(message = "Đang kết nối luồng phát...")

            is WatchUiState.Error -> ErrorView(
                message = s.message,
                onRetry = { viewModel.load() },
            )

            is WatchUiState.Success -> {
                if (isTv) {
                    TvWatchLayout(
                        state = s,
                        onBack = onBack,
                        onSelectEpisode = { serverIdx, epIdx ->
                            viewModel.selectEpisode(serverIdx, epIdx)
                        },
                        onNext = { viewModel.nextEpisode() },
                    )
                } else {
                    MobileWatchLayout(
                        state = s,
                        onBack = onBack,
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onSelectEpisode = { serverIdx, epIdx ->
                            viewModel.selectEpisode(serverIdx, epIdx)
                        },
                        onNext = { viewModel.nextEpisode() },
                        onMovieClick = onMovieClick,
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        },
                    )
                }
            }
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
private fun MobileWatchLayout(
    state: WatchUiState.Success,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
    onNext: () -> Unit,
    onMovieClick: (slug: String) -> Unit,
    onShowMessage: (String) -> Unit,
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedServerIdx by remember(state.currentServerIndex) {
        mutableStateOf(state.currentServerIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Top Video Player Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Safe Area status bar spacer for seamless immersion
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                )

                // 16:9 Video Player Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    VideoPlayer(
                        url = state.playbackUrl,
                        fallbackUrl = state.embedUrl,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // In-Player Floating Top Bar (Back button + Title overlay)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // VIP Server badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, CinemaColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CinemaColors.GreenBadge),
                        )
                        Text(
                            text = state.movie.servers.getOrNull(state.currentServerIndex)?.name?.ifBlank { "VIP Server" }
                                ?: "VIP Server",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        // Scrollable Media Hub beneath the Video Player
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Title & Episode Subtitle
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = state.movieName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Black,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CinemaColors.Primary.copy(alpha = 0.18f))
                                .border(1.dp, CinemaColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = state.episodeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.Primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (state.movie.rating > 0) {
                            Text(
                                text = "★ ${((state.movie.rating * 10).toInt() / 10.0)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.Gold,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        state.movie.year?.let {
                            Text(
                                text = "•  $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.TextSecondary,
                            )
                        }

                        state.movie.quality?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "•  $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.BlueBadge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // Quick Native Action Bar: [Next Episode] [Favorite] [Share]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (state.hasNextEpisode) {
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaColors.Primary,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .bounceClick(onClick = onNext),
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Tập tiếp theo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                        }
                    }

                    // Favorite Button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                            .bounceClick(onClick = onToggleFavorite)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Yêu thích",
                                tint = if (state.isFavorite) CinemaColors.Primary else Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (state.isFavorite) "Đã lưu" else "Yêu thích",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Share Button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                            .bounceClick(onClick = { onShowMessage("Đã sao chép liên kết phim!") })
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // In-Player Episode Selector Hub
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(CinemaColors.Primary),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Danh sách tập",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        val totalEpisodes = state.movie.servers.getOrNull(selectedServerIdx)?.episodes?.size ?: 0
                        Text(
                            text = "$totalEpisodes tập",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaColors.TextMuted,
                        )
                    }

                    // Server Tabs (if multiple servers available)
                    if (state.movie.servers.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            itemsIndexed(state.movie.servers) { idx, srv ->
                                val isSelected = idx == selectedServerIdx
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CinemaColors.Primary else CinemaColors.SurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) CinemaColors.Primary else CinemaColors.BorderSubtle,
                                            RoundedCornerShape(8.dp),
                                        )
                                        .bounceClick { selectedServerIdx = idx }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = srv.name.ifBlank { "Nguồn ${idx + 1}" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    // Episode Grid / Pills
                    val server = state.movie.servers.getOrNull(selectedServerIdx)
                        ?: state.movie.servers.firstOrNull()
                    val episodes = server?.episodes.orEmpty()

                    if (episodes.isNotEmpty()) {
                        InPlayerEpisodeGrid(
                            episodes = episodes,
                            currentServerIndex = selectedServerIdx,
                            activeEpisodeIndex = if (selectedServerIdx == state.currentServerIndex) state.currentEpisodeIndex else -1,
                            onSelectEpisode = { epIdx ->
                                onSelectEpisode(selectedServerIdx, epIdx)
                            },
                        )
                    }
                }
            }

            // Movie Synopsis Accordion
            if (state.movie.description.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Nội dung phim",
                            style = MaterialTheme.typography.titleSmall,
                            color = CinemaColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.movie.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = CinemaColors.TextSecondary,
                            lineHeight = 20.sp,
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

            // Related Movies Carousel
            if (state.related.isNotEmpty()) {
                item {
                    MovieRow(
                        title = "Phim liên quan đề xuất",
                        movies = state.related,
                        onMovieClick = { onMovieClick(it.slug) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InPlayerEpisodeGrid(
    episodes: List<nolelamphim.vercel.app.model.Episode>,
    currentServerIndex: Int,
    activeEpisodeIndex: Int,
    onSelectEpisode: (Int) -> Unit,
) {
    // If under 30 episodes, render a sleek FlowRow grid. If large, horizontal or paginated.
    if (episodes.size <= 40) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            episodes.forEachIndexed { epIdx, ep ->
                val isActive = epIdx == activeEpisodeIndex
                val label = ep.name.ifBlank { "${epIdx + 1}" }
                val displayLabel = if (label.startsWith("Tập", ignoreCase = true)) label else "Tập $label"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) CinemaColors.Primary else CinemaColors.SurfaceCard)
                        .border(
                            1.dp,
                            if (isActive) CinemaColors.Primary else CinemaColors.BorderSubtle,
                            RoundedCornerShape(8.dp),
                        )
                        .bounceClick { onSelectEpisode(epIdx) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) Color.White else CinemaColors.TextPrimary,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    } else {
        // High episode count: Horizontal scroll row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(episodes) { epIdx, ep ->
                val isActive = epIdx == activeEpisodeIndex
                val label = ep.name.ifBlank { "${epIdx + 1}" }
                val displayLabel = if (label.startsWith("Tập", ignoreCase = true)) label else "Tập $label"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) CinemaColors.Primary else CinemaColors.SurfaceCard)
                        .border(
                            1.dp,
                            if (isActive) CinemaColors.Primary else CinemaColors.BorderSubtle,
                            RoundedCornerShape(8.dp),
                        )
                        .bounceClick { onSelectEpisode(epIdx) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) Color.White else CinemaColors.TextPrimary,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvWatchLayout(
    state: WatchUiState.Success,
    onBack: () -> Unit,
    onSelectEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Top TV control header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC0D0D11))
                .padding(horizontal = 32.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .cinemaFocusable(onClick = onBack, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column {
                    Text(
                        text = state.movieName,
                        style = MaterialTheme.typography.titleLarge,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.episodeName,
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (state.hasNextEpisode) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaColors.Primary,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cinemaFocusable(
                        onClick = onNext,
                        shape = RoundedCornerShape(10.dp),
                        scaleFocused = 1.08f,
                    ),
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tập tiếp theo", fontWeight = FontWeight.Bold)
                }
            }
        }

        // TV Fullscreen Video Player
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            VideoPlayer(
                url = state.playbackUrl,
                fallbackUrl = state.embedUrl,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
