package nolelamphim.vercel.app.data

import kotlinx.coroutines.CancellationException
import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.MultiSourceService
import nolelamphim.vercel.app.data.remote.dto.MovieDetailDto
import nolelamphim.vercel.app.data.remote.dto.MovieListResponse
import nolelamphim.vercel.app.data.remote.dto.MovieSummaryDto
import nolelamphim.vercel.app.data.remote.dto.ServerDto
import nolelamphim.vercel.app.data.stream.StreamUrlResolver
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

class MovieRepository(
    private val api: MovieApi,
    private val multiSourceService: MultiSourceService? = null,
) {

    suspend fun latest(page: Int = 1): List<Movie> =
        api.latest(page).items.map { it.toMovie() }

    suspend fun byCategory(categorySlug: String, page: Int = 1): List<Movie> =
        api.byCategory(categorySlug, page).items.map { it.toMovie() }

    suspend fun filtered(
        category: String? = null,
        country: String? = null,
        year: Int? = null,
        page: Int = 1,
    ): List<Movie> =
        api.filtered(category, country, year, page).items.map { it.toMovie() }

    suspend fun search(keyword: String, page: Int = 1): List<Movie> =
        searchPage(keyword, page).movies

    suspend fun latestPage(page: Int = 1): MoviePage =
        api.latest(page).toPage(page)

    suspend fun byCategoryPage(categorySlug: String, page: Int = 1): MoviePage =
        api.byCategory(categorySlug, page).toPage(page)

    suspend fun filteredPage(
        category: String? = null,
        country: String? = null,
        year: Int? = null,
        page: Int = 1,
    ): MoviePage =
        api.filtered(category, country, year, page).toPage(page)

    suspend fun searchPage(keyword: String, page: Int = 1): MoviePage {
        val basePage = try {
            api.search(keyword, page).toPage(page)
        } catch (_: Exception) {
            MoviePage(emptyList(), page, 1, 0)
        }

        if (page > 1) return basePage

        val altMovies = try {
            multiSourceService?.searchAlternativeSources(keyword).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }

        if (altMovies.isEmpty()) return basePage

        val combinedMovies = (basePage.movies + altMovies)
            .distinctBy { it.slug }

        return basePage.copy(
            movies = combinedMovies,
            totalItems = basePage.totalItems + altMovies.size,
        )
    }

    suspend fun relatedMovies(slug: String, categorySlug: String?): List<Movie> {
        if (categorySlug.isNullOrBlank()) return emptyList()
        return byCategoryPage(categorySlug).movies.filterNot { it.slug == slug }
    }

    suspend fun detail(slug: String): MovieDetail {
        val response = api.detail(slug)
        val baseDetail = response.movie.toDetail(response.episodes)

        val altServers = try {
            multiSourceService?.resolveAlternativeServers(slug, baseDetail.name).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }

        if (altServers.isEmpty()) return baseDetail

        // Merge servers: KKPhim VIP HLS first (fast native m3u8), then VSMov, then NguonC
        val mergedServers = buildList {
            // 1. KKPhim (HLS VIP) servers
            addAll(altServers.filter { it.name.startsWith("KKPhim") })
            // 2. VSMov servers
            addAll(baseDetail.servers)
            // 3. NguonC backup servers
            addAll(altServers.filter { it.name.startsWith("NguonC") })
        }

        return baseDetail.copy(servers = mergedServers)
    }

    /**
     * Resolves an episode's embed URL to a URL a native player can open. Sources that use
     * signed playlists need one extra request to the embed page; everything is best-effort so a
     * failure still yields the plain playlist URL (or null when the source is unknown).
     */
    suspend fun resolveStreamUrl(embedUrl: String): String? {
        val fallback = StreamUrlResolver.resolve(embedUrl) ?: return null
        if (StreamUrlResolver.isMediaUrl(embedUrl)) return fallback

        val pageHtml = try {
            api.embedPage(embedUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        return StreamUrlResolver.signedMasterUrl(pageHtml) ?: fallback
    }

    suspend fun genres(): List<Taxonomy> =
        api.genres().map { Taxonomy(it.name, it.slug) }

    suspend fun countries(): List<Taxonomy> =
        api.countries().map { Taxonomy(it.name, it.slug) }
}

data class Taxonomy(val name: String, val slug: String)

data class MoviePage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int,
) {
    val hasMore: Boolean get() = page < totalPages
}

private fun MovieListResponse.toPage(page: Int): MoviePage = MoviePage(
    movies = items.map { it.toMovie() },
    page = page,
    totalPages = pagination.totalPages.coerceAtLeast(page),
    totalItems = pagination.totalItems,
)

private fun MovieSummaryDto.toMovie(): Movie = Movie(
    id = id,
    name = name,
    originName = originName,
    slug = slug,
    posterUrl = posterUrl?.takeIf { it.isNotBlank() },
    thumbUrl = thumbUrl?.takeIf { it.isNotBlank() },
    year = year,
    rating = tmdb?.voteAverage?.toDoubleOrNull() ?: 0.0,
)

private fun MovieDetailDto.toDetail(servers: List<ServerDto>): MovieDetail = MovieDetail(
    id = id,
    name = name,
    originName = originName,
    slug = slug,
    description = content,
    posterUrl = posterUrl?.takeIf { it.isNotBlank() },
    year = year,
    quality = quality?.takeIf { it.isNotBlank() },
    lang = lang?.takeIf { it.isNotBlank() },
    status = status?.takeIf { it.isNotBlank() },
    episodeCurrent = episodeCurrent?.takeIf { it.isNotBlank() },
    episodeTotal = episodeTotal?.takeIf { it.isNotBlank() },
    duration = time?.takeIf { it.isNotBlank() },
    rating = tmdb?.voteAverage?.toDoubleOrNull() ?: 0.0,
    categories = category.map { it.name },
    categorySlugs = category.map { it.slug },
    countries = country.map { it.name },
    actors = actor,
    directors = director,
    servers = servers.map { it.toServer() },
)

private fun ServerDto.toServer(): EpisodeServer = EpisodeServer(
    name = serverName.replace(Regex("\\s+"), " ").trim(),
    episodes = serverData.map {
        Episode(
            name = it.name,
            slug = it.slug,
            embedUrl = it.linkEmbed,
        )
    },
)
