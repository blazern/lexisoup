plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.aggregation"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain:model"))
            implementation(project(":domain:backend-address"))
            implementation(project(":data:kaikki"))
            implementation(project(":core:ktor"))
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:chatgpt"))
            implementation(project(":data:lexical-item-details-source:kaikki"))
            implementation(project(":data:lexical-item-details-source:panlex"))
            implementation(project(":data:lexical-item-details-source:tatoeba"))
            implementation(project(":data:lexical-item-details-source:wortschatz-leipzig"))
            implementation(project(":data:lexical-item-details-source:utils:cache"))
            implementation(project(":data:lexical-item-details-source:utils:examples-tools"))
        }
    }
}
