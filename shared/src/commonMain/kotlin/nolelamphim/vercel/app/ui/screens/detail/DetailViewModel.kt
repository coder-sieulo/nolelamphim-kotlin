package nolelamphim.vercel.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository

class DetailViewModel(
    private val repository: MovieRepository,
    private val slug: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                _uiState.value = DetailUiState.Success(repository.detail(slug))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.message ?: "Không tải được chi tiết phim",
                )
            }
        }
    }
}
