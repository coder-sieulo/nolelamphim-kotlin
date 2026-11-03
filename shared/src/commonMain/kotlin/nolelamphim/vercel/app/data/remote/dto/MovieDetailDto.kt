package nolelamphim.vercel.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDto(
    @SerialName("_id") val id: Int,
    val name: String,
    @SerialName("origin_name") val originName: String = "",
    val slug: String,
    val content: String = "",
    val type: String = "",
    val status: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("poster_url") val posterUrl: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("trailer_url") val trailerUrl: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val time: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("episode_current") val episodeCurrent: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("episode_total") val episodeTotal: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val quality: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val lang: String? = null,
    val year: Int? = null,
    val view: Long? = null,
    val chieurap: Boolean? = null,
    val actor: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val category: List<TaxonomyDto> = emptyList(),
    val country: List<TaxonomyDto> = emptyList(),
    val tmdb: TmdbDto? = null,
)

@Serializable
data class EpisodeDto(
    val name: String,
    val slug: String = "",
    val filename: String = "",
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("link_embed") val linkEmbed: String = "",
)

@Serializable
data class ServerDto(
    @SerialName("server_name") val serverName: String,
    @SerialName("server_data") val serverData: List<EpisodeDto> = emptyList(),
)

@Serializable
data class MovieDetailResponse(
    val movie: MovieDetailDto,
    val episodes: List<ServerDto> = emptyList(),
)
