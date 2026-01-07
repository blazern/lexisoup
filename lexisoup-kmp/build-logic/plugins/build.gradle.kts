plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin.api)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradle.plugin)

    implementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins {
        create("corePlugin") {
            id = "blazern.lexisoup.plugin.core"
            implementationClass = "CorePlugin"
        }
        create("libraryPlugin") {
            id = "blazern.lexisoup.plugin.library"
            implementationClass = "LibraryPlugin"
        }
        create("featurePlugin") {
            id = "blazern.lexisoup.plugin.feature"
            implementationClass = "FeaturePlugin"
        }
    }
}
