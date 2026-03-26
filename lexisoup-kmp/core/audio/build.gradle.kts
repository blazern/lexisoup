plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.core.audio"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":domain:model"))
            implementation(libs.ktor.client.core)
        }
    }
}
