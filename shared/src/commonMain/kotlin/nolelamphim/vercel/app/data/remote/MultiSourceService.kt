package nolelamphim.vercel.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nolelamphim.vercel.app.data.remote.dto.KkphimDetailResponse
import nolelamphim.vercel.app.data.remote.dto.KkphimSearchResponse
import nolelamphim.vercel.app.data.remote.dto.NguoncDetailResponse
import nolelamphim.vercel.app.data.remote.dto.NguoncSearchResponse
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.Movie

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

    private suspend fun resolveKkphimServers(slug: String, movieName: String): List<EpisodeServer> {
        try {
            // 1. Try direct slug match
            val direct = fetchKkphimDetail(slug)
            if (direct?.data?.item?.episodes?.isNotEmpty() == true) {
                return mapKkphimEpisodes(direct.data.item.episodes)
            }

            // 2. Search KKPhim by movie title
            val search = client.get("https://phimapi.com/v1/api/tim-kiem") {
                parameter("keyword", movieName)
                parameter("limit", 5)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimSearchResponse>()

            val matchedHit = search.data?.items?.firstOrNull() ?: return emptyList()
            val searchedDetail = fetchKkphimDetail(matchedHit.slug)
            return mapKkphimEpisodes(searchedDetail?.data?.item?.episodes.orEmpty())
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private suspend fun fetchKkphimDetail(slug: String): KkphimDetailResponse? {
        return try {
            client.get("https://phimapi.com/v1/api/phim/$slug") {
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimDetailResponse>()
        } catch (_: Exception) {
            null
        }
    }

    private fun mapKkphimEpisodes(servers: List<nolelamphim.vercel.app.data.remote.dto.KkphimServerDto>): List<EpisodeServer> {
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

            val matchedHit = search.items.firstOrNull() ?: return emptyList()
            val searchedDetail = fetchNguoncDetail(matchedHit.slug)
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

    private fun mapNguoncEpisodes(servers: List<nolelamphim.vercel.app.data.remote.dto.NguoncServerDto>): List<EpisodeServer> {
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
                parameter("limit", 12)
                header(HttpHeaders.UserAgent, BROWSER_USER_AGENT)
            }.body<KkphimSearchResponse>()

            res.data?.items.orEmpty().map { item ->
                Movie(
                    id = item.slug.hashCode(),
                    name = item.name,
                    originName = item.originName,
                    slug = item.slug,
                    posterUrl = item.posterUrl,
                    thumbUrl = item.thumbUrl ?: item.posterUrl,
                    year = item.year,
                    rating = 0.0,
                )
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

            res.items.map { item ->
                Movie(
                    id = item.slug.hashCode(),
                    name = item.name,
                    originName = item.originalName,
                    slug = item.slug,
                    posterUrl = item.posterUrl,
                    thumbUrl = item.thumbUrl ?: item.posterUrl,
                    year = item.year,
                    rating = 0.0,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }
}
