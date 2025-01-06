package io.github.smiley4.ktorswaggerui.routing

import io.github.smiley4.ktoropenapi.dsl.routing.route
import io.github.smiley4.ktorswaggerui.PLUGIN_CONFIG
import io.github.smiley4.ktorswaggerui.SWAGGER_UI_WEBJARS_VERSION
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.SwaggerUIData
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSort
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSyntaxHighlight
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


/**
 * Registers the route for serving all swagger-ui resources. The url to the openapi-spec file to use has to be specified.
 */
fun Route.swaggerUI(apiUrl: String) {
    route({ hidden = true }) {
        get {
            call.respondRedirect("${call.request.uri}/index.html")
        }
        get("{filename}") {
            serveStaticResource(call.parameters["filename"]!!, SWAGGER_UI_WEBJARS_VERSION, call)
        }
        get("swagger-initializer.js") {
            serveSwaggerInitializer(call, PLUGIN_CONFIG!!, apiUrl)
        }
    }
}

private suspend fun serveSwaggerInitializer(call: ApplicationCall, swaggerUiConfig: SwaggerUIData, apiUrl: String) {
    // see https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md for reference
    val propValidatorUrl = swaggerUiConfig.validatorUrl?.let { "validatorUrl: \"$it\"" } ?: "validatorUrl: false"
    val propDisplayOperationId = "displayOperationId: ${swaggerUiConfig.displayOperationId}"
    val propFilter = "filter: ${swaggerUiConfig.showTagFilterInput}"
    val propSort = "operationsSorter: " +
            if (swaggerUiConfig.sort == SwaggerUiSort.NONE) "undefined"
            else "\"${swaggerUiConfig.sort.value}\""
    val propSyntaxHighlight = "syntaxHighlight: " +
            if (swaggerUiConfig.syntaxHighlight == SwaggerUiSyntaxHighlight.DISABLED) "false"
            else "{ theme: \"${swaggerUiConfig.syntaxHighlight.value}\" }"
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
				withCredentials: ${swaggerUiConfig.withCredentials},
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

private suspend fun serveStaticResource(filename: String, swaggerWebjarVersion: String, call: ApplicationCall) {
    val resourceName = "/META-INF/resources/webjars/swagger-ui/$swaggerWebjarVersion/$filename"
    val resource = SwaggerUI::class.java.getResource(resourceName)
    if (resource != null) {
        call.respond(ResourceContent(resource))
    } else {
        call.respond(HttpStatusCode.NotFound, "$filename could not be found")
    }
}
