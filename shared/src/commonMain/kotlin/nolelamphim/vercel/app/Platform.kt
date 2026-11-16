package nolelamphim.vercel.app

interface Platform {
    val name: String
    val isTv: Boolean
}

expect fun getPlatform(): Platform

expect fun getCurrentTimeString(): String
expect fun getCurrentHourOfDay(): Int