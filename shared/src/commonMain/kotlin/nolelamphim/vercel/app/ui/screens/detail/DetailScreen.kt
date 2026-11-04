package nolelamphim.vercel.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow

@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (slug: String, serverIndex: Int, episodeIndex: Int) -> Unit,
    onMovieClick: (slug: String) -> Unit,
    viewModel: DetailViewModel = viewModel {
        DetailViewModel(AppContainer.movieRepository, AppContainer.localLibrary, slug)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        DetailUiState.Loading -> LoadingView()

        is DetailUiState.Error -> ErrorView(
            message = s.message,
            onRetry = viewModel::load,
        )

        is DetailUiState.Success -> DetailContent(
            movie = s.movie,
            related = s.related,
            isFavorite = s.isFavorite,
            onBack = onBack,
            onPlay = onPlay,
            onMovieClick = onMovieClick,
            onToggleFavorite = viewModel::toggleFavorite,
        )
    }
}

@Composable
private fun DetailContent(
    movie: MovieDetail,
    related: List<Movie>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlay: (slug: String, serverIndex: Int, episodeIndex: Int) -> Unit,
    onMovieClick: (slug: String) -> Unit,
    onToggleFavorite: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DetailHeader(
                movie = movie,
                isFavorite = isFavorite,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (movie.originName.isNotBlank()) {
                    Text(
                        text = movie.originName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = listOfNotNull(
                        movie.year?.toString(),
                        movie.quality,
                        movie.lang,
                        movie.status,
                        movie.episodeCurrent,
                    ).joinToString("  •  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (movie.rating > 0) {
                    Text(
                        text = "★ ${movie.rating}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        movie.servers.firstOrNull()?.episodes?.firstOrNull()?.let { episode ->
            item {
                Button(
                    onClick = { onPlay(movie.slug, 0, 0) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text("Xem ngay · ${episode.name.ifBlank { "Tập 1" }}")
                }
            }
        }

        if (movie.categories.isNotEmpty()) {
            item {
                ChipRow(
                    title = "Thể loại",
                    labels = movie.categories,
                )
            }
        }

        if (movie.countries.isNotEmpty()) {
            item {
                ChipRow(
                    title = "Quốc gia",
                    labels = movie.countries,
                )
            }
        }

        if (movie.duration != null) {
            item {
                InfoLine(label = "Thời lượng", value = movie.duration)
            }
        }

        if (movie.episodeTotal != null) {
            item {
                InfoLine(
                    label = "Số tập",
                    value = listOfNotNull(movie.episodeCurrent, movie.episodeTotal)
                        .joinToString(" / "),
                )
            }
        }

        if (movie.description.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Nội dung",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (movie.directors.isNotEmpty()) {
            item {
                InfoLine(label = "Đạo diễn", value = movie.directors.joinToString(", "))
            }
        }

        if (movie.actors.isNotEmpty()) {
            item {
                InfoLine(label = "Diễn viên", value = movie.actors.joinToString(", "))
            }
        }

        item {
            Text(
                text = "Tập phim",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (movie.servers.isEmpty()) {
            item {
                Text(
                    text = "Phim chưa có tập phim",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        } else {
            itemsIndexed(movie.servers) { serverIndex, server ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = server.name.ifBlank { "Server ${serverIndex + 1}" },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(
                            items = server.episodes,
                            key = { episodeIndex, episode ->
                                "${serverIndex}_${episode.slug}_$episodeIndex"
                            },
                        ) { episodeIndex, episode ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onPlay(movie.slug, serverIndex, episodeIndex)
                                },
                                label = {
                                    Text(
                                        text = episode.name.ifBlank { "Tập ${episodeIndex + 1}" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

        if (related.isNotEmpty()) {
            item {
                MovieRow(
                    title = "Phim liên quan",
                    movies = related,
                    onMovieClick = { movie -> onMovieClick(movie.slug) },
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(
    movie: MovieDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color.Transparent,
                            Color(0xFF121218),
                        ),
                    ),
                ),
        )
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x66000000)),
        ) {
            Text("← Quay lại")
        }
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color(0x66000000)),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Bỏ yêu thích" else "Yêu thích",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
            )
        }
    }
}

@Composable
private fun ChipRow(
    title: String,
    labels: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(labels) { label ->
                AssistChip(
                    onClick = {},
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}
