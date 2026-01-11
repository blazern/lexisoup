import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val versionNameProvider = providers.gradleProperty("versionName").orElse("0.0.0-dev")
val versionCodeProvider = providers.gradleProperty("versionCode").orElse("1")
val appVersionName = versionNameProvider.get()
val appVersionCode = versionCodeProvider.get().toInt()

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
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
            implementation(project(":domain:model"))
            implementation(project(":domain:settings"))
            implementation(project(":data:lexisoup-graphql"))
            implementation(project(":data:suggestions"))
            implementation(project(":data:lexical-item-details-source:aggregation"))

            implementation(project(":feature:home"))
            implementation(project(":feature:search-results"))
            implementation(project(":feature:privacy-policy"))
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "blazern.lexisoup"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        val keystoreFile = providers.gradleProperty("keystoreFile").orNull
        val keystorePassword = providers.gradleProperty("keystorePassword").orNull
        val keystoreKeyAlias = providers.gradleProperty("keystoreKeyAlias").orNull
        val keystoreKeyPassword = providers.gradleProperty("keystoreKeyPassword").orNull

        if (keystoreFile != null) {
            create("signed") {
                storeFile = file(keystoreFile)
                if (keystorePassword != null) storePassword = keystorePassword
                if (keystoreKeyAlias != null) keyAlias = keystoreKeyAlias
                if (keystoreKeyPassword != null) keyPassword = keystoreKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "blazern.lexisoup"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
        signingConfigs.findByName("signed")?.let {
            signingConfig = it
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
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
