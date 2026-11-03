package nolelamphim.vercel.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.appJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovieRepositoryTest {

    @Test
    fun search_mapsItems_andSendsKeyword() = runTest {
        var requestedUrl = ""

        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = """
                    {
                      "status": true,
                      "items": [
                        {
                          "_id": 1,
                          "name": "Avengers",
                          "origin_name": "Avengers",
                          "slug": "avengers",
                          "poster_url": null,
                          "thumb_url": null,
                          "year": 2019,
                          "tmdb": { "vote_average": "8.2" }
                        },
                        {
                          "_id": 2,
                          "name": "Bad Poster",
                          "origin_name": "Bad Poster",
                          "slug": "bad-poster",
                          "poster_url": {},
                          "thumb_url": {},
                          "year": 2020,
                          "tmdb": { "vote_average": "7.1" }
                        }
                      ],
                      "pagination": {
                        "totalItems": 1,
                        "currentPage": 1,
                        "totalPages": 1
                      }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(appJson) }
        }
        val repository = MovieRepository(MovieApi(client))

        val result = repository.search("avengers")

        assertEquals(2, result.size)
        assertEquals("Avengers", result.first().name)
        assertEquals(8.2, result.first().rating)
        assertEquals("Bad Poster", result[1].name)
        assertEquals(null, result[1].posterUrl)
        assertTrue(requestedUrl.contains("keyword=avengers"))
    }
}
