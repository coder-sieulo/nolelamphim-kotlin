package nolelamphim.vercel.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal indicating if current device is Android TV / 10-foot UI.
 */
val LocalIsTv = compositionLocalOf { false }

/**
 * Reusable modifier for focus & hover interactions that look stunning on both TV (D-pad)
 * and Mobile/Desktop.
 */
@Composable
fun Modifier.cinemaFocusable(
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    scaleFocused: Float = 1.05f,
    borderWidth: Dp = 2.dp,
    focusedBorderColor: Color = Color(0xFFF97316),
    focusedGlowColor: Color = Color(0x66F97316),
    enabled: Boolean = true,
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val isTv = LocalIsTv.current

    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleFocused else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "cinema-focus-scale",
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "cinema-focus-elevation",
    )

    val interactionSource = remember { MutableInteractionSource() }

    return this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            // Bring focused item to the top z-index if focused
            if (isFocused) {
                shadowElevation.toPx()
            }
        }
        .then(
            if (isFocused) {
                Modifier
                    .shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        spotColor = focusedGlowColor,
                        ambientColor = focusedGlowColor,
                    )
                    .border(width = borderWidth, color = focusedBorderColor, shape = shape)
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = shape,
                )
            }
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

