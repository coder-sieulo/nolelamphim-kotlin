package nolelamphim.vercel.app.model

data class MovieDetail(
    val id: Int,
    val name: String,
    val originName: String,
    val slug: String,
    val description: String,
    val posterUrl: String?,
    val year: Int?,
    val quality: String?,
    val lang: String?,
    val status: String?,
    val episodeCurrent: String?,
    val episodeTotal: String?,
    val duration: String?,
    val rating: Double,
    val categories: List<String>,
    val countries: List<String>,
    val actors: List<String>,
    val directors: List<String>,
    val servers: List<EpisodeServer>,
)
