package nolelamphim.vercel.app.ui.screens.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import nolelamphim.vercel.app.data.local.equivalentEpisodeIndex
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.PlaybackController
import nolelamphim.vercel.app.ui.components.PlaybackStatus
import nolelamphim.vercel.app.ui.components.VideoPlayer
import nolelamphim.vercel.app.ui.components.VideoResizeMode
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable
import nolelamphim.vercel.app.ui.tv.PlatformBackHandler

private enum class PlayerPanel { Episodes, Sources, Settings }

/** A single Compose focus/input owner. The underlying PlayerView is a video surface only. */
@Composable
internal fun TvPlaybackScreen(
    state: WatchUiState.Success,
    initialPositionMs: Long,
    onProgressChanged: (Long, Long) -> Unit,
    onSaveProgress: () -> Unit,
    onBack: () -> Unit,
    onSelectEpisode: (Int, Int) -> Unit,
    onSelectSource: (Int) -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
) {
    val controller = remember(state.sessionId) { PlaybackController() }
    val playback = controller.snapshot
    var controlsVisible by remember(state.sessionId) { mutableStateOf(true) }
    var panel by remember(state.sessionId) { mutableStateOf<PlayerPanel?>(null) }
    var activity by remember { mutableStateOf(0) }
    var seekTarget by remember(state.sessionId) { mutableStateOf<Long?>(null) }
    var seekRepeat by remember { mutableStateOf(0) }
    var timelineFocused by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableStateOf(VideoResizeMode.FIT) }
    var speed by remember { mutableStateOf(1f) }
    var cancelledAutoplay by remember(state.sessionId) { mutableStateOf(false) }
    var hasBeenForeground by remember(state.sessionId) { mutableStateOf(false) }
    var countdown by remember(state.sessionId) { mutableStateOf(8) }
    val autoplay by AppContainer.tvPreferences.autoplayNextEpisode.collectAsStateWithLifecycle()
    val rootFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }
    val episodesFocus = remember { FocusRequester() }
    val sourcesFocus = remember { FocusRequester() }
    val settingsFocus = remember { FocusRequester() }
    val errorFocus = remember { FocusRequester() }
    var returnFocus by remember { mutableStateOf(playFocus) }

    fun back() {
        when {
            panel != null -> { panel = null; controlsVisible = true; activity++ }
            seekTarget != null -> { seekTarget = null; activity++ }
            controlsVisible && playback.status != PlaybackStatus.Error -> controlsVisible = false
            else -> { onSaveProgress(); onBack() }
        }
    }
    fun openPanel(value: PlayerPanel, opener: FocusRequester) {
        cancelledAutoplay = true
        returnFocus = opener
        panel = value
        controlsVisible = true
        activity++
    }
    fun moveSeek(forward: Boolean) {
        if (!playback.canSeek) return
        val step = if (++seekRepeat > 8) 30_000L else 10_000L
        val target = (seekTarget ?: playback.positionMs) + if (forward) step else -step
        seekTarget = if (playback.durationMs > 0L) target.coerceIn(0L, playback.durationMs) else target.coerceAtLeast(0L)
    }

    PlatformBackHandler(onBack = ::back)
    DisposableEffect(state.sessionId) { onDispose { onSaveProgress() } }
    LaunchedEffect(controlsVisible, panel, activity, playback.status, seekTarget) {
        if (controlsVisible && panel == null && seekTarget == null && playback.status == PlaybackStatus.Playing) {
            delay(4000)
            controlsVisible = false
        }
    }
    LaunchedEffect(controlsVisible, panel, playback.status == PlaybackStatus.Error) {
        withFrameNanos { }
        runCatching {
            when {
                panel != null -> Unit // The panel requests its selected item after its lazy list is laid out.
                playback.status == PlaybackStatus.Error -> errorFocus.requestFocus()
                controlsVisible -> returnFocus.requestFocus()
                else -> rootFocus.requestFocus()
            }
        }
    }
    LaunchedEffect(playback.status) {
        if (playback.status == PlaybackStatus.Paused || playback.status == PlaybackStatus.Ended || playback.status == PlaybackStatus.Error) {
            controlsVisible = true
            onSaveProgress()
        }
    }
    LaunchedEffect(playback.isForeground) {
        if (playback.isForeground) hasBeenForeground = true
        else if (hasBeenForeground) { cancelledAutoplay = true; controlsVisible = true }
    }
    LaunchedEffect(playback.status, playback.isForeground, autoplay, cancelledAutoplay) {
        if (playback.status == PlaybackStatus.Ended && playback.isForeground && autoplay && state.hasNextEpisode && !cancelledAutoplay) {
            for (remaining in 8 downTo 1) { countdown = remaining; delay(1000) }
            onNext()
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .focusRequester(rootFocus)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    activity++
                    if (playback.status == PlaybackStatus.Ended) cancelledAutoplay = true
                }
                val isBack = event.key == Key.Back || event.key == Key.Escape
                if (isBack) {
                    if (event.type == KeyEventType.KeyUp) back()
                    true
                } else if (panel != null) false
                else if (event.key == Key.MediaPlayPause || event.key == Key.MediaPlay || event.key == Key.MediaPause) {
                    if (event.type == KeyEventType.KeyUp) {
                        when (event.key) {
                            Key.MediaPlay -> controller.play()
                            Key.MediaPause -> controller.pause()
                            else -> controller.toggle()
                        }
                        controlsVisible = true
                        returnFocus = playFocus
                    }
                    true
                } else if ((!controlsVisible || timelineFocused) &&
                    (event.key == Key.DirectionLeft || event.key == Key.DirectionRight)) {
                    if (event.type == KeyEventType.KeyDown) moveSeek(event.key == Key.DirectionRight)
                    if (event.type == KeyEventType.KeyUp) {
                        seekTarget?.let { controller.seekTo(it) }
                        seekTarget = null
                        seekRepeat = 0
                        controlsVisible = true
                        returnFocus = playFocus
                    }
                    true
                } else if (!controlsVisible) {
                    when (event.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            if (event.type == KeyEventType.KeyUp) {
                                controller.toggle(); controlsVisible = true; returnFocus = playFocus
                            }
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            if (event.type == KeyEventType.KeyUp) { controlsVisible = true; returnFocus = playFocus }
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable(),
    ) {
        VideoPlayer(
            url = state.playbackUrl,
            fallbackUrl = state.embedUrl,
            modifier = Modifier.fillMaxSize(),
            playbackSpeed = speed,
            resizeMode = resizeMode,
            initialPositionMs = initialPositionMs,
            showControls = false,
            onPositionChanged = onProgressChanged,
            controller = controller,
            allowEmbeddedPlayback = false,
            onPlaybackStopped = onSaveProgress,
        )

        if (playback.status == PlaybackStatus.Preparing || playback.status == PlaybackStatus.Buffering) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(32.dp), color = CinemaColors.Primary, strokeWidth = 3.dp)
                Text(if (playback.status == PlaybackStatus.Preparing) "Đang mở phim…" else "Đang tải video…", color = Color.White, fontSize = 16.sp)
            }
        }

        if ((controlsVisible || seekTarget != null) && panel == null && playback.status != PlaybackStatus.Error) {
            Column(
                Modifier.align(Alignment.TopStart).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .8f), Color.Transparent)))
                    .padding(horizontal = 48.dp, vertical = 27.dp),
            ) {
                Text(state.movieName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.episodeName, color = Color(0xFFB4BDC9), fontSize = 16.sp)
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .92f))))
                    .padding(start = 48.dp, end = 48.dp, top = 32.dp, bottom = 27.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                val displayPosition = seekTarget ?: playback.positionMs
                Column(
                    Modifier.fillMaxWidth().cinemaFocusable(
                        onClick = { seekTarget?.let(controller::seekTo); seekTarget = null; playFocus.requestFocus() },
                        enabled = playback.canSeek,
                        scaleFocused = 1f,
                        onFocusChanged = { timelineFocused = it },
                    ).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatPlaybackTime(displayPosition), color = Color.White, fontSize = 16.sp)
                        Text(if (playback.durationMs > 0L) formatPlaybackTime(playback.durationMs) else "—", color = Color(0xFFB4BDC9), fontSize = 16.sp)
                    }
                    Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = .22f))) {
                        if (playback.durationMs > 0L) {
                            Box(Modifier.fillMaxWidth((playback.bufferedPositionMs.toFloat() / playback.durationMs).coerceIn(0f, 1f)).fillMaxHeight().background(Color.White.copy(alpha = .25f)))
                            Box(Modifier.fillMaxWidth((displayPosition.toFloat() / playback.durationMs).coerceIn(0f, 1f)).fillMaxHeight().background(CinemaColors.Primary))
                        }
                    }
                    if (timelineFocused) Text("← / → Tua · OK về điều khiển", color = Color(0xFFB4BDC9), fontSize = 14.sp)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    PlayerAction("Lùi 10 giây", enabled = playback.canSeek, icon = Icons.Default.FastRewind, iconOnly = true) { controller.seekTo((playback.positionMs - 10_000).coerceAtLeast(0)); activity++ }
                    PlayerAction(if (playback.playWhenReady && playback.status != PlaybackStatus.Ended) "Tạm dừng" else if (playback.status == PlaybackStatus.Ended) "Xem lại" else "Phát", Modifier.focusRequester(playFocus), primary = true, icon = if (playback.playWhenReady && playback.status != PlaybackStatus.Ended) Icons.Default.Pause else Icons.Default.PlayArrow, iconOnly = true) { controller.toggle(); activity++ }
                    PlayerAction("Tới 10 giây", enabled = playback.canSeek, icon = Icons.Default.FastForward, iconOnly = true) { controller.seekTo(playback.positionMs + 10_000); activity++ }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    PlayerAction("Tập phim", Modifier.focusRequester(episodesFocus), icon = Icons.Default.VideoLibrary, iconOnly = true) { openPanel(PlayerPanel.Episodes, episodesFocus) }
                    PlayerAction("Nguồn phát", Modifier.focusRequester(sourcesFocus), icon = Icons.Default.Dns, iconOnly = true) { openPanel(PlayerPanel.Sources, sourcesFocus) }
                    PlayerAction("Cài đặt phát", Modifier.focusRequester(settingsFocus), icon = Icons.Default.Settings, iconOnly = true) { openPanel(PlayerPanel.Settings, settingsFocus) }
                    if (state.hasNextEpisode) PlayerAction("Tập tiếp theo", icon = Icons.Default.SkipNext, iconOnly = true) { onSaveProgress(); onNext() }
                }
                if (playback.status == PlaybackStatus.Ended && playback.isForeground && autoplay && state.hasNextEpisode && !cancelledAutoplay) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                        Text("Phát tập tiếp theo sau $countdown giây", color = Color.White, fontSize = 16.sp)
                        PlayerAction("Hủy tự phát", icon = Icons.Default.Close, iconOnly = true) { cancelledAutoplay = true }
                    }
                }
            }
        }

        if (playback.status == PlaybackStatus.Error && panel == null) {
            Column(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .88f)).padding(48.dp), verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                Text("Chưa phát được phim", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                Text(playback.errorMessage ?: "Kiểm tra kết nối và thử lại.", color = Color(0xFFB4BDC9), fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlayerAction("Thử lại", Modifier.focusRequester(errorFocus), primary = true, onClick = onRetry)
                    PlayerAction("Đổi nguồn") { openPanel(PlayerPanel.Sources, errorFocus) }
                    PlayerAction("Chọn tập") { openPanel(PlayerPanel.Episodes, errorFocus) }
                    PlayerAction("Quay lại") { onSaveProgress(); onBack() }
                }
            }
        }

        when (panel) {
            PlayerPanel.Episodes -> EpisodePanel(state.movie, state.currentServerIndex, state.currentEpisodeIndex, onSelectEpisode, { panel = null })
            PlayerPanel.Sources -> SourcePanel(state, onSelectSource, { panel = null })
            PlayerPanel.Settings -> SettingsPanel(speed, { speed = it }, resizeMode, { resizeMode = it }, { panel = null })
            null -> Unit
        }
    }
}

@Composable
private fun PlayerAction(
    label: String,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconOnly: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier.clip(RoundedCornerShape(8.dp))
            .background(if (primary) CinemaColors.Primary else Color(0xFF232A35))
            .cinemaFocusable(onClick = onClick, enabled = enabled, scaleFocused = 1f)
            .then(if (iconOnly) Modifier.size(56.dp) else Modifier.padding(horizontal = 18.dp, vertical = 13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (iconOnly && icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (!enabled) Color(0xFF727C8C) else if (primary) Color(0xFF211108) else Color.White,
                modifier = Modifier.size(26.dp),
            )
        } else {
            Text(label, color = if (!enabled) Color(0xFF727C8C) else if (primary) Color(0xFF211108) else Color.White,
                fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun PanelFrame(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .7f)), contentAlignment = Alignment.CenterEnd) {
        Column(
            Modifier.width(600.dp).fillMaxHeight().background(Color(0xFF171B23)).padding(32.dp).focusGroup(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                PlayerAction("Đóng", onClick = onClose)
            }
            content()
        }
    }
}

@Composable
private fun EpisodePanel(movie: MovieDetail, currentServerIndex: Int, currentEpisodeIndex: Int, onSelect: (Int, Int) -> Unit, onClose: () -> Unit) {
    var serverIndex by remember { mutableStateOf(currentServerIndex) }
    var group by remember { mutableStateOf(currentEpisodeIndex / 20) }
    val server = movie.servers.getOrNull(serverIndex)
    val episodes = server?.episodes.orEmpty()
    val groupCount = (episodes.size + 19) / 20
    val listState = rememberLazyListState()
    val initialFocus = remember { FocusRequester() }
    val currentEpisode = movie.servers.getOrNull(currentServerIndex)?.episodes?.getOrNull(currentEpisodeIndex)
    val activeIndex = if (serverIndex == currentServerIndex) currentEpisodeIndex
        else if (currentEpisode != null && server != null) equivalentEpisodeIndex(currentEpisode, server) ?: 0 else 0
    val targetIndex = if (activeIndex / 20 == group) activeIndex % 20 else 0
    val progress by AppContainer.localLibrary.episodeProgress.collectAsStateWithLifecycle()
    LaunchedEffect(serverIndex, group) {
        if (episodes.isNotEmpty()) {
            listState.scrollToItem(targetIndex)
            withFrameNanos { }
            runCatching { initialFocus.requestFocus() }
        }
    }
    PanelFrame("Tập phim", onClose) {
        Text(movie.name, color = Color(0xFFB4BDC9), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (movie.servers.size > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(movie.servers) { index, source ->
                PlayerAction(source.name.ifBlank { "Nguồn ${index + 1}" }, primary = index == serverIndex) {
                    serverIndex = index
                    val currentEpisode = movie.servers.getOrNull(currentServerIndex)?.episodes?.getOrNull(currentEpisodeIndex)
                    group = (currentEpisode?.let { equivalentEpisodeIndex(it, source) } ?: 0) / 20
                }
            }
        }
        if (groupCount > 1) LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed((0 until groupCount).toList()) { index, _ ->
                PlayerAction("${index * 20 + 1}–${minOf((index + 1) * 20, episodes.size)}", primary = group == index) { group = index }
            }
        }
        if (serverIndex == currentServerIndex && group != currentEpisodeIndex / 20) {
            PlayerAction("Đến tập đang xem") { group = currentEpisodeIndex / 20 }
        }
        if (episodes.isEmpty()) Text("Nguồn này chưa có tập phim.", color = Color.White, fontSize = 18.sp)
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(4.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
            itemsIndexed(episodes.drop(group * 20).take(20)) { localIndex, episode ->
                val index = group * 20 + localIndex
                val active = serverIndex == currentServerIndex && index == currentEpisodeIndex
                val completed = progress.isNotEmpty() && AppContainer.localLibrary.isEpisodeCompleted(movie.slug, server?.name.orEmpty(), episode)
                val label = episode.name.ifBlank { "Tập ${index + 1}" }
                PlayerAction(
                    "$label${if (active) " · Đang xem" else if (completed) " · Đã xem" else ""}",
                    Modifier.fillMaxWidth().then(if (localIndex == targetIndex) Modifier.focusRequester(initialFocus) else Modifier),
                    primary = active,
                ) { onSelect(serverIndex, index) }
            }
        }
    }
}

@Composable
private fun SourcePanel(state: WatchUiState.Success, onSelect: (Int) -> Unit, onClose: () -> Unit) {
    val selectedFocus = remember { FocusRequester() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.currentServerIndex)
    val currentEpisode = state.movie.servers.getOrNull(state.currentServerIndex)?.episodes?.getOrNull(state.currentEpisodeIndex)
    LaunchedEffect(Unit) { withFrameNanos { }; runCatching { selectedFocus.requestFocus() } }
    PanelFrame("Nguồn phát", onClose) {
        Text("Giữ tập và vị trí hiện tại khi nguồn hỗ trợ.", color = Color(0xFFB4BDC9), fontSize = 16.sp)
        state.message?.let { Text(it, color = Color(0xFFFFB579), fontSize = 16.sp) }
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(4.dp)) {
            itemsIndexed(state.movie.servers) { index, source ->
                val available = currentEpisode != null && equivalentEpisodeIndex(currentEpisode, source) != null
                val active = index == state.currentServerIndex
                PlayerAction(
                    source.name.ifBlank { "Nguồn ${index + 1}" } + if (active) " · Đang phát" else if (!available) " · Thiếu tập này" else "",
                    Modifier.fillMaxWidth().then(if (active) Modifier.focusRequester(selectedFocus) else Modifier),
                    primary = active,
                    enabled = available || active,
                ) { if (active) onClose() else onSelect(index) }
            }
        }
        Text("Nguồn thiếu tập: chọn nguồn và tập khác trong Tập phim.", color = Color(0xFFB4BDC9), fontSize = 14.sp)
    }
}

@Composable
private fun SettingsPanel(speed: Float, onSpeed: (Float) -> Unit, resizeMode: VideoResizeMode, onResize: (VideoResizeMode) -> Unit, onClose: () -> Unit) {
    val initialFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { withFrameNanos { }; runCatching { initialFocus.requestFocus() } }
    PanelFrame("Cài đặt phát", onClose) {
        Text("Tốc độ phát", color = Color(0xFFB4BDC9), fontSize = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(.75f, 1f, 1.25f, 1.5f, 2f).forEach { value ->
                PlayerAction("${value}×", if (value == speed) Modifier.focusRequester(initialFocus) else Modifier, primary = value == speed) { onSpeed(value) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Khung hình", color = Color(0xFFB4BDC9), fontSize = 18.sp)
        VideoResizeMode.entries.forEach { mode ->
            PlayerAction(mode.displayName, Modifier.fillMaxWidth(), primary = mode == resizeMode) { onResize(mode) }
        }
    }
}

@Composable
internal fun TvPlaybackLoadError(message: String, movie: MovieDetail?, onRetry: () -> Unit, onSelectEpisode: (Int, Int) -> Unit, onBack: () -> Unit) {
    var episodesOpen by remember { mutableStateOf(false) }
    val initialFocus = remember { FocusRequester() }
    PlatformBackHandler { if (episodesOpen) episodesOpen = false else onBack() }
    LaunchedEffect(episodesOpen) { if (!episodesOpen) { withFrameNanos { }; runCatching { initialFocus.requestFocus() } } }
    if (!episodesOpen) Column(Modifier.fillMaxSize().padding(48.dp), verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
        Text("Chưa mở được phim", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text(message, color = Color(0xFFB4BDC9), fontSize = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PlayerAction("Thử lại", Modifier.focusRequester(initialFocus), primary = true, onClick = onRetry)
            if (movie?.servers?.any { it.episodes.isNotEmpty() } == true) PlayerAction("Chọn nguồn / tập") { episodesOpen = true }
            PlayerAction("Quay lại", onClick = onBack)
        }
    }
    if (episodesOpen && movie != null) EpisodePanel(movie, 0, 0, onSelectEpisode, { episodesOpen = false })
}

internal fun formatPlaybackTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0L) / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainder = seconds % 60
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:${remainder.toString().padStart(2, '0')}"
    else "$minutes:${remainder.toString().padStart(2, '0')}"
}
