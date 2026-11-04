package nolelamphim.vercel.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nolelamphim.vercel.app.data.local.InMemoryKeyValueStore
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

class LocalLibraryTest {

    @Test
    fun toggleFavorite_addsThenRemovesMovie() {
        val library = LocalLibrary(InMemoryKeyValueStore())
        val movie = movie(id = 1, slug = "avengers")

        library.toggleFavorite(movie)
        assertTrue(library.isFavorite("avengers"))
        assertEquals(1, library.favorites.value.size)
        assertEquals("avengers", library.favorites.value.first().slug)

        library.toggleFavorite(movie)
        assertFalse(library.isFavorite("avengers"))
        assertTrue(library.favorites.value.isEmpty())
    }

    @Test
    fun recordWatch_keepsNewestEpisodeFirst_andDeduplicatesBySlug() {
        val library = LocalLibrary(InMemoryKeyValueStore())
        val detail = detail(id = 1, slug = "avengers")

        library.recordWatch(detail, 0, "Vietsub", 0, episode("Tập 1"))
        library.recordWatch(detail, 0, "Vietsub", 1, episode("Tập 2"))

        assertEquals(1, library.history.value.size)
        assertEquals("Tập 2", library.history.value.first().episodeName)
        assertEquals(1, library.history.value.first().episodeIndex)
    }

    @Test
    fun searchHistory_isDedupedAndCapped() {
        val library = LocalLibrary(InMemoryKeyValueStore())

        repeat(12) { library.addSearchQuery("phim $it") }
        assertEquals(10, library.searchHistory.value.size)

        library.addSearchQuery("phim 5")
        assertEquals("phim 5", library.searchHistory.value.first())
        assertEquals(10, library.searchHistory.value.size)
    }

    @Test
    fun favorites_surviveNewLocalLibraryInstance() {
        val store = InMemoryKeyValueStore()
        LocalLibrary(store).toggleFavorite(movie(id = 1, slug = "avengers"))

        val reopened = LocalLibrary(store)
        assertTrue(reopened.isFavorite("avengers"))
        assertEquals("Avengers", reopened.favorites.value.first().name)
    }

    private fun movie(id: Int, slug: String) = Movie(
        id = id,
        name = "Avengers",
        originName = "The Avengers",
        slug = slug,
        posterUrl = null,
        thumbUrl = null,
        year = 2012,
        rating = 8.1,
    )

    private fun episode(name: String) = Episode(
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        embedUrl = "https://example.com/embed",
    )

    private fun detail(id: Int, slug: String) = MovieDetail(
        id = id,
        name = "Avengers",
        originName = "The Avengers",
        slug = slug,
        description = "",
        posterUrl = null,
        year = 2012,
        quality = "HD",
        lang = "Vietsub",
        status = "completed",
        episodeCurrent = "2",
        episodeTotal = "2",
        duration = "120 phút",
        rating = 8.1,
        categories = listOf("Hành động"),
        categorySlugs = listOf("hanh-dong"),
        countries = listOf("Âu Mỹ"),
        actors = emptyList(),
        directors = emptyList(),
        servers = emptyList(),
    )
}
