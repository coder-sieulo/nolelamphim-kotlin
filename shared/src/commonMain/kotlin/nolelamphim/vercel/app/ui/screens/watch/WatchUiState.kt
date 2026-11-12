package nolelamphim.vercel.app.ui.screens.watch

import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

sealed interface WatchUiState {
    data object Loading : WatchUiState

    data class Success(
        val movie: MovieDetail,
        val currentServerIndex: Int,
        val currentEpisodeIndex: Int,
        val movieName: String,
        val episodeName: String,
        val embedUrl: String,
        val playbackUrl: String,
        val hasNextEpisode: Boolean = false,
        val isFavorite: Boolean = false,
        val related: List<Movie> = emptyList(),
    ) : WatchUiState

    data class Error(val message: String) : WatchUiState
}
