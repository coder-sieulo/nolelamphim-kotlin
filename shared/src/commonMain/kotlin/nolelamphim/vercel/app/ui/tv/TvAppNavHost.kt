package nolelamphim.vercel.app.ui.tv

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.navigation.Routes
import nolelamphim.vercel.app.ui.screens.catalog.CatalogScreen
import nolelamphim.vercel.app.ui.screens.catalog.CatalogType
import nolelamphim.vercel.app.ui.screens.detail.DetailScreen
import nolelamphim.vercel.app.ui.screens.filter.FilterScreen
import nolelamphim.vercel.app.ui.screens.home.HomeScreen
import nolelamphim.vercel.app.ui.screens.library.LibraryScreen
import nolelamphim.vercel.app.ui.screens.search.SearchScreen
import nolelamphim.vercel.app.ui.screens.watch.WatchScreen
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.LocalTvReducedMotion
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

private const val SETTINGS = "tv-settings"

/** A separate TV shell keeps mobile navigation and TV focus lifecycles independent. */
@Composable
fun TvAppNavHost() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Routes.HOME
    val primaryRoutes = listOf(Routes.HOME, Routes.SEARCH, Routes.FILTER, Routes.LIBRARY, SETTINGS)
    val topLevel = route in primaryRoutes || route == Routes.SEARCH_QUERY || route == Routes.FILTER_QUERY
    val currentTab = when (route) {
        Routes.SEARCH_QUERY -> Routes.SEARCH
        Routes.FILTER_QUERY -> Routes.FILTER
        else -> route
    }
    val reducedMotion by AppContainer.tvPreferences.reducedMotion.collectAsStateWithLifecycle()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val navigationFocus = remember { primaryRoutes.associateWith { FocusRequester() } }
    val contentFocus = remember { FocusRequester() }
    var navigationFocused by remember { mutableStateOf(false) }
    fun focusNavigation() { navigationFocus[currentTab]?.requestFocus() }
    fun focusContent() { contentFocus.requestFocus() }
    fun navigatePrimary(destination: String) {
        if (currentTab == destination) { focusContent(); return }
        nav.navigate(destination) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(
        LocalIsTv provides true,
        LocalTvReducedMotion provides reducedMotion,
        LocalTvNavigation provides TvNavigationActions(::focusNavigation, ::focusContent, navigationFocused),
    ) {
        // Panels register their handlers inside the screen and take priority over this handler.
        PlatformBackHandler(enabled = topLevel && !imeVisible && (!navigationFocused || currentTab != Routes.HOME)) {
            if (!navigationFocused) focusNavigation() else navigatePrimary(Routes.HOME)
        }
        Row(Modifier.fillMaxSize().background(CinemaColors.TvBackground)) {
            if (topLevel) {
                TvNavigationRail(
                    currentTab = currentTab,
                    navigationFocus = navigationFocus,
                    onNavigate = ::navigatePrimary,
                    onFocusChanged = { navigationFocused = it },
                )
            }
            Box(Modifier.weight(1f).fillMaxSize().focusRequester(contentFocus).focusRestorer().focusGroup()) {
                NavHost(
                    navController = nav, startDestination = Routes.HOME,
                    enterTransition = { if (reducedMotion) EnterTransition.None else fadeIn(tween(140)) },
                    exitTransition = { if (reducedMotion) ExitTransition.None else fadeOut(tween(100)) },
                    popEnterTransition = { if (reducedMotion) EnterTransition.None else fadeIn(tween(140)) },
                    popExitTransition = { if (reducedMotion) ExitTransition.None else fadeOut(tween(100)) },
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onMovieClick = { nav.navigate(Routes.detail(it.slug)) },
                            onSeeAll = { nav.navigate(Routes.catalog(it)) },
                            onContinueClick = { nav.navigate(Routes.resume(it.slug)) },
                            onOpenSearch = { navigatePrimary(Routes.SEARCH) },
                            onOpenFilter = { navigatePrimary(Routes.FILTER) },
                        )
                    }
                    composable(Routes.SEARCH) { SearchScreen(onMovieClick = { nav.navigate(Routes.detail(it.slug)) }, onOpenFilter = { navigatePrimary(Routes.FILTER) }) }
                    composable(Routes.FILTER) { FilterScreen(onBack = ::focusNavigation, onMovieClick = { nav.navigate(Routes.detail(it.slug)) }) }
                    composable(Routes.LIBRARY) {
                        LibraryScreen(
                            onMovieClick = { nav.navigate(Routes.detail(it)) },
                            onWatchClick = { nav.navigate(Routes.resume(it.slug)) },
                            onExploreClick = { navigatePrimary(Routes.FILTER) },
                        )
                    }
                    composable(SETTINGS) { TvSettingsScreen() }
                    composable(Routes.CATALOG, arguments = listOf(navArgument("type") { type = NavType.StringType })) { backEntry ->
                        val type = backEntry.arguments?.read { getStringOrNull("type") } ?: CatalogType.LATEST
                        CatalogScreen(type, CatalogType.titleOf(type), onBack = { nav.popBackStack() }, onMovieClick = { nav.navigate(Routes.detail(it.slug)) })
                    }
                    composable(Routes.DETAIL, arguments = listOf(navArgument("slug") { type = NavType.StringType })) { backEntry ->
                        val slug = backEntry.arguments?.read { getStringOrNull("slug") } ?: return@composable
                        DetailScreen(slug, onBack = { nav.popBackStack() },
                            onPlay = { movie, server, episode -> nav.navigate(Routes.watch(movie, server, episode)) },
                            onMovieClick = { nav.navigate(Routes.detail(it)) })
                    }
                    composable(Routes.WATCH, arguments = listOf(navArgument("slug") { type = NavType.StringType }, navArgument("server") { type = NavType.IntType }, navArgument("episode") { type = NavType.IntType })) { backEntry ->
                        val slug = backEntry.arguments?.read { getStringOrNull("slug") } ?: return@composable
                        val server = backEntry.arguments?.read { getIntOrNull("server") } ?: 0
                        val episode = backEntry.arguments?.read { getIntOrNull("episode") } ?: 0
                        WatchScreen(slug, server, episode, onBack = { nav.popBackStack() })
                    }
                    composable(Routes.WATCH_RESUME, arguments = listOf(navArgument("slug") { type = NavType.StringType })) { backEntry ->
                        val slug = backEntry.arguments?.read { getStringOrNull("slug") } ?: return@composable
                        WatchScreen(slug, 0, 0, onBack = { nav.popBackStack() }, preferSavedEpisode = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun TvNavigationRail(
    currentTab: String,
    navigationFocus: Map<String, FocusRequester>,
    onNavigate: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
) {
    val items = listOf(
        Triple(Routes.HOME, "Trang chủ", Icons.Default.Home),
        Triple(Routes.SEARCH, "Tìm kiếm", Icons.Default.Search),
        Triple(Routes.FILTER, "Khám phá", Icons.Default.Explore),
        Triple(Routes.LIBRARY, "Thư viện", Icons.Default.Bookmark),
        Triple(SETTINGS, "Cài đặt", Icons.Default.Settings),
    )
    Column(
        modifier = Modifier
            .width(112.dp)
            .fillMaxHeight()
            .background(CinemaColors.TvSurface)
            .padding(horizontal = 12.dp, vertical = 28.dp)
            .onFocusChanged { onFocusChanged(it.hasFocus) }
            .focusRestorer()
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "nolelamphim",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp),
            color = CinemaColors.TvOrange,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        Spacer(Modifier.height(20.dp))
        items.forEach { (destination, label, icon) ->
            TvRailItem(
                text = label,
                icon = icon,
                selected = destination == currentTab,
                modifier = Modifier.focusRequester(navigationFocus.getValue(destination)),
                onClick = { onNavigate(destination) },
            )
        }
    }
}

@Composable
private fun TvRailItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(56.dp)
            .background(if (selected) Color(0xFF364253) else Color.Transparent, shape)
            .semantics {
                contentDescription = text
                role = Role.Tab
                this.selected = selected
            }
            .cinemaFocusable(onClick = onClick, shape = shape, scaleFocused = 1f),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) CinemaColors.TvOrange else CinemaColors.TvMuted,
            modifier = Modifier.size(24.dp),
        )
    }
}
