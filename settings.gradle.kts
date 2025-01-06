rootProject.name = "ktor-openapi"

include("ktor-openapi")
include("ktor-swagger-ui")
include("ktor-swagger-ui-examples")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}