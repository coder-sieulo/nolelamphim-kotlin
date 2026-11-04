package nolelamphim.vercel.app.ui.screens.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class FilterViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val genres = repository.genres()
                val countries = repository.countries()
                _uiState.value = _uiState.value.copy(
                    genres = genres,
                    countries = countries,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Không tải được bộ lọc",
                )
            }
        }
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
        _uiState.value = _uiState.value.copy(sort = sort)
    }

    fun reset() {
        _uiState.value = FilterUiState(
            genres = _uiState.value.genres,
            countries = _uiState.value.countries,
            sort = _uiState.value.sort,
        )
    }

    fun apply() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val result = repository.filteredPage(
                    category = state.selectedGenre,
                    country = state.selectedCountry,
                    year = state.selectedYear,
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = result.movies,
                    page = result.page,
                    totalPages = result.totalPages,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lọc phim thất bại",
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoadingMore = true, error = null)
            try {
                val result = repository.filteredPage(
                    category = state.selectedGenre,
                    country = state.selectedCountry,
                    year = state.selectedYear,
                    page = state.page + 1,
                )
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    results = state.results + result.movies,
                    page = result.page,
                    totalPages = result.totalPages,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Không tải thêm được",
                )
            }
        }
    }
}
