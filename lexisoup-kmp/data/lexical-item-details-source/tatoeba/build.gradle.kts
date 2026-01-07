plugins {
    id("blazern.lexisoup.plugin.library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexical_item_details_source.tatoeba"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.ktor.client.core)

                api(project(":domain:model"))
                api(project(":data:lexical-item-details-source:api"))
                implementation(project(":core:ktor"))
                implementation(project(":core:utils"))
                implementation(project(":domain:backend-address"))
                implementation(project(":data:lexical-item-details-source:utils:cache"))
                implementation(project(":data:lexical-item-details-source:utils:examples-tools"))
            }
        }
    }
}
