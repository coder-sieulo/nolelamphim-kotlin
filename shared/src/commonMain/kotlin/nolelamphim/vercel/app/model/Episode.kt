package nolelamphim.vercel.app.model

data class Episode(
    val name: String,
    val slug: String,
    val embedUrl: String,
    val m3u8Url: String? = null,
)

data class EpisodeServer(
    val name: String,
    val episodes: List<Episode>,
)
