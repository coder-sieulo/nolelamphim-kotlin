package nolelamphim.vercel.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * @param url media URL a platform player can open directly (native playback).
 * @param fallbackUrl original embed page, used when [url] cannot be played natively.
 */
@Composable
expect fun VideoPlayer(
    url: String,
    fallbackUrl: String? = null,
    modifier: Modifier = Modifier,
)
