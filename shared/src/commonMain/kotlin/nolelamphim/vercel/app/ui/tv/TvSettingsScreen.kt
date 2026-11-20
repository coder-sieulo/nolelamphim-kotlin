package nolelamphim.vercel.app.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.theme.CinemaColors

@Composable
fun TvSettingsScreen() {
    val preferences = AppContainer.tvPreferences
    val reducedMotion by preferences.reducedMotion.collectAsStateWithLifecycle()
    val autoplay by preferences.autoplayNextEpisode.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().focusRestorer().focusGroup().verticalScroll(rememberScrollState()).padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        PreferenceRow(
            title = "Giảm chuyển động",
            description = "Giữ focus rõ ràng, chuyển trạng thái ngay và không phóng card.",
            enabled = reducedMotion,
            onToggle = { preferences.setReducedMotion(!reducedMotion) },
        )
        PreferenceRow(
            title = "Tự phát tập tiếp theo",
            description = "Sau khi kết thúc tập, đếm ngược trước khi phát tập tiếp theo. Bạn có thể hủy.",
            enabled = autoplay,
            onToggle = { preferences.setAutoplayNextEpisode(!autoplay) },
        )
        Text("Nolelamphim TV", style = MaterialTheme.typography.titleLarge)
        Text("Tùy chọn được lưu trên thiết bị này.", color = CinemaColors.TvMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PreferenceRow(title: String, description: String, enabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(CinemaColors.TvSurface).padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, color = CinemaColors.TvMuted, style = MaterialTheme.typography.bodyMedium)
        }
        TvActionButton(
            text = if (enabled) "Đang bật" else "Đang tắt",
            onClick = onToggle,
            modifier = Modifier.widthIn(min = 132.dp).semantics { stateDescription = if (enabled) "Bật" else "Tắt" },
        )
    }
}
