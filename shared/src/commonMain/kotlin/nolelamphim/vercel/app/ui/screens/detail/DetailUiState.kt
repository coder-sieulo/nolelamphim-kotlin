package nolelamphim.vercel.app.ui.screens.detail

import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Success(
        val movie: MovieDetail,
        val related: List<Movie> = emptyList(),
        val isFavorite: Boolean = false,
    ) : DetailUiState

    data class Error(val message: String) : DetailUiState
}
