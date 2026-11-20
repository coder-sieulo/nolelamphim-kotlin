package nolelamphim.vercel.app.ui.screens.watch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.data.local.PlaybackIdentity
import nolelamphim.vercel.app.data.local.equivalentEpisodeIndex
import nolelamphim.vercel.app.data.local.sameEpisode
import nolelamphim.vercel.app.model.Movie
import nolelamphim.vercel.app.model.MovieDetail
import nolelamphim.vercel.app.model.toMovie

class WatchViewModel(
    private val repository: MovieRepository,
    private val localLibrary: LocalLibrary,
    private val slug: String,
    initialServerIndex: Int,
    initialEpisodeIndex: Int,
    private val preferSavedEpisode: Boolean = false,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WatchUiState>(WatchUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var currentDetail: MovieDetail? = null
    private var currentServerIdx = initialServerIndex
    private var currentEpisodeIdx = initialEpisodeIndex
    private var relatedMovies: List<Movie> = emptyList()
    private var loadJob: Job? = null
    private var generation = 0L
    private var activeIdentity: PlaybackIdentity? = null
    private var latestPositionMs = 0L
    private var latestDurationMs = 0L
    private var lastSavedPositionMs = 0L
    private val savedResume = if (preferSavedEpisode) localLibrary.history.value.firstOrNull { it.slug == slug } else null
    private var pendingResumeIntent = preferSavedEpisode

    init { load() }

    fun load(serverIdx: Int = currentServerIdx, episodeIdx: Int = currentEpisodeIdx, resumePositionMs: Long? = null) {
        saveProgress()
        loadJob?.cancel()
        val sessionId = ++generation
        currentServerIdx = serverIdx
        currentEpisodeIdx = episodeIdx
        activeIdentity = null
        latestPositionMs = 0L
        latestDurationMs = 0L
        lastSavedPositionMs = 0L
        _uiState.value = WatchUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val detail = currentDetail ?: repository.detail(slug).also { currentDetail = it }
                ensureActive()
                var resolvedServer = serverIdx
                var resolvedEpisode = episodeIdx
                var resumeOverride = resumePositionMs
                if (pendingResumeIntent) {
                    val saved = savedResume
                    if (saved != null) {
                        val source = detail.servers.indexOfFirst { it.name.trim().equals(saved.serverName.trim(), ignoreCase = true) }
                        val mapped = detail.servers.getOrNull(source)?.episodes?.indexOfFirst {
                            sameEpisode(it.slug, it.name, saved.episodeSlug, saved.episodeName)
                        } ?: -1
                        if (source < 0 || mapped < 0) {
                            _uiState.value = WatchUiState.Error("Tập đang xem đã thay đổi trên nguồn phát. Hãy chọn lại nguồn và tập phim.", detail)
                            return@launch
                        }
                        resolvedServer = source
                        resolvedEpisode = mapped
                        resumeOverride = saved.positionMs
                        if (saved.durationMs > 0L && saved.positionMs.toDouble() / saved.durationMs >= .95) {
                            val next = detail.servers[source].episodes.withIndex().firstOrNull {
                                it.index > mapped && (it.value.embedUrl.isNotBlank() || !it.value.m3u8Url.isNullOrBlank())
                            }
                            if (next != null) resolvedEpisode = next.index
                            resumeOverride = 0L
                        }
                    } else {
                        detail.servers.withIndex().firstNotNullOfOrNull { (source, server) ->
                            server.episodes.indexOfFirst { it.embedUrl.isNotBlank() || !it.m3u8Url.isNullOrBlank() }
                                .takeIf { it >= 0 }?.let { source to it }
                        }?.let { (source, episode) -> resolvedServer = source; resolvedEpisode = episode }
                    }
                }
                val server = detail.servers.getOrNull(resolvedServer)
                val episode = server?.episodes?.getOrNull(resolvedEpisode)
                if (server == null || episode == null || (episode.embedUrl.isBlank() && episode.m3u8Url.isNullOrBlank())) {
                    _uiState.value = WatchUiState.Error("Tập phim chưa có nguồn phát phù hợp", detail)
                    return@launch
                }
                val playbackUrl = episode.m3u8Url?.takeIf { it.isNotBlank() }
                    ?: repository.resolveStreamUrl(episode.embedUrl) ?: episode.embedUrl
                ensureActive()
                if (sessionId != generation) return@launch
                currentServerIdx = resolvedServer
                currentEpisodeIdx = resolvedEpisode
                val identity = PlaybackIdentity(slug, resolvedServer, server.name, resolvedEpisode, episode.slug, episode.name)
                val savedProgress = localLibrary.getSavedProgress(identity)
                val initialPosition = resumeOverride ?: if (savedProgress?.isCompleted == true) 0L else savedProgress?.positionMs ?: 0L
                activeIdentity = identity
                pendingResumeIntent = false
                latestPositionMs = initialPosition
                latestDurationMs = savedProgress?.durationMs ?: 0L
                lastSavedPositionMs = initialPosition
                localLibrary.recordWatch(detail, resolvedServer, server.name, resolvedEpisode, episode)
                // Reset a replay immediately so Back before the first video frame cannot restore credits.
                if (initialPosition == 0L && savedProgress != null) localLibrary.updateProgress(identity, 0L, savedProgress.durationMs)
                _uiState.value = WatchUiState.Success(
                    movie = detail,
                    currentServerIndex = resolvedServer,
                    currentEpisodeIndex = resolvedEpisode,
                    movieName = detail.name,
                    episodeName = episode.name.ifBlank { "Tập ${resolvedEpisode + 1}" },
                    embedUrl = episode.embedUrl,
                    playbackUrl = playbackUrl,
                    hasNextEpisode = resolvedEpisode + 1 < server.episodes.size,
                    isFavorite = localLibrary.isFavorite(slug),
                    related = relatedMovies,
                    sessionId = sessionId,
                    initialPositionMs = initialPosition,
                )
                // Suggestions cannot delay opening a video.
                if (relatedMovies.isEmpty() && detail.categorySlugs.isNotEmpty()) {
                    try {
                        val related = repository.relatedMovies(slug, detail.categorySlugs.firstOrNull())
                        ensureActive()
                        if (sessionId == generation) {
                            relatedMovies = related
                            val current = _uiState.value as? WatchUiState.Success
                            if (current?.sessionId == sessionId) _uiState.value = current.copy(related = related)
                        }
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { /* optional content */ }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (sessionId == generation) _uiState.value = WatchUiState.Error("Không kết nối được nguồn phát. Hãy thử lại hoặc chọn nguồn khác.", currentDetail)
            }
        }
    }

    fun selectEpisode(serverIdx: Int, episodeIdx: Int) {
        pendingResumeIntent = false
        load(serverIdx, episodeIdx)
    }

    fun selectSource(serverIdx: Int) {
        val state = _uiState.value as? WatchUiState.Success ?: return
        if (serverIdx == state.currentServerIndex) return
        val currentEpisode = state.movie.servers.getOrNull(state.currentServerIndex)?.episodes?.getOrNull(state.currentEpisodeIndex) ?: return
        val target = state.movie.servers.getOrNull(serverIdx) ?: return
        val targetEpisode = equivalentEpisodeIndex(currentEpisode, target)
        if (targetEpisode == null) {
            _uiState.value = state.copy(message = "Nguồn này không có ${state.episodeName}. Hãy chọn tập khác trong danh sách tập.")
        } else load(serverIdx, targetEpisode, latestPositionMs)
    }

    fun clearMessage() {
        val state = _uiState.value as? WatchUiState.Success ?: return
        _uiState.value = state.copy(message = null)
    }

    fun nextEpisode() {
        val state = _uiState.value as? WatchUiState.Success ?: return
        if (state.hasNextEpisode) selectEpisode(state.currentServerIndex, state.currentEpisodeIndex + 1)
    }

    fun toggleFavorite() {
        val detail = currentDetail ?: return
        localLibrary.toggleFavorite(detail.toMovie())
        val state = _uiState.value as? WatchUiState.Success ?: return
        _uiState.value = state.copy(isFavorite = localLibrary.isFavorite(slug))
    }

    fun updateProgress(positionMs: Long, durationMs: Long, sessionId: Long = generation) {
        if (sessionId != generation || activeIdentity == null || positionMs < 0L) return
        latestPositionMs = positionMs
        latestDurationMs = durationMs
        if (kotlin.math.abs(positionMs - lastSavedPositionMs) >= 5000L) saveProgress(sessionId)
    }

    fun saveProgress(sessionId: Long = generation) {
        if (sessionId != generation) return
        val identity = activeIdentity ?: return
        if (latestPositionMs <= 0L && latestDurationMs <= 0L) return
        localLibrary.updateProgress(identity, latestPositionMs, latestDurationMs)
        lastSavedPositionMs = latestPositionMs
    }

    override fun onCleared() {
        saveProgress()
        super.onCleared()
    }
}
