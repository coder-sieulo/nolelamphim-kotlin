package nolelamphim.vercel.app.data.local

import kotlinx.serialization.Serializable
import nolelamphim.vercel.app.model.Episode
import nolelamphim.vercel.app.model.EpisodeServer

data class PlaybackIdentity(
    val movieSlug: String,
    val serverIndex: Int,
    val serverName: String,
    val episodeIndex: Int,
    val episodeSlug: String,
    val episodeName: String,
)

@Serializable
data class EpisodeProgress(
    val movieSlug: String,
    val serverName: String,
    val episodeSlug: String = "",
    val episodeName: String,
    val serverIndex: Int,
    val episodeIndex: Int,
    val positionMs: Long,
    val durationMs: Long,
) {
    val isCompleted: Boolean get() = durationMs > 0L && positionMs.toDouble() / durationMs >= 0.95
}

private fun normalizedEpisode(value: String): String {
    val compact = value.lowercase().trim().removePrefix("tập").removePrefix("tap")
        .replace(Regex("[^\\p{L}\\p{N}]+"), "")
    return if (compact.isNotEmpty() && compact.all(Char::isDigit)) compact.trimStart('0').ifEmpty { "0" } else compact
}

fun sameEpisode(firstSlug: String, firstName: String, secondSlug: String, secondName: String): Boolean {
    val slug = normalizedEpisode(firstSlug)
    val otherSlug = normalizedEpisode(secondSlug)
    if (slug.isNotEmpty() && slug == otherSlug) return true
    val name = normalizedEpisode(firstName)
    return name.isNotEmpty() && name == normalizedEpisode(secondName)
}

/** Never silently switch to an unrelated episode at the same array index. */
fun equivalentEpisodeIndex(episode: Episode, target: EpisodeServer): Int? =
    target.episodes.indexOfFirst { sameEpisode(episode.slug, episode.name, it.slug, it.name) }
        .takeIf { it >= 0 }

internal fun EpisodeProgress.matches(identity: PlaybackIdentity): Boolean =
    movieSlug == identity.movieSlug && serverName.trim().equals(identity.serverName.trim(), ignoreCase = true) &&
        sameEpisode(episodeSlug, episodeName, identity.episodeSlug, identity.episodeName)
