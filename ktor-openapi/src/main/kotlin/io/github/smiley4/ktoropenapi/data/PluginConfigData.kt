package io.github.smiley4.ktoropenapi.data

import io.github.smiley4.ktoropenapi.dsl.config.OpenApiPluginConfigDsl
import kotlin.reflect.KClass

/**
 * Complete plugin configuration
 */
internal data class PluginConfigData(
    val specAssigner: SpecAssigner,
    val pathFilter: PathFilter,
    val ignoredRouteSelectors: Set<KClass<*>>,
    val ignoredRouteSelectorClassNames: Set<String>,
    val info: InfoData,
    val servers: List<ServerData>,
    val externalDocs: ExternalDocsData,
    val specConfigs: MutableMap<String, PluginConfigData>,
    val postBuild: PostBuild?,
    val schemaConfig: SchemaConfigData,
    val exampleConfig: ExampleConfigData,
    val securityConfig: SecurityData,
    val tagsConfig: TagsData,
    val outputFormat: OutputFormat,
    val rootPath: String?
) {

    companion object {
        val DEFAULT = PluginConfigData(
            specAssigner = { _, _ -> OpenApiPluginConfigDsl.DEFAULT_SPEC_ID },
            pathFilter = { _, _ -> true },
            ignoredRouteSelectors = emptySet(),
            ignoredRouteSelectorClassNames = emptySet(),
            info = InfoData.DEFAULT,
            servers = emptyList(),
            externalDocs = ExternalDocsData.DEFAULT,
            specConfigs = mutableMapOf(),
            postBuild = null,
            schemaConfig = SchemaConfigData.DEFAULT,
            exampleConfig = ExampleConfigData.DEFAULT,
            securityConfig = SecurityData.DEFAULT,
            tagsConfig = TagsData.DEFAULT,
            outputFormat = OutputFormat.JSON,
            rootPath = "todo" // TODO
        )
    }

}
