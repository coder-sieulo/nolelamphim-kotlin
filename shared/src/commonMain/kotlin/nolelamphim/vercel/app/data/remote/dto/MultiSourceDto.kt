package nolelamphim.vercel.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── KKPHIM (phimapi.com) ──

@Serializable
data class KkphimTaxonomyDto(
    val name: String = "",
    val slug: String = "",
)

@Serializable
data class KkphimEpisodeDto(
    val name: String = "",
    val slug: String = "",
    val filename: String = "",
    @SerialName("link_embed") val linkEmbed: String = "",
    @SerialName("link_m3u8") val linkM3u8: String? = null,
)

@Serializable
data class KkphimServerDto(
    @SerialName("server_name") val serverName: String = "KKPhim",
    @SerialName("server_data") val serverData: List<KkphimEpisodeDto> = emptyList(),
)

/** Format 2 detail item (/v1/api/phim/{slug}) */
@Serializable
data class KkphimItem(
    val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    val slug: String = "",
    val content: String? = null,
    val type: String? = null,
    val status: String? = null,
    val year: Int? = null,
    val time: String? = null,
    @SerialName("episode_current") val episodeCurrent: String? = null,
    @SerialName("episode_total") val episodeTotal: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val actor: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val category: List<KkphimTaxonomyDto> = emptyList(),
    val country: List<KkphimTaxonomyDto> = emptyList(),
    val episodes: List<KkphimServerDto> = emptyList(),
)

@Serializable
data class KkphimDetailData(
    val item: KkphimItem? = null,
    @SerialName("APP_DOMAIN_CDN_IMAGE") val cdnImage: String? = null,
)

@Serializable
data class KkphimDetailResponse(
    val data: KkphimDetailData? = null,
)

/** Format 1 detail response (/phim/{slug}) */
@Serializable
data class KkphimFormat1Movie(
    val name: String = "",
    @SerialName("origin_name") val originName: String = "",
    val slug: String = "",
    val content: String? = null,
    val type: String? = null,
    val status: String? = null,
    val year: Int? = null,
    val time: String? = null,
    @SerialName("episode_current") val episodeCurrent: String? = null,
    @SerialName("episode_total") val episodeTotal: String? = null,
    val quality: String? = null,
    val lang: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val actor: List<String> = emptyList(),
    val director: List<String> = emptyList(),
    val category: List<KkphimTaxonomyDto> = emptyList(),
    val country: List<KkphimTaxonomyDto> = emptyList(),
)

@Serializable
data class KkphimFormat1Response(
    val movie: KkphimFormat1Movie? = null,
    val episodes: List<KkphimServerDto> = emptyList(),
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

@Serializable
data class KkphimSearchData(
    val items: List<KkphimSearchItem> = emptyList(),
    val params: KkphimSearchParams? = null,
)

@Serializable
data class KkphimSearchParams(
    val pagination: KkphimPagination? = null,
)

@Serializable
data class KkphimPagination(
    val totalItems: Int = 0,
    val totalItemsPerPage: Int = 20,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
)

@Serializable
data class KkphimSearchResponse(
    val data: KkphimSearchData? = null,
)

@Serializable
data class KkphimNewListResponse(
    val items: List<KkphimSearchItem> = emptyList(),
    val pagination: KkphimPagination? = null,
)


// ── NGUONC (phim.nguonc.com) ──

@Serializable
data class NguoncItemDto(
    val name: String = "",
    val slug: String = "",
    val embed: String = "",
    val m3u8: String? = null,
)

@Serializable
data class NguoncServerDto(
    @SerialName("server_name") val serverName: String = "NguonC",
    val items: List<NguoncItemDto> = emptyList(),
)

@Serializable
data class NguoncMovie(
    val name: String = "",
    @SerialName("original_name") val originalName: String = "",
    val slug: String = "",
    val description: String? = null,
    val year: Int? = null,
    val time: String? = null,
    @SerialName("current_episode") val currentEpisode: String? = null,
    val quality: String? = null,
    val language: String? = null,
    val director: String? = null,
    val casts: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val episodes: List<NguoncServerDto> = emptyList(),
)

@Serializable
data class NguoncDetailResponse(
    val status: String? = null,
    val movie: NguoncMovie? = null,
)

@Serializable
data class NguoncSearchItem(
    val name: String = "",
    @SerialName("original_name") val originalName: String = "",
    val slug: String = "",
    val year: Int? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val quality: String? = null,
    val language: String? = null,
)

@Serializable
data class NguoncPaginate(
    @SerialName("current_page") val currentPage: Int = 1,
    @SerialName("total_page") val totalPage: Int = 1,
    @SerialName("total_items") val totalItems: Int = 0,
    @SerialName("items_per_page") val itemsPerPage: Int = 10,
)

@Serializable
data class NguoncSearchResponse(
    val status: String? = null,
    val items: List<NguoncSearchItem> = emptyList(),
    val paginate: NguoncPaginate? = null,
)
