package io.github.smiley4.ktorswaggerui

import io.github.smiley4.ktorswaggerui.config.SwaggerUIConfig
import io.github.smiley4.ktorswaggerui.config.OperationsSort
import io.github.smiley4.ktorswaggerui.config.SwaggerUISyntaxHighlight
import io.github.smiley4.ktorswaggerui.config.TagSort
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Registers the route for serving all swagger-ui resources. The path to the OpenApi-file to use has to be given.
 * @param openApiUrl the url of the openapi spec
 * @param config the swagger configuration
 */
fun Route.swaggerUI(openApiUrl: String, config: SwaggerUIConfig.() -> Unit = {}) = this.swaggerUI(mapOf("Api" to openApiUrl), config)


/**
 * Registers the route for serving all swagger-ui resources. The path to the OpenApi-file to use has to be given.
 * @param openApiUrls the urls of the available openapi specs. Key is the name of the spec and the value is the url.
 * @param config the swagger configuration
 */
fun Route.swaggerUI(openApiUrls: Map<String, String>, config: SwaggerUIConfig.() -> Unit = {}) {
    val swaggerUIConfig = SwaggerUIConfig().apply(config)
    markedSwaggerUI {
        get {
            call.respondRedirect("${call.request.uri}/index.html")
        }
        get("swagger-initializer.js") {
            SwaggerUI.serveSwaggerInitializer(call, swaggerUIConfig, openApiUrls)
        }
        get("{filename}") {
            SwaggerUI.serveStaticResource(call.parameters["filename"]!!, swaggerUIConfig, call)
        }
    }
}

internal object SwaggerUI {

    internal suspend fun serveSwaggerInitializer(call: ApplicationCall, config: SwaggerUIConfig, openApiUrls: Map<String, String>) {
        val properties = buildProperties(config, openApiUrls)
        val content = """
		  window.onload = function() {
		    window.ui = SwaggerUIBundle({
		      $properties
		    });
		  };
		""".trimIndent()
        call.respondText(ContentType.Application.JavaScript, HttpStatusCode.OK) { content }
    }


    // see https://swagger.io/docs/open-source-tools/swagger-ui/usage/configuration for reference
    private fun buildProperties(config: SwaggerUIConfig, openApiUrls: Map<String, String>): String {
        return PropertyBuilder().also { properties ->
            // Core
            properties["dom_id"] = "#swagger-ui"
            properties["urls"] = PropertyBuilder.Raw(
                "[" + openApiUrls.map { (name, url) -> "{name: '$name', url: '$url'}" }.joinToString(",") + "]"
            )

            // Plugin System
            properties["layout"] = "StandaloneLayout"
            properties["plugins"] = PropertyBuilder.Raw("[SwaggerUIBundle.plugins.DownloadUrl]")
            properties["presets"] = PropertyBuilder.Raw("[SwaggerUIBundle.presets.apis,SwaggerUIStandalonePreset]")

            // Display
            properties["deepLinking"] = config.deepLinking
            properties["displayOperationId "] = config.displayOperationId
            properties["defaultModelsExpandDepth"] = config.defaultModelsExpandDepth
            properties["defaultModelExpandDepth"] = config.defaultModelExpandDepth
            properties["displayRequestDuration"] = config.displayRequestDuration
            properties["filter"] = config.filter
            properties["maxDisplayedTags"] = config.maxDisplayedTags
            properties["operationsSorter"] = when(config.operationsSorter) {
                OperationsSort.NONE -> null
                else -> config.operationsSorter.value
            }
            properties["showExtensions"] = config.showExtensions
            properties["showCommonExtensions"] = config.showCommonExtensions
            properties["tagsSorter"] = when(config.tagsSorter) {
                TagSort.NONE -> null
                else -> config.tagsSorter.value
            }
            properties["syntaxHighlight"] = when(config.syntaxHighlight) {
                SwaggerUISyntaxHighlight.DISABLED -> false
                else -> PropertyBuilder.Raw("{theme: '${config.syntaxHighlight.value}'}")
            }
            properties["tryItOutEnabled"] = config.tryItOutEnabled
            properties["requestSnippetsEnabled"] = config.requestSnippetsEnabled

            // Network
            properties["oauth2RedirectUrl"] = config.oauth2RedirectUrl
            properties["requestInterceptor"] = config.requestInterceptor?.let { PropertyBuilder.Raw(it) }
            properties["responseInterceptor"] = config.responseInterceptor?.let { PropertyBuilder.Raw(it) }
            properties["supportedSubmitMethods"] = PropertyBuilder.Raw("[" + config.supportedSubmitMethods.joinToString(",") { "'$it'" } + "]")
            properties["validatorUrl"] = config.validatorUrl
            properties["withCredentials"] = config.withCredentials

            // Authorization
            properties["persistAuthorization"] = config.persistAuthorization

        }.render(
            linePrefix = "\t\t      ",
            prefixFirst = false,
            separator = ",\n",
            nullBehavior = "remove"
        )
    }

    internal suspend fun serveStaticResource(filename: String, config: SwaggerUIConfig, call: ApplicationCall) {
        val resourceName = "${config.staticResourcesPath}/$filename"
        val resource = SwaggerUI::class.java.getResource(resourceName)
        if (resource != null) {
            call.respond(ResourceContent(resource))
        } else {
            call.respond(HttpStatusCode.NotFound, "'$filename' could not be found.")
        }
    }

}
//
//
//internal suspend fun OLD_serveSwaggerInitializer(call: ApplicationCall, config: SwaggerUIConfig, openApiUrls: List<String>) {
//    // see https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md for reference
//    val propUrls = "urls: [" + openApiUrls.joinToString(", ") { "{name: \"$it\", url: \"$it\"}" } + "]"
//    val propValidatorUrl = config.validatorUrl?.let { "validatorUrl: \"$it\"" } ?: "validatorUrl: false"
//    val propDisplayOperationId = "displayOperationId: ${config.displayOperationId}"
//    val propFilter = "filter: ${config.showTagFilterInput}"
//    val propSort = "operationsSorter: " +
//            if (config.sort == OperationsSort.NONE) "undefined"
//            else "\"${config.sort.value}\""
//    val propSyntaxHighlight = "syntaxHighlight: " +
//            if (config.syntaxHighlight == SwaggerUISyntaxHighlight.DISABLED) "false"
//            else "{ theme: \"${config.syntaxHighlight.value}\" }"
//    val content = """
//			window.onload = function() {
//			  window.ui = SwaggerUIBundle({
//				dom_id: '#swagger-ui',
//                $propUrls,
//				deepLinking: true,
//				presets: [
//				  SwaggerUIBundle.presets.apis,
//				  SwaggerUIStandalonePreset
//				],
//				plugins: [
//				  SwaggerUIBundle.plugins.DownloadUrl
//				],
//				layout: "StandaloneLayout",
//				withCredentials: ${config.withCredentials},
//				$propValidatorUrl,
//  				$propDisplayOperationId,
//    		    $propFilter,
//    		    $propSort,
//				$propSyntaxHighlight
//			  });
//			};
//		""".trimIndent()
//    call.respondText(ContentType.Application.JavaScript, HttpStatusCode.OK) { content }
//}