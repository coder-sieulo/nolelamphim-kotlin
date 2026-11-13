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
)

object MiniPlayerManager {
    var activePlayer by mutableStateOf<MiniPlayerData?>(null)
        private set

    fun minimize(data: MiniPlayerData) {
        activePlayer = data
    }

    fun close() {
        activePlayer = null
    }
}
