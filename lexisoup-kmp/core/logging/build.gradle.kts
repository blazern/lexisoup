plugins {
    id("blazern.lexisoup.plugin.core")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.core.logging"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
        }
    }
}
