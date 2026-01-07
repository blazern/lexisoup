plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.domain.backend_address"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:settings"))
        }
    }
}
