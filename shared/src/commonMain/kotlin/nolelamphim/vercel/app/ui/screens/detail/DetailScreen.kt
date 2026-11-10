package nolelamphim.vercel.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.ui.components.DetailSkeletonScreen
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieRow
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

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
        DetailUiState.Loading -> DetailSkeletonScreen()

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
    val isTv = LocalIsTv.current
    var selectedServerIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
        contentPadding = PaddingValues(bottom = if (isTv) 32.dp else 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Hero / Header Section
        item {
            if (isTv) {
                TvDetailHeader(
                    movie = movie,
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onPlayFirst = { onPlay(movie.slug, 0, 0) },
                    onToggleFavorite = onToggleFavorite,
                )
            } else {
                MobileDetailHeader(
                    movie = movie,
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onPlayFirst = { onPlay(movie.slug, 0, 0) },
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }

        // Synopsis Section
        if (movie.description.isNotBlank()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = if (isTv) 32.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionTitle(title = "Nội dung phim")
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CinemaColors.TextSecondary,
                        lineHeight = 22.sp,
                    )
                }
            }
        }

        // Metadata grid (Directors, Actors, Country)
        item {
            MetadataSection(movie = movie, isTv = isTv)
        }

        // Episodes Selector
        item {
            EpisodeSelectorSection(
                movie = movie,
                selectedServerIndex = selectedServerIndex,
                onSelectServer = { selectedServerIndex = it },
                onSelectEpisode = { serverIdx, epIdx ->
                    onPlay(movie.slug, serverIdx, epIdx)
                },
                isTv = isTv,
            )
        }

        // Related Movies
        if (related.isNotEmpty()) {
            item {
                MovieRow(
                    title = "Có thể bạn muốn xem",
                    movies = related,
                    onMovieClick = { onMovieClick(it.slug) },
                )
            }
        }
    }
}

@Composable
private fun MobileDetailHeader(
    movie: MovieDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlayFirst: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(410.dp),
    ) {
        // Backdrop poster
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Gradient blend (Top vignette + bottom cinema dark)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.75f),
                        0.25f to Color.Transparent,
                        0.55f to Color.Black.copy(alpha = 0.5f),
                        0.85f to CinemaColors.Background.copy(alpha = 0.95f),
                        1f to CinemaColors.Background,
                    ),
                ),
        )

        // Top action bar (Back, Favorite) safely below tai thỏ / Dynamic Island
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) CinemaColors.Primary else Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // Bottom title & info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CinemaColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Badges row
            BadgeRow(movie = movie)

            Spacer(Modifier.height(6.dp))

            // Big CTA Play button
            Button(
                onClick = onPlayFirst,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .cinemaFocusable(onClick = onPlayFirst, shape = RoundedCornerShape(14.dp)),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Xem ngay ${movie.episodeCurrent?.let { "· $it" } ?: ""}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun TvDetailHeader(
    movie: MovieDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlayFirst: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Floating poster card
        Box(
            modifier = Modifier
                .width(220.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Details column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.headlineLarge,
                color = CinemaColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
            )

            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.titleMedium,
                    color = CinemaColors.TextSecondary,
                )
            }

            BadgeRow(movie = movie)

            // CTA Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onPlayFirst,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaColors.Primary,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cinemaFocusable(onClick = onPlayFirst, shape = RoundedCornerShape(10.dp)),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Xem ngay", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cinemaFocusable(onClick = onToggleFavorite, shape = RoundedCornerShape(10.dp)),
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) CinemaColors.Primary else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isFavorite) "Đã yêu thích" else "Thêm yêu thích", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaColors.TextSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.cinemaFocusable(onClick = onBack, shape = RoundedCornerShape(10.dp)),
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Quay lại", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BadgeRow(movie: MovieDetail) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (movie.rating > 0) {
            PillBadge(
                text = "★ ${((movie.rating * 10).toInt() / 10.0)}",
                bgColor = Color(0xE61E1B18),
                textColor = CinemaColors.Gold,
                borderColor = CinemaColors.Gold.copy(alpha = 0.35f),
                isBold = true,
            )
        }
        movie.year?.let {
            PillBadge(text = it.toString())
        }
        movie.quality?.takeIf { it.isNotBlank() }?.let {
            PillBadge(
                text = it,
                bgColor = CinemaColors.BlueBadge.copy(alpha = 0.15f),
                textColor = CinemaColors.BlueBadge,
                borderColor = CinemaColors.BlueBadge.copy(alpha = 0.35f),
                isBold = true,
            )
        }
        movie.lang?.takeIf { it.isNotBlank() }?.let {
            PillBadge(
                text = it,
                bgColor = CinemaColors.GreenBadge.copy(alpha = 0.15f),
                textColor = CinemaColors.GreenBadge,
                borderColor = CinemaColors.GreenBadge.copy(alpha = 0.35f),
            )
        }
        movie.duration?.takeIf { it.isNotBlank() }?.let {
            PillBadge(text = it)
        }
        movie.episodeCurrent?.takeIf { it.isNotBlank() }?.let {
            PillBadge(text = it)
        }
    }
}

@Composable
private fun PillBadge(
    text: String,
    bgColor: Color = Color.White.copy(alpha = 0.08f),
    textColor: Color = CinemaColors.TextPrimary,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    isBold: Boolean = false,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun MetadataSection(movie: MovieDetail, isTv: Boolean) {
    val padding = if (isTv) 32.dp else 16.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (movie.categories.isNotEmpty()) {
            MetaItem(label = "Thể loại", value = movie.categories.joinToString(", "))
        }
        if (movie.countries.isNotEmpty()) {
            MetaItem(label = "Quốc gia", value = movie.countries.joinToString(", "))
        }
        if (movie.directors.isNotEmpty()) {
            MetaItem(label = "Đạo diễn", value = movie.directors.joinToString(", "))
        }
        if (movie.actors.isNotEmpty()) {
            MetaItem(label = "Diễn viên", value = movie.actors.joinToString(", "))
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = CinemaColors.TextMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(85.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = CinemaColors.TextSecondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EpisodeSelectorSection(
    movie: MovieDetail,
    selectedServerIndex: Int,
    onSelectServer: (Int) -> Unit,
    onSelectEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
    isTv: Boolean,
) {
    val horizontalPadding = if (isTv) 32.dp else 16.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
            SectionTitle(title = "Danh sách tập phim")
        }

        if (movie.servers.isEmpty()) {
            Text(
                text = "Phim chưa có tập phát sóng",
                style = MaterialTheme.typography.bodyMedium,
                color = CinemaColors.TextMuted,
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
            return
        }

        // Server switcher chips
        if (movie.servers.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(movie.servers) { idx, server ->
                    val isSelected = idx == selectedServerIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CinemaColors.Primary else CinemaColors.SurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) CinemaColors.Primary else CinemaColors.BorderSubtle,
                                RoundedCornerShape(8.dp),
                            )
                            .cinemaFocusable(
                                onClick = { onSelectServer(idx) },
                                shape = RoundedCornerShape(8.dp),
                                scaleFocused = 1.05f,
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = server.name.ifBlank { "Server ${idx + 1}" },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else CinemaColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // Episodes Grid / Row
        val currentServer = movie.servers.getOrNull(selectedServerIndex) ?: movie.servers.first()
        val episodes = currentServer.episodes

        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(
                items = episodes,
                key = { epIdx, ep -> "${selectedServerIndex}_${ep.slug}_$epIdx" },
            ) { epIdx, ep ->
                val epTitle = ep.name.ifBlank { "${epIdx + 1}" }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CinemaColors.SurfaceCard)
                        .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(8.dp))
                        .cinemaFocusable(
                            onClick = { onSelectEpisode(selectedServerIndex, epIdx) },
                            shape = RoundedCornerShape(8.dp),
                            scaleFocused = 1.08f,
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (epTitle.startsWith("Tập", ignoreCase = true)) epTitle else "Tập $epTitle",
                        style = MaterialTheme.typography.labelMedium,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CinemaColors.Primary),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CinemaColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}
