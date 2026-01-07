plugins {
    id("blazern.lexisoup.plugin.library")
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.kaikki"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)

                api(project(":domain:model"))
                implementation(project(":domain:backend-address"))
                implementation(project(":core:ktor"))
                implementation(project(":core:utils"))
            }
        }
    }
}
