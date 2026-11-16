package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nolelamphim.vercel.app.getCurrentHourOfDay
import nolelamphim.vercel.app.getCurrentTimeString

/**
 * Topline Header for Cinefy TV matching mockup:
 * - Left: "Chào buổi tối, Vien • Sẵn sàng cho buổi xem phim?"
 * - Right: Real-time digital clock, notification status, wifi indicator
 */
@Composable
fun TvToplineHeader(
    modifier: Modifier = Modifier,
) {
    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    val hour = remember { getCurrentHourOfDay() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            currentTime = getCurrentTimeString()
        }
    }

    val greeting = when (hour) {
        in 5..11 -> "Chào buổi sáng,"
        in 12..17 -> "Chào buổi chiều,"
        else -> "Chào buổi tối,"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Welcome Greeting
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = Color(0xFFAAAAAA),
            )
            Text(
                text = "Vien.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "• Sẵn sàng cho buổi xem phim?",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color(0xFFAAAAAA),
            )
        }

        // Status & Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                ),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFDDDCDA),
            )
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFFB8B4B1),
                modifier = Modifier.size(17.dp),
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = Color(0xFFB8B4B1),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
