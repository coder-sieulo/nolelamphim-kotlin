package nolelamphim.vercel.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import nolelamphim.vercel.app.data.remote.dto.MovieDetailResponse
import nolelamphim.vercel.app.data.remote.dto.MovieListResponse
import nolelamphim.vercel.app.data.remote.dto.TaxonomyDto
import nolelamphim.vercel.app.data.remote.dto.TaxonomyResponse

class MovieApi(private val client: HttpClient) {

    suspend fun latest(
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach/phim-moi-cap-nhat") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun byCategory(
        categorySlug: String,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach/$categorySlug") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun filtered(
        category: String? = null,
        country: String? = null,
        year: Int? = null,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("danh-sach") {
            category?.let { parameter("category", it) }
            country?.let { parameter("country", it) }
            year?.let { parameter("year", it) }
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun search(
        keyword: String,
        page: Int = 1,
        limit: Int = DEFAULT_PAGE_SIZE,
    ): MovieListResponse =
        client.get("tim-kiem") {
            parameter("keyword", keyword)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun detail(slug: String): MovieDetailResponse =
        client.get("phim/$slug").body()

    suspend fun genres(): List<TaxonomyDto> =
        client.get("the-loai").body<TaxonomyResponse>().data.items

    suspend fun countries(): List<TaxonomyDto> =
        client.get("quoc-gia").body<TaxonomyResponse>().data.items
}
