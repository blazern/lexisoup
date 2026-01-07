plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.core.ui.components"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":core:ui:theme"))
            }
        }
    }
}
