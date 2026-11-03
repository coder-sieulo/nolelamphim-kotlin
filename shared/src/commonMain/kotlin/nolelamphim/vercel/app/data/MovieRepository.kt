package nolelamphim.vercel.app.data

import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.dto.MovieDetailDto
import nolelamphim.vercel.app.data.remote.dto.MovieSummaryDto
import nolelamphim.vercel.app.data.remote.dto.ServerDto
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

class MovieRepository(private val api: MovieApi) {

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
        api.search(keyword, page).items.map { it.toMovie() }

    suspend fun detail(slug: String): MovieDetail {
        val response = api.detail(slug)
        return response.movie.toDetail(response.episodes)
    }

    suspend fun genres(): List<Taxonomy> =
        api.genres().map { Taxonomy(it.name, it.slug) }

    suspend fun countries(): List<Taxonomy> =
        api.countries().map { Taxonomy(it.name, it.slug) }
}

data class Taxonomy(val name: String, val slug: String)

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
