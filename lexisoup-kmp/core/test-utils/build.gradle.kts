plugins {
    id("blazern.lexisoup.plugin.feature")
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.a.template.kmp.common"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":data:lexisoup-graphql"))
        }
    }
}
