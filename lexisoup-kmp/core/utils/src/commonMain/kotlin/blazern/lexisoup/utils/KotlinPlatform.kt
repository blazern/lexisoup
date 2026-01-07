package blazern.lexisoup.utils

enum class KotlinPlatform {
    ANDROID,
    IOS,
    JS,
}

expect fun getKotlinPlatform(): KotlinPlatform
