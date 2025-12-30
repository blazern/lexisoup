plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.suggestions"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:model"))
            implementation(project(":data:lexisoup-graphql"))
        }
    }
}
