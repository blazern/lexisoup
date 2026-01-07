plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.model.lexical_item_details_source.chatgpt"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)

            api(project(":data:lexical-item-details-source:api"))
            implementation(project(":domain:model"))
            implementation(project(":data:lexical-item-details-source:utils:cache"))
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":core:utils"))
        }

        commonTest.dependencies {
            implementation(libs.apollo.testing)
            implementation(project(":core:test-utils"))
        }
    }
}
