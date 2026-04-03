import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.feature.search_results"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(project(":core:ui:theme"))
            implementation(project(":core:ui:components"))
            implementation(project(":core:utils"))
            implementation(project(":domain:analytics"))
            implementation(project(":domain:model"))
            implementation(project(":domain:settings"))
            implementation(project(":data:lexical-item-details-source:aggregation"))
            implementation(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:utils:examples-tools"))
            implementation(project(":data:translator:aggregation"))
        }
    }
}