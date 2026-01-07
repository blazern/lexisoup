plugins {
    id("blazern.lexisoup.plugin.library")
}

compose.resources {
    publicResClass = true
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.core.ui.strings"
    }
}
