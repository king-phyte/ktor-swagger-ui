package io.github.smiley4.ktorswaggerui

import io.github.smiley4.ktorswaggerui.config.SwaggerUIConfig
import io.github.smiley4.ktorswaggerui.config.SwaggerUISort
import io.github.smiley4.ktorswaggerui.config.SwaggerUISyntaxHighlight
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
 */
fun Route.swaggerUI(openApiUrl: String, config: SwaggerUIConfig.() -> Unit = {}) {
    val swaggerUIConfig = SwaggerUIConfig().apply(config)
    markedSwaggerUI {
        get {
            call.respondRedirect("${call.request.uri}/index.html")
        }
        get("{filename}") {
            SwaggerUI.serveStaticResource(call.parameters["filename"]!!, swaggerUIConfig, call)
        }
        get("swagger-initializer.js") {
            SwaggerUI.serveSwaggerInitializer(call, swaggerUIConfig, openApiUrl)
        }
    }
}

internal object SwaggerUI {

    internal suspend fun serveSwaggerInitializer(call: ApplicationCall, config: SwaggerUIConfig, apiUrl: String) {
        // see https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md for reference
        val propValidatorUrl = config.validatorUrl?.let { "validatorUrl: \"$it\"" } ?: "validatorUrl: false"
        val propDisplayOperationId = "displayOperationId: ${config.displayOperationId}"
        val propFilter = "filter: ${config.showTagFilterInput}"
        val propSort = "operationsSorter: " +
                if (config.sort == SwaggerUISort.NONE) "undefined"
                else "\"${config.sort.value}\""
        val propSyntaxHighlight = "syntaxHighlight: " +
                if (config.syntaxHighlight == SwaggerUISyntaxHighlight.DISABLED) "false"
                else "{ theme: \"${config.syntaxHighlight.value}\" }"
        val content = """
			window.onload = function() {
			  window.ui = SwaggerUIBundle({
				url: "$apiUrl",
				dom_id: '#swagger-ui',
				deepLinking: true,
				presets: [
				  SwaggerUIBundle.presets.apis,
				  SwaggerUIStandalonePreset
				],
				plugins: [
				  SwaggerUIBundle.plugins.DownloadUrl
				],
				layout: "StandaloneLayout",
				withCredentials: ${config.withCredentials},
				$propValidatorUrl,
  				$propDisplayOperationId,
    		    $propFilter,
    		    $propSort,
				$propSyntaxHighlight
			  });
			};
		""".trimIndent()
        call.respondText(ContentType.Application.JavaScript, HttpStatusCode.OK) { content }
    }

    internal suspend fun serveStaticResource(filename: String, config: SwaggerUIConfig, call: ApplicationCall) {
        val resourceName = "/META-INF/resources/webjars/swagger-ui/${config.staticResourcesPath}/$filename"
        val resource = SwaggerUI::class.java.getResource(resourceName)
        if (resource != null) {
            call.respond(ResourceContent(resource))
        } else {
            call.respond(HttpStatusCode.NotFound, "'$filename' could not be found.")
        }
    }

}
