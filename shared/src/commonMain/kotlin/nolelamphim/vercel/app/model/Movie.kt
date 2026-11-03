package nolelamphim.vercel.app.model

data class Movie(
    val id: Int,
    val name: String,
    val originName: String,
    val slug: String,
    val posterUrl: String?,
    val thumbUrl: String?,
    val year: Int?,
    val rating: Double,
)
