import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class FeaturePlugin : LibraryPlugin() {
    override fun apply(target: Project) = with(target) {
        super.apply(target)

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                val commonMain = getByName("commonMain")
                val androidMain = maybeCreate("androidMain")

                commonMain.dependencies {
                    implementation(libs.findLibrary("androidx-appcompat").get())
                    implementation(libs.findLibrary("androidx-core-ktx").get())
                    implementation(project(":core:ui:strings"))

                    // Draw icons
                    implementation(libs.findLibrary("compose-material-icons-core").get())
                    implementation(libs.findLibrary("compose-ui-graphics").get())

                    // For compose previews
                    implementation(libs.findLibrary("androidx-emoji").get())
                    implementation(libs.findLibrary("androidx-customview-poolingcontainer").get())
                }

                androidMain.dependencies {
                    implementation(libs.findLibrary("androidx-activity-compose").get())
                    // For compose previews
                    implementation(libs.findLibrary("androidx-ui-tooling").get())
                }
            }
        }
    }
}
