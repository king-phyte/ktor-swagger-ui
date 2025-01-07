package io.github.smiley4.ktoropenapi

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.builder.OpenApiSpecBuilder
import io.github.smiley4.ktoropenapi.builder.route.RouteCollector
import io.github.smiley4.ktoropenapi.data.OutputFormat
import io.github.smiley4.ktoropenapi.data.PluginConfigData
import io.github.smiley4.ktoropenapi.dsl.config.OpenApiPluginConfigDsl
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.plugin
import io.ktor.server.routing.RoutingRoot

private val logger = KotlinLogging.logger {}

val OpenApi = createApplicationPlugin(name = "OpenApi", createConfiguration = ::OpenApiPluginConfigDsl) {
    OpenApiPlugin.config = pluginConfig.build(PluginConfigData.DEFAULT)
    on(MonitoringEvent(ApplicationStarted)) { application ->
        try {
            OpenApiPlugin.generateOpenApiSpecs(application)
        } catch (e: Exception) {
            logger.error(e) { "Error during application startup in openapi-plugin" }
        }
    }
}


object OpenApiPlugin {

    internal var config = PluginConfigData.DEFAULT

    val openApiSpecs = mutableMapOf<String, Pair<String, OutputFormat>>()

    fun generateOpenApiSpecs(application: Application) {
        val routes = RouteCollector().collect({ application.plugin(RoutingRoot) }, config)
        val specs = OpenApiSpecBuilder().build(config, routes)
        openApiSpecs.clear()
        openApiSpecs.putAll(specs)
    }

}
