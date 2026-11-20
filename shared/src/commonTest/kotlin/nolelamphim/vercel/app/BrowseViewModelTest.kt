package nolelamphim.vercel.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.InMemoryKeyValueStore
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.appJson
import nolelamphim.vercel.app.ui.screens.filter.FilterViewModel
import nolelamphim.vercel.app.ui.screens.search.SearchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    @Test
    fun changingQueryWhileLoadingNextPage_doesNotRestoreOldResults() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(MockEngine(MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                val query = request.url.parameters["keyword"].orEmpty()
                val page = request.url.parameters["page"]?.toInt() ?: 1
                if (query == "old" && page == 2) delay(2_000)
                respond(pageJson("$query-$page", page, 2), headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }))
        try {
            val model = SearchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(InMemoryKeyValueStore()), "old")
            advanceUntilIdle()
            assertEquals("old-1", model.uiState.value.results.single().slug)

            model.loadMore()
            runCurrent()
            model.onQueryChange("new")
            advanceUntilIdle()

            assertEquals("new", model.uiState.value.query)
            assertEquals("new", model.uiState.value.resultsQuery)
            assertEquals(listOf("new-1"), model.uiState.value.results.map { it.slug })
            assertFalse(model.uiState.value.isLoading)
            assertFalse(model.uiState.value.isLoadingMore)
        } finally {
            client.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedTaxonomy_doesNotBlockExploreCatalog() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(MockEngine(MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(testScheduler)
            addHandler { request ->
                if (request.url.encodedPath.contains("the-loai")) error("taxonomy unavailable")
                respond(pageJson("available-movie", 1, 1), headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }))
        try {
            val model = FilterViewModel(MovieRepository(MovieApi(client)))
            advanceUntilIdle()

            assertEquals("available-movie", model.uiState.value.results.single().slug)
            assertNotNull(model.uiState.value.taxonomyError)
            assertFalse(model.uiState.value.isLoading)
        } finally {
            client.close()
            Dispatchers.resetMain()
        }
    }

    private fun client(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { json(appJson) }
    }

    private fun pageJson(slug: String, page: Int, totalPages: Int) = """
        {"status":true,"items":[{"_id":$page,"name":"$slug","origin_name":"","slug":"$slug","year":2026}],
        "pagination":{"totalItems":2,"currentPage":$page,"totalPages":$totalPages}}
    """.trimIndent()
}
