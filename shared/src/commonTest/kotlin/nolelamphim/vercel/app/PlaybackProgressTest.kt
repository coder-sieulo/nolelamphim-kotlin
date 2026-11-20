package nolelamphim.vercel.app

import nolelamphim.vercel.app.data.local.InMemoryKeyValueStore
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.data.local.PlaybackIdentity
import nolelamphim.vercel.app.data.local.equivalentEpisodeIndex
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer
import nolelamphim.vercel.app.model.MovieDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlaybackProgressTest {
    @Test
    fun episodeOffsetsSurviveSwitchingAndReopeningStorage() {
        val store = InMemoryKeyValueStore()
        val library = LocalLibrary(store)
        val first = episode("1")
        val second = episode("2")
        library.recordWatch(detail, 0, "Nguồn A", 0, first)
        library.updateProgress(identity(0, first), 60_000, 600_000)
        library.recordWatch(detail, 0, "Nguồn A", 1, second)
        library.updateProgress(identity(1, second), 15_000, 600_000)

        val reopened = LocalLibrary(store)
        assertEquals(60_000L, reopened.getSavedPosition(identity(0, first)))
        assertEquals(15_000L, reopened.getSavedPosition(identity(1, second)))
        reopened.recordWatch(detail, 0, "Nguồn A", 0, first)
        assertEquals(60_000L, reopened.history.value.first().positionMs)
        // The stable episode key also survives a provider reordering its array.
        assertEquals(60_000L, reopened.getSavedPosition(identity(8, first).copy(serverIndex = 3)))
    }

    @Test
    fun legacyHistoryMigratesWithoutDiscardingItsResumeOffset() {
        val store = InMemoryKeyValueStore()
        store.putString("library.history.v1", """[{"id":1,"name":"Phim","slug":"phim","serverIndex":0,"serverName":"Nguồn A","episodeIndex":0,"episodeName":"Tập 1","positionMs":42000,"durationMs":600000}]""")
        val library = LocalLibrary(store)
        assertEquals(42_000L, library.getSavedPosition(identity(0, episode("1"))))
        library.recordWatch(detail, 0, "Nguồn A", 0, episode("1"))
        assertEquals(42_000L, library.history.value.first().positionMs)
    }

    @Test
    fun lateProgressCannotChangeTheCurrentEpisodesIdentity() {
        val library = LocalLibrary(InMemoryKeyValueStore())
        library.recordWatch(detail, 0, "Nguồn A", 0, episode("1"))
        library.recordWatch(detail, 0, "Nguồn A", 1, episode("2"))
        library.updateProgress(identity(0, episode("1")), 80_000, 100_000)
        assertEquals(1, library.history.value.first().episodeIndex)
        assertEquals(0L, library.history.value.first().positionMs)
        assertEquals(80_000L, library.getSavedPosition(identity(0, episode("1"))))
    }

    @Test
    fun sourceMappingMatchesEpisodeLabelsRatherThanArrayPositions() {
        val target = EpisodeServer("B", listOf(episode("2"), episode("1")))
        assertEquals(1, equivalentEpisodeIndex(Episode("Tập 1", "tap-1", ""), target))
        assertEquals(1, equivalentEpisodeIndex(Episode("Tập 01", "tap-01", ""), target))
        assertNull(equivalentEpisodeIndex(episode("3"), target))
    }

    @Test
    fun removingMovieAlsoRemovesItsEpisodeOffsetsDurably() {
        val store = InMemoryKeyValueStore()
        val library = LocalLibrary(store)
        library.recordWatch(detail, 0, "Nguồn A", 0, episode("1"))
        library.updateProgress(identity(0, episode("1")), 95_000, 100_000)
        assertTrue(library.isEpisodeCompleted("phim", "Nguồn A", episode("1")))
        library.removeHistory("phim")
        val reopened = LocalLibrary(store)
        assertTrue(reopened.history.value.isEmpty())
        assertTrue(reopened.episodeProgress.value.isEmpty())
    }

    private fun identity(index: Int, episode: Episode) = PlaybackIdentity("phim", 0, "Nguồn A", index, episode.slug, episode.name)
    private fun episode(number: String) = Episode(number, number, "https://example.com/$number.m3u8")
    private val detail = MovieDetail(
        id = 1, name = "Phim", originName = "", slug = "phim", description = "", posterUrl = null,
        year = null, quality = null, lang = null, status = null, episodeCurrent = null, episodeTotal = null,
        duration = null, rating = 0.0, categories = emptyList(), categorySlugs = emptyList(), countries = emptyList(),
        actors = emptyList(), directors = emptyList(), servers = emptyList(),
    )
}
