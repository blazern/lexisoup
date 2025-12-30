plugins {
    id("blazern.lexisoup.plugin.library")
    alias(libs.plugins.apollo)
}

kotlin {
    androidLibrary {
        namespace = "blazern.lexisoup.data.lexisoup.graphql"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.apollo.runtime)
                implementation(libs.ktor.client.core)
                implementation(libs.apollo.engine.ktor)
                implementation(project(":core:ktor"))
                implementation(project(":domain:backend-address"))
                implementation(project(":domain:model"))
            }
        }
    }
}

apollo {
    service("lexisoup") {
        packageName.set("blazern.lexisoup.graphql.model")

        // ./gradlew :data:lexisoup-graphql:downloadLexisoupApolloSchemaFromIntrospection
        // ./gradlew :data:lexisoup-graphql:generateApolloSources
        introspection {
            endpointUrl.set("http://localhost:8888/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }
    }
}
