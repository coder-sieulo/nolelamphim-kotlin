package nolelamphim.vercel.app.ui.screens.watch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.components.SetScreenFullscreen
import nolelamphim.vercel.app.ui.components.VideoPlayer
import nolelamphim.vercel.app.ui.components.VideoResizeMode
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
    var isFullscreen by remember { mutableStateOf(false) }
    var isOverlayVisible by remember { mutableStateOf(true) }
    var isEpisodeDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var resizeMode by remember { mutableStateOf(VideoResizeMode.FIT) }
    var seekDelta by remember { mutableStateOf(0L) }
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var seekFeedbackSide by remember { mutableStateOf<Alignment>(Alignment.Center) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedServerIdx by remember(state.currentServerIndex) {
        mutableStateOf(state.currentServerIndex)
    }

    val gestureScope = rememberCoroutineScope()
    fun triggerSeek(delta: Long, isForward: Boolean) {
        seekDelta = delta
        seekFeedbackText = if (isForward) "+10s »" else "« 10s"
        seekFeedbackSide = if (isForward) Alignment.CenterEnd else Alignment.CenterStart
        gestureScope.launch {
            delay(650)
            seekFeedbackText = null
            seekDelta = 0L
        }
    }

    // Auto-hide overlay in fullscreen mode after 4 seconds if drawer & settings are closed
    LaunchedEffect(isFullscreen, isOverlayVisible, isEpisodeDrawerOpen, isSettingsOpen) {
        if (isFullscreen && isOverlayVisible && !isEpisodeDrawerOpen && !isSettingsOpen) {
            delay(4000)
            isOverlayVisible = false
        }
    }

    // Trigger platform-level sensor landscape rotation & immersive bar hiding
    SetScreenFullscreen(isFullscreen)

    if (isFullscreen) {
        // --- 100% IMMERSIVE FULLSCREEN MODE (LANDSCAPE) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            isOverlayVisible = !isOverlayVisible
                        },
                        onDoubleTap = { offset ->
                            val width = size.width
                            if (offset.x < width * 0.38f) {
                                triggerSeek(-10000L, isForward = false)
                            } else if (offset.x > width * 0.62f) {
                                triggerSeek(10000L, isForward = true)
                            } else {
                                isOverlayVisible = !isOverlayVisible
                            }
                        },
                    )
                },
        ) {
            // Video Player taking 100% of the screen
            VideoPlayer(
                url = state.playbackUrl,
                fallbackUrl = state.embedUrl,
                modifier = Modifier.fillMaxSize(),
                playbackSpeed = playbackSpeed,
                resizeMode = resizeMode,
                seekDelta = seekDelta,
            )

            // Animated Double-Tap Seek Feedback Badge
            AnimatedVisibility(
                visible = seekFeedbackText != null,
                enter = fadeIn(tween(120)) + scaleIn(tween(180)),
                exit = fadeOut(tween(220)) + scaleOut(tween(180)),
                modifier = Modifier
                    .align(seekFeedbackSide)
                    .padding(horizontal = 64.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.82f))
                        .border(1.5.dp, CinemaColors.Primary, CircleShape)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = if (seekFeedbackSide == Alignment.CenterEnd) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = seekFeedbackText ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }

            // Floating Cinema Controls Overlay
            AnimatedVisibility(
                visible = isOverlayVisible || isEpisodeDrawerOpen,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top Gradient Vignette
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Transparent,
                                    ),
                                ),
                            )
                            .align(Alignment.TopCenter),
                    )

                    // Top Floating Bar (Exit Fullscreen / Back + Title + VIP Badge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .bounceClick { isFullscreen = false },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FullscreenExit,
                                    contentDescription = "Thu nhỏ màn hình",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }

                            Column {
                                Text(
                                    text = state.movieName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = state.episodeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaColors.Primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        // VIP Badge & Quick Exit Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CinemaColors.Primary)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "VIP 1080p",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .bounceClick { isFullscreen = false }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Thu nhỏ", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Bottom Gradient Vignette
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f),
                                    ),
                                ),
                            )
                            .align(Alignment.BottomCenter),
                    )

                    // Bottom Floating Actions (when drawer & settings are closed)
                    if (!isEpisodeDrawerOpen && !isSettingsOpen) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 20.dp, end = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Quick Settings Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .bounceClick { isSettingsOpen = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cài đặt", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Episode Drawer Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .bounceClick { isEpisodeDrawerOpen = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Danh sách tập", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Next Episode Button
                            if (state.hasNextEpisode) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CinemaColors.Primary)
                                        .bounceClick(onClick = onNext)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Tập tiếp theo", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Sliding Episode Drawer (even in fullscreen!)
                    AnimatedVisibility(
                        visible = isEpisodeDrawerOpen,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        TvEpisodeDrawerSheet(
                            movie = state.movie,
                            selectedServerIndex = selectedServerIdx,
                            activeEpisodeIndex = if (selectedServerIdx == state.currentServerIndex) state.currentEpisodeIndex else -1,
                            onSelectServer = { selectedServerIdx = it },
                            onSelectEpisode = { epIdx ->
                                onSelectEpisode(selectedServerIdx, epIdx)
                                isEpisodeDrawerOpen = false
                            },
                            onClose = { isEpisodeDrawerOpen = false },
                        )
                    }

                    // Quick Settings Sheet in Fullscreen
                    AnimatedVisibility(
                        visible = isSettingsOpen,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        PlayerQuickSettingsSheet(
                            playbackSpeed = playbackSpeed,
                            onPlaybackSpeedChange = { playbackSpeed = it },
                            resizeMode = resizeMode,
                            onResizeModeChange = { resizeMode = it },
                            servers = state.movie.servers,
                            currentServerIndex = state.currentServerIndex,
                            onSelectServer = { serverIdx ->
                                onSelectEpisode(serverIdx, 0)
                                isSettingsOpen = false
                            },
                            onClose = { isSettingsOpen = false },
                        )
                    }
                }
            }
        }
    } else {
        // --- NORMAL PORTRAIT LAYOUT ---
        Box(modifier = Modifier.fillMaxSize()) {
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

                        // 16:9 Video Player Container with Double-Tap Seek Gestures
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .background(Color.Black)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { offset ->
                                            val width = size.width
                                            if (offset.x < width * 0.38f) {
                                                triggerSeek(-10000L, isForward = false)
                                            } else if (offset.x > width * 0.62f) {
                                                triggerSeek(10000L, isForward = true)
                                            }
                                        },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            VideoPlayer(
                                url = state.playbackUrl,
                                fallbackUrl = state.embedUrl,
                                modifier = Modifier.fillMaxSize(),
                                playbackSpeed = playbackSpeed,
                                resizeMode = resizeMode,
                                seekDelta = seekDelta,
                            )

                            // Animated Double-Tap Seek Feedback Badge in Portrait
                            androidx.compose.animation.AnimatedVisibility(
                                visible = seekFeedbackText != null,
                                enter = fadeIn(tween(120)) + scaleIn(tween(180)),
                                exit = fadeOut(tween(220)) + scaleOut(tween(180)),
                                modifier = Modifier
                                    .align(seekFeedbackSide)
                                    .padding(horizontal = 28.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.85f))
                                        .border(1.5.dp, CinemaColors.Primary, CircleShape)
                                        .padding(horizontal = 20.dp, vertical = 11.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (seekFeedbackSide == Alignment.CenterEnd) Icons.Default.FastForward else Icons.Default.FastRewind,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = seekFeedbackText ?: "",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                        )
                                    }
                                }
                            }

                            // Floating Fullscreen Trigger Button (Bottom-Right of Video)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.70f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .bounceClick { isFullscreen = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Mở toàn màn hình",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Toàn màn hình",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                    }

                    // In-Player Floating Top Bar (Back button + VIP Server badge + Settings)
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
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

                            // Settings Icon Button in Top Bar
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                                    .bounceClick { isSettingsOpen = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Cài đặt trình phát",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
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

                    // Fullscreen Button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                            .bounceClick(onClick = { isFullscreen = true })
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Toàn màn hình",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Phóng to",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
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

                    // Quick Settings Button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaColors.SurfaceCard)
                            .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp))
                            .bounceClick(onClick = { isSettingsOpen = true })
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "Cài đặt",
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
        } // Close LazyColumn
    } // Close Column

    // Quick Settings Sheet in Portrait
    AnimatedVisibility(
        visible = isSettingsOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter),
    ) {
        PlayerQuickSettingsSheet(
            playbackSpeed = playbackSpeed,
            onPlaybackSpeedChange = { playbackSpeed = it },
            resizeMode = resizeMode,
            onResizeModeChange = { resizeMode = it },
            servers = state.movie.servers,
            currentServerIndex = state.currentServerIndex,
            onSelectServer = { serverIdx: Int ->
                onSelectEpisode(serverIdx, 0)
                isSettingsOpen = false
            },
            onClose = { isSettingsOpen = false },
        )
    }
} // Close Box
} // Close if/else
} // Close MobileWatchLayout

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
    var isHudVisible by remember { mutableStateOf(true) }
    var isEpisodeDrawerOpen by remember { mutableStateOf(false) }
    var selectedServerIdx by remember(state.currentServerIndex) {
        mutableStateOf(state.currentServerIndex)
    }

    // Auto-hide HUD after 5 seconds if drawer is closed
    LaunchedEffect(isHudVisible, isEpisodeDrawerOpen) {
        if (isHudVisible && !isEpisodeDrawerOpen) {
            delay(5000)
            isHudVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                isHudVisible = !isHudVisible
            },
    ) {
        // 1. Fullscreen Video
        VideoPlayer(
            url = state.playbackUrl,
            fallbackUrl = state.embedUrl,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. Cinema TV HUD Overlay
        AnimatedVisibility(
            visible = isHudVisible || isEpisodeDrawerOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top gradient vignette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.95f),
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Transparent,
                                ),
                            ),
                        )
                        .align(Alignment.TopCenter),
                )

                // Top Floating Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 36.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .cinemaFocusable(
                                    onClick = onBack,
                                    shape = CircleShape,
                                    scaleFocused = 1.12f,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = state.movieName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CinemaColors.Primary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = "VIP 1080p",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = state.episodeName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                    color = CinemaColors.Primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "•  ${state.movie.servers.getOrNull(state.currentServerIndex)?.name?.ifBlank { "Máy chủ chính" } ?: "Máy chủ chính"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f),
                                )
                            }
                        }
                    }

                    // Top Right Rating
                    if (state.movie.rating > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, CinemaColors.Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = CinemaColors.Gold, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "${((state.movie.rating * 10).toInt() / 10.0)}",
                                    color = CinemaColors.Gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }

                // Bottom vignette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.65f),
                                    Color.Black.copy(alpha = 0.95f),
                                ),
                            ),
                        )
                        .align(Alignment.BottomCenter),
                )

                // Bottom Floating Control Bar (when drawer is closed)
                if (!isEpisodeDrawerOpen) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp, start = 36.dp, end = 36.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Episode Drawer Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .cinemaFocusable(
                                    onClick = {
                                        isEpisodeDrawerOpen = true
                                        isHudVisible = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    scaleFocused = 1.08f,
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Danh sách tập", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        // Next Episode CTA
                        if (state.hasNextEpisode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CinemaColors.Primary)
                                    .cinemaFocusable(
                                        onClick = onNext,
                                        shape = RoundedCornerShape(12.dp),
                                        scaleFocused = 1.08f,
                                        focusedBorderColor = Color.White,
                                    )
                                    .padding(horizontal = 22.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tập tiếp theo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        // Server Switcher Button
                        if (state.movie.servers.size > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .cinemaFocusable(
                                        onClick = {
                                            val nextServer = (selectedServerIdx + 1) % state.movie.servers.size
                                            selectedServerIdx = nextServer
                                            onSelectEpisode(nextServer, state.currentEpisodeIndex)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        scaleFocused = 1.08f,
                                    )
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = state.movie.servers.getOrNull(selectedServerIdx)?.name?.ifBlank { "Đổi server" } ?: "Đổi server",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Sliding Episode Drawer (TV)
                AnimatedVisibility(
                    visible = isEpisodeDrawerOpen,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    TvEpisodeDrawerSheet(
                        movie = state.movie,
                        selectedServerIndex = selectedServerIdx,
                        activeEpisodeIndex = if (selectedServerIdx == state.currentServerIndex) state.currentEpisodeIndex else -1,
                        onSelectServer = { selectedServerIdx = it },
                        onSelectEpisode = { epIdx ->
                            onSelectEpisode(selectedServerIdx, epIdx)
                            isEpisodeDrawerOpen = false
                        },
                        onClose = { isEpisodeDrawerOpen = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun TvEpisodeDrawerSheet(
    movie: MovieDetail,
    selectedServerIndex: Int,
    activeEpisodeIndex: Int,
    onSelectServer: (Int) -> Unit,
    onSelectEpisode: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val server = movie.servers.getOrNull(selectedServerIndex) ?: movie.servers.firstOrNull()
    val episodes = server?.episodes.orEmpty()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (activeEpisodeIndex - 2).coerceAtLeast(0))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF70E0E16))
            .border(1.dp, Color.White.copy(alpha = 0.12f))
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CinemaColors.Primary),
                )
                Text(
                    text = "DANH SÁCH TẬP PHIM",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "(${episodes.size} tập)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaColors.TextMuted,
                )
            }

            // Server Chips & Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (movie.servers.size > 1) {
                    movie.servers.forEachIndexed { idx, srv ->
                        val isSelected = idx == selectedServerIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.08f))
                                .cinemaFocusable(
                                    onClick = { onSelectServer(idx) },
                                    shape = RoundedCornerShape(8.dp),
                                    scaleFocused = 1.08f,
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = srv.name.ifBlank { "Server ${idx + 1}" },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }

                // Close button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .cinemaFocusable(
                            onClick = onClose,
                            shape = RoundedCornerShape(8.dp),
                            scaleFocused = 1.08f,
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text("Đóng ✕", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Horizontal Row of Episode Cards
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(episodes) { epIdx, ep ->
                val isActive = epIdx == activeEpisodeIndex
                val label = ep.name.ifBlank { "${epIdx + 1}" }
                val displayLabel = if (label.startsWith("Tập", ignoreCase = true)) label else "Tập $label"

                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(95.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) CinemaColors.PrimaryContainer
                            else Color(0xFF161622),
                        )
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = if (isActive) CinemaColors.Primary else Color.White.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .cinemaFocusable(
                            onClick = { onSelectEpisode(epIdx) },
                            shape = RoundedCornerShape(12.dp),
                            scaleFocused = 1.12f,
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = displayLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isActive) CinemaColors.Primary else Color.White,
                                fontWeight = FontWeight.Black,
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CinemaColors.Primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "ĐANG PHÁT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                            }
                        }

                        Text(
                            text = movie.name,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = CinemaColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerQuickSettingsSheet(
    playbackSpeed: Float,
    onPlaybackSpeedChange: (Float) -> Unit,
    resizeMode: VideoResizeMode,
    onResizeModeChange: (VideoResizeMode) -> Unit,
    servers: List<EpisodeServer>,
    currentServerIndex: Int,
    onSelectServer: (Int) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF13131D))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                )
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CinemaColors.Primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Cài đặt trình phát",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .bounceClick(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Section 1: Playback Speed
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TỐC ĐỘ PHÁT",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    speeds.forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        val speedLabel = if (speed == 1.0f) "1.0x" else "${speed}x"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.08f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .bounceClick { onPlaybackSpeedChange(speed) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = speedLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // Section 2: Aspect Ratio
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TỈ LỆ KHUNG HÌNH",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VideoResizeMode.entries.forEach { mode ->
                        val isSelected = resizeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.08f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .bounceClick { onResizeModeChange(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // Section 3: Máy chủ phát nhanh (Fast Server Switcher)
            if (servers.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "MÁY CHỦ PHÁT (CHUYỂN NHANH)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(servers) { idx, srv ->
                            val isSelected = idx == currentServerIndex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.08f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CinemaColors.Primary else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                    )
                                    .bounceClick { onSelectServer(idx) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                    Text(
                                        text = srv.name.ifBlank { "Server ${idx + 1}" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
