package nolelamphim.vercel.app.data.stream

/**
 * Turns the player page URL returned by the API (`link_embed`) into a media URL that a
 * native player can open.
 *
 * VSMOV pages mount JW Player with a public master playlist at
 * `<origin>/stream/<videoHash>/master.m3u8`, where `videoHash` is the last path segment of
 * `<origin>/video/<videoHash>`. When the URL already points at a playlist or a media file we
 * pass it through untouched. Anything else returns `null` so the caller can fall back to a
 * WebView.
 */
object StreamUrlResolver {

    private const val MASTER_PLAYLIST = "master.m3u8"

    private val MEDIA_PATH_HINTS = listOf(
        ".m3u8",
        ".mp4",
        ".mpd",
        ".webm",
    )

    private val SIGNED_MASTER_URL =
        Regex("""signedMasterUrl\s*:\s*["']([^"']+)["']""")

    fun resolve(embedUrl: String?): String? {
        val url = embedUrl?.trim().orEmpty()
        if (url.isEmpty()) return null

        val withoutQuery = url.substringBefore('#').substringBefore('?')
        if (isMediaUrl(withoutQuery)) {
            return url
        }

        val origin = originOf(url) ?: return null
        val segments = withoutQuery
            .removePrefix(origin)
            .split('/')
            .filter { it.isNotBlank() }
        if (segments.size != 2 || segments.first() != "video") return null

        val videoHash = segments.last()
        if (videoHash.isBlank()) return null

        return "$origin/stream/$videoHash/$MASTER_PLAYLIST"
    }

    /** True when the URL already points at something a media player can open directly. */
    fun isMediaUrl(url: String?): Boolean {
        val withoutQuery = url?.trim()?.substringBefore('#')?.substringBefore('?').orEmpty()
        return MEDIA_PATH_HINTS.any { withoutQuery.endsWith(it, ignoreCase = true) }
    }

    /**
     * Extracts the signed master playlist URL that the embedded player page exposes for
     * sources that reject unsigned requests (`signedMasterUrl: "..."`). Returns null when the
     * page has no usable signature.
     */
    fun signedMasterUrl(pageHtml: String?): String? {
        if (pageHtml.isNullOrBlank()) return null
        val match = SIGNED_MASTER_URL.find(pageHtml) ?: return null
        val candidate = match.groupValues.getOrNull(1)?.trim().orEmpty()
        return candidate.takeIf { it.startsWith("http") && isMediaUrl(it) }
    }

    /** Returns `scheme://host[:port]` for an absolute http(s) URL. */
    private fun originOf(url: String): String? {
        val schemeEnd = url.indexOf("://").takeIf { it > 0 } ?: return null
        val scheme = url.substring(0, schemeEnd)
        if (!scheme.equals("http", true) && !scheme.equals("https", true)) return null

        val authorityStart = schemeEnd + 3
        val authorityEnd = url.indexOf('/', authorityStart)
            .takeIf { it >= 0 }
            ?: url.length
        if (authorityEnd <= authorityStart) return null

        return url.substring(0, authorityEnd)
    }
}
