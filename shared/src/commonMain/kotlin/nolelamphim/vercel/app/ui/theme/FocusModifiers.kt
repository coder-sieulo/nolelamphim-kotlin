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

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

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

    // Snappy, subtle scale: 1.06x on TV, 1.03x on mobile/desktop
    val effectiveScale = if (scaleFocused == 1.05f && isTv) 1.06f else scaleFocused

    val scale by animateFloatAsState(
        targetValue = if (isFocused) effectiveScale else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = "cinema-focus-scale",
    )

    // Snappy border highlight: instant response on D-pad with zero frame-by-frame recomposition
    val effectiveBorderColor = if (isFocused) focusedBorderColor else Color.White.copy(alpha = 0.08f)
    val currentBorderWidth = if (isFocused) borderWidth else 1.dp
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
            // Draw-phase scale update: 0 recompositions, 60fps hardware accelerated
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
                Modifier
                    .onKeyEvent { keyEvent ->
                        if (enabled && keyEvent.type == KeyEventType.KeyUp &&
                            (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
                        ) {
                            onClick.invoke()
                            true
                        } else {
                            false
                        }
                    }
                    .clickable(
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

