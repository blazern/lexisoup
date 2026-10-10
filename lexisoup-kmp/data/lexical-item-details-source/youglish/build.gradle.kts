plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.youglish"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":data:lexical-item-details-source:api"))
            implementation(libs.ktor.client.core)
        }
    }
}
