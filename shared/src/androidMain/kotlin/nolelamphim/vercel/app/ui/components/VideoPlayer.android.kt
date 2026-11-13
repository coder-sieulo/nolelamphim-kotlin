package nolelamphim.vercel.app.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
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
) {
    val streamUrl = remember(url) { StreamUrlResolver.resolve(url) }
    if (streamUrl == null) {
        // Unknown source shape: keep the original page player as a safety net.
        EmbeddedWebPlayer(url = fallbackUrl ?: url, modifier = modifier)
    } else {
        NativeVideoPlayer(
            streamUrl = streamUrl,
            modifier = modifier,
            playbackSpeed = playbackSpeed,
            resizeMode = resizeMode,
            seekDelta = seekDelta,
            initialPositionMs = initialPositionMs,
            showControls = showControls,
            onPositionChanged = onPositionChanged,
        )
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
) {
    val context = LocalContext.current

    val player = remember(streamUrl) {
        SharedPlayerSession.getOrCreate(context, streamUrl, initialPositionMs)
    }

    LaunchedEffect(player, playbackSpeed) {
        player.setPlaybackSpeed(playbackSpeed)
    }

    LaunchedEffect(seekDelta) {
        if (seekDelta != 0L) {
            val currentPos = player.currentPosition
            val totalDuration = player.duration.coerceAtLeast(0L)
            val target = if (totalDuration > 0L) {
                (currentPos + seekDelta).coerceIn(0L, totalDuration)
            } else {
                (currentPos + seekDelta).coerceAtLeast(0L)
            }
            player.seekTo(target)
        }
    }

    // Periodically update progress callback every 500ms
    LaunchedEffect(player, onPositionChanged) {
        if (onPositionChanged == null) return@LaunchedEffect
        while (true) {
            val cur = player.currentPosition.coerceAtLeast(0L)
            val dur = player.duration.coerceAtLeast(0L)
            if (cur > 0L || dur > 0L) {
                onPositionChanged(cur, dur)
            }
            delay(500)
        }
    }

    DisposableEffect(streamUrl) {
        onDispose {
            val cur = player.currentPosition.coerceAtLeast(0L)
            val dur = player.duration.coerceAtLeast(0L)
            if (cur > 0L) {
                onPositionChanged?.invoke(cur, dur)
            }
            if (!SharedPlayerSession.isRetained) {
                SharedPlayerSession.releaseIfUnused(streamUrl)
            } else {
                // Was retained for transition (minimizing/expanding), reset flag for next cycle
                SharedPlayerSession.isRetained = false
            }
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
                useController = showControls
                keepScreenOn = true
                setShowBuffering(if (showControls) PlayerView.SHOW_BUFFERING_WHEN_PLAYING else PlayerView.SHOW_BUFFERING_NEVER)
                this.resizeMode = exoResizeMode
                if (showControls) {
                    setControllerShowTimeoutMs(3000)
                }
            }
        },
        update = { view ->
            if (view.player != player) {
                view.player = player
            }
            view.useController = showControls
            view.resizeMode = exoResizeMode
        },
        onRelease = { view ->
            // Detach player from view to allow clean surface transition
            view.player = null
        },
    )
}

@Composable
private fun EmbeddedWebPlayer(url: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.destroy()
        },
    )
}
