package nolelamphim.vercel.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nolelamphim.vercel.app.ui.screens.catalog.CatalogScreen
import nolelamphim.vercel.app.ui.screens.catalog.CatalogType
import nolelamphim.vercel.app.ui.screens.detail.DetailScreen
import nolelamphim.vercel.app.ui.screens.filter.FilterScreen
import nolelamphim.vercel.app.ui.screens.home.HomeScreen
import nolelamphim.vercel.app.ui.screens.library.LibraryScreen
import nolelamphim.vercel.app.ui.screens.search.SearchScreen
import nolelamphim.vercel.app.ui.screens.watch.WatchScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showQuickMenu = currentRoute == Routes.HOME ||
        currentRoute == Routes.SEARCH ||
        currentRoute == Routes.LIBRARY

    Scaffold(
        floatingActionButton = {
            if (showQuickMenu) {
                QuickAccessButton(
                    currentRoute = currentRoute,
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSearch = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLibrary = {
                        navController.navigate(Routes.LIBRARY) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                    onSeeAll = { type ->
                        navController.navigate(Routes.catalog(type))
                    },
                    onContinueClick = { entry ->
                        navController.navigate(
                            Routes.watch(entry.slug, entry.serverIndex, entry.episodeIndex),
                        )
                    },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                    onOpenFilter = {
                        navController.navigate(Routes.FILTER)
                    },
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onMovieClick = { slug ->
                        navController.navigate(Routes.detail(slug))
                    },
                    onWatchClick = { entry ->
                        navController.navigate(
                            Routes.watch(entry.slug, entry.serverIndex, entry.episodeIndex),
                        )
                    },
                )
            }

            composable(
                route = Routes.CATALOG,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val type = entry.arguments?.getString("type") ?: CatalogType.LATEST
                CatalogScreen(
                    type = type,
                    title = CatalogType.titleOf(type),
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                )
            }

            composable(Routes.FILTER) {
                FilterScreen(
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movie ->
                        navController.navigate(Routes.detail(movie.slug))
                    },
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                ),
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                DetailScreen(
                    slug = slug,
                    onBack = { navController.popBackStack() },
                    onPlay = { movieSlug, server, episode ->
                        navController.navigate(Routes.watch(movieSlug, server, episode))
                    },
                    onMovieClick = { movieSlug ->
                        navController.navigate(Routes.detail(movieSlug)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = Routes.WATCH,
                arguments = listOf(
                    navArgument("slug") { type = NavType.StringType },
                    navArgument("server") { type = NavType.IntType },
                    navArgument("episode") { type = NavType.IntType },
                ),
            ) { entry ->
                val slug = entry.arguments?.getString("slug") ?: return@composable
                val server = entry.arguments?.getInt("server") ?: 0
                val episode = entry.arguments?.getInt("episode") ?: 0
                WatchScreen(
                    slug = slug,
                    serverIndex = server,
                    episodeIndex = episode,
                    onBack = { navController.popBackStack() },
                    onNextEpisode = {
                        navController.navigate(Routes.watch(slug, server, episode + 1)) {
                            popUpTo(Routes.WATCH) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun QuickAccessButton(
    currentRoute: String?,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Trang chủ") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                onClick = {
                    expanded = false
                    onHome()
                },
            )
            DropdownMenuItem(
                text = { Text("Tìm kiếm") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                onClick = {
                    expanded = false
                    onSearch()
                },
            )
            DropdownMenuItem(
                text = { Text("Thư viện") },
                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                onClick = {
                    expanded = false
                    onLibrary()
                },
            )
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = Color(0xFF2E2E34),
            contentColor = if (currentRoute == Routes.HOME) Color(0xFFF97316) else Color.White,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Mở điều hướng",
            )
        }
    }
}
