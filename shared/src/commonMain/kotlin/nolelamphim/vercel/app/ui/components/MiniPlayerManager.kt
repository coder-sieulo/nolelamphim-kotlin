package nolelamphim.vercel.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class MiniPlayerData(
    val slug: String,
    val serverIndex: Int,
    val episodeIndex: Int,
    val movieName: String,
    val episodeName: String,
    val posterUrl: String?,
    val playbackUrl: String,
    val embedUrl: String?,
    val positionMs: Long = 0L,
)

object MiniPlayerManager {
    var activePlayer by mutableStateOf<MiniPlayerData?>(null)
        private set

    private val resumePositions = mutableMapOf<String, Long>()

    fun minimize(data: MiniPlayerData) {
        activePlayer = data
        if (data.positionMs > 0L) {
            setResumePosition(data.slug, data.serverIndex, data.episodeIndex, data.positionMs)
        }
    }

    fun updatePosition(positionMs: Long) {
        val current = activePlayer ?: return
        if (positionMs > 0L) {
            activePlayer = current.copy(positionMs = positionMs)
            setResumePosition(current.slug, current.serverIndex, current.episodeIndex, positionMs)
        }
    }

    fun setResumePosition(slug: String, serverIndex: Int, episodeIndex: Int, positionMs: Long) {
        if (positionMs > 0L) {
            resumePositions["$slug/$serverIndex/$episodeIndex"] = positionMs
        }
    }

    fun consumeResumePosition(slug: String, serverIndex: Int, episodeIndex: Int): Long {
        val key = "$slug/$serverIndex/$episodeIndex"
        return resumePositions.remove(key) ?: 0L
    }

    fun getResumePosition(slug: String, serverIndex: Int, episodeIndex: Int): Long {
        val key = "$slug/$serverIndex/$episodeIndex"
        return resumePositions[key] ?: 0L
    }

    fun close() {
        val current = activePlayer
        if (current != null && current.positionMs > 0L) {
            setResumePosition(current.slug, current.serverIndex, current.episodeIndex, current.positionMs)
        }
        activePlayer = null
    }
}
