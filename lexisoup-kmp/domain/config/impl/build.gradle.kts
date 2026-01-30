plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.domain.config.impl"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":domain:config:api"))
            implementation(project(":data:lexisoup-graphql"))
        }
    }
}
