package nolelamphim.vercel.app.ui.screens.home

import androidx.compose.runtime.Immutable
import nolelamphim.vercel.app.model.Movie

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hero: Movie? = null,
    val latest: List<Movie> = emptyList(),
    val series: List<Movie> = emptyList(),
    val singles: List<Movie> = emptyList(),
    val cinema: List<Movie> = emptyList(),
)
