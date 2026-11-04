package nolelamphim.vercel.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nolelamphim.vercel.app.data.stream.StreamUrlResolver

class StreamUrlResolverTest {

    @Test
    fun resolve_buildsMasterPlaylistFromVideoPage() {
        assertEquals(
            "https://v10.streamvsmov.com/stream/016b17ea-cf3e-4ede-9a86-368d3b6bd597/master.m3u8",
            StreamUrlResolver.resolve(
                "https://v10.streamvsmov.com/video/016b17ea-cf3e-4ede-9a86-368d3b6bd597",
            ),
        )
    }

    @Test
    fun resolve_handlesTrailingSlashAndQuery() {
        assertEquals(
            "https://v10.streamvsmov.com/stream/abc/master.m3u8",
            StreamUrlResolver.resolve("https://v10.streamvsmov.com/video/abc/?ref=home"),
        )
    }

    @Test
    fun resolve_passesThroughMediaUrls() {
        assertEquals(
            "https://cdn.example.com/movie/master.m3u8?token=1",
            StreamUrlResolver.resolve("https://cdn.example.com/movie/master.m3u8?token=1"),
        )
        assertEquals(
            "https://cdn.example.com/movie.mp4",
            StreamUrlResolver.resolve("https://cdn.example.com/movie.mp4"),
        )
    }

    @Test
    fun signedMasterUrl_extractsPlaylistFromEmbedPage() {
        val html = """
            const playerOptions = {
                signedMasterUrl: "https://v2.streamvsmov.com/stream/abc/master.m3u8?expires=1&signature=xyz",
                enableSignedUrl: true,
            };
        """.trimIndent()

        assertEquals(
            "https://v2.streamvsmov.com/stream/abc/master.m3u8?expires=1&signature=xyz",
            StreamUrlResolver.signedMasterUrl(html),
        )
        assertNull(StreamUrlResolver.signedMasterUrl("""signedMasterUrl: "","""))
        assertNull(StreamUrlResolver.signedMasterUrl(null))
    }

    @Test
    fun resolve_rejectsUnsupportedSources() {
        assertNull(StreamUrlResolver.resolve(null))
        assertNull(StreamUrlResolver.resolve(""))
        assertNull(StreamUrlResolver.resolve("not a url"))
        assertNull(StreamUrlResolver.resolve("ftp://example.com/video/abc"))
        assertNull(StreamUrlResolver.resolve("https://example.com/watch/abc"))
        assertNull(StreamUrlResolver.resolve("https://example.com/video/abc/extra"))
    }
}
