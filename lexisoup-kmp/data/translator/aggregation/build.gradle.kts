plugins {
    id("blazern.lexisoup.plugin.library")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.translator.aggregation"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":data:translator:api"))
            implementation(project(":core:utils"))
            implementation(project(":data:translator:backend"))
            implementation(project(":data:translator:mlkit"))
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":domain:config:api"))
        }
    }
}
