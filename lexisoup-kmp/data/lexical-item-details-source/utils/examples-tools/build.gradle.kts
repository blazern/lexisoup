plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(project(":domain:model"))
            implementation(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:kaikki"))
        }
    }
}
