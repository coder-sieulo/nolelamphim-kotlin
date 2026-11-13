package nolelamphim.vercel.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── KKPHIM (phimapi.com) ──
@Serializable
data class KkphimDetailResponse(
    val status: String? = null,
    val data: KkphimDetailData? = null,
)

@Serializable
data class KkphimDetailData(
    val item: KkphimItem? = null,
)

@Serializable
data class KkphimSearchResponse(
    val status: String? = null,
    val data: KkphimSearchData? = null,
)

@Serializable
data class KkphimSearchData(
    val items: List<KkphimSearchItem> = emptyList(),
)

@Serializable
data class KkphimItem(
    val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    val slug: String = "",
    val year: Int? = null,
    val episodes: List<KkphimServerDto> = emptyList(),
)

@Serializable
data class KkphimServerDto(
    @SerialName("server_name") val serverName: String = "KKPhim",
    @SerialName("server_data") val serverData: List<KkphimEpisodeDto> = emptyList(),
)

@Serializable
data class KkphimEpisodeDto(
    val name: String = "",
    val slug: String = "",
    @SerialName("link_embed") val linkEmbed: String = "",
    @SerialName("link_m3u8") val linkM3u8: String? = null,
)

@Serializable
data class KkphimSearchItem(
    val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    val slug: String = "",
    val year: Int? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
)

// ── NGUONC (phim.nguonc.com) ──
@Serializable
data class NguoncDetailResponse(
    val status: String? = null,
    val movie: NguoncMovie? = null,
)

@Serializable
data class NguoncMovie(
    val name: String = "",
    @SerialName("original_name") val originalName: String = "",
    val slug: String = "",
    val year: Int? = null,
    val episodes: List<NguoncServerDto> = emptyList(),
)

@Serializable
data class NguoncServerDto(
    @SerialName("server_name") val serverName: String = "NguonC",
    val items: List<NguoncItemDto> = emptyList(),
)

@Serializable
data class NguoncItemDto(
    val name: String = "",
    val slug: String = "",
    val embed: String = "",
    val m3u8: String? = null,
)

@Serializable
data class NguoncSearchResponse(
    val status: String? = null,
    val items: List<NguoncSearchItem> = emptyList(),
)

@Serializable
data class NguoncSearchItem(
    val name: String = "",
    @SerialName("original_name") val originalName: String = "",
    val slug: String = "",
    val year: Int? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
)
