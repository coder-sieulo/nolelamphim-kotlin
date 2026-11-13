package nolelamphim.vercel.app.ui.screens.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nolelamphim.vercel.app.data.Taxonomy
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.ui.components.EmptyView
import nolelamphim.vercel.app.ui.components.ErrorView
import nolelamphim.vercel.app.ui.components.LoadingView
import nolelamphim.vercel.app.ui.components.MovieGrid
import nolelamphim.vercel.app.ui.theme.CinemaColors
import nolelamphim.vercel.app.ui.theme.LocalIsTv
import nolelamphim.vercel.app.ui.theme.bounceClick
import nolelamphim.vercel.app.ui.theme.cinemaFocusable

private val filterYears = (2026 downTo 1995).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: FilterViewModel = viewModel {
        FilterViewModel(AppContainer.movieRepository)
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    val selectedGenreName = state.genres.firstOrNull { it.slug == state.selectedGenre }?.name
    val selectedCountryName = state.countries.firstOrNull { it.slug == state.selectedCountry }?.name

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaColors.Background),
    ) {
        // 1. Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CinemaColors.SurfaceCard)
                    .border(1.dp, CinemaColors.BorderSubtle, CircleShape)
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
                    text = "Bộ lọc phim",
                    style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    color = CinemaColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Khám phá phim theo thể loại, quốc gia, năm và sắp xếp",
                    style = MaterialTheme.typography.labelSmall,
                    color = CinemaColors.TextMuted,
                )
            }
        }

        // 2. Dropdown Filter Section (Responsive: Row for TV, 2x2 Grid for Mobile)
        if (isTv) {
            // TV: 4 dropdowns side by side in a single sleek toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CinemaDropdownField(
                    label = "Thể loại",
                    selectedText = selectedGenreName ?: "Tất cả thể loại",
                    hasValue = state.selectedGenre != null,
                    items = state.genres,
                    itemLabel = { it.name },
                    isSelected = { it.slug == state.selectedGenre },
                    onSelect = { viewModel.selectGenre(it.slug) },
                    onClear = { viewModel.selectGenre(null) },
                    modifier = Modifier.weight(1f),
                )

                CinemaDropdownField(
                    label = "Quốc gia",
                    selectedText = selectedCountryName ?: "Tất cả quốc gia",
                    hasValue = state.selectedCountry != null,
                    items = state.countries,
                    itemLabel = { it.name },
                    isSelected = { it.slug == state.selectedCountry },
                    onSelect = { viewModel.selectCountry(it.slug) },
                    onClear = { viewModel.selectCountry(null) },
                    modifier = Modifier.weight(1f),
                )

                CinemaDropdownField(
                    label = "Năm phát hành",
                    selectedText = state.selectedYear?.toString() ?: "Tất cả các năm",
                    hasValue = state.selectedYear != null,
                    items = filterYears,
                    itemLabel = { it.toString() },
                    isSelected = { it == state.selectedYear },
                    onSelect = { viewModel.selectYear(it) },
                    onClear = { viewModel.selectYear(null) },
                    modifier = Modifier.weight(1f),
                )

                CinemaDropdownField(
                    label = "Sắp xếp theo",
                    selectedText = state.sort.label,
                    hasValue = state.sort != SortOption.DEFAULT,
                    items = SortOption.entries,
                    itemLabel = { it.label },
                    isSelected = { it == state.sort },
                    onSelect = { viewModel.selectSort(it) },
                    onClear = { viewModel.selectSort(SortOption.DEFAULT) },
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            // Mobile: 2x2 grid of modern cinema dropdown cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CinemaDropdownField(
                        label = "Thể loại",
                        selectedText = selectedGenreName ?: "Tất cả thể loại",
                        hasValue = state.selectedGenre != null,
                        items = state.genres,
                        itemLabel = { it.name },
                        isSelected = { it.slug == state.selectedGenre },
                        onSelect = { viewModel.selectGenre(it.slug) },
                        onClear = { viewModel.selectGenre(null) },
                        modifier = Modifier.weight(1f),
                    )

                    CinemaDropdownField(
                        label = "Quốc gia",
                        selectedText = selectedCountryName ?: "Tất cả quốc gia",
                        hasValue = state.selectedCountry != null,
                        items = state.countries,
                        itemLabel = { it.name },
                        isSelected = { it.slug == state.selectedCountry },
                        onSelect = { viewModel.selectCountry(it.slug) },
                        onClear = { viewModel.selectCountry(null) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CinemaDropdownField(
                        label = "Năm phát hành",
                        selectedText = state.selectedYear?.toString() ?: "Tất cả các năm",
                        hasValue = state.selectedYear != null,
                        items = filterYears,
                        itemLabel = { it.toString() },
                        isSelected = { it == state.selectedYear },
                        onSelect = { viewModel.selectYear(it) },
                        onClear = { viewModel.selectYear(null) },
                        modifier = Modifier.weight(1f),
                    )

                    CinemaDropdownField(
                        label = "Sắp xếp theo",
                        selectedText = state.sort.label,
                        hasValue = state.sort != SortOption.DEFAULT,
                        items = SortOption.entries,
                        itemLabel = { it.label },
                        isSelected = { it == state.sort },
                        onSelect = { viewModel.selectSort(it) },
                        onClear = { viewModel.selectSort(SortOption.DEFAULT) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 3. Active Filter Badges Strip (Quick Dismiss Pills)
        if (state.hasFilters || state.sort != SortOption.DEFAULT) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedGenreName != null) {
                    item {
                        ActiveFilterPill(
                            label = selectedGenreName,
                            onRemove = { viewModel.selectGenre(null) },
                        )
                    }
                }
                if (selectedCountryName != null) {
                    item {
                        ActiveFilterPill(
                            label = selectedCountryName,
                            onRemove = { viewModel.selectCountry(null) },
                        )
                    }
                }
                if (state.selectedYear != null) {
                    item {
                        ActiveFilterPill(
                            label = "Năm ${state.selectedYear}",
                            onRemove = { viewModel.selectYear(null) },
                        )
                    }
                }
                if (state.sort != SortOption.DEFAULT) {
                    item {
                        ActiveFilterPill(
                            label = state.sort.label,
                            onRemove = { viewModel.selectSort(SortOption.DEFAULT) },
                        )
                    }
                }
                item {
                    Text(
                        text = "Xóa tất cả",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.reset() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        // 4. Action Buttons (Apply & Reset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = viewModel::apply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaColors.Primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .cinemaFocusable(onClick = viewModel::apply, shape = RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (state.hasFilters) "Áp dụng bộ lọc" else "Khám phá tất cả",
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedButton(
                onClick = viewModel::reset,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CinemaColors.TextSecondary,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(44.dp)
                    .cinemaFocusable(onClick = viewModel::reset, shape = RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Đặt lại", fontWeight = FontWeight.SemiBold)
            }
        }

        // 5. Results Section
        when {
            state.isLoading && state.results.isEmpty() ->
                LoadingView(message = "Đang áp dụng bộ lọc...")

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::apply,
            )

            state.results.isEmpty() -> EmptyView(
                message = "Chọn tiêu chí trong các menu dropdown bên trên rồi bấm Áp dụng bộ lọc",
            )

            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::apply,
                modifier = Modifier.weight(1f),
            ) {
                MovieGrid(
                    movies = sortMovies(state.results, state.sort),
                    onMovieClick = onMovieClick,
                    onLoadMore = viewModel::loadMore,
                )
            }
        }
    }
}

/**
 * Modern Cinema Dropdown Field with popover menu, rotating chevron, and clear state.
 */
@Composable
private fun <T> CinemaDropdownField(
    label: String,
    selectedText: String,
    hasValue: Boolean,
    items: List<T>,
    itemLabel: (T) -> String,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Box(modifier = modifier) {
        // Trigger Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CinemaColors.SurfaceCard)
                .border(
                    width = if (hasValue) 1.5.dp else 1.dp,
                    color = if (hasValue) CinemaColors.Primary else CinemaColors.BorderSubtle,
                    shape = RoundedCornerShape(12.dp),
                )
                .cinemaFocusable(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(12.dp),
                    scaleFocused = 1.05f,
                )
                .bounceClick { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                    color = if (hasValue) CinemaColors.Primary else CinemaColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (hasValue) Color.White else CinemaColors.TextSecondary,
                        fontWeight = if (hasValue) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Mở danh sách $label",
                        tint = if (hasValue) CinemaColors.Primary else CinemaColors.TextMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                    )
                }
            }
        }

        // Popover Dropdown Menu
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .heightIn(max = 340.dp)
                .widthIn(min = 180.dp)
                .background(Color(0xFF14141E))
                .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp)),
        ) {
            // "Tất cả" (Default / Clear) Option
            val isDefault = !hasValue
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Tất cả",
                        color = if (isDefault) CinemaColors.Primary else Color.White,
                        fontWeight = if (isDefault) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                trailingIcon = {
                    if (isDefault) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CinemaColors.Primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                },
                onClick = {
                    onClear()
                    isExpanded = false
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                ),
            )

            // Dynamic items
            items.forEach { item ->
                val selected = isSelected(item)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(item),
                            color = if (selected) CinemaColors.Primary else Color.White,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    trailingIcon = {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CinemaColors.Primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },
                    onClick = {
                        onSelect(item)
                        isExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White,
                    ),
                )
            }
        }
    }
}

/**
 * Chip pill to quickly display and dismiss an active filter.
 */
@Composable
private fun ActiveFilterPill(
    label: String,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CinemaColors.PrimaryContainer)
            .border(1.dp, CinemaColors.Primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable(onClick = onRemove)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Xóa bộ lọc $label",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(13.dp),
            )
        }
    }
}
