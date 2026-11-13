package nolelamphim.vercel.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import nolelamphim.shared.generated.resources.Res
import nolelamphim.shared.generated.resources.nolelamphim_logo
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String = "Đang tải dữ liệu...",
) {
    val transition = rememberInfiniteTransition(label = "loading-pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-scale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(CinemaColors.Primary.copy(alpha = 0.18f)),
                )
                Image(
                    painter = painterResource(Res.drawable.nolelamphim_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit,
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(68.dp),
                    color = CinemaColors.Primary,
                    strokeWidth = 2.5.dp,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaColors.TextSecondary,
            )
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CinemaColors.Surface)
                .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(16.dp))
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Đã xảy ra lỗi",
                style = MaterialTheme.typography.titleMedium,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = CinemaColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.cinemaFocusable(onClick = onRetry, shape = RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Thử lại", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun EmptyView(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CinemaColors.SurfaceElevated)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.nolelamphim_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .alpha(0.65f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaColors.SurfaceElevated,
                        contentColor = CinemaColors.Primary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cinemaFocusable(onClick = onAction, shape = RoundedCornerShape(10.dp)),
                ) {
                    Text(actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
