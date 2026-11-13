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
import nolelamphim.vercel.app.data.remote.MultiSourceService
import nolelamphim.vercel.app.data.remote.appJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientTest {

    @Test
    fun testKkphimDetailFallback() = runTest {
        val engine = MockEngine { request ->
            val url = request.url.toString()
            when {
                url.contains("vsmov.com") -> {
                    respond(
                        content = """{"status": false, "message": "Not found"}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                url.contains("phimapi.com/v1/api/phim/test-kkphim") -> {
                    respond(
                        content = """
                            {
                              "status": "success",
                              "data": {
                                "item": {
                                  "name": "KKPhim Movie",
                                  "origin_name": "KKPhim Origin",
                                  "slug": "test-kkphim",
                                  "year": 2024,
                                  "episodes": [
                                    {
                                      "server_name": "Vietsub",
                                      "server_data": [
                                        {
                                          "name": "01",
                                          "slug": "tap-01",
                                          "link_embed": "https://player.phimapi.com/embed",
                                          "link_m3u8": "https://cdn.phimapi.com/test.m3u8"
                                        }
                                      ]
                                    }
                                  ]
                                }
                              }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                else -> {
                    respond(
                        content = """{"status": false}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        }

        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(appJson) }
        }
        val multiSource = MultiSourceService(client)
        val api = MovieApi(client)
        val repository = MovieRepository(api, multiSource)

        val detail = repository.detail("test-kkphim")
        assertNotNull(detail)
        assertEquals("KKPhim Movie", detail.name)
        assertEquals(2024, detail.year)
        assertTrue(detail.servers.isNotEmpty())
        assertEquals("https://cdn.phimapi.com/test.m3u8", detail.servers.first().episodes.first().m3u8Url)
    }

    @Test
    fun testNguoncDetailFallback() = runTest {
        val engine = MockEngine { request ->
            val url = request.url.toString()
            when {
                url.contains("phim.nguonc.com/api/film/test-nguonc") -> {
                    respond(
                        content = """
                            {
                              "status": "success",
                              "movie": {
                                "name": "NguonC Movie",
                                "original_name": "NguonC Origin",
                                "slug": "test-nguonc",
                                "year": 2023,
                                "episodes": [
                                  {
                                    "server_name": "Vietsub #1",
                                    "items": [
                                      {
                                        "name": "1",
                                        "slug": "tap-1",
                                        "embed": "https://embed.streamc.xyz/test",
                                        "m3u8": null
                                      }
                                    ]
                                  }
                                ]
                              }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
                else -> {
                    respond(
                        content = """{"status": false}""",
                        status = HttpStatusCode.NotFound,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            }
        }

        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(appJson) }
        }
        val multiSource = MultiSourceService(client)
        val api = MovieApi(client)
        val repository = MovieRepository(api, multiSource)

        val detail = repository.detail("test-nguonc")
        assertNotNull(detail)
        assertEquals("NguonC Movie", detail.name)
        assertEquals(2023, detail.year)
        assertTrue(detail.servers.isNotEmpty())
        assertEquals("https://embed.streamc.xyz/test", detail.servers.first().episodes.first().embedUrl)
    }
}
