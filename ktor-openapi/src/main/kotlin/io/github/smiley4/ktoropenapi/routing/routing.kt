package io.github.smiley4.ktoropenapi.routing

import io.github.smiley4.ktoropenapi.data.OutputFormat
import io.github.smiley4.ktoropenapi.dsl.config.OpenApiPluginConfigDsl
import io.github.smiley4.ktoropenapi.dsl.routing.route
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Registers the route for serving an openapi-spec. When multiple specs are configured, the id of the one to serve has to be provided.
 */
fun Route.openApiSpec(specId: String = OpenApiPluginConfigDsl.DEFAULT_SPEC_ID) {
    route({ hidden = true }) {
        get {
            val contentType = when (ApiSpec.getFormat(specId)) {
                OutputFormat.JSON -> ContentType.Application.Json
                OutputFormat.YAML -> ContentType.Text.Plain
            }
            call.respondText(contentType, HttpStatusCode.OK) { ApiSpec.get(specId) }
        }
    }
}
