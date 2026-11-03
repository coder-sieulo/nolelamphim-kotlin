package nolelamphim.vercel.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaxonomyDto(
    @SerialName("_id") val id: Int = 0,
    val name: String,
    val slug: String,
)

@Serializable
data class TaxonomyDataDto(
    val items: List<TaxonomyDto> = emptyList(),
)

@Serializable
data class TaxonomyResponse(
    val data: TaxonomyDataDto = TaxonomyDataDto(),
)
