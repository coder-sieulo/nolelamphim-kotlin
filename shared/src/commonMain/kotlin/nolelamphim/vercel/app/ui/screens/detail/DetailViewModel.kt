package nolelamphim.vercel.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.model.toMovie

class DetailViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary,
    private val slug: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            localLibrary.favorites.collect { favorites ->
                val current = _uiState.value
                if (current is DetailUiState.Success) {
                    _uiState.value = current.copy(
                        isFavorite = favorites.any { it.slug == slug },
                    )
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val detail = repository.detail(slug)
                val related = try {
                    repository.relatedMovies(slug, detail.categorySlugs.firstOrNull())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emptyList()
                }
                _uiState.value = DetailUiState.Success(
                    movie = detail,
                    related = related,
                    isFavorite = localLibrary.isFavorite(slug),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(
                    e.message ?: "Không tải được chi tiết phim",
                )
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? DetailUiState.Success ?: return
        localLibrary.toggleFavorite(current.movie.toMovie())
    }
}
