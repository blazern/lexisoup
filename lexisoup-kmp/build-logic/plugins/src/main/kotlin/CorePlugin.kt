import com.android.build.api.dsl.androidLibrary
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

open class CorePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        with(pluginManager) {
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("androidKotlinMultiplatformLibrary").get().get().pluginId)
            apply(libs.findPlugin("detekt").get().get().pluginId)
        }

        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        }
        tasks.withType<Detekt>().configureEach {
            reports.html.required.set(true)
            reports.md.required.set(true)

            setSource(
                files(
                    "src/commonMain/kotlin",
                    "src/commonTest/kotlin",
                    "src/androidMain/kotlin",
                    "src/androidTest/kotlin",
                    "src/iosMain/kotlin",
                    "src/iosTest/kotlin",
                    "src/jsMain/kotlin",
                    "src/jsTest/kotlin",
                )
            )
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            @Suppress("UnstableApiUsage")
            androidLibrary {
                compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
                minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

                withHostTestBuilder {
                }

                experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
            }

            iosX64()
            iosArm64()
            iosSimulatorArm64()

            js {
                browser()
                binaries.library()
            }

            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("kotlin-stdlib").get())
                    implementation(libs.findLibrary("kotlinx-io").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                }
                commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                    implementation(libs.findLibrary("ktor-client-mock").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}
