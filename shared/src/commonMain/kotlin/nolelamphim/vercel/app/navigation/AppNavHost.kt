package nolelamphim.vercel.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nolelamphim.vercel.app.getPlatform
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
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTv = remember { getPlatform().isTv }

    val isTopLevelRoute = currentRoute in listOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.FILTER)
    val isFullscreenRoute = currentRoute?.startsWith("watch") == true

    CompositionLocalProvider(LocalIsTv provides isTv) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemaColors.Background),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // TV Side Navigation Rail
                if (isTv && !isFullscreenRoute) {
                    TvSideNavigationRail(
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
                        onFilter = {
                            navController.navigate(Routes.FILTER) {
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

                // Main NavHost Content
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f),
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
                            onOpenSearch = {
                                navController.navigate(Routes.SEARCH) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenFilter = {
                                navController.navigate(Routes.FILTER) {
                                    launchSingleTop = true
                                }
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
                            onExploreClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.HOME) { inclusive = false }
                                    launchSingleTop = true
                                }
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

            // Mobile Floating Translucent Bottom Navigation Bar
            if (!isTv && isTopLevelRoute) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                ) {
                    MobileBottomNav(
                        currentRoute = currentRoute,
                        onHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onFilter = {
                            navController.navigate(Routes.FILTER) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
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
            }
        }
    }
}

@Composable
private fun MobileBottomNav(
    currentRoute: String?,
    onHome: () -> Unit,
    onFilter: () -> Unit,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
) {
    val navShape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .shadow(
                elevation = 20.dp,
                shape = navShape,
                spotColor = Color.Black.copy(alpha = 0.7f),
                ambientColor = Color.Black.copy(alpha = 0.5f),
            )
            .clip(navShape)
            .background(Color(0xF0181822))
            .border(1.dp, Color.White.copy(alpha = 0.12f), navShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MobileNavItem(
                icon = Icons.Default.Home,
                label = "Trang chủ",
                isSelected = currentRoute == Routes.HOME,
                onClick = onHome,
            )
            MobileNavItem(
                icon = Icons.Default.Tune,
                label = "Bộ lọc",
                isSelected = currentRoute == Routes.FILTER,
                onClick = onFilter,
            )
            MobileNavItem(
                icon = Icons.Default.Search,
                label = "Tìm kiếm",
                isSelected = currentRoute == Routes.SEARCH,
                onClick = onSearch,
            )
            MobileNavItem(
                icon = Icons.Default.Bookmark,
                label = "Thư viện",
                isSelected = currentRoute == Routes.LIBRARY,
                onClick = onLibrary,
            )
        }
    }
}

@Composable
private fun MobileNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) CinemaColors.Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = if (isSelected) 14.dp else 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else CinemaColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            if (isSelected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TvSideNavigationRail(
    currentRoute: String?,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    onLibrary: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(88.dp)
            .background(CinemaColors.Surface)
            .border(1.dp, CinemaColors.BorderSubtle)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Logo
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CinemaColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Navigation Items
        TvRailItem(
            icon = Icons.Default.Home,
            label = "Trang chủ",
            isSelected = currentRoute == Routes.HOME,
            onClick = onHome,
        )
        TvRailItem(
            icon = Icons.Default.Search,
            label = "Tìm kiếm",
            isSelected = currentRoute == Routes.SEARCH,
            onClick = onSearch,
        )
        TvRailItem(
            icon = Icons.Default.Tune,
            label = "Bộ lọc",
            isSelected = currentRoute == Routes.FILTER,
            onClick = onFilter,
        )
        TvRailItem(
            icon = Icons.Default.Bookmark,
            label = "Thư viện",
            isSelected = currentRoute == Routes.LIBRARY,
            onClick = onLibrary,
        )
    }
}

@Composable
private fun TvRailItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(shape)
            .background(if (isSelected) CinemaColors.PrimaryContainer else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) CinemaColors.Primary else Color.Transparent,
                shape,
            )
            .cinemaFocusable(
                onClick = onClick,
                shape = shape,
                scaleFocused = 1.12f,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CinemaColors.Primary else CinemaColors.TextSecondary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isSelected) CinemaColors.Primary else CinemaColors.TextMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}
