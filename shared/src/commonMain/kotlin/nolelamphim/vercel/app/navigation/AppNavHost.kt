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

    var focusedRailIndex by remember { mutableStateOf<Int?>(null) }
    val isRailExpanded = isTv && focusedRailIndex != null

    val isTopLevelRoute = currentRoute in listOf(Routes.HOME, Routes.SEARCH, Routes.LIBRARY, Routes.FILTER)
    val isFullscreenRoute = currentRoute?.startsWith("watch") == true

    CompositionLocalProvider(LocalIsTv provides isTv) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CinemaColors.Background),
        ) {
            // Main Content Area (Fixed start padding on TV so content never jitters during rail animation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isTv && !isFullscreenRoute) Modifier.padding(start = 84.dp) else Modifier),
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

            // TV Scrim Gradient Overlay when rail expands
            if (isTv && !isFullscreenRoute) {
                AnimatedVisibility(
                    visible = isRailExpanded,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(180)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 84.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    )
                }

                // Floating Expandable TV Side Navigation Rail
                TvSideNavigationRail(
                    currentRoute = currentRoute,
                    isExpanded = isRailExpanded,
                    onItemFocusChanged = { index, hasFocus ->
                        if (hasFocus) {
                            focusedRailIndex = index
                        } else if (focusedRailIndex == index) {
                            focusedRailIndex = null
                        }
                    },
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
    isExpanded: Boolean,
    onItemFocusChanged: (index: Int, hasFocus: Boolean) -> Unit,
    onHome: () -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    onLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val railWidth by animateDpAsState(
        targetValue = if (isExpanded) 226.dp else 84.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "tv-rail-width",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(railWidth)
            .shadow(
                elevation = if (isExpanded) 24.dp else 4.dp,
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black,
            )
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .background(if (isExpanded) Color(0xF7101018) else Color(0xEE0D0D14))
            .border(
                1.dp,
                if (isExpanded) Color.White.copy(alpha = 0.14f) else CinemaColors.BorderSubtle,
                RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            )
            .padding(vertical = 24.dp, horizontal = if (isExpanded) 14.dp else 12.dp),
        horizontalAlignment = if (isExpanded) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Logo Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if (isExpanded) 4.dp else 0.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14141E))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nolelamphim_logo),
                    contentDescription = "Nô Lệ Làm Phim",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(180)) + slideInHorizontally { -20 },
                exit = fadeOut(tween(100)),
            ) {
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "NÔ LỆ LÀM PHIM",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = "Rạp phim tại gia",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Navigation Items
        TvRailItem(
            icon = Icons.Default.Home,
            label = "Trang chủ",
            isSelected = currentRoute == Routes.HOME,
            isExpanded = isExpanded,
            onClick = onHome,
            onFocusChanged = { onItemFocusChanged(0, it) },
        )
        TvRailItem(
            icon = Icons.Default.Search,
            label = "Tìm kiếm",
            isSelected = currentRoute == Routes.SEARCH,
            isExpanded = isExpanded,
            onClick = onSearch,
            onFocusChanged = { onItemFocusChanged(1, it) },
        )
        TvRailItem(
            icon = Icons.Default.Tune,
            label = "Bộ lọc",
            isSelected = currentRoute == Routes.FILTER,
            isExpanded = isExpanded,
            onClick = onFilter,
            onFocusChanged = { onItemFocusChanged(2, it) },
        )
        TvRailItem(
            icon = Icons.Default.Bookmark,
            label = "Thư viện",
            isSelected = currentRoute == Routes.LIBRARY,
            isExpanded = isExpanded,
            onClick = onLibrary,
            onFocusChanged = { onItemFocusChanged(3, it) },
        )
    }
}

@Composable
private fun TvRailItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    val itemWidth by animateDpAsState(
        targetValue = if (isExpanded) 198.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "rail-item-width",
    )

    Box(
        modifier = Modifier
            .width(itemWidth)
            .height(50.dp)
            .clip(shape)
            .background(
                if (isSelected) CinemaColors.PrimaryContainer
                else if (isFocused) Color.White.copy(alpha = 0.08f)
                else Color.Transparent,
            )
            .border(
                width = if (isFocused) 2.dp else (if (isSelected) 1.dp else 0.dp),
                color = if (isFocused) CinemaColors.Primary else (if (isSelected) CinemaColors.Primary.copy(alpha = 0.5f) else Color.Transparent),
                shape = shape,
            )
            .cinemaFocusable(
                onClick = onClick,
                shape = shape,
                scaleFocused = 1.05f,
                onFocusChanged = {
                    isFocused = it
                    onFocusChanged(it)
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isExpanded) 14.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isFocused) Color.White else (if (isSelected) CinemaColors.Primary else CinemaColors.TextSecondary),
                modifier = Modifier.size(22.dp),
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(160)) + slideInHorizontally { -15 },
                exit = fadeOut(tween(80)),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = if (isFocused) Color.White else (if (isSelected) CinemaColors.Primary else CinemaColors.TextSecondary),
                    fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 14.dp),
                )
            }
        }
    }
}
