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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import nolelamphim.vercel.app.data.stream.StreamUrlResolver

@Composable
actual fun VideoPlayer(
    url: String,
    fallbackUrl: String?,
    modifier: Modifier,
    playbackSpeed: Float,
    resizeMode: VideoResizeMode,
    seekDelta: Long,
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
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val player = remember(streamUrl) {
        val dataSourceFactory = PngWrappedTsDataSource.Factory(
            DefaultDataSource.Factory(context),
        )
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(streamUrl))
                playWhenReady = true
                prepare()
            }
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

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                player.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                useController = true
                keepScreenOn = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                this.resizeMode = exoResizeMode
                setControllerShowTimeoutMs(3000)
            }
        },
        update = { view ->
            if (view.player !== player) {
                view.player = player
            }
            if (view.resizeMode != exoResizeMode) {
                view.resizeMode = exoResizeMode
            }
        },
    )
}

@Composable
private fun EmbeddedWebPlayer(url: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                setBackgroundColor(android.graphics.Color.BLACK)
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
    )
}
