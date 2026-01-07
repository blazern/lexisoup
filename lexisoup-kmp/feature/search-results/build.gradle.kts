import org.gradle.kotlin.dsl.kotlin

plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.feature.search_results"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui:theme"))
            implementation(project(":core:ui:components"))
            implementation(project(":core:utils"))
            implementation(project(":domain:model"))
            implementation(project(":data:lexical-item-details-source:aggregation"))
            implementation(project(":data:lexical-item-details-source:api"))
            implementation(project(":data:lexical-item-details-source:utils:examples-tools"))
        }
    }
}
