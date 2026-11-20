package nolelamphim.vercel.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * CompositionLocal indicating if current device is Android TV / 10-foot UI.
 */
val LocalIsTv = compositionLocalOf { false }

/** The TV experience starts with decorative movement disabled. */
val LocalTvReducedMotion = compositionLocalOf { true }

/**
 * One activation target for touch and remote input. TV focus keeps a stable outline.
 */
@Composable
fun Modifier.cinemaFocusable(
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    scaleFocused: Float = 1.05f,
    borderWidth: Dp = 2.dp,
    focusedBorderColor: Color = Color(0xFFFFB16D),
    focusedGlowColor: Color = Color(0x66FF812B),
    enabled: Boolean = true,
    onFocusChanged: ((Boolean) -> Unit)? = null,
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val isTv = LocalIsTv.current

    val reducedMotion = LocalTvReducedMotion.current
    val effectiveScale = when {
        isTv && reducedMotion -> 1f
        isTv -> scaleFocused.coerceIn(1f, 1.02f)
        else -> scaleFocused
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) effectiveScale else 1f,
        animationSpec = tween(durationMillis = if (isTv && reducedMotion) 0 else 120, easing = FastOutSlowInEasing),
        label = "cinema-focus-scale",
    )

    val focusColor = if (isTv && focusedBorderColor == Color(0xFFFFB16D)) CinemaColors.TvWhite else focusedBorderColor
    val effectiveBorderColor = when {
        !enabled -> Color.Transparent
        isFocused -> focusColor
        isTv -> Color.Transparent
        else -> Color.White.copy(alpha = 0.08f)
    }
    val currentBorderWidth = if (isTv) 3.dp else if (isFocused) borderWidth else 1.dp
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .onFocusChanged { focusState ->
            val nowFocused = focusState.isFocused
            if (isFocused != nowFocused) {
                isFocused = nowFocused
                onFocusChanged?.invoke(nowFocused)
            }
        }
        .zIndex(if (isFocused) 10f else 1f)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

        .border(
            width = currentBorderWidth,
            color = effectiveBorderColor,
            shape = shape,
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier.focusable(enabled = enabled, interactionSource = interactionSource)
            }
        )
}

/**
 * Fast, lightweight scale feedback on touch press for Mobile/Desktop cards
 */
@Composable
fun Modifier.bounceClick(
    scaleDown: Float = 0.97f,
    onClick: () -> Unit,
): Modifier {
    if (LocalIsTv.current) return cinemaFocusable(onClick = onClick, scaleFocused = 1f)
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing),
        label = "bounce-scale",
    )
    val interactionSource = remember { MutableInteractionSource() }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
}

