package nolelamphim.vercel.app.ui.screens.detail

import nolelamphim.vercel.app.model.MovieDetail

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val movie: MovieDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
