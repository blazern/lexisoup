plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.translator.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":domain:model"))
        }
    }
}
