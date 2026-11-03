package nolelamphim.vercel.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbDto(
    val type: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("vote_average") val voteAverage: String? = null,
)

@Serializable
data class MovieSummaryDto(
    @SerialName("_id") val id: Int,
    val name: String,
    @SerialName("origin_name") val originName: String = "",
    val slug: String,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("poster_url") val posterUrl: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val year: Int? = null,
    val tmdb: TmdbDto? = null,
)

@Serializable
data class PaginationDto(
    val totalItems: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
)

@Serializable
data class MovieListResponse(
    val items: List<MovieSummaryDto> = emptyList(),
    val pagination: PaginationDto = PaginationDto(),
)
