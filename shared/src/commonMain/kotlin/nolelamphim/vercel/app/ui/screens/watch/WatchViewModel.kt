package nolelamphim.vercel.app.ui.screens.watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.model.toMovie

class WatchViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary,
    private val slug: String,
    initialServerIndex: Int,
    initialEpisodeIndex: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchUiState>(WatchUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var currentDetail: MovieDetail? = null
    private var currentServerIdx: Int = initialServerIndex
    private var currentEpisodeIdx: Int = initialEpisodeIndex
    private var relatedMovies: List<Movie> = emptyList()

    init {
        load(initialServerIndex, initialEpisodeIndex)
    }

    fun load(serverIdx: Int = currentServerIdx, episodeIdx: Int = currentEpisodeIdx) {
        viewModelScope.launch {
            _uiState.value = WatchUiState.Loading
            try {
                val detail = currentDetail ?: repository.detail(slug).also { currentDetail = it }
                if (relatedMovies.isEmpty() && detail.categorySlugs.isNotEmpty()) {
                    try {
                        relatedMovies = repository.relatedMovies(slug, detail.categorySlugs.firstOrNull())
                    } catch (_: Exception) {
                        // Non-critical, ignore
                    }
                }

                currentServerIdx = serverIdx
                currentEpisodeIdx = episodeIdx

                val server = detail.servers.getOrNull(serverIdx)
                val episode = server?.episodes?.getOrNull(episodeIdx)

                if (episode == null || episode.embedUrl.isBlank()) {
                    _uiState.value = WatchUiState.Error("Tập phim không khả dụng hoặc chưa có nguồn phát")
                } else {
                    localLibrary.recordWatch(
                        detail = detail,
                        serverIndex = serverIdx,
                        serverName = server.name,
                        episodeIndex = episodeIdx,
                        episode = episode,
                    )
                    _uiState.value = WatchUiState.Success(
                        movie = detail,
                        currentServerIndex = serverIdx,
                        currentEpisodeIndex = episodeIdx,
                        movieName = detail.name,
                        episodeName = episode.name.ifBlank { "Tập ${episodeIdx + 1}" },
                        embedUrl = episode.embedUrl,
                        playbackUrl = episode.m3u8Url?.takeIf { it.isNotBlank() }
                            ?: (repository.resolveStreamUrl(episode.embedUrl) ?: episode.embedUrl),
                        hasNextEpisode = episodeIdx + 1 < server.episodes.size,
                        isFavorite = localLibrary.isFavorite(slug),
                        related = relatedMovies,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = WatchUiState.Error(
                    e.message ?: "Không mở được tập phim",
                )
            }
        }
    }

    fun selectEpisode(serverIdx: Int, episodeIdx: Int) {
        load(serverIdx, episodeIdx)
    }

    fun nextEpisode() {
        val s = _uiState.value as? WatchUiState.Success ?: return
        if (s.hasNextEpisode) {
            selectEpisode(s.currentServerIndex, s.currentEpisodeIndex + 1)
        }
    }

    fun toggleFavorite() {
        val detail = currentDetail ?: return
        localLibrary.toggleFavorite(detail.toMovie())
        val s = _uiState.value as? WatchUiState.Success ?: return
        _uiState.value = s.copy(isFavorite = localLibrary.isFavorite(slug))
    }
}
