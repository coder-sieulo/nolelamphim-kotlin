package nolelamphim.vercel.app.navigation

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val SEARCH_QUERY = "search/{query}"
    const val LIBRARY = "library"
    const val FILTER = "filter"
    const val FILTER_QUERY = "filter/{query}"

    const val CATALOG = "catalog/{type}"
    const val DETAIL = "detail/{slug}"
    const val WATCH = "watch/{slug}/{server}/{episode}"
    const val WATCH_RESUME = "resume/{slug}"

    fun catalog(type: String) = "catalog/$type"

    fun detail(slug: String) = "detail/$slug"

    fun search(query: String? = null): String =
        if (query.isNullOrBlank()) "search" else "search/${query.trim()}"

    fun filter(query: String? = null): String =
        if (query.isNullOrBlank()) "filter" else "filter/${query.trim()}"

    fun watch(slug: String, server: Int, episode: Int) =
        "watch/$slug/$server/$episode"

    fun resume(slug: String) = "resume/$slug"
}
