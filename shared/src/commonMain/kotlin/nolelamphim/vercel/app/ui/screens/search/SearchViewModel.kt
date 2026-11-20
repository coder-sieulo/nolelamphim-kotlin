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
    initialQuery: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState(query = initialQuery?.trim().orEmpty()))
    val uiState = _uiState.asStateFlow()

    val searchHistory: StateFlow<List<String>> = localLibrary.searchHistory

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var generation = 0

    init {
        if (!initialQuery.isNullOrBlank()) {
            submitSearch()
        }
    }

    fun onQueryChange(query: String) {
        val requestGeneration = ++generation
        _uiState.value = _uiState.value.copy(query = query, isLoading = query.isNotBlank(), isLoadingMore = false, error = null, canLoadMore = false)
        searchJob?.cancel()
        loadMoreJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState(query = query)
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            executeSearch(query.trim(), requestGeneration)
        }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        localLibrary.addSearchQuery(query)
        searchJob?.cancel()
        loadMoreJob?.cancel()
        val requestGeneration = ++generation
        _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null)
        searchJob = viewModelScope.launch { executeSearch(query, requestGeneration) }
    }

    fun onHistoryClick(query: String) {
        if (query.isBlank()) return
        localLibrary.addSearchQuery(query)
        _uiState.value = _uiState.value.copy(query = query, isLoading = true, isLoadingMore = false, error = null)
        searchJob?.cancel()
        loadMoreJob?.cancel()
        val requestGeneration = ++generation
        searchJob = viewModelScope.launch { executeSearch(query, requestGeneration) }
    }

    fun clearHistory() = localLibrary.clearSearchHistory()

    fun retry() {
        if (_uiState.value.results.isNotEmpty() && _uiState.value.resultsQuery == _uiState.value.query.trim()) {
            loadMore()
        } else submitSearch()
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore || state.query.isBlank() || state.resultsQuery != state.query.trim()) return
        val requestGeneration = generation
        _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
        loadMoreJob = viewModelScope.launch {
            try {
                val result = repository.searchPage(state.query, state.page + 1)
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    results = (state.results + result.movies).distinctBy { it.slug },
                    page = result.page,
                    canLoadMore = result.hasMore,
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

    private suspend fun executeSearch(query: String, requestGeneration: Int) {
        if (requestGeneration != generation) return
        _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null)
        try {
            val result = repository.searchPage(query, page = 1)
            if (requestGeneration != generation) return
            _uiState.value = SearchUiState(
                query = query,
                resultsQuery = query,
                results = result.movies,
                page = result.page,
                canLoadMore = result.hasMore,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (requestGeneration != generation) return
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
