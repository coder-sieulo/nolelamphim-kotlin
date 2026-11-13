package nolelamphim.vercel.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

@Serializable
data class FavoriteMovie(
    val id: Int,
    val name: String,
    val slug: String,
    val posterUrl: String? = null,
    val year: Int? = null,
    val rating: Double = 0.0,
)

@Serializable
data class WatchEntry(
    val id: Int,
    val name: String,
    val slug: String,
    val posterUrl: String? = null,
    val year: Int? = null,
    val rating: Double = 0.0,
    val serverIndex: Int,
    val serverName: String,
    val episodeIndex: Int,
    val episodeName: String,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

fun FavoriteMovie.toMovie(): Movie =
    Movie(id, name, "", slug, posterUrl, posterUrl, year, rating)

fun WatchEntry.toMovie(): Movie =
    Movie(id, name, "", slug, posterUrl, posterUrl, year, rating)

/**
 * Offline-first user library backed by [KeyValueStore].
 *
 * Exposes [StateFlow]s so screens can observe favourites, watch history and
 * recent searches without polling. The newest item is always first.
 */
class LocalLibrary(private val store: KeyValueStore = createKeyValueStore()) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _favorites = MutableStateFlow(readList(KEY_FAVORITES, FavoriteMovie.serializer()))
    val favorites: StateFlow<List<FavoriteMovie>> = _favorites.asStateFlow()

    private val _history = MutableStateFlow(readList(KEY_HISTORY, WatchEntry.serializer()))
    val history: StateFlow<List<WatchEntry>> = _history.asStateFlow()

    private val _searchHistory = MutableStateFlow(readSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    fun isFavorite(slug: String): Boolean = _favorites.value.any { it.slug == slug }

    fun toggleFavorite(movie: Movie) {
        val current = _favorites.value
        val updated = if (current.any { it.slug == movie.slug }) {
            current.filterNot { it.slug == movie.slug }
        } else {
            listOf(movie.toFavorite()) + current
        }
        _favorites.value = updated
        store.putString(
            KEY_FAVORITES,
            json.encodeToString(ListSerializer(FavoriteMovie.serializer()), updated),
        )
    }

    fun removeFavorite(slug: String) {
        val updated = _favorites.value.filterNot { it.slug == slug }
        if (updated.size == _favorites.value.size) return
        _favorites.value = updated
        store.putString(
            KEY_FAVORITES,
            json.encodeToString(ListSerializer(FavoriteMovie.serializer()), updated),
        )
    }

    fun recordWatch(
        detail: MovieDetail,
        serverIndex: Int,
        serverName: String,
        episodeIndex: Int,
        episode: Episode,
    ) {
        val entry = WatchEntry(
            id = detail.id,
            name = detail.name,
            slug = detail.slug,
            posterUrl = detail.posterUrl,
            year = detail.year,
            rating = detail.rating,
            serverIndex = serverIndex,
            serverName = serverName,
            episodeIndex = episodeIndex,
            episodeName = episode.name.ifBlank { "Tập ${episodeIndex + 1}" },
        )
        val updated = (listOf(entry) + _history.value.filterNot { it.slug == entry.slug })
            .take(MAX_HISTORY)
        _history.value = updated
        store.putString(
            KEY_HISTORY,
            json.encodeToString(ListSerializer(WatchEntry.serializer()), updated),
        )
    }

    fun updateProgress(
        slug: String,
        serverIndex: Int,
        episodeIndex: Int,
        positionMs: Long,
        durationMs: Long,
    ) {
        if (positionMs <= 0L) return
        val current = _history.value
        val existing = current.firstOrNull { it.slug == slug } ?: return
        val updatedEntry = existing.copy(
            serverIndex = serverIndex,
            episodeIndex = episodeIndex,
            positionMs = positionMs,
            durationMs = durationMs,
        )
        val updated = listOf(updatedEntry) + current.filterNot { it.slug == slug }
        _history.value = updated
        store.putString(
            KEY_HISTORY,
            json.encodeToString(ListSerializer(WatchEntry.serializer()), updated),
        )
    }

    fun getSavedPosition(slug: String, serverIndex: Int, episodeIndex: Int): Long {
        val entry = _history.value.firstOrNull {
            it.slug == slug && it.serverIndex == serverIndex && it.episodeIndex == episodeIndex
        }
        return entry?.positionMs ?: 0L
    }

    fun clearHistory() {
        _history.value = emptyList()
        store.remove(KEY_HISTORY)
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val updated = (
            listOf(trimmed) + _searchHistory.value.filterNot { it.equals(trimmed, ignoreCase = true) }
            ).take(MAX_SEARCH_HISTORY)
        _searchHistory.value = updated
        store.putString(KEY_SEARCH_HISTORY, json.encodeToString(updated))
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
        store.remove(KEY_SEARCH_HISTORY)
    }

    private fun <T> readList(key: String, serializer: KSerializer<T>): List<T> {
        val raw = store.getString(key) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), raw)
        }.getOrDefault(emptyList())
    }

    private fun readSearchHistory(): List<String> {
        val raw = store.getString(KEY_SEARCH_HISTORY) ?: return emptyList()
        return runCatching { json.decodeFromString<List<String>>(raw) }
            .getOrDefault(emptyList())
    }

    private fun Movie.toFavorite(): FavoriteMovie =
        FavoriteMovie(id, name, slug, posterUrl, year, rating)

    private companion object {
        const val KEY_FAVORITES = "library.favorites.v1"
        const val KEY_HISTORY = "library.history.v1"
        const val KEY_SEARCH_HISTORY = "library.search.v1"
        const val MAX_HISTORY = 30
        const val MAX_SEARCH_HISTORY = 10
    }
}
