package nolelamphim.vercel.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.data.local.WatchEntry
import nolelamphim.vercel.app.data.local.toMovie
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.MovieGrid
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

@Composable
fun LibraryScreen(
    onMovieClick: (String) -> Unit,
    onWatchClick: (WatchEntry) -> Unit,
    onExploreClick: (() -> Unit)? = null,
    viewModel: LibraryViewModel = viewModel {
        LibraryViewModel(AppContainer.localLibrary)
    },
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val isTv = LocalIsTv.current
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        // Library Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Thư viện cá nhân",
                style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            // Segmented Pills
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CinemaColors.SurfaceCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LibraryTabPill(
                    label = "Yêu thích",
                    count = favorites.size,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                )
                LibraryTabPill(
                    label = "Lịch sử xem",
                    count = history.size,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                )
            }
        }

        // Tab Content
        if (selectedTab == 0) {
            if (favorites.isEmpty()) {
                EmptyView(
                    message = "Chưa có phim trong danh sách yêu thích.\nNhấn nút ♥ trên bất kỳ trang chi tiết phim nào để lưu lại.",
                    actionLabel = "Khám phá phim ngay",
                    onAction = onExploreClick,
                )
            } else {
                MovieGrid(
                    movies = favorites.map { it.toMovie() },
                    onMovieClick = { movie -> onMovieClick(movie.slug) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            if (history.isEmpty()) {
                EmptyView(
                    message = "Chưa có lịch sử xem phim.\nNhững bộ phim bạn đã xem sẽ tự động lưu tại đây.",
                    actionLabel = "Bắt đầu xem phim",
                    onAction = onExploreClick,
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 6.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = viewModel::clearHistory) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = CinemaColors.TextMuted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Xoá toàn bộ lịch sử",
                                style = MaterialTheme.typography.labelSmall,
                                color = CinemaColors.TextMuted,
                            )
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = horizontalPadding,
                            end = horizontalPadding,
                            bottom = if (isTv) 32.dp else 80.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = history,
                            key = { "${it.slug}-${it.serverIndex}-${it.episodeIndex}" },
                        ) { entry ->
                            HistoryCard(entry = entry, onClick = { onWatchClick(entry) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CinemaColors.Primary else Color.Transparent)
            .cinemaFocusable(onClick = onClick, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f)
                        else CinemaColors.SurfaceElevated,
                    )
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) Color.White else CinemaColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: WatchEntry,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CinemaColors.SurfaceCard)
            .border(1.dp, CinemaColors.BorderSubtle, cardShape)
            .cinemaFocusable(onClick = onClick, shape = cardShape, scaleFocused = 1.03f)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(CinemaColors.SurfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = entry.posterUrl,
                contentDescription = entry.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = CinemaColors.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleSmall,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CinemaColors.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = entry.episodeName.ifBlank { "Tập ${entry.episodeIndex + 1}" },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = entry.serverName.ifBlank { "Đã lưu vị trí" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = CinemaColors.TextMuted,
                )
            }
        }
    }
}
