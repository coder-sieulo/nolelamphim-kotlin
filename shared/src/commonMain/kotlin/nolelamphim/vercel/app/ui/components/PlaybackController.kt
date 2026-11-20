package nolelamphim.vercel.app.ui.components

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class PlaybackStatus { Preparing, Playing, Paused, Buffering, Ended, Error }

data class PlaybackSnapshot(
    val status: PlaybackStatus = PlaybackStatus.Preparing,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val canSeek: Boolean = false,
    val playWhenReady: Boolean = false,
    val isForeground: Boolean = true,
    val errorMessage: String? = null,
)

enum class PlaybackAction { Play, Pause, Toggle, Seek, Retry, Speed }

data class PlaybackCommand(val id: Long, val action: PlaybackAction, val value: Long = 0L)

/** Commands carry a sequence so repeated seeks/retries are never lost to state equality. */
@Stable
class PlaybackController {
    val sessionKey: String = "tv-playback-${++nextSession}"
    var snapshot by mutableStateOf(PlaybackSnapshot())
        private set
    var command by mutableStateOf<PlaybackCommand?>(null)
        private set
    private var sequence = 0L

    fun update(snapshot: PlaybackSnapshot) { this.snapshot = snapshot }
    fun play() = send(PlaybackAction.Play)
    fun pause() = send(PlaybackAction.Pause)
    fun toggle() = send(PlaybackAction.Toggle)
    fun seekTo(positionMs: Long) = send(PlaybackAction.Seek, positionMs.coerceAtLeast(0L))
    fun retry() = send(PlaybackAction.Retry)
    fun setSpeed(speed: Float) = send(PlaybackAction.Speed, (speed * 1000).toLong())

    private fun send(action: PlaybackAction, value: Long = 0L) {
        command = PlaybackCommand(++sequence, action, value)
    }

    private companion object { var nextSession = 0L }
}
