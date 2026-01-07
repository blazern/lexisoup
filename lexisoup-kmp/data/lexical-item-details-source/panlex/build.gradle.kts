plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.panlex"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:utils:cache"))
            implementation(project(":core:utils"))
            implementation(project(":domain:model"))
            implementation(project(":data:lexisoup-graphql"))
        }

        commonTest.dependencies {
            implementation(libs.apollo.testing)
            implementation(project(":core:test-utils"))
        }
    }
}
