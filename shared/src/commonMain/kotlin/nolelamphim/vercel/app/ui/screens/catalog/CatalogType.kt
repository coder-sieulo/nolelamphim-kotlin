package nolelamphim.vercel.app.ui.screens.catalog

object CatalogType {
    const val LATEST = "latest"
    const val SERIES = "phim-bo"
    const val SINGLES = "phim-le"
    const val CINEMA = "phim-chieu-rap"

    fun titleOf(type: String): String = when (type) {
        SERIES -> "Phim bộ"
        SINGLES -> "Phim lẻ"
        CINEMA -> "Phim chiếu rạp"
        else -> "Mới cập nhật"
    }
}
