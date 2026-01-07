plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.kaikki"
    }

    sourceSets.apply {
        commonMain.dependencies {
            api(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:utils:cache"))
            implementation(project(":data:kaikki"))
            implementation(project(":core:utils"))
            implementation(project(":domain:settings"))
        }
    }
}
