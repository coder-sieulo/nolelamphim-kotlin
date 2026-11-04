package nolelamphim.vercel.app.ui.screens.catalog

import nolelamphim.vercel.app.model.Movie

data class CatalogUiState(
    val title: String = "",
    val movies: List<Movie> = emptyList(),
    val page: Int = 0,
    val totalPages: Int = 1,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = page < totalPages
}
