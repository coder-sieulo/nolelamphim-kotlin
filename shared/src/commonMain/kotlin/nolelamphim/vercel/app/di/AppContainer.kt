package nolelamphim.vercel.app.di

import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.local.LocalLibrary
import nolelamphim.vercel.app.data.local.createKeyValueStore
import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.MultiSourceService
import nolelamphim.vercel.app.data.remote.createPlatformHttpClient

object AppContainer {
    val httpClient by lazy { createPlatformHttpClient() }
    val movieApi by lazy { MovieApi(httpClient) }
    val multiSourceService by lazy { MultiSourceService(httpClient) }
    val movieRepository by lazy { MovieRepository(movieApi, multiSourceService) }
    val localLibrary by lazy { LocalLibrary(createKeyValueStore()) }
}
