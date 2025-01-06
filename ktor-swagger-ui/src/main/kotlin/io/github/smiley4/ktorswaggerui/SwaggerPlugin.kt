package io.github.smiley4.ktorswaggerui

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktorswaggerui.data.SwaggerUIData
import io.github.smiley4.ktorswaggerui.dsl.SwaggerUiPluginConfigDsl
import io.ktor.server.application.createApplicationPlugin

/**
 * This version must match the version of the gradle dependency
 */
internal const val SWAGGER_UI_WEBJARS_VERSION = "5.17.11"

internal var PLUGIN_CONFIG: SwaggerUIData? = null

private val logger = KotlinLogging.logger {}

val SwaggerUI = createApplicationPlugin(name = "SwaggerUI", createConfiguration = ::SwaggerUiPluginConfigDsl) {
    PLUGIN_CONFIG = pluginConfig.build(SwaggerUIData.DEFAULT)
}

