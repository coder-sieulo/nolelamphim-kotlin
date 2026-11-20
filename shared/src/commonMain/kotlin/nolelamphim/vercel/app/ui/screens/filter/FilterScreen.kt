package nolelamphim.vercel.app.ui.screens.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
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
import nolelamphim.vercel.app.ui.tv.TvBrowseButton
import nolelamphim.vercel.app.ui.tv.TvBrowseMessage

private val filterYears = (2026 downTo 1995).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    initialQuery: String? = null,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: FilterViewModel = viewModel(key = "filter_${initialQuery.orEmpty()}") {
        FilterViewModel(
            repository = AppContainer.movieRepository,
            localLibrary = AppContainer.localLibrary,
            initialQuery = initialQuery,
        )
    },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isTv = LocalIsTv.current
    if (isTv) {
        TvExploreScreen(state, viewModel, onMovieClick)
        return
    }
    val horizontalPadding = if (isTv) 28.dp else 16.dp

    val selectedGenreName = state.genres.firstOrNull { it.slug == state.selectedGenre }?.name
    val selectedCountryName = state.countries.firstOrNull { it.slug == state.selectedCountry }?.name

    var showDropdowns by remember {
        mutableStateOf(!isTv || state.results.isEmpty())
    }
    val shouldShowDropdowns = if (isTv) showDropdowns else true

    val activeDropdownFilterCount = (if (state.selectedGenre != null) 1 else 0) +
        (if (state.selectedCountry != null) 1 else 0) +
        (if (state.selectedYear != null) 1 else 0) +
        (if (state.sort != SortOption.DEFAULT) 1 else 0)

    LaunchedEffect(state.results.isEmpty(), state.isLoading) {
        if (state.results.isEmpty() && !state.isLoading) {
            showDropdowns = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTv) Color.Transparent else CinemaColors.Background),
    ) {
        // 1. Top Header with optional TV Filter Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = horizontalPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
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
                        text = "Khám phá",
                        style = if (isTv) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                        color = CinemaColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Lọc phim theo thể loại, quốc gia, năm và sắp xếp",
                        style = MaterialTheme.typography.labelSmall,
                        color = CinemaColors.TextMuted,
                    )
                }
            }

            if (isTv) {
                // TV Filter Toggle Pill (Opens/Closes dropdowns bar on TV)
                val isFilterActive = activeDropdownFilterCount > 0
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (showDropdowns) CinemaColors.Primary.copy(alpha = 0.22f)
                            else if (isFilterActive) CinemaColors.Primary.copy(alpha = 0.12f)
                            else CinemaColors.SurfaceCard,
                        )
                        .border(
                            width = if (showDropdowns || isFilterActive) 1.5.dp else 1.dp,
                            color = if (showDropdowns || isFilterActive) CinemaColors.Primary else CinemaColors.BorderSubtle,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .cinemaFocusable(
                            onClick = { showDropdowns = !showDropdowns },
                            shape = RoundedCornerShape(12.dp),
                        )
                        .bounceClick { showDropdowns = !showDropdowns }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Bộ lọc",
                            tint = if (showDropdowns || isFilterActive) CinemaColors.Primary else Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (isFilterActive) "Bộ lọc ($activeDropdownFilterCount)" else "Bộ lọc",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (showDropdowns || isFilterActive) CinemaColors.Primary else CinemaColors.TextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (showDropdowns) 180f else 0f),
                        )
                    }
                }
            }
        }

        // 3. Dropdown Filters, Badges & Action Buttons (Collapsible on TV)
        AnimatedVisibility(
            visible = shouldShowDropdowns,
            enter = expandVertically(animationSpec = tween(180)) + fadeIn(animationSpec = tween(140)),
            exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
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

                // 4. Active Filter Badges Strip (Quick Dismiss Pills)
                if (state.hasFilters || state.sort != SortOption.DEFAULT) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                                    .clickable {
                                        viewModel.reset()
                                        if (isTv) showDropdowns = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // 5. Action Buttons (Apply & Reset)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            viewModel.apply()
                            if (isTv) showDropdowns = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaColors.Primary,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .cinemaFocusable(
                                onClick = {
                                    viewModel.apply()
                                    if (isTv) showDropdowns = false
                                },
                                shape = RoundedCornerShape(10.dp),
                            ),
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (state.hasFilters) "Áp dụng bộ lọc" else "Khám phá tất cả",
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.reset()
                            if (isTv) showDropdowns = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CinemaColors.TextSecondary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .cinemaFocusable(
                                onClick = {
                                    viewModel.reset()
                                    if (isTv) showDropdowns = true
                                },
                                shape = RoundedCornerShape(10.dp),
                            ),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Đặt lại", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 6. Results Section
        when {
            state.isLoading && state.results.isEmpty() ->
                LoadingView(message = "Đang tải danh sách phim...")

            state.error != null && state.results.isEmpty() -> ErrorView(
                message = state.error.orEmpty(),
                onRetry = viewModel::apply,
            )

            state.results.isEmpty() -> EmptyView(
                message = "Không tìm thấy phim phù hợp với tiêu chí lọc đã chọn",
            )

            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::apply,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (isTv && focusState.hasFocus && state.results.isNotEmpty()) {
                            showDropdowns = false
                        }
                    },
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Results count banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 4.dp),
                    ) {
                        Text(
                            text = if (state.totalItems > 0) "Tìm thấy ${state.totalItems} phim phù hợp" else "Tìm thấy ${state.results.size} phim phù hợp",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaColors.TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    MovieGrid(
                        movies = sortMovies(state.results, state.sort),
                        onMovieClick = onMovieClick,
                        onLoadMore = viewModel::loadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun TvExploreScreen(
    state: FilterUiState,
    viewModel: FilterViewModel,
    onMovieClick: (Movie) -> Unit,
) {
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    var focusResults by rememberSaveable { mutableStateOf(false) }
    val filterRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val criteriaKey = "${state.query}|${state.selectedGenre}|${state.selectedCountry}|${state.selectedYear}|${state.sort}"
    val summary = buildList {
        state.query.takeIf { it.isNotBlank() }?.let { add("Từ khóa: $it") }
        state.genres.firstOrNull { it.slug == state.selectedGenre }?.let { add(it.name) }
        state.countries.firstOrNull { it.slug == state.selectedCountry }?.let { add(it.name) }
        state.selectedYear?.let { add(it.toString()) }
        if (state.sort != SortOption.DEFAULT) add("${state.sort.label} trong danh sách đã tải")
    }.joinToString(" · ").ifBlank { "Tất cả phim · Chọn Bộ lọc để thu gọn danh sách" }
    fun closeFilters() {
        filtersOpen = false
        scope.launch {
            withFrameNanos { }
            filterRequester.requestFocus()
        }
    }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Khám phá", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
                Text(summary, fontSize = 15.sp, lineHeight = 21.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = CinemaColors.TextSecondary)
            }
            if (state.query.isNotBlank()) {
                TvBrowseButton("Xóa từ khóa", viewModel::clearQueryAndApply, navigateUpToMenu = true)
            }
            TvBrowseButton("Bộ lọc", { filtersOpen = true }, Modifier.focusRequester(filterRequester), navigateUpToMenu = true)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading && state.results.isEmpty() -> TvBrowseMessage("Đang tải phim", "Danh sách sẽ xuất hiện tại đây.")
                state.error != null && state.results.isEmpty() -> TvBrowseMessage(
                    "Chưa tải được phim", "Kiểm tra kết nối rồi thử lại.", "Thử lại", viewModel::apply, requestFocus = !filtersOpen,
                )
                state.results.isEmpty() -> TvBrowseMessage(
                    "Không tìm thấy phim phù hợp", "Thử thay đổi thể loại, quốc gia hoặc năm phát hành.",
                    "Sửa bộ lọc", { filtersOpen = true }, requestFocus = !filtersOpen,
                )
                else -> key(criteriaKey) {
                    MovieGrid(
                        movies = state.results,
                        onMovieClick = onMovieClick,
                        modifier = Modifier.fillMaxSize(),
                        onLoadMore = if (state.error == null) viewModel::loadMore else null,
                        focusKey = "explore:$criteriaKey",
                        requestInitialFocus = focusResults && !filtersOpen,
                        topFocusRequester = filterRequester,
                        focusEnabled = !filtersOpen,
                    )
                }
            }
        }
        if (state.error != null && state.results.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa tải thêm được phim", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.weight(1f))
                TvBrowseButton("Thử lại", viewModel::loadMore)
            }
        }
        if (state.isLoadingMore) {
            Text("Đang tải thêm…", fontSize = 15.sp, color = CinemaColors.TextSecondary, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
        }
    }
    if (filtersOpen) {
        TvFilterPanel(
            state = state,
            onDismiss = ::closeFilters,
            onReloadTaxonomy = viewModel::loadTaxonomy,
            onApply = { genre, country, year, sort ->
                focusResults = true
                filtersOpen = false
                viewModel.applyFilters(genre, country, year, sort)
            },
        )
    }
}

private data class TvFilterChoice(val key: String, val label: String)

@Composable
private fun TvFilterPanel(
    state: FilterUiState,
    onDismiss: () -> Unit,
    onReloadTaxonomy: () -> Unit,
    onApply: (String?, String?, Int?, SortOption) -> Unit,
) {
    var genre by remember { mutableStateOf(state.selectedGenre) }
    var country by remember { mutableStateOf(state.selectedCountry) }
    var year by remember { mutableStateOf(state.selectedYear) }
    var sort by remember { mutableStateOf(state.sort) }
    var group by remember { mutableIntStateOf(if (state.query.isBlank()) 0 else 2) }
    val optionRequester = remember { FocusRequester() }
    val selectedKey = when (group) {
        0 -> genre.orEmpty()
        1 -> country.orEmpty()
        2 -> year?.toString().orEmpty()
        else -> sort.name
    }
    val choices = when (group) {
        0 -> listOf(TvFilterChoice("", "Tất cả thể loại")) + state.genres.map { TvFilterChoice(it.slug, it.name) }
        1 -> listOf(TvFilterChoice("", "Tất cả quốc gia")) + state.countries.map { TvFilterChoice(it.slug, it.name) }
        2 -> listOf(TvFilterChoice("", "Tất cả các năm")) + filterYears.map { TvFilterChoice(it.toString(), it.toString()) }
        else -> SortOption.entries.map { TvFilterChoice(it.name, it.label) }
    }
    val initialChoiceKey = selectedKey.takeIf { selected -> choices.any { it.key == selected } } ?: choices.first().key
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth(0.86f).widthIn(max = 840.dp).height(480.dp)
                .background(CinemaColors.SurfaceElevated, RoundedCornerShape(12.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Bộ lọc phim", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CinemaColors.TextPrimary)
            Text(
                if (state.query.isBlank()) "Chọn tiêu chí rồi nhấn Áp dụng. Quay lại để hủy thay đổi."
                else "Lọc năm trong kết quả tìm kiếm. Xóa từ khóa để lọc theo thể loại và quốc gia.",
                fontSize = 15.sp, color = CinemaColors.TextSecondary,
            )
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.width(190.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Thể loại", "Quốc gia", "Năm phát hành", "Sắp xếp đã tải").forEachIndexed { index, label ->
                        TvBrowseButton(label, { group = index }, Modifier.fillMaxWidth(), isSelected = group == index,
                            enabled = state.query.isBlank() || index >= 2)
                    }
                }
                key(group) {
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = choices.indexOfFirst { it.key == initialChoiceKey }.coerceAtLeast(0))
                    LazyColumn(Modifier.weight(1f), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(4.dp)) {
                        items(choices, key = { it.key }) { choice ->
                            TvBrowseButton(
                                text = if (choice.key == selectedKey) "✓ ${choice.label}" else choice.label,
                                onClick = {
                                    when (group) {
                                        0 -> genre = choice.key.ifBlank { null }
                                        1 -> country = choice.key.ifBlank { null }
                                        2 -> year = choice.key.toIntOrNull()
                                        else -> sort = SortOption.valueOf(choice.key)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().then(if (choice.key == initialChoiceKey) Modifier.focusRequester(optionRequester) else Modifier),
                                isSelected = choice.key == selectedKey,
                            )
                        }
                    }
                    LaunchedEffect(Unit) {
                        withFrameNanos { }
                        optionRequester.requestFocus()
                    }
                }
            }
            if (state.taxonomyError != null && group < 2) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Không tải được danh mục lọc", fontSize = 14.sp, color = CinemaColors.TextSecondary, modifier = Modifier.weight(1f))
                    TvBrowseButton("Thử lại", onReloadTaxonomy)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TvBrowseButton("Hủy", onDismiss, Modifier.weight(1f))
                TvBrowseButton("Đặt lại", { genre = null; country = null; year = null; sort = SortOption.DEFAULT }, Modifier.weight(1f))
                TvBrowseButton("Áp dụng", { onApply(genre, country, year, sort) }, Modifier.weight(1f), primary = true)
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasValue) Color.White else CinemaColors.TextSecondary,
                        fontWeight = if (hasValue) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Mở danh sách",
                        tint = if (hasValue) CinemaColors.Primary else CinemaColors.TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation),
                    )
                }
            }
        }

        // The Dropdown Menu
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .widthIn(min = 180.dp, max = 280.dp)
                .heightIn(max = 340.dp)
                .background(Color(0xFF14141E))
                .border(1.dp, CinemaColors.BorderSubtle, RoundedCornerShape(12.dp)),
        ) {
            // "All / Clear" option item
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Tất cả",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!hasValue) CinemaColors.Primary else CinemaColors.TextSecondary,
                        fontWeight = if (!hasValue) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                trailingIcon = {
                    if (!hasValue) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CinemaColors.Primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                },
                colors = MenuDefaults.itemColors(
                    textColor = Color.White,
                ),
                onClick = {
                    onClear()
                    isExpanded = false
                },
            )

            // Dynamic items
            items.forEach { item ->
                val selected = isSelected(item)
                val itemText = itemLabel(item)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected) Color.White else CinemaColors.TextPrimary,
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
                    colors = MenuDefaults.itemColors(
                        textColor = Color.White,
                    ),
                    modifier = Modifier.background(
                        if (selected) CinemaColors.Primary.copy(alpha = 0.12f) else Color.Transparent,
                    ),
                    onClick = {
                        onSelect(item)
                        isExpanded = false
                    },
                )
            }
        }
    }
}

/**
 * Filter Chip pill that can be tapped to immediately remove the filter.
 */
@Composable
private fun ActiveFilterPill(
    label: String,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CinemaColors.Primary.copy(alpha = 0.16f))
            .border(1.dp, CinemaColors.Primary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
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
                contentDescription = "Xóa lọc",
                tint = CinemaColors.Primary,
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
            )
        }
    }
}
