plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.api"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:model"))
        }
    }
}
