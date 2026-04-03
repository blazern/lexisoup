package blazern.lexisoup.domain.analytics

interface Analytics {
    fun log(event: Event)
}

internal expect fun createAnalytics(): Analytics
