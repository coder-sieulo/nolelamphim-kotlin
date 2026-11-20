package nolelamphim.vercel.app.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

/** A single remote focus target; never wrap another clickable control in this. */
@Composable
fun TvBrowseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    primary: Boolean = false,
    enabled: Boolean = true,
    navigateUpToMenu: Boolean = false,
) {
    val shape = RoundedCornerShape(8.dp)
    val navigation = LocalTvNavigation.current
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(
                when {
                    primary -> CinemaColors.Primary
                    isSelected -> Color(0xFF303844)
                    else -> CinemaColors.SurfaceCard
                },
                shape,
            )
            .semantics {
                role = Role.Button
                selected = isSelected
            }
            .onPreviewKeyEvent {
                if (navigateUpToMenu && it.type == KeyEventType.KeyDown && it.key == Key.DirectionUp) {
                    navigation.focusNavigation()
                    true
                } else false
            }
            .cinemaFocusable(onClick = onClick, shape = shape, scaleFocused = 1f, enabled = enabled)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = if (!enabled) CinemaColors.TextMuted else if (primary) Color(0xFF211108) else CinemaColors.TextPrimary,
        )
    }
}

@Composable
fun TvBrowseMessage(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    requestFocus: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val actionRequester = remember { FocusRequester() }
    val navigation = LocalTvNavigation.current
    var focusRequested by remember { mutableStateOf(false) }
    LaunchedEffect(requestFocus, actionLabel, navigation.isNavigationFocused) {
        if (requestFocus && !focusRequested && !navigation.isNavigationFocused && actionLabel != null && onAction != null) {
            withFrameNanos { }
            actionRequester.requestFocus()
            focusRequested = true
        }
    }
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
        Text(message, fontSize = 16.sp, lineHeight = 24.sp, color = CinemaColors.TextSecondary)
        if (actionLabel != null && onAction != null) {
            TvBrowseButton(actionLabel, onAction, Modifier.focusRequester(actionRequester), primary = true)
        }
    }
}

/** Native Dialog owns focus and consumes Back. Cancel receives initial focus. */
@Composable
fun TvBrowseConfirmation(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val cancelRequester = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp)
                .background(CinemaColors.SurfaceElevated, RoundedCornerShape(12.dp))
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
            Text(message, fontSize = 16.sp, lineHeight = 24.sp, color = CinemaColors.TextSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvBrowseButton("Hủy", onDismiss, Modifier.weight(1f).focusRequester(cancelRequester))
                TvBrowseButton(confirmLabel, onConfirm, Modifier.weight(1f))
            }
        }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            cancelRequester.requestFocus()
        }
    }
}
