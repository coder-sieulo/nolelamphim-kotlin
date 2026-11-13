package nolelamphim.vercel.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nolelamphim.vercel.app.data.MoviePage
import nolelamphim.vercel.app.data.remote.dto.KkphimDetailResponse
import nolelamphim.vercel.app.data.remote.dto.KkphimFormat1Response
import nolelamphim.vercel.app.data.remote.dto.KkphimNewListResponse
import nolelamphim.vercel.app.data.remote.dto.KkphimSearchResponse
import nolelamphim.vercel.app.data.remote.dto.KkphimServerDto
import nolelamphim.vercel.app.data.remote.dto.NguoncDetailResponse
import nolelamphim.vercel.app.data.remote.dto.NguoncSearchResponse
import nolelamphim.vercel.app.data.remote.dto.NguoncServerDto
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

class MultiSourceService(private val client: HttpClient) {

    /**
     * Resolves alternative servers from KKPhim (phimapi.com) and NguonC (phim.nguonc.com)
     * for a given movie slug or title.
     */
    suspend fun resolveAlternativeServers(slug: String, movieName: String): List<EpisodeServer> = coroutineScope {
        val kkphimDeferred = async { resolveKkphimServers(slug, movieName) }
        val nguoncDeferred = async { resolveNguoncServers(slug, movieName) }

        val kkphimServers = try { kkphimDeferred.await() } catch (_: Exception) { emptyList() }
        val nguoncServers = try { nguoncDeferred.await() } catch (_: Exception) { emptyList() }

        buildList {
            addAll(kkphimServers)
            addAll(nguoncServers)
        }
    }

    /**
     * Searches movies from KKPhim and NguonC to complement VSMov catalog results.
     */
    suspend fun searchAlternativeSources(keyword: String): List<Movie> = coroutineScope {
        val kkphimDeferred = async { searchKkphim(keyword) }
        val nguoncDeferred = async { searchNguonc(keyword) }

        val kkphimMovies = try { kkphimDeferred.await() } catch (_: Exception) { emptyList() }
        val nguoncMovies = try { nguoncDeferred.await() } catch (_: Exception) { emptyList() }

        buildList {
            addAll(kkphimMovies)
            addAll(nguoncMovies)
        }
    }

    /**
     * Attempts to fetch movie detail directly from KKPhim (phimapi.com).
     * Used when the slug belongs to KKPhim or when VSMov 404s.
     */
    suspend fun getKkphimDetailAsMovieDetail(slug: String): MovieDetail? {
        // Try Format 2 (/v1/api/phim/{slug})
        try {
            val res = client.get("https://phimapi.com/v1/api/phim/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimDetailResponse>()

            val item = res.data?.item
            if (item != null && item.name.isNotBlank()) {
                val servers = mapKkphimEpisodes(item.episodes)
                return MovieDetail(
                    id = item.slug.hashCode(),
                    name = item.name,
                    originName = item.originName,
                    slug = item.slug,
                    description = item.content.orEmpty(),
                    posterUrl = formatKkphimImageUrl(item.posterUrl ?: item.thumbUrl),
                    year = item.year,
                    quality = item.quality,
                    lang = item.lang,
                    status = item.status,
                    episodeCurrent = item.episodeCurrent,
                    episodeTotal = item.episodeTotal,
                    duration = item.time,
                    rating = 0.0,
                    categories = item.category.map { it.name },
                    categorySlugs = item.category.map { it.slug },
                    countries = item.country.map { it.name },
                    actors = item.actor,
                    directors = item.director,
                    servers = servers,
                )
            }
        } catch (_: Exception) {}

        // Try Format 1 (/phim/{slug})
        try {
            val res1 = client.get("https://phimapi.com/phim/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimFormat1Response>()

            val m = res1.movie
            if (m != null && m.name.isNotBlank()) {
                val servers = mapKkphimEpisodes(res1.episodes)
                return MovieDetail(
                    id = m.slug.hashCode(),
                    name = m.name,
                    originName = m.originName,
                    slug = m.slug,
                    description = m.content.orEmpty(),
                    posterUrl = formatKkphimImageUrl(m.posterUrl ?: m.thumbUrl),
                    year = m.year,
                    quality = m.quality,
                    lang = m.lang,
                    status = m.status,
                    episodeCurrent = m.episodeCurrent,
                    episodeTotal = m.episodeTotal,
                    duration = m.time,
                    rating = 0.0,
                    categories = m.category.map { it.name },
                    categorySlugs = m.category.map { it.slug },
                    countries = m.country.map { it.name },
                    actors = m.actor,
                    directors = m.director,
                    servers = servers,
                )
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Attempts to fetch movie detail directly from NguonC (phim.nguonc.com).
     * Used when the slug belongs to NguonC or when other sources fail.
     */
    suspend fun getNguoncDetailAsMovieDetail(slug: String): MovieDetail? {
        try {
            val res = client.get("https://phim.nguonc.com/api/film/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<NguoncDetailResponse>()

            val movie = res.movie
            if (movie != null && movie.name.isNotBlank()) {
                val servers = mapNguoncEpisodes(movie.episodes)
                val castList = movie.casts?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
                val dirList = movie.director?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()

                return MovieDetail(
                    id = movie.slug.hashCode(),
                    name = movie.name,
                    originName = movie.originalName,
                    slug = movie.slug,
                    description = movie.description.orEmpty(),
                    posterUrl = movie.posterUrl ?: movie.thumbUrl,
                    year = movie.year,
                    quality = movie.quality,
                    lang = movie.language,
                    status = null,
                    episodeCurrent = movie.currentEpisode,
                    episodeTotal = null,
                    duration = movie.time,
                    rating = 0.0,
                    categories = emptyList(),
                    categorySlugs = emptyList(),
                    countries = emptyList(),
                    actors = castList,
                    directors = dirList,
                    servers = servers,
                )
            }
        } catch (_: Exception) {}

        return null
    }

    private suspend fun resolveKkphimServers(slug: String, movieName: String): List<EpisodeServer> {
        try {
            // 1. Try direct slug match
            val direct = fetchKkphimDetail(slug)
            if (direct != null && direct.isNotEmpty()) {
                return mapKkphimEpisodes(direct)
            }

            // 2. Search KKPhim by movie title
            val search = client.get("https://phimapi.com/v1/api/tim-kiem") {
                parameter("keyword", movieName)
                parameter("limit", 6)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimSearchResponse>()

            val items = search.data?.items.orEmpty()
            if (items.isEmpty()) return emptyList()

            // Pick closest match by comparing normalized names
            val normTarget = normalizeTitle(movieName)
            val bestHit = items.firstOrNull { normalizeTitle(it.name) == normTarget || normalizeTitle(it.originName) == normTarget }
                ?: items.first()

            val searchedEpisodes = fetchKkphimDetail(bestHit.slug)
            return if (searchedEpisodes != null) mapKkphimEpisodes(searchedEpisodes) else emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private suspend fun fetchKkphimDetail(slug: String): List<KkphimServerDto>? {
        // Try format 2
        try {
            val res2 = client.get("https://phimapi.com/v1/api/phim/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimDetailResponse>()
            val eps = res2.data?.item?.episodes
            if (!eps.isNullOrEmpty()) return eps
        } catch (_: Exception) {}

        // Try format 1
        try {
            val res1 = client.get("https://phimapi.com/phim/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimFormat1Response>()
            if (res1.episodes.isNotEmpty()) return res1.episodes
        } catch (_: Exception) {}

        return null
    }

    private fun mapKkphimEpisodes(servers: List<KkphimServerDto>): List<EpisodeServer> {
        return servers.mapNotNull { srv ->
            if (srv.serverData.isEmpty()) null
            else {
                val serverLabel = srv.serverName.ifBlank { "VIP 1080p" }
                EpisodeServer(
                    name = "KKPhim - $serverLabel (HLS VIP)",
                    episodes = srv.serverData.map { ep ->
                        Episode(
                            name = ep.name.ifBlank { "Tập" },
                            slug = ep.slug,
                            embedUrl = ep.linkEmbed.ifBlank { ep.linkM3u8.orEmpty() },
                            m3u8Url = ep.linkM3u8?.takeIf { it.isNotBlank() },
                        )
                    },
                )
            }
        }
    }

    private suspend fun resolveNguoncServers(slug: String, movieName: String): List<EpisodeServer> {
        try {
            // 1. Try direct slug match
            val direct = fetchNguoncDetail(slug)
            if (direct?.movie?.episodes?.isNotEmpty() == true) {
                return mapNguoncEpisodes(direct.movie.episodes)
            }

            // 2. Search NguonC by movie title
            val search = client.get("https://phim.nguonc.com/api/films/search") {
                parameter("keyword", movieName)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<NguoncSearchResponse>()

            val items = search.items
            if (items.isEmpty()) return emptyList()

            val normTarget = normalizeTitle(movieName)
            val bestHit = items.firstOrNull { normalizeTitle(it.name) == normTarget || normalizeTitle(it.originalName) == normTarget }
                ?: items.first()

            val searchedDetail = fetchNguoncDetail(bestHit.slug)
            return mapNguoncEpisodes(searchedDetail?.movie?.episodes.orEmpty())
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private suspend fun fetchNguoncDetail(slug: String): NguoncDetailResponse? {
        return try {
            client.get("https://phim.nguonc.com/api/film/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<NguoncDetailResponse>()
        } catch (_: Exception) {
            null
        }
    }

    private fun mapNguoncEpisodes(servers: List<NguoncServerDto>): List<EpisodeServer> {
        return servers.mapNotNull { srv ->
            if (srv.items.isEmpty()) null
            else {
                val serverLabel = srv.serverName.ifBlank { "Dự phòng" }
                EpisodeServer(
                    name = "NguonC - $serverLabel (Dự phòng)",
                    episodes = srv.items.map { item ->
                        Episode(
                            name = item.name.ifBlank { "Tập" },
                            slug = item.slug,
                            embedUrl = item.embed,
                            m3u8Url = item.m3u8?.takeIf { it.isNotBlank() },
                        )
                    },
                )
            }
        }
    }

    private suspend fun searchKkphim(keyword: String): List<Movie> {
        return try {
            val res = client.get("https://phimapi.com/v1/api/tim-kiem") {
                parameter("keyword", keyword)
                parameter("limit", 15)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimSearchResponse>()

            res.data?.items.orEmpty().mapNotNull { item ->
                if (item.slug.isBlank() || item.name.isBlank()) null
                else {
                    Movie(
                        id = item.slug.hashCode(),
                        name = item.name,
                        originName = item.originName,
                        slug = item.slug,
                        posterUrl = formatKkphimImageUrl(item.posterUrl ?: item.thumbUrl),
                        thumbUrl = formatKkphimImageUrl(item.thumbUrl ?: item.posterUrl),
                        year = item.year,
                        rating = 0.0,
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun searchNguonc(keyword: String): List<Movie> {
        return try {
            val res = client.get("https://phim.nguonc.com/api/films/search") {
                parameter("keyword", keyword)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<NguoncSearchResponse>()

            res.items.mapNotNull { item ->
                if (item.slug.isBlank() || item.name.isBlank()) null
                else {
                    Movie(
                        id = item.slug.hashCode(),
                        name = item.name,
                        originName = item.originalName,
                        slug = item.slug,
                        posterUrl = item.posterUrl ?: item.thumbUrl,
                        thumbUrl = item.thumbUrl ?: item.posterUrl,
                        year = item.year,
                        rating = 0.0,
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fetches newest movies from KKPhim (/danh-sach/phim-moi-cap-nhat?page={page}).
     */
    suspend fun fetchKkphimNewList(page: Int = 1): MoviePage {
        val res = client.get("https://phimapi.com/danh-sach/phim-moi-cap-nhat") {
            parameter("page", page)
            header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
        }.body<KkphimNewListResponse>()

        val movies = res.items.mapNotNull { item ->
            if (item.slug.isBlank() || item.name.isBlank()) null
            else Movie(
                id = item.slug.hashCode(),
                name = item.name,
                originName = item.originName,
                slug = item.slug,
                posterUrl = formatKkphimImageUrl(item.posterUrl ?: item.thumbUrl),
                thumbUrl = formatKkphimImageUrl(item.thumbUrl ?: item.posterUrl),
                year = item.year,
                rating = 0.0,
            )
        }
        val p = res.pagination
        return MoviePage(
            movies = movies,
            page = p?.currentPage ?: page,
            totalPages = p?.totalPages ?: page,
            totalItems = p?.totalItems ?: movies.size,
        )
    }

    /**
     * Fetches newest movies from NguonC (/api/films/phim-moi-cap-nhat?page={page}).
     */
    suspend fun fetchNguoncNewList(page: Int = 1): MoviePage {
        val res = client.get("https://phim.nguonc.com/api/films/phim-moi-cap-nhat") {
            parameter("page", page)
            header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
        }.body<NguoncSearchResponse>()

        val movies = res.items.mapNotNull { item ->
            if (item.slug.isBlank() || item.name.isBlank()) null
            else Movie(
                id = item.slug.hashCode(),
                name = item.name,
                originName = item.originalName,
                slug = item.slug,
                posterUrl = item.posterUrl ?: item.thumbUrl,
                thumbUrl = item.thumbUrl ?: item.posterUrl,
                year = item.year,
                rating = 0.0,
            )
        }
        val p = res.paginate
        return MoviePage(
            movies = movies,
            page = p?.currentPage ?: page,
            totalPages = p?.totalPage ?: page,
            totalItems = p?.totalItems ?: movies.size,
        )
    }

    companion object {
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        fun formatKkphimImageUrl(url: String?): String? {
            if (url.isNullOrBlank()) return null
            if (url.startsWith("http://") || url.startsWith("https://")) return url
            val trimmed = url.trimStart('/')
            return "https://phimimg.com/$trimmed"
        }

        fun normalizeTitle(input: String?): String {
            if (input.isNullOrBlank()) return ""
            var s = input.lowercase()
            val vietnameseAccents = mapOf(
                'à' to 'a', 'á' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
                'ă' to 'a', 'ắ' to 'a', 'ằ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
                'â' to 'a', 'ấ' to 'a', 'ầ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
                'è' to 'e', 'é' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
                'ê' to 'e', 'ế' to 'e', 'ề' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
                'ì' to 'i', 'í' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
                'ò' to 'o', 'ó' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
                'ô' to 'o', 'ố' to 'o', 'ồ' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
                'ơ' to 'o', 'ớ' to 'o', 'ờ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
                'ù' to 'u', 'ú' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
                'ư' to 'u', 'ứ' to 'u', 'ừ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
                'ỳ' to 'y', 'ý' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
                'đ' to 'd',
            )
            val sb = StringBuilder()
            for (ch in s) {
                sb.append(vietnameseAccents[ch] ?: ch)
            }
            return sb.toString().replace(Regex("[^a-z0-9]+"), " ").trim()
        }
    }
}
