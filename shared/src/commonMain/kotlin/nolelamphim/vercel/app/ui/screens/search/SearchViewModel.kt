package nolelamphim.vercel.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class SearchViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.value = SearchUiState(query = query)
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val movies = repository.search(query, page = 1)
                _uiState.value = SearchUiState(
                    query = query,
                    results = movies,
                    page = 1,
                    canLoadMore = movies.isNotEmpty(),
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
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || !state.canLoadMore || state.query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            try {
                val nextPage = state.page + 1
                val movies = repository.search(state.query, nextPage)
                _uiState.value = state.copy(
                    isLoading = false,
                    results = state.results + movies,
                    page = nextPage,
                    canLoadMore = movies.isNotEmpty(),
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
}
