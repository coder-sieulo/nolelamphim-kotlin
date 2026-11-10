package nolelamphim.vercel.app.ui.screens.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.VideoPlayer
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

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
    val isTv = LocalIsTv.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        when (val s = state) {
            WatchUiState.Loading -> LoadingView(message = "Đang kết nối luồng phát...")

            is WatchUiState.Error -> ErrorView(
                message = s.message,
                onRetry = viewModel::load,
            )

            is WatchUiState.Success -> {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xCC0D0D11))
                        .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                        .padding(horizontal = if (isTv) 28.dp else 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .cinemaFocusable(onClick = onBack, shape = CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        Column {
                            Text(
                                text = s.movieName,
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Text(
                                text = s.episodeName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.Primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .cinemaFocusable(
                                onClick = { uriHandler.openUri(s.embedUrl) },
                                shape = RoundedCornerShape(8.dp),
                            ),
                    ) {
                        IconButton(onClick = { uriHandler.openUri(s.embedUrl) }) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Mở bằng trình duyệt",
                                tint = CinemaColors.TextSecondary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                // Video Player Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    VideoPlayer(
                        url = s.playbackUrl,
                        fallbackUrl = s.embedUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                }

                // Bottom Action & Info Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CinemaColors.Surface)
                        .padding(horizontal = if (isTv) 32.dp else 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = s.episodeName,
                            style = MaterialTheme.typography.titleMedium,
                            color = CinemaColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Đang phát native HLS player",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaColors.TextMuted,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (s.hasNextEpisode) {
                            Button(
                                onClick = onNextEpisode,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinemaColors.Primary,
                                    contentColor = Color.White,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.cinemaFocusable(
                                    onClick = onNextEpisode,
                                    shape = RoundedCornerShape(10.dp),
                                    scaleFocused = 1.08f,
                                ),
                            ) {
                                Text("Tập tiếp theo", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { uriHandler.openUri(s.embedUrl) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CinemaColors.TextSecondary,
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.cinemaFocusable(
                                onClick = { uriHandler.openUri(s.embedUrl) },
                                shape = RoundedCornerShape(10.dp),
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Dự phòng web", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
