plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.feature.settings"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui:theme"))
            implementation(project(":domain:model"))
            implementation(project(":domain:settings"))
        }
    }
}
