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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import nolelamphim.shared.generated.resources.Res
import nolelamphim.shared.generated.resources.nolelamphim_logo
import org.jetbrains.compose.resources.painterResource
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
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
import nolelamphim.vercel.app.ui.components.FloatingMiniPlayer
import nolelamphim.vercel.app.ui.components.MiniPlayerManager
import nolelamphim.vercel.app.ui.screens.search.SearchScreen
import nolelamphim.vercel.app.ui.screens.watch.WatchScreen
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.zIndex
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTv = remember { getPlatform().isTv }

    val isTopLevelRoute = currentRoute in listOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.FILTER) || currentRoute?.startsWith("filter") == true || currentRoute?.startsWith("search") == true
    val isFullscreenRoute = currentRoute?.startsWith("watch") == true

    CompositionLocalProvider(LocalIsTv provides isTv) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isTv) CinemaColors.TvBackground else CinemaColors.Background)
                .drawBehind {
                    if (isTv) {
                        // Cinefy ambient lighting matching body:before in cinefy-tv.html
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(CinemaColors.TvAmbientOrange, Color.Transparent),
                                center = Offset(size.width * 0.77f, size.height * 0.10f),
                                radius = size.width * 0.35f,
                            ),
                        )
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(CinemaColors.TvAmbientPurple, Color.Transparent),
                                center = Offset(size.width * 0.10f, size.height * 0.80f),
                                radius = size.width * 0.35f,
                            ),
                        )
                    }
                },
        ) {

            // Main Content Area (Fixed 96.dp start padding on TV for Cinefy rail)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isTv && !isFullscreenRoute) Modifier.padding(start = 96.dp) else Modifier),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.fillMaxSize(),
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
                            onOpenFilter = {
                                navController.navigate(Routes.FILTER) {
                                    launchSingleTop = true
                                }
                            },
                            onSearchSubmit = { query ->
                                navController.navigate(Routes.search(query)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }

                    composable(Routes.SEARCH) {
                        SearchScreen(
                            initialQuery = null,
                            onMovieClick = { movie ->
                                navController.navigate(Routes.detail(movie.slug))
                            },
                            onOpenFilter = {
                                navController.navigate(Routes.FILTER) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }

                    composable(
                        route = Routes.SEARCH_QUERY,
                        arguments = listOf(
                            navArgument("query") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val query = entry.arguments?.read { getStringOrNull("query") }
                        SearchScreen(
                            initialQuery = query,
                            onMovieClick = { movie ->
                                navController.navigate(Routes.detail(movie.slug))
                            },
                            onOpenFilter = {
                                navController.navigate(Routes.FILTER) {
                                    launchSingleTop = true
                                }
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
                            initialQuery = null,
                            onBack = { navController.popBackStack() },
                            onMovieClick = { movie ->
                                navController.navigate(Routes.detail(movie.slug))
                            },
                        )
                    }

                    composable(
                        route = Routes.FILTER_QUERY,
                        arguments = listOf(
                            navArgument("query") { type = NavType.StringType },
                        ),
                    ) { entry ->
                        val query = entry.arguments?.read { getStringOrNull("query") }
                        FilterScreen(
                            initialQuery = query,
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
                        val slug = try {
                            entry.arguments?.read { getStringOrNull("slug") }
                        } catch (_: Throwable) {
                            null
                        } ?: return@composable
                        val server = try {
                            entry.arguments?.read { getIntOrNull("server") } ?: 0
                        } catch (_: Throwable) {
                            0
                        }
                        val episode = try {
                            entry.arguments?.read { getIntOrNull("episode") } ?: 0
                        } catch (_: Throwable) {
                            0
                        }
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
                            onMinimize = { miniData ->
                                MiniPlayerManager.minimize(miniData)
                                navController.popBackStack()
                            },
                        )
                    }
                }
            }

            // Fixed Cinefy TV Navigation Rail (Clean, stable rail without intrusive scrims)
            if (isTv && !isFullscreenRoute) {
                TvSideNavigationRail(
                    currentRoute = currentRoute,
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onExplore = {
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
                    onSearch = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(100f),
                )
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
                        onExplore = {
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

            // Floating YouTube-Style Corner Mini Player (Bottom-Right)
            val activeMiniPlayer = MiniPlayerManager.activePlayer
            AnimatedVisibility(
                visible = activeMiniPlayer != null && !isFullscreenRoute,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp,
                        bottom = if (!isTv && isTopLevelRoute) 76.dp else 20.dp,
                    )
                    .zIndex(999f),
            ) {
                activeMiniPlayer?.let { data ->
                    FloatingMiniPlayer(
                        data = data,
                        onExpand = {
                            val resumePos = data.positionMs
                            MiniPlayerManager.setResumePosition(
                                data.slug,
                                data.serverIndex,
                                data.episodeIndex,
                                resumePos,
                            )
                            MiniPlayerManager.expand()
                            navController.navigate(
                                Routes.watch(data.slug, data.serverIndex, data.episodeIndex),
                            ) {
                                launchSingleTop = true
                            }
                        },
                        onClose = {
                            MiniPlayerManager.close()
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
    onExplore: () -> Unit,
    onSearch: () -> Unit,
    onLibrary: () -> Unit,
) {
    val isExploreSelected = currentRoute == Routes.FILTER || currentRoute?.startsWith("filter") == true
    val isSearchSelected = currentRoute == Routes.SEARCH || currentRoute?.startsWith("search") == true

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
                icon = Icons.Default.Explore,
                label = "Khám phá",
                isSelected = isExploreSelected,
                onClick = onExplore,
                modifier = Modifier.weight(1f),
            )
            MobileNavItem(
                icon = Icons.Default.Search,
                label = "Tìm kiếm",
                isSelected = isSearchSelected,
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
    onExplore: () -> Unit,
    onLibrary: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFilterSelected = currentRoute == Routes.FILTER || currentRoute?.startsWith("filter") == true
    val isSearchSelected = currentRoute == Routes.SEARCH || currentRoute?.startsWith("search") == true

    val railGradient = Brush.horizontalGradient(
        listOf(
            Color(0xF5050608),
            Color(0xCC050608),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(96.dp)
            .background(railGradient)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(0.dp),
            )
            .padding(vertical = 32.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Cinefy TV Brand Logo Header (Play icon with orange gradient)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFFF812B))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFC18F), Color(0xFFFF791F)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Cinefy TV",
                tint = Color(0xFF211108),
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.height(8.dp))

        // 1. Home
        TvNavButton(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = currentRoute == Routes.HOME,
            onClick = onHome,
        )

        // 2. Explore
        TvNavButton(
            icon = Icons.Default.Explore,
            label = "Explore",
            isSelected = isFilterSelected,
            onClick = onExplore,
        )

        // 3. Library
        TvNavButton(
            icon = Icons.Default.Bookmark,
            label = "Library",
            isSelected = currentRoute == Routes.LIBRARY,
            onClick = onLibrary,
        )

        // 4. Search
        TvNavButton(
            icon = Icons.Default.Search,
            label = "Search",
            isSelected = isSearchSelected,
            onClick = onSearch,
        )

        Spacer(Modifier.weight(1f))

        // Profile Avatar Circle (Matching cinefy-tv.html)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF5C49C), Color(0xFF89543E)),
                    ),
                )
                .border(2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                .cinemaFocusable(
                    onClick = onLibrary,
                    shape = CircleShape,
                    scaleFocused = 1.10f,
                    focusedBorderColor = Color(0xFFFFB16D),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "V",
                color = Color(0xFF23150F),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun TvNavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)

    val activeColor = Color(0xFFFFB579)
    val inactiveColor = Color(0xFF85858B)

    Box(
        modifier = Modifier
            .size(width = 68.dp, height = 64.dp)
            .cinemaFocusable(
                onClick = onClick,
                shape = shape,
                scaleFocused = 1.05f,
                focusedBorderColor = Color(0xFFFFB16D),
                onFocusChanged = { isFocused = it },
            )
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier.background(
                        Brush.linearGradient(
                            listOf(Color(0x3BFF812B), Color(0x14FF812B)),
                        ),
                    )
                } else if (isFocused) {
                    Modifier.background(Color.White.copy(alpha = 0.12f))
                } else {
                    Modifier
                },
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color(0x5CFF9B5C) else Color.Transparent,
                shape = shape,
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
                tint = if (isFocused || isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
                color = if (isFocused || isSelected) activeColor else inactiveColor,
            )
        }
    }
}
