plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.domain.analytics"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:logging"))
            implementation(project(":domain:model"))
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
        }
    }
}
