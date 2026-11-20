package nolelamphim.vercel.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MoviePage
import nolelamphim.vercel.app.data.MovieRepository

class CatalogViewModel(
    private val repository: MovieRepository,
    private val type: String,
    private val title: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(title = title))
    val uiState = _uiState.asStateFlow()
    private var request: Job? = null
    private var generation = 0

    init {
        loadFirstPage()
    }

    private suspend fun fetch(page: Int): MoviePage =
        if (type == CatalogType.LATEST) {
            repository.latestPage(page)
        } else {
            repository.byCategoryPage(type, page)
        }

    fun loadFirstPage() {
        request?.cancel()
        val requestGeneration = ++generation
        _uiState.value = _uiState.value.copy(isLoading = true, isLoadingMore = false, error = null)
        request = viewModelScope.launch {
            try {
                val result = fetch(1)
                if (requestGeneration != generation) return@launch
                _uiState.value = CatalogUiState(
                    title = title,
                    movies = result.movies,
                    page = result.page,
                    totalPages = result.totalPages,
                    isLoading = false,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Không tải được danh sách phim",
                )
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        request?.cancel()
        val requestGeneration = ++generation
        _uiState.value = _uiState.value.copy(isRefreshing = true, isLoadingMore = false, error = null)
        request = viewModelScope.launch {
            try {
                val result = fetch(1)
                if (requestGeneration != generation) return@launch
                _uiState.value = CatalogUiState(
                    title = title,
                    movies = result.movies,
                    page = result.page,
                    totalPages = result.totalPages,
                    isLoading = false,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Không tải lại được",
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || !state.hasMore) return
        val requestGeneration = generation
        _uiState.value = state.copy(isLoadingMore = true, error = null)
        request = viewModelScope.launch {
            try {
                val result = fetch(state.page + 1)
                if (requestGeneration != generation) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    movies = (state.movies + result.movies).distinctBy { it.slug },
                    page = result.page,
                    totalPages = result.totalPages,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestGeneration != generation) return@launch
                _uiState.value = state.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Không tải thêm được",
                )
            }
        }
    }
}
