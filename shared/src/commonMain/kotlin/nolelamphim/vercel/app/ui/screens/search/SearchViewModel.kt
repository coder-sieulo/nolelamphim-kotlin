package nolelamphim.vercel.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary

class SearchViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<String>> = localLibrary.searchHistory

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState(query = query)
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            executeSearch(query)
        }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        localLibrary.addSearchQuery(query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch { executeSearch(query) }
    }

    fun onHistoryClick(query: String) {
        if (query.isBlank()) return
        localLibrary.addSearchQuery(query)
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch { executeSearch(query) }
    }

    fun clearHistory() = localLibrary.clearSearchHistory()

    fun retry() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch { executeSearch(query) }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || !state.canLoadMore || state.query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val result = repository.searchPage(state.query, state.page + 1)
                _uiState.value = state.copy(
                    isLoading = false,
                    results = state.results + result.movies,
                    page = result.page,
                    canLoadMore = result.hasMore,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = e.message ?: "Không tải thêm được",
                )
            }
        }
    }

    private suspend fun executeSearch(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val result = repository.searchPage(query, page = 1)
            _uiState.value = SearchUiState(
                query = query,
                results = result.movies,
                page = result.page,
                canLoadMore = result.hasMore,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Tìm kiếm thất bại",
            )
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
