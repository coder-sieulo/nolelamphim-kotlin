package nolelamphim.vercel.app.ui.components

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory

object SharedPlayerSession {
    private var exoPlayer: ExoPlayer? = null
    private var currentStreamUrl: String? = null

    @Volatile
    var isRetained: Boolean = false

    init {
        MiniPlayerManager.onRetainSession = {
            isRetained = true
        }
        MiniPlayerManager.onReleaseSession = {
            forceRelease()
        }
    }

    fun getOrCreate(
        context: Context,
        streamUrl: String,
        initialPositionMs: Long = 0L,
    ): ExoPlayer {
        val existing = exoPlayer
        if (existing != null && currentStreamUrl == streamUrl) {
            // Reusing running player session - seamlessly continuous!
            return existing
        }

        // URL changed or player was released: create new
        existing?.release()

        val appContext = context.applicationContext
        val dataSourceFactory = PngWrappedTsDataSource.Factory(
            DefaultDataSource.Factory(appContext),
        )
        val builder = ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                /* handleAudioFocus = */ true,
            )

        try {
            builder.setHandleAudioBecomingNoisy(true)
        } catch (_: Throwable) {
            // Some TV custom ROMs don't support noisy audio receiver
        }

        val newPlayer = builder.build()
        try {
            if (streamUrl.isNotBlank()) {
                newPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
                if (initialPositionMs > 0L) {
                    newPlayer.seekTo(initialPositionMs)
                }
                newPlayer.playWhenReady = true
                newPlayer.prepare()
            }
        } catch (t: Throwable) {
            android.util.Log.e("SharedPlayerSession", "Failed to setMediaItem for $streamUrl", t)
        }

        exoPlayer = newPlayer
        currentStreamUrl = streamUrl
        isRetained = false
        return newPlayer
    }

    fun releaseIfUnused(streamUrl: String) {
        if (!isRetained && currentStreamUrl == streamUrl) {
            exoPlayer?.release()
            exoPlayer = null
            currentStreamUrl = null
        }
    }

    fun forceRelease() {
        isRetained = false
        exoPlayer?.release()
        exoPlayer = null
        currentStreamUrl = null
    }

    fun currentPosition(): Long = exoPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L
    fun duration(): Long = exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L
}
