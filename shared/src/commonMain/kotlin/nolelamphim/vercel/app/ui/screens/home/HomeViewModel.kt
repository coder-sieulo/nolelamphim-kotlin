package nolelamphim.vercel.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class HomeViewModel(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                coroutineScope {
                    val latest = async { safe { repository.latest() } }
                    val series = async { safe { repository.byCategory("phim-bo") } }
                    val singles = async { safe { repository.byCategory("phim-le") } }
                    val cinema = async { safe { repository.byCategory("phim-chieu-rap") } }

                    val latestMovies = latest.await().getOrThrow()
                    _uiState.value = HomeUiState(
                        isLoading = false,
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
                _uiState.value = HomeUiState(
                    isLoading = false,
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
