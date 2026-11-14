package nolelamphim.vercel.app.model

import androidx.compose.runtime.Immutable

@Immutable
data class Episode(
    val name: String,
    val slug: String,
    val embedUrl: String,
    val m3u8Url: String? = null,
)

@Immutable
data class EpisodeServer(
    val name: String,
    val episodes: List<Episode>,
)
