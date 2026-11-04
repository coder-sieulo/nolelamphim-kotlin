package nolelamphim.vercel.app.ui.screens.watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary

class WatchViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary,
    private val slug: String,
    private val serverIndex: Int,
    private val episodeIndex: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WatchUiState>(WatchUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = WatchUiState.Loading
            try {
                val detail = repository.detail(slug)
                val server = detail.servers.getOrNull(serverIndex)
                val episode = server?.episodes?.getOrNull(episodeIndex)

                _uiState.value = if (episode == null || episode.embedUrl.isBlank()) {
                    WatchUiState.Error("Tập phim không khả dụng")
                } else {
                    // Remember the current episode so Home can offer "Xem tiếp".
                    localLibrary.recordWatch(
                        detail = detail,
                        serverIndex = serverIndex,
                        serverName = server.name,
                        episodeIndex = episodeIndex,
                        episode = episode,
                    )
                    WatchUiState.Success(
                        movieName = detail.name,
                        episodeName = episode.name.ifBlank { "Tập ${episodeIndex + 1}" },
                        embedUrl = episode.embedUrl,
                        hasNextEpisode = episodeIndex + 1 < server.episodes.size,
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
}
