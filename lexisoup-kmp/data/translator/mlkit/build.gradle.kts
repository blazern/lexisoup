plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.translator.mlkit"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data:translator:api"))
        }

        androidMain.dependencies {
            implementation(libs.mlkit)
        }
    }
}
