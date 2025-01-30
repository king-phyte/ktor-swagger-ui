package io.github.smiley4.ktorredoc

import io.github.smiley4.ktorredoc.config.RedocConfig
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
 * Registers the route for serving all redoc resources. The path to the OpenApi-file to use has to be given.
 */
fun Route.redoc(openApiUrl: String, config: RedocConfig.() -> Unit = {}) {
    val redocConfig = RedocConfig().apply(config)
    markedRedoc {
        get {
            call.respondRedirect("${call.request.uri}/index.html")
        }
        get("index.html") {
            Redoc.serveIndexHtml(call, redocConfig, openApiUrl)
        }
        get("{filename}") {
            Redoc.serveStaticResource(call.parameters["filename"]!!, redocConfig, call)
        }
    }
}

internal object Redoc {

    internal suspend fun serveIndexHtml(call: ApplicationCall, config: RedocConfig, openApiUrl: String) {
        val properties = buildProperties(config, openApiUrl)
        val content = """
          <!DOCTYPE html>
          <html>
            <head>
              <title>${config.pageTitle}</title>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <style>
                body {
                  margin: 0;
                  padding: 0;
                }
              </style>
            </head>
            <body>
              <redoc ${"\n"}${properties.entries.joinToString("\n") { (key, value) -> "$key=$value" }}></redoc>
              <script src="./redoc.standalone.js"> </script>
            </body>
          </html>
		""".trimIndent()
        call.respondText(ContentType.Text.Html, HttpStatusCode.OK) { content }
    }

    private fun buildProperties(config: RedocConfig, openApiUrl: String) = buildMap {
        this["spec-url"] = "'$openApiUrl'"
        config.disableSearch?.also { this["disable-search"] = it.toString() }
        config.minCharacterLengthToInitSearch?.also { this["min-character-length-to-init-search"] = it.toString() }
        config.expandDefaultServerVariables?.also { this["expand-default-server-variables"] = it.toString() }
        config.expandResponses?.also {
            val values = it.toSet()
            if (values.any { v -> v.equals("all", ignoreCase = true) }) {
                this["expand-responses"] = "'all'"
            } else {
                this["expand-responses"] = "'${values.joinToString(",")}'"
            }
        }
        config.expandSingleSchemaField?.also { this["expand-single-schema-field"] = it.toString() }
        config.hideDownloadButton?.also { this["hide-download-button"] = it.toString() }
        config.hideHostname?.also { this["hide-hostname"] = it.toString() }
        config.hideLoading?.also { this["hide-loading"] = it.toString() }
        config.hideRequestPayloadSample?.also { this["hide-request-payload-sample"] = it.toString() }
        config.hideOneOfDescription?.also { this["hide-one-of-description"] = it.toString() }
        config.hideSchemaPattern?.also { this["hide-schema-pattern"] = it.toString() }
        config.hideSchemaTitles?.also { this["hide-schema-titles"] = it.toString() }
        config.hideSecuritySection?.also { this["hide-security-section"] = it.toString() }
        config.hideSingleRequestSampleTab?.also { this["hide-single-request-sample-tab"] = it.toString() }
        config.htmlTemplate?.also { this["html-template"] = "'$it'" }
        config.jsonSampleExpandLevel?.also { this["json-sample-expand-level"] = "'$it'" }
        config.maxDisplayedEnumValues?.also { this["max-displayed-enum-values"] = it.toString() }
        config.menuToggle?.also { this["menu-toggle"] = it.toString() }
        config.nativeScrollbars?.also { this["native-scrollbars"] = it.toString() }
        config.onlyRequiredInSamples?.also { this["only-required-in-samples"] = it.toString() }
        config.pathInMiddlePanel?.also { this["path-in-middle-panel"] = it.toString() }
        config.payloadSampleIdx?.also { this["payload-sample-idx"] = it.toString() }
        config.requiredPropsFirst?.also { this["required-props-first"] = it.toString() }
        config.schemaExpansionLevel?.also { this["schema-expansion-level"] = "'$it'" }
        config.showObjectSchemaExamples?.also { this["show-object-schema-examples"] = it.toString() }
        config.showWebhookVerb?.also { this["show-webhook-verb"] = it.toString() }
        config.simpleOneOfTypeLabel?.also { this["simple-one-of-type-label"] = it.toString() }
        config.sortEnumValuesAlphabetically?.also { this["sort-enum-values-alphabetically"] = it.toString() }
        config.sortOperationsAlphabetically?.also { this["sort-operations-alphabetically"] = it.toString() }
        config.sortPropsAlphabetically?.also { this["sort-props-alphabetically"] = it.toString() }
        config.sortTagsAlphabetically?.also { this["sort-tags-alphabetically"] = it.toString() }
        config.untrustedDefinition?.also { this["untrusted-definition"] = it.toString() }
        config.theme?.also { this["theme"] = "'$it'" }
    }

    internal suspend fun serveStaticResource(filename: String, config: RedocConfig, call: ApplicationCall) {
        val resourceName = "${config.staticResourcesPath}/$filename"
        val resource = Redoc::class.java.getResource(resourceName)
        if (resource != null) {
            call.respond(ResourceContent(resource))
        } else {
            call.respond(HttpStatusCode.NotFound, "'$filename' could not be found.")
        }
    }

}
