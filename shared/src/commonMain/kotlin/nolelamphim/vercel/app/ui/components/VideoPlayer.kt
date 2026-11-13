package nolelamphim.vercel.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class VideoResizeMode(val displayName: String) {
    FIT("16:9 Chuẩn"),
    FILL("Kéo dãn"),
    ZOOM("Thu phóng 21:9"),
}

/**
 * @param url media URL a platform player can open directly (native playback).
 * @param fallbackUrl original embed page, used when [url] cannot be played natively.
 */
@Composable
expect fun VideoPlayer(
    url: String,
    fallbackUrl: String? = null,
    modifier: Modifier = Modifier,
    playbackSpeed: Float = 1f,
    resizeMode: VideoResizeMode = VideoResizeMode.FIT,
    seekDelta: Long = 0L,
)
