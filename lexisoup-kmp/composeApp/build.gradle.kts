import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.jetpack.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            implementation(project(":a-template-kmp-common"))
            implementation(project(":core:ui:strings"))
            implementation(project(":core:ui:theme"))
            implementation(project(":core:ktor"))
            implementation(project(":domain:backend-address"))
            implementation(project(":domain:config:impl"))
            implementation(project(":domain:model"))
            implementation(project(":domain:settings"))
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":data:suggestions"))
            implementation(project(":data:lexical-item-details-source:aggregation"))
            implementation(project(":data:translator:aggregation"))

            implementation(project(":feature:home"))
            implementation(project(":feature:privacy-policy"))
            implementation(project(":feature:search-results"))
            implementation(project(":feature:settings"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.desktop {
    application {
        mainClass = "blazern.lexisoup.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "blazern.lexisoup"
            packageVersion = "1.0.0"
        }
    }
}
