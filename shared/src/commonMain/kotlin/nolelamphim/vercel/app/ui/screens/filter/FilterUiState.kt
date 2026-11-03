package nolelamphim.vercel.app.ui.screens.filter

import nolelamphim.vercel.app.data.Taxonomy
import nolelamphim.vercel.app.model.Movie

data class FilterUiState(
    val genres: List<Taxonomy> = emptyList(),
    val countries: List<Taxonomy> = emptyList(),
    val selectedGenre: String? = null,
    val selectedCountry: String? = null,
    val selectedYear: Int? = null,
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
