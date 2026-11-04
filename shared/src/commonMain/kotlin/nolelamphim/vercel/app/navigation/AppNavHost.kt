package nolelamphim.vercel.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
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

    val showQuickMenu = currentRoute != null

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
    val idleTransition = rememberInfiniteTransition(label = "quick-access-idle")
    val idleScale by idleTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "quick-access-pulse",
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (expanded) 1.08f else idleScale,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "quick-access-scale",
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "quick-access-rotation",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.24f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "quick-access-glow",
    )
    val homeY by animateDpAsState(
        targetValue = if (expanded) (-80).dp else 0.dp,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "home-radial-y",
    )
    val searchX by animateDpAsState(
        targetValue = if (expanded) (-57).dp else 0.dp,
        animationSpec = tween(260, delayMillis = 30, easing = FastOutSlowInEasing),
        label = "search-radial-x",
    )
    val searchY by animateDpAsState(
        targetValue = if (expanded) (-57).dp else 0.dp,
        animationSpec = tween(260, delayMillis = 30, easing = FastOutSlowInEasing),
        label = "search-radial-y",
    )
    val libraryX by animateDpAsState(
        targetValue = if (expanded) (-80).dp else 0.dp,
        animationSpec = tween(260, delayMillis = 60, easing = FastOutSlowInEasing),
        label = "library-radial-x",
    )

    Box(modifier = Modifier.size(180.dp)) {
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 0.dp, y = homeY),
            enter = fadeIn(tween(160)) + scaleIn(animationSpec = tween(220)),
            exit = fadeOut(tween(120)) + scaleOut(animationSpec = tween(180)),
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { expanded = false; onHome() }) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Trang chủ",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                            spotColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                        ),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = searchX, y = searchY),
            enter = fadeIn(tween(200, delayMillis = 35)) + scaleIn(animationSpec = tween(220, delayMillis = 35)),
            exit = fadeOut(tween(100)) + scaleOut(animationSpec = tween(160)),
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { expanded = false; onSearch() }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Tìm kiếm",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                            spotColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                        ),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = libraryX, y = 0.dp),
            enter = fadeIn(tween(240, delayMillis = 70)) + scaleIn(animationSpec = tween(220, delayMillis = 70)),
            exit = fadeOut(tween(80)) + scaleOut(animationSpec = tween(140)),
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { expanded = false; onLibrary() }) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Thư viện",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                            spotColor = Color(0xFFF97316).copy(alpha = glowAlpha),
                        ),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
                .size(58.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                    rotationZ = iconRotation * 0.08f
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFFF97316).copy(alpha = 0.12f), CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF97316).copy(alpha = 0.48f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                        contentDescription = "Mở điều hướng",
                        tint = if (currentRoute == Routes.HOME) Color(0xFFF97316) else Color.White,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = iconRotation
                        },
                    )
                }
            }
        }
    }
}
