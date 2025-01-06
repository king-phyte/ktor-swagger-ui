package io.github.smiley4.ktoropenapi

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.builder.example.ExampleContext
import io.github.smiley4.ktoropenapi.builder.example.ExampleContextImpl
import io.github.smiley4.ktoropenapi.builder.openapi.ComponentsBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ContactBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ContentBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ExternalDocumentationBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.HeaderBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.InfoBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.LicenseBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.OAuthFlowsBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.OpenApiBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.OperationBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.OperationTagsBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ParameterBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.PathBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.PathsBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.RequestBodyBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ResponseBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ResponsesBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.SecurityRequirementsBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.SecuritySchemesBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.ServerBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.TagBuilder
import io.github.smiley4.ktoropenapi.builder.openapi.TagExternalDocumentationBuilder
import io.github.smiley4.ktoropenapi.builder.route.RouteCollector
import io.github.smiley4.ktoropenapi.builder.route.RouteDocumentationMerger
import io.github.smiley4.ktoropenapi.builder.route.RouteMeta
import io.github.smiley4.ktoropenapi.builder.schema.SchemaContextImpl
import io.github.smiley4.ktoropenapi.builder.schema.SchemaContext
import io.github.smiley4.ktoropenapi.data.OutputFormat
import io.github.smiley4.ktoropenapi.data.PluginConfigData
import io.github.smiley4.ktoropenapi.dsl.config.OpenApiPluginConfigDsl
import io.github.smiley4.ktoropenapi.routing.ApiSpec
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.MonitoringEvent
import io.ktor.server.application.plugin
import io.ktor.server.routing.RoutingRoot
import io.swagger.v3.core.util.Json31
import io.swagger.v3.core.util.Yaml31

private val logger = KotlinLogging.logger {}

val OpenApi = createApplicationPlugin(name = "OpenApi", createConfiguration = ::OpenApiPluginConfigDsl) {

    val config = pluginConfig.build(PluginConfigData.DEFAULT)

    on(MonitoringEvent(ApplicationStarted)) { application ->

        try {
            val routes = routes(application, config)
            ApiSpec.setAll(buildOpenApiSpecs(config, routes))
        } catch (e: Exception) {
            logger.error(e) { "Error during application startup in openapi-plugin" }
        }

    }
}

private fun buildOpenApiSpecs(config: PluginConfigData, routes: List<RouteMeta>): Map<String, Pair<String, OutputFormat>> {
    val routesBySpec = buildMap<String, MutableList<RouteMeta>> {
        routes.forEach { route ->
            val specName =
                route.documentation.specId ?: config.specAssigner(route.path, route.documentation.tags.toList())
            computeIfAbsent(specName) { mutableListOf() }.add(route)
        }
    }
    return buildMap {
        routesBySpec.forEach { (specName, routes) ->
            val specConfig = config.specConfigs[specName] ?: config
            this[specName] = buildOpenApiSpec(specName, specConfig, routes)
        }
    }
}

private fun buildOpenApiSpec(specName: String, pluginConfig: PluginConfigData, routes: List<RouteMeta>): Pair<String, OutputFormat> {
    return try {
        val schemaContext = SchemaContextImpl(pluginConfig.schemaConfig).also {
            it.addGlobal(pluginConfig.schemaConfig)
            it.add(routes)
        }
        val exampleContext = ExampleContextImpl(pluginConfig.exampleConfig.exampleEncoder).also {
            it.addShared(pluginConfig.exampleConfig)
            it.add(routes)
        }
        val openApi = builder(pluginConfig, schemaContext, exampleContext).build(routes)
        pluginConfig.postBuild?.let { it(openApi, specName) }
        when (pluginConfig.outputFormat) {
            OutputFormat.JSON -> Json31.pretty(openApi) to pluginConfig.outputFormat
            OutputFormat.YAML -> Yaml31.pretty(openApi) to pluginConfig.outputFormat
        }
    } catch (e: Exception) {
        logger.error(e) { "Error during openapi-spec generation" }
        return pluginConfig.outputFormat.empty to pluginConfig.outputFormat
    }
}

private fun routes(application: Application, config: PluginConfigData): List<RouteMeta> {
    return RouteCollector(RouteDocumentationMerger())
        .collectRoutes({ application.plugin(RoutingRoot) }, config)
        .map { it.copy(path = "${application.rootPath()}${it.path}") }
        .toList()
}

private fun Application.rootPath(): String =
    environment.config.propertyOrNull("ktor.deployment.rootPath")?.getString() ?: ""

private fun builder(
    config: PluginConfigData,
    schemaContext: SchemaContext,
    exampleContext: ExampleContext,
): OpenApiBuilder {
    return OpenApiBuilder(
        config = config,
        schemaContext = schemaContext,
        exampleContext = exampleContext,
        infoBuilder = InfoBuilder(
            contactBuilder = ContactBuilder(),
            licenseBuilder = LicenseBuilder()
        ),
        externalDocumentationBuilder = ExternalDocumentationBuilder(),
        serverBuilder = ServerBuilder(),
        tagBuilder = TagBuilder(
            tagExternalDocumentationBuilder = TagExternalDocumentationBuilder()
        ),
        pathsBuilder = PathsBuilder(
            pathBuilder = PathBuilder(
                operationBuilder = OperationBuilder(
                    operationTagsBuilder = OperationTagsBuilder(config),
                    parameterBuilder = ParameterBuilder(
                        schemaContext = schemaContext,
                        exampleContext = exampleContext,
                    ),
                    requestBodyBuilder = RequestBodyBuilder(
                        contentBuilder = ContentBuilder(
                            schemaContext = schemaContext,
                            exampleContext = exampleContext,
                            headerBuilder = HeaderBuilder(schemaContext)
                        )
                    ),
                    responsesBuilder = ResponsesBuilder(
                        responseBuilder = ResponseBuilder(
                            headerBuilder = HeaderBuilder(schemaContext),
                            contentBuilder = ContentBuilder(
                                schemaContext = schemaContext,
                                exampleContext = exampleContext,
                                headerBuilder = HeaderBuilder(schemaContext)
                            )
                        ),
                        config = config
                    ),
                    securityRequirementsBuilder = SecurityRequirementsBuilder(config),
                    externalDocumentationBuilder = ExternalDocumentationBuilder(),
                    serverBuilder = ServerBuilder()
                )
            )
        ),
        componentsBuilder = ComponentsBuilder(
            config = config,
            securitySchemesBuilder = SecuritySchemesBuilder(
                oAuthFlowsBuilder = OAuthFlowsBuilder()
            )
        )
    )
}
