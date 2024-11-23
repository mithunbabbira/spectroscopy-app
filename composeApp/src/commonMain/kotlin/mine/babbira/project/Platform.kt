package mine.babbira.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform