package nolelamphim.vercel.app.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import nolelamphim.vercel.app.data.stream.StreamUrlResolver

@Composable
actual fun VideoPlayer(
    url: String,
    fallbackUrl: String?,
    modifier: Modifier,
    playbackSpeed: Float,
    resizeMode: VideoResizeMode,
    seekDelta: Long,
    initialPositionMs: Long,
    showControls: Boolean,
    onPositionChanged: ((currentMs: Long, durationMs: Long) -> Unit)?,
    controller: PlaybackController?,
    allowEmbeddedPlayback: Boolean,
    onPlaybackStopped: (() -> Unit)?,
) {
    val streamUrl = remember(url) { StreamUrlResolver.resolve(url) }
    if (streamUrl == null) {
        if (allowEmbeddedPlayback) {
            EmbeddedWebPlayer(url = fallbackUrl ?: url, modifier = modifier)
        } else {
            LaunchedEffect(url, controller) {
                controller?.update(PlaybackSnapshot(
                    status = PlaybackStatus.Error,
                    errorMessage = "Nguồn này chưa hỗ trợ phát bằng remote. Hãy chọn nguồn khác.",
                ))
            }
            Box(modifier.background(Color.Black))
        }
    } else {
        NativeVideoPlayer(streamUrl, modifier, playbackSpeed, resizeMode, seekDelta,
            initialPositionMs, showControls, onPositionChanged, controller, onPlaybackStopped)
    }
}

@Composable
private fun NativeVideoPlayer(
    streamUrl: String,
    modifier: Modifier,
    playbackSpeed: Float,
    resizeMode: VideoResizeMode,
    seekDelta: Long,
    initialPositionMs: Long,
    showControls: Boolean,
    onPositionChanged: ((currentMs: Long, durationMs: Long) -> Unit)?,
    controller: PlaybackController?,
    onPlaybackStopped: (() -> Unit)?,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sessionKey = controller?.sessionKey ?: streamUrl
    val player = remember(streamUrl, sessionKey) { SharedPlayerSession.getOrCreate(context, streamUrl, initialPositionMs, sessionKey) }
    // Freeze callbacks with the media session. A late disposal/event must never call a new
    // episode's callback after Compose has already updated the surrounding parameters.
    val progressCallback = remember(player) { onPositionChanged }
    val stoppedCallback = remember(player) { onPlaybackStopped }

    fun publish() {
        val position = player.currentPosition.coerceAtLeast(0L)
        val duration = player.duration.coerceAtLeast(0L)
        val status = when {
            player.playerError != null -> PlaybackStatus.Error
            player.playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
            player.playbackState == Player.STATE_IDLE -> PlaybackStatus.Preparing
            player.playbackState == Player.STATE_BUFFERING && player.playWhenReady -> PlaybackStatus.Buffering
            player.isPlaying -> PlaybackStatus.Playing
            else -> PlaybackStatus.Paused
        }
        controller?.update(PlaybackSnapshot(
            status = status,
            positionMs = position,
            durationMs = duration,
            bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
            canSeek = player.isCurrentMediaItemSeekable,
            playWhenReady = player.playWhenReady,
            isForeground = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED),
            errorMessage = if (player.playerError != null) "Không phát được nguồn này. Kiểm tra mạng hoặc chọn nguồn khác." else null,
        ))
        if (position > 0L || duration > 0L) progressCallback?.invoke(position, duration)
    }

    LaunchedEffect(player, playbackSpeed) { player.setPlaybackSpeed(playbackSpeed) }
    LaunchedEffect(player, seekDelta) {
        if (seekDelta != 0L) {
            val duration = player.duration.coerceAtLeast(0L)
            val target = (player.currentPosition + seekDelta).coerceAtLeast(0L)
            player.seekTo(if (duration > 0L) target.coerceAtMost(duration) else target)
        }
    }
    val command = controller?.command
    LaunchedEffect(player, command?.id) {
        when (command?.action) {
            PlaybackAction.Play -> { if (player.playbackState == Player.STATE_ENDED) player.seekTo(0L); player.play() }
            PlaybackAction.Pause -> player.pause()
            PlaybackAction.Toggle -> when {
                player.playbackState == Player.STATE_ENDED -> { player.seekTo(0L); player.play() }
                player.isPlaying || player.playWhenReady -> player.pause()
                else -> player.play()
            }
            PlaybackAction.Seek -> {
                if (player.isCurrentMediaItemSeekable) {
                    val duration = player.duration.coerceAtLeast(0L)
                    player.seekTo(if (duration > 0L) command.value.coerceIn(0L, duration) else command.value.coerceAtLeast(0L))
                }
            }
            PlaybackAction.Retry -> { player.prepare(); player.play() }
            PlaybackAction.Speed -> player.setPlaybackSpeed(command.value / 1000f)
            null -> Unit
        }
        publish()
    }
    LaunchedEffect(player, controller) {
        while (true) { publish(); delay(500) }
    }
    DisposableEffect(player, controller, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) { publish() }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                // Resuming the Activity never silently restarts playback.
                player.pause()
                publish()
                stoppedCallback?.invoke()
            }
            if (event == Lifecycle.Event.ON_RESUME) publish()
        }
        player.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(observer)
        publish()
        onDispose {
            // A changed stream can replace/release the shared session before this surface disposes.
            runCatching { publish() }
            stoppedCallback?.invoke()
            runCatching { player.removeListener(listener) }
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (!SharedPlayerSession.isRetained) SharedPlayerSession.releaseIfUnused(streamUrl, sessionKey)
            else SharedPlayerSession.isRetained = false
        }
    }
    val exoResizeMode = when (resizeMode) {
        VideoResizeMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
        VideoResizeMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
        VideoResizeMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    }
    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                this.player = player
                keepScreenOn = true
                setControllerShowTimeoutMs(4000)
                setShowBuffering(if (showControls) PlayerView.SHOW_BUFFERING_WHEN_PLAYING else PlayerView.SHOW_BUFFERING_NEVER)
                useController = showControls
                isFocusable = showControls
                descendantFocusability = if (showControls) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
                this.resizeMode = exoResizeMode
            }
        },
        update = { view ->
            view.player = player
            view.useController = showControls
            view.isFocusable = showControls
            view.descendantFocusability = if (showControls) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
            view.resizeMode = exoResizeMode
            if (!showControls) view.hideController()
        },
        onRelease = { it.player = null },
    )
}

@Composable
private fun EmbeddedWebPlayer(url: String, modifier: Modifier) {
    var webViewFailed by remember { mutableStateOf(false) }

    if (webViewFailed) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Không hỗ trợ phát nhúng trên thiết bị này",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Vui lòng chuyển sang máy chủ KKPhim (HLS VIP)",
                    color = Color(0xFFF97316),
                    fontSize = 12.sp,
                )
            }
        }
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            try {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            } catch (t: Throwable) {
                webViewFailed = true
                android.view.View(context)
            }
        },
        update = { view ->
            if (view is WebView && view.url != url) {
                try {
                    view.loadUrl(url)
                } catch (_: Throwable) {}
            }
        },
        onRelease = { view ->
            if (view is WebView) {
                try {
                    view.stopLoading()
                    view.destroy()
                } catch (_: Throwable) {}
            }
        },
    )
}
