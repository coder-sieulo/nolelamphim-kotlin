package nolelamphim.vercel.app.ui.screens.search

import nolelamphim.vercel.app.model.Movie

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Movie> = emptyList(),
    val error: String? = null,
    val page: Int = 0,
    val canLoadMore: Boolean = false,
)
