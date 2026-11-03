package nolelamphim.vercel.app.di

import nolelamphim.vercel.app.data.MovieRepository
import nolelamphim.vercel.app.data.remote.MovieApi
import nolelamphim.vercel.app.data.remote.createPlatformHttpClient

object AppContainer {
    val httpClient by lazy { createPlatformHttpClient() }
    val movieApi by lazy { MovieApi(httpClient) }
    val movieRepository by lazy { MovieRepository(movieApi) }
}
