package nolelamphim.vercel.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.ui.screens.catalog.CatalogType

class HomeViewModel(
    private val repository: MovieRepository,
    localLibrary: LocalLibrary,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    val continueWatching: StateFlow<List<WatchEntry>> = localLibrary.history

    init {
        fetch(isRefresh = false)
    }

    fun load() = fetch(isRefresh = false)

    fun refresh() = fetch(isRefresh = true)

    private fun fetch(isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = if (isRefresh) {
                _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                HomeUiState(isLoading = true)
            }
            try {
                coroutineScope {
                    val latest = async { safe { repository.latest() } }
                    val series = async { safe { repository.byCategory(CatalogType.SERIES) } }
                    val singles = async { safe { repository.byCategory(CatalogType.SINGLES) } }
                    val cinema = async { safe { repository.byCategory(CatalogType.CINEMA) } }

                    val latestMovies = latest.await().getOrThrow()
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        isRefreshing = false,
                        hero = latestMovies.firstOrNull(),
                        latest = latestMovies,
                        series = series.await().getOrDefault(emptyList()),
                        singles = singles.await().getOrDefault(emptyList()),
                        cinema = cinema.await().getOrDefault(emptyList()),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Không tải được trang chủ",
                )
            }
        }
    }
}

private suspend fun <T> safe(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
