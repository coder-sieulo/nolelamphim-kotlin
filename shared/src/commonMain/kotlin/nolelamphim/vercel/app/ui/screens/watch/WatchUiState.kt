package nolelamphim.vercel.app.ui.screens.watch

sealed interface WatchUiState {
    data object Loading : WatchUiState

    data class Success(
        val movieName: String,
        val episodeName: String,
        val embedUrl: String,
        val playbackUrl: String,
        val hasNextEpisode: Boolean = false,
    ) : WatchUiState

    data class Error(val message: String) : WatchUiState
}
