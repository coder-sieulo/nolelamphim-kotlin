package nolelamphim.vercel.app.ui.screens.filter

import nolelamphim.vercel.app.data.Taxonomy
import nolelamphim.vercel.app.model.Movie

data class FilterUiState(
    val query: String = "",
    val genres: List<Taxonomy> = emptyList(),
    val countries: List<Taxonomy> = emptyList(),
    val selectedGenre: String? = null,
    val selectedCountry: String? = null,
    val selectedYear: Int? = null,
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 0,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val sort: SortOption = SortOption.DEFAULT,
) {
    val hasMore: Boolean get() = page < totalPages
    val hasFilters: Boolean
        get() = query.isNotBlank() || selectedGenre != null || selectedCountry != null || selectedYear != null
}

enum class SortOption(val label: String) {
    DEFAULT("Mặc định"),
    YEAR_DESC("Năm mới nhất"),
    RATING_DESC("Điểm cao"),
}

fun sortMovies(movies: List<Movie>, sort: SortOption): List<Movie> = when (sort) {
    SortOption.DEFAULT -> movies
    SortOption.YEAR_DESC -> movies.sortedWith(
        compareByDescending<Movie> { it.year ?: 0 }.thenByDescending { it.rating },
    )

    SortOption.RATING_DESC -> movies.sortedWith(
        compareByDescending<Movie> { it.rating }.thenByDescending { it.year ?: 0 },
    )
}
