plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.translator.backend"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":data:translator:api"))
            implementation(project(":domain:config:api"))
        }
    }
}
