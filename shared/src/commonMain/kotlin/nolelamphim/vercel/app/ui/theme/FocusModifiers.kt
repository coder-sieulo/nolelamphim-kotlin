package nolelamphim.vercel.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.draw.shadow
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

/**
 * Reusable modifier for focus & hover interactions that look stunning on both TV (D-pad)
 * and Mobile/Desktop, featuring bouncy spring physics and glowing neon borders.
 */
@Composable
fun Modifier.cinemaFocusable(
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    scaleFocused: Float = 1.05f,
    borderWidth: Dp = 2.dp,
    focusedBorderColor: Color = Color(0xFFF97316),
    focusedGlowColor: Color = Color(0x80F97316),
    enabled: Boolean = true,
    onFocusChanged: ((Boolean) -> Unit)? = null,
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val isTv = LocalIsTv.current

    // On TV, if caller didn't customize scaleFocused, default to a bold 1.13x zoom so it pops from 3 meters away
    val effectiveScale = if (scaleFocused == 1.05f && isTv) 1.13f else scaleFocused

    val scale by animateFloatAsState(
        targetValue = if (isFocused) effectiveScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cinema-focus-scale",
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) (if (isTv) 20.dp else 12.dp) else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cinema-focus-elevation",
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) focusedBorderColor else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(durationMillis = 220),
        label = "cinema-focus-border-color",
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isFocused) (if (isTv) (borderWidth + 0.5.dp) else borderWidth) else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "cinema-focus-border-width",
    )

    val interactionSource = remember { MutableInteractionSource() }

    return this
        .onFocusChanged { focusState ->
            val nowFocused = focusState.isFocused
            if (isFocused != nowFocused) {
                isFocused = nowFocused
                onFocusChanged?.invoke(nowFocused)
            }
        }
        .zIndex(if (isFocused) 30f else 1f)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = shadowElevation,
            shape = shape,
            spotColor = focusedGlowColor,
            ambientColor = focusedGlowColor,
            clip = false,
        )
        .border(
            width = animatedBorderWidth,
            color = animatedBorderColor,
            shape = shape,
        )
        .focusable(enabled = enabled, interactionSource = interactionSource)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier
            }
        )
}

/**
 * Smooth spring-like scale bounce on touch press for Mobile/Desktop cards
 */
@Composable
fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    onClick: () -> Unit,
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
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

