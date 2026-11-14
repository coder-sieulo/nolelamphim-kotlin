package nolelamphim.vercel.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformHttpClient(): HttpClient =
    HttpClient(Darwin) { applyCommonConfig() }

actual fun createImageHttpClient(): HttpClient =
    HttpClient(Darwin) { applyImageConfig() }
