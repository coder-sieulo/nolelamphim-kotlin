package nolelamphim.vercel.app.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nolelamphim.vercel.app.ui.screens.detail.DetailScreen
import nolelamphim.vercel.app.ui.screens.filter.FilterScreen
import nolelamphim.vercel.app.ui.screens.home.HomeScreen
import nolelamphim.vercel.app.ui.screens.search.SearchScreen
import nolelamphim.vercel.app.ui.screens.watch.WatchScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute == Routes.HOME || currentRoute == Routes.SEARCH

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Trang chủ",
                            )
                        },
                        label = { Text("Trang chủ") },
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
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                            )
                        },
                        label = { Text("Tìm kiếm") },
                    )
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
                )
            }
        }
    }
}
