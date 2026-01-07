plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.domain.model"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:ui:strings"))
            }
        }
    }
}
