package nolelamphim.vercel.app.ui.screens.detail

import androidx.compose.runtime.Immutable
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail

@Immutable
sealed interface DetailUiState {
    data object Loading : DetailUiState

    @Immutable
    data class Success(
        val movie: MovieDetail,
        val related: List<Movie> = emptyList(),
        val isFavorite: Boolean = false,
    ) : DetailUiState

    data class Error(val message: String) : DetailUiState
}
