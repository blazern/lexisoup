import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val versionNameProvider = providers.gradleProperty("versionName").orElse("0.0.0-dev")
val versionCodeProvider = providers.gradleProperty("versionCode").orElse("1")
val appVersionName = versionNameProvider.get()
val appVersionCode = versionCodeProvider.get().toInt()

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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)
}
