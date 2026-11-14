package nolelamphim.vercel.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import nolelamphim.vercel.app.di.AppContainer
import nolelamphim.vercel.app.navigation.AppNavHost
import nolelamphim.vercel.app.ui.theme.CinemaTheme
import okio.FileSystem

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(
                    KtorNetworkFetcherFactory(
                        httpClient = { AppContainer.imageHttpClient },
                    ),
                )
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "nolelamphim_img_cache")
                    .maxSizeBytes(50L * 1024 * 1024) // Capped at exactly 50MB (LRU Auto-cleanup)
                    .build()
            }
            .build()
    }

    CinemaTheme {
        AppNavHost()
    }
}
