package nolelamphim.vercel.app.ui.screens.watch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.VideoPlayer

@Composable
fun WatchScreen(
    slug: String,
    serverIndex: Int,
    episodeIndex: Int,
    onBack: () -> Unit,
    onNextEpisode: () -> Unit,
    viewModel: WatchViewModel = viewModel {
        WatchViewModel(
            repository = AppContainer.movieRepository,
            localLibrary = AppContainer.localLibrary,
            slug = slug,
            serverIndex = serverIndex,
            episodeIndex = episodeIndex,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            TextButton(onClick = onBack) {
                Text("← Quay lại")
            }
        }

        when (val s = state) {
            WatchUiState.Loading -> LoadingView()

            is WatchUiState.Error -> ErrorView(
                message = s.message,
                onRetry = viewModel::load,
            )

            is WatchUiState.Success -> {
                VideoPlayer(
                    url = s.embedUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = s.movieName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = s.episodeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (s.hasNextEpisode) {
                            TextButton(onClick = onNextEpisode) {
                                Text("Tập tiếp theo →")
                            }
                        }
                        TextButton(onClick = { uriHandler.openUri(s.embedUrl) }) {
                            Text("Mở bằng trình duyệt")
                        }
                    }
                }
            }
        }
    }
}
