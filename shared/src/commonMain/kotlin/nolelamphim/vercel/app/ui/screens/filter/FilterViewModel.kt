package nolelamphim.vercel.app.ui.screens.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary

class FilterViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary? = null,
    initialQuery: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FilterUiState(query = initialQuery?.trim().orEmpty()),
    )
    val uiState = _uiState.asStateFlow()
    private var request: Job? = null
    private var generation = 0

    init {
        loadTaxonomy()
        apply()
    }

    fun loadTaxonomy() {
        viewModelScope.launch {
            try {
                val genres = repository.genres()
                val countries = repository.countries()
                _uiState.value = _uiState.value.copy(
                    genres = genres,
                    countries = countries,
                    taxonomyError = null,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    taxonomyError = "Không tải được thể loại và quốc gia. Bạn vẫn có thể duyệt phim hoặc thử lại.",
                )
            }
        }
    }

    /** TV edits draft values locally. Commit all criteria together, then fetch once. */
    fun applyFilters(genre: String?, country: String?, year: Int?, sort: SortOption) {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedGenre = if (state.query.isBlank()) genre else null,
            selectedCountry = if (state.query.isBlank()) country else null,
            selectedYear = year,
            sort = sort,
            results = emptyList(),
            page = 0,
        )
        apply()
    }

    fun clearQueryAndApply() {
        _uiState.value = _uiState.value.copy(query = "", results = emptyList(), page = 0)
        apply()
    }

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun selectGenre(slug: String?) {
        _uiState.value = _uiState.value.copy(
            selectedGenre = if (_uiState.value.selectedGenre == slug) null else slug,
        )
    }

    fun selectCountry(slug: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = if (_uiState.value.selectedCountry == slug) null else slug,
        )
    }

    fun selectYear(year: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedYear = if (_uiState.value.selectedYear == year) null else year,
        )
    }

    fun selectSort(sort: SortOption) {
        val currentMovies = _uiState.value.results
        _uiState.value = _uiState.value.copy(
            sort = sort,
            results = sortMovies(currentMovies, sort),
        )
    }

    fun reset() {
        generation++
        request?.cancel()
        _uiState.value = FilterUiState(
            genres = _uiState.value.genres,
            countries = _uiState.value.countries,
            sort = _uiState.value.sort,
        )
    }

    fun apply() {
        request?.cancel()
        val requestGeneration = ++generation
        val state = _uiState.value
        val trimmedQuery = state.query.trim()
        if (trimmedQuery.isNotBlank()) {
            localLibrary?.addSearchQuery(trimmedQuery)
        }
        _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null)
        request = viewModelScope.launch {
            try {
                val pageResult = if (trimmedQuery.isNotBlank()) {
                    repository.searchPage(keyword = trimmedQuery, page = 1)
                } else {
                    repository.filteredPage(
                        category = state.selectedGenre,
                        country = state.selectedCountry,
                        year = state.selectedYear,
                        page = 1,
                    )
                }

                // If user applied both keyword and dropdown filters, refine results
                var movies = pageResult.movies
                if (trimmedQuery.isNotBlank()) {
                    if (state.selectedYear != null) {
                        movies = movies.filter { it.year == state.selectedYear }
                    }
                }
                movies = sortMovies(movies, state.sort)
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = movies,
                    page = pageResult.page,
                    totalPages = pageResult.totalPages,
                    totalItems = pageResult.totalItems,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Không tìm thấy phim phù hợp",
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        val trimmedQuery = state.query.trim()
        val requestGeneration = generation
        _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
        request = viewModelScope.launch {
            try {
                val nextPage = state.page + 1
                val pageResult = if (trimmedQuery.isNotBlank()) {
                    repository.searchPage(keyword = trimmedQuery, page = nextPage)
                } else {
                    repository.filteredPage(
                        category = state.selectedGenre,
                        country = state.selectedCountry,
                        year = state.selectedYear,
                        page = nextPage,
                    )
                }

                var newMovies = pageResult.movies
                if (trimmedQuery.isNotBlank() && state.selectedYear != null) {
                    newMovies = newMovies.filter { it.year == state.selectedYear }
                }

                if (requestGeneration != generation) return@launch
                val combined = sortMovies((state.results + newMovies).distinctBy { it.slug }, state.sort)
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    results = combined,
                    page = pageResult.page,
                    totalPages = pageResult.totalPages,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Không tải thêm được",
                )
            }
        }
    }
}
