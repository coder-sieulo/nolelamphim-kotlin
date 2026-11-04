package nolelamphim.vercel.app.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException

/**
 * VSMOV serves every HLS segment as a tiny PNG whose bytes are followed by a raw MPEG-TS
 * payload. Browsers get away with it because hls.js scans for the TS sync byte, while
 * ExoPlayer's [androidx.media3.extractor.ts.TsExtractor] expects the stream to start with a
 * TS packet. This source probes the beginning of the resource, and when it finds the PNG
 * wrapper it hides everything up to the first run of TS packets.
 *
 * Content that is not PNG-wrapped (playlists, plain TS/fMP4 segments) passes through untouched.
 */
@OptIn(UnstableApi::class)
internal class PngWrappedTsDataSource(
    private val upstream: DataSource,
) : DataSource {

    private var buffered: ByteArray = EMPTY
    private var bufferedOffset = 0
    private var opened = false

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        close()
        upstream.open(dataSpec)
        opened = true

        val probe = ByteArray(PROBE_SIZE)
        var read = 0
        while (read < probe.size) {
            val count = upstream.read(probe, read, probe.size - read)
            if (count == C.RESULT_END_OF_INPUT) break
            read += count
        }

        buffered = if (read == probe.size) probe else probe.copyOf(read)
        bufferedOffset = payloadStart(buffered)
        // Length is unknown now that the prefix is hidden; readers consume until EOF.
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        check(opened) { "open() must be called before read()" }
        if (bufferedOffset < buffered.size) {
            val count = minOf(length, buffered.size - bufferedOffset)
            buffered.copyInto(buffer, destinationOffset = offset, startIndex = bufferedOffset, endIndex = bufferedOffset + count)
            bufferedOffset += count
            return count
        }
        return upstream.read(buffer, offset, length)
    }

    override fun getUri(): Uri? = upstream.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun close() {
        opened = false
        buffered = EMPTY
        bufferedOffset = 0
        upstream.close()
    }

    /** Returns where the media payload starts, or 0 when the resource should not be rewritten. */
    private fun payloadStart(bytes: ByteArray): Int {
        if (bytes.size < PNG_SIGNATURE.size) return 0
        if (!PNG_SIGNATURE.indices.all { bytes[it] == PNG_SIGNATURE[it] }) return 0
        val pngEnd = findPngEnd(bytes) ?: return 0
        return findTsRun(bytes, pngEnd) ?: 0
    }

    /** Walks PNG chunks and returns the offset right after IEND. */
    private fun findPngEnd(bytes: ByteArray): Int? {
        var position = PNG_SIGNATURE.size
        while (position + CHUNK_HEADER_SIZE <= bytes.size) {
            val chunkLength = readInt(bytes, position)
            if (chunkLength < 0) return null
            val next = position.toLong() + CHUNK_HEADER_SIZE + chunkLength
            if (next > bytes.size) return null
            if (bytes[position + 4] == IEND[0] &&
                bytes[position + 5] == IEND[1] &&
                bytes[position + 6] == IEND[2] &&
                bytes[position + 7] == IEND[3]
            ) {
                return next.toInt()
            }
            position = next.toInt()
        }
        return null
    }

    /** Finds the first offset from [from] where several consecutive 188-byte TS packets line up. */
    private fun findTsRun(bytes: ByteArray, from: Int): Int? {
        val lastStart = bytes.size - TS_PACKET_SIZE * (TS_PACKETS_TO_DETECT - 1)
        var position = from
        while (position < lastStart) {
            var packets = 0
            while (packets < TS_PACKETS_TO_DETECT &&
                bytes[position + packets * TS_PACKET_SIZE] == TS_SYNC_BYTE
            ) {
                packets++
            }
            if (packets == TS_PACKETS_TO_DETECT) return position
            position++
        }
        return null
    }

    private fun readInt(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)

    @OptIn(UnstableApi::class)
    class Factory(
        private val upstreamFactory: DataSource.Factory,
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource =
            PngWrappedTsDataSource(upstreamFactory.createDataSource())
    }

    private companion object {
        const val PROBE_SIZE = 32 * 1024
        const val CHUNK_HEADER_SIZE = 12
        const val TS_PACKET_SIZE = 188
        const val TS_PACKETS_TO_DETECT = 4

        val TS_SYNC_BYTE = 0x47.toByte()
        val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        )
        val IEND = byteArrayOf(0x49, 0x45, 0x4E, 0x44)
        val EMPTY = ByteArray(0)
    }
}
