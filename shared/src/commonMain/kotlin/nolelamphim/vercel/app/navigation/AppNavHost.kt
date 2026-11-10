package nolelamphim.vercel.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.savedstate.read
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
                        val type = entry.arguments?.read { getStringOrNull("type") } ?: CatalogType.LATEST
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
                        val slug = entry.arguments?.read { getStringOrNull("slug") } ?: return@composable
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
                        val slug = entry.arguments?.read { getStringOrNull("slug") } ?: return@composable
                        val server = entry.arguments?.read { getIntOrNull("server") } ?: 0
                        val episode = entry.arguments?.read { getIntOrNull("episode") } ?: 0
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

            // Mobile Fixed Bottom Navigation Bar
            if (!isTv && isTopLevelRoute) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF7101016)),
    ) {
        // Thin top divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.12f)),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MobileNavItem(
                icon = Icons.Default.Home,
                label = "Trang chủ",
                isSelected = currentRoute == Routes.HOME,
                onClick = onHome,
                modifier = Modifier.weight(1f),
            )
            MobileNavItem(
                icon = Icons.Default.Tune,
                label = "Bộ lọc",
                isSelected = currentRoute == Routes.FILTER,
                onClick = onFilter,
                modifier = Modifier.weight(1f),
            )
            MobileNavItem(
                icon = Icons.Default.Search,
                label = "Tìm kiếm",
                isSelected = currentRoute == Routes.SEARCH,
                onClick = onSearch,
                modifier = Modifier.weight(1f),
            )
            MobileNavItem(
                icon = Icons.Default.Bookmark,
                label = "Thư viện",
                isSelected = currentRoute == Routes.LIBRARY,
                onClick = onLibrary,
                modifier = Modifier.weight(1f),
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
    modifier: Modifier = Modifier,
) {
    val activeColor by animateColorAsState(
        targetValue = if (isSelected) CinemaColors.Primary else Color(0xFF8E8E98),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav-item-color",
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = activeColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = activeColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
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
