package io.github.smiley4.ktoropenapi

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.builder.OpenApiSpecBuilder
import io.github.smiley4.ktoropenapi.builder.route.RouteCollector
import io.github.smiley4.ktoropenapi.data.OutputFormat
import io.github.smiley4.ktoropenapi.data.OpenApiPluginData
import io.github.smiley4.ktoropenapi.dsl.config.OpenApiPluginConfig
import io.github.smiley4.ktoropenapi.dsl.routing.route
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.plugin
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingRoot
import io.ktor.server.routing.get

private val logger = KotlinLogging.logger {}

val OpenApi = createApplicationPlugin(name = "OpenApi", createConfiguration = ::OpenApiPluginConfig) {
    OpenApiPlugin.config = pluginConfig.build(OpenApiPluginData.DEFAULT)
    on(MonitoringEvent(ApplicationStarted)) { application ->
        try {
            OpenApiPlugin.generateOpenApiSpecs(application)
        } catch (e: Exception) {
            logger.error(e) { "Error during application startup in openapi-plugin" }
        }
    }
}


object OpenApiPlugin {

    internal var config = OpenApiPluginData.DEFAULT

    internal val openApiSpecs = mutableMapOf<String, Pair<String, OutputFormat>>()

    fun generateOpenApiSpecs(application: Application) {
        val routes = RouteCollector().collect({ application.plugin(RoutingRoot) }, config)
        val specs = OpenApiSpecBuilder().build(config, routes)
        openApiSpecs.clear()
        openApiSpecs.putAll(specs)
    }

}

/**
 * Registers the route for serving an openapi-spec. When multiple specs are configured, the id of the one to serve has to be provided.
 */
fun Route.openApi(specId: String = OpenApiPluginConfig.DEFAULT_SPEC_ID) {
    route({ hidden = true }) {
        get {
            val (content, format) = OpenApiPlugin.openApiSpecs[specId] ?: throw Exception("No openapi-spec found for id $specId.")
            val contentType = when (format) {
                OutputFormat.JSON -> ContentType.Application.Json
                OutputFormat.YAML -> ContentType.Text.Plain
            }
            call.respondText(contentType, HttpStatusCode.OK) { content }
        }
    }
}
