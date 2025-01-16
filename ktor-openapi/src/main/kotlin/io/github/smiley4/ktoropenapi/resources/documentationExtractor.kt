@file:OptIn(ExperimentalSerializationApi::class)

package io.github.smiley4.ktoropenapi.resources

import io.github.smiley4.ktoropenapi.config.ParameterLocation
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.config.SerialTypeDescriptor
import io.ktor.resources.serialization.ResourcesFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementNames

fun <T> extractTypesafeDocumentation(serializer: KSerializer<T>, resourcesFormat: ResourcesFormat): RouteConfig.() -> Unit {
    // Note: typesafe routing only defines information about path & query parameters - no other information is available
    val path = resourcesFormat.encodeToPathPattern(serializer)
    return {
        request {
            collectParameters(serializer.descriptor).forEach { (name, parameterDescriptor) ->
                parameter(getLocation(name, path), name, SerialTypeDescriptor(parameterDescriptor)) {
                    // todo: if elementDescriptor is not class, see "resourcesFormat.encodeToQueryParameters")
                    required = parameterDescriptor.isNullable
                }
            }
        }
    }
}


private fun collectParameters(descriptor: SerialDescriptor): List<Pair<String, SerialDescriptor>> {
    val parameters = mutableListOf<Pair<String, SerialDescriptor>>()

    descriptor.elementNames.forEach { name ->
        val index = descriptor.getElementIndex(name)
        val elementDescriptor = descriptor.getElementDescriptor(index)
        if (!elementDescriptor.isInline && elementDescriptor.kind is StructureKind.CLASS) {
            parameters.addAll(collectParameters(elementDescriptor))
        } else {
            parameters.add(name to elementDescriptor)
        }
    }

    return parameters;
}


/**
 * path contains parameter with given name -> "PATH"
 * path does not contain parameter with given name -> "QUERY"
 */
private fun getLocation(name: String, path: String): ParameterLocation {
    return if (path.contains("{$name}") || path.contains("{$name?}") || path.contains("{$name...}")) {
        ParameterLocation.PATH
    } else {
        ParameterLocation.QUERY
    }
}
