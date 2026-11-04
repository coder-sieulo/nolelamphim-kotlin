package nolelamphim.vercel.app.navigation

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val FILTER = "filter"

    const val CATALOG = "catalog/{type}"
    const val DETAIL = "detail/{slug}"
    const val WATCH = "watch/{slug}/{server}/{episode}"

    fun catalog(type: String) = "catalog/$type"

    fun detail(slug: String) = "detail/$slug"

    fun watch(slug: String, server: Int, episode: Int) =
        "watch/$slug/$server/$episode"
}
