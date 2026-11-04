package nolelamphim.vercel.app.ui.screens.library

import androidx.lifecycle.ViewModel
import nolelamphim.vercel.app.data.local.LocalLibrary

class LibraryViewModel(
    private val localLibrary: LocalLibrary,
) : ViewModel() {

    val favorites = localLibrary.favorites
    val history = localLibrary.history

    fun removeFavorite(slug: String) = localLibrary.removeFavorite(slug)

    fun clearHistory() = localLibrary.clearHistory()
}
