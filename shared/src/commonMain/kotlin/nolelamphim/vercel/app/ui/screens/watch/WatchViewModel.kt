package nolelamphim.vercel.app.ui.screens.watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class WatchViewModel(
    private val repository: MovieRepository,
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
                val episode = detail.servers
                    .getOrNull(serverIndex)
                    ?.episodes
                    ?.getOrNull(episodeIndex)

                _uiState.value = if (episode == null || episode.embedUrl.isBlank()) {
                    WatchUiState.Error("Tập phim không khả dụng")
                } else {
                    WatchUiState.Success(
                        movieName = detail.name,
                        episodeName = episode.name,
                        embedUrl = episode.embedUrl,
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
