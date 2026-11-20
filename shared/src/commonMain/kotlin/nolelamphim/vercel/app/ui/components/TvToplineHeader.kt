package nolelamphim.vercel.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nolelamphim.vercel.app.getCurrentTimeString
import nolelamphim.vercel.app.ui.theme.CinemaColors

/** Only information backed by application state is shown in the TV header. */
@Composable
fun TvToplineHeader(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(getCurrentTimeString()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTime = getCurrentTimeString()
        }
    }
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Nolelamphim",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = CinemaColors.TvWhite,
        )
        Text(currentTime, style = MaterialTheme.typography.bodyLarge, color = CinemaColors.TvMuted)
    }
}
