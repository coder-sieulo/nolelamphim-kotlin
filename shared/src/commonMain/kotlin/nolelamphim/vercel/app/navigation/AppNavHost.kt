package nolelamphim.vercel.app.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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

    val showBottomBar = currentRoute == Routes.HOME ||
        currentRoute == Routes.SEARCH ||
        currentRoute == Routes.LIBRARY

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    val pillShape = RoundedCornerShape(32.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 360.dp)
                            .height(64.dp)
                            .shadow(22.dp, pillShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color(0xFF2E2E34).copy(alpha = 0.94f),
                                        Color(0xFF202024).copy(alpha = 0.98f),
                                    ),
                                ),
                                shape = pillShape,
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.16f),
                                shape = pillShape,
                            ),
                    ) {
                        NavigationBar(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == Routes.HOME,
                                onClick = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color(0xFFB3B3B3),
                                    indicatorColor = Color(0x33F97316),
                                ),
                            )
                            NavigationBarItem(
                                selected = currentRoute == Routes.SEARCH,
                                onClick = {
                                    navController.navigate(Routes.SEARCH) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Search, contentDescription = "Tìm kiếm") },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color(0xFFB3B3B3),
                                    indicatorColor = Color(0x33F97316),
                                ),
                            )
                            NavigationBarItem(
                                selected = currentRoute == Routes.LIBRARY,
                                onClick = {
                                    navController.navigate(Routes.LIBRARY) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(Icons.Default.Bookmark, contentDescription = "Thư viện") },
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color(0xFFB3B3B3),
                                    indicatorColor = Color(0x33F97316),
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = if (showBottomBar) {
                Modifier.padding(padding)
            } else {
                Modifier.padding(padding).navigationBarsPadding()
            },
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
