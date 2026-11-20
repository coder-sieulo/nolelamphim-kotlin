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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
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
import nolelamphim.vercel.app.ui.screens.watch.WatchUiState
import nolelamphim.vercel.app.ui.screens.watch.WatchViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class WatchPlaybackSessionTest {
    @Test
    fun continueMapsSavedIdentityDespiteDifferentRouteIndices() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val store = InMemoryKeyValueStore()
            store.putString("library.history.v1", """[{"id":1,"name":"Phim","slug":"phim","serverIndex":0,"serverName":"B","episodeIndex":0,"episodeName":"Tập 1","episodeSlug":"tap-1","positionMs":42000,"durationMs":600000}]""")
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(store), "phim", 0, 0, preferSavedEpisode = true)
            val state = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(1, state.currentServerIndex)
            assertEquals(1, state.currentEpisodeIndex)
            assertEquals(42_000L, state.initialPositionMs)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun completedContinueStartsNextEpisodeFromZero() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val store = InMemoryKeyValueStore()
            store.putString("library.history.v1", """[{"id":1,"name":"Phim","slug":"phim","serverIndex":0,"serverName":"A","episodeIndex":0,"episodeName":"Tập 1","positionMs":590000,"durationMs":600000}]""")
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(store), "phim", 0, 0, preferSavedEpisode = true)
            val state = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(1, state.currentEpisodeIndex)
            assertEquals(0L, state.initialPositionMs)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun completedFinalEpisodeReplaysFromZero() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val store = InMemoryKeyValueStore()
            store.putString("library.history.v1", """[{"id":1,"name":"Phim","slug":"phim","serverIndex":0,"serverName":"A","episodeIndex":1,"episodeName":"Tập 2","positionMs":590000,"durationMs":600000}]""")
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(store), "phim", 0, 0, preferSavedEpisode = true)
            val state = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(1, state.currentEpisodeIndex)
            assertEquals(0L, state.initialPositionMs)
            assertEquals(false, state.hasNextEpisode)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun explicitCompletedEpisodeReplaysWithoutAdvancingAndPersistsZero() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val store = InMemoryKeyValueStore()
            store.putString("library.history.v1", """[{"id":1,"name":"Phim","slug":"phim","serverIndex":0,"serverName":"A","episodeIndex":0,"episodeName":"Tập 1","positionMs":590000,"durationMs":600000}]""")
            val library = LocalLibrary(store)
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), library, "phim", 0, 0)
            val state = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(0, state.currentEpisodeIndex)
            assertEquals(0L, state.initialPositionMs)
            assertEquals(0L, library.history.value.first().positionMs)
            assertEquals(600_000L, library.history.value.first().durationMs)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun changingEpisodeRejectsLateProgressFromThePreviousSession() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val library = LocalLibrary(InMemoryKeyValueStore())
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), library, "phim", 0, 0)
            val first = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            viewModel.updateProgress(60_000, 600_000, first.sessionId)
            viewModel.selectEpisode(0, 1)
            val second = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(0L, second.initialPositionMs)
            viewModel.updateProgress(99_000, 600_000, first.sessionId)
            viewModel.saveProgress(second.sessionId)
            assertEquals(0L, library.history.value.first().positionMs)
            assertEquals(60_000L, library.getSavedPosition("phim", 0, 0))
            viewModel.selectEpisode(0, 0)
            assertEquals(60_000L, viewModel.uiState.filterIsInstance<WatchUiState.Success>().first().initialPositionMs)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun changingSourceMapsReorderedEpisodesAndPreservesTime() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(InMemoryKeyValueStore()), "phim", 0, 0)
            val first = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            viewModel.updateProgress(42_000, 600_000, first.sessionId)
            viewModel.selectSource(1)
            val changed = viewModel.uiState.filterIsInstance<WatchUiState.Success>().first()
            assertEquals(1, changed.currentServerIndex)
            assertEquals(1, changed.currentEpisodeIndex)
            assertEquals(42_000L, changed.initialPositionMs)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    @Test
    fun selectingAnotherEpisodeCancelsPendingStreamResolution() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = client(StandardTestDispatcher(testScheduler), slowFirstEpisode = true)
        try {
            val viewModel = WatchViewModel(MovieRepository(MovieApi(client)), LocalLibrary(InMemoryKeyValueStore()), "phim", 0, 0)
            runCurrent()
            assertIs<WatchUiState.Loading>(viewModel.uiState.value)
            viewModel.selectEpisode(0, 1)
            advanceUntilIdle()
            assertEquals(1, assertIs<WatchUiState.Success>(viewModel.uiState.value).currentEpisodeIndex)
        } finally { client.close(); Dispatchers.resetMain() }
    }

    private fun client(testDispatcher: CoroutineDispatcher, slowFirstEpisode: Boolean = false): HttpClient {
        val firstUrl = if (slowFirstEpisode) "https://v2.streamvsmov.com/video/slow" else "https://example.com/1.m3u8"
        val engine = MockEngine(MockEngineConfig().apply {
            dispatcher = testDispatcher
            addHandler { request ->
            if (request.url.host.contains("streamvsmov")) {
                delay(1000)
                respond("signedMasterUrl: \"https://example.com/slow.m3u8\"", headers = headersOf(HttpHeaders.ContentType, "text/html"))
            } else respond("""{
                "movie":{"_id":1,"name":"Phim","slug":"phim"},
                "episodes":[
                  {"server_name":"A","server_data":[
                    {"name":"Tập 1","slug":"tap-1","link_embed":"$firstUrl"},
                    {"name":"Tập 2","slug":"tap-2","link_embed":"https://example.com/2.m3u8"}]},
                  {"server_name":"B","server_data":[
                    {"name":"2","slug":"2","link_embed":"https://example.com/b2.m3u8"},
                    {"name":"1","slug":"1","link_embed":"https://example.com/b1.m3u8"}]}
                ]
            }""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
            }
        })
        return HttpClient(engine) { install(ContentNegotiation) { json(appJson) } }
    }
}
