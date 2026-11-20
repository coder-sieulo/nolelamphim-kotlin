package nolelamphim.vercel.app.ui.tv

import androidx.compose.runtime.compositionLocalOf

data class TvNavigationActions(
    val focusNavigation: () -> Unit = {},
    val focusContent: () -> Unit = {},
    val isNavigationFocused: Boolean = false,
)

val LocalTvNavigation = compositionLocalOf { TvNavigationActions() }
