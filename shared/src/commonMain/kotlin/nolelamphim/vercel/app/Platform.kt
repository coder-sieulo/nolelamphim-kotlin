package nolelamphim.vercel.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform