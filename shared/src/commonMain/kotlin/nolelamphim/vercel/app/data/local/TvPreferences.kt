package nolelamphim.vercel.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-local preferences; quiet motion and explicit episode changes are the defaults. */
class TvPreferences(private val store: KeyValueStore) {
    private val _reducedMotion = MutableStateFlow(store.getString(MOTION)?.toBooleanStrictOrNull() ?: true)
    val reducedMotion = _reducedMotion.asStateFlow()
    private val _autoplayNextEpisode = MutableStateFlow(store.getString(AUTOPLAY)?.toBooleanStrictOrNull() ?: false)
    val autoplayNextEpisode = _autoplayNextEpisode.asStateFlow()

    fun setReducedMotion(enabled: Boolean) {
        store.putString(MOTION, enabled.toString())
        _reducedMotion.value = enabled
    }

    fun setAutoplayNextEpisode(enabled: Boolean) {
        store.putString(AUTOPLAY, enabled.toString())
        _autoplayNextEpisode.value = enabled
    }

    private companion object {
        const val MOTION = "tv.reducedMotion.v1"
        const val AUTOPLAY = "tv.autoplay.v1"
    }
}
