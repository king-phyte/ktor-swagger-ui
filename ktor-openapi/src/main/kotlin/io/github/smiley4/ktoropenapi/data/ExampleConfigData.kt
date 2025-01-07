package io.github.smiley4.ktoropenapi.data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Encoder to produce the final example value.
 * Return the unmodified example to fall back to the default encoder.
 */
internal typealias ExampleEncoder = (type: TypeDescriptor?, example: Any?) -> Any?

/**
 * [ExampleEncoder] using kotlinx-serialization to encode example objects.
 */
internal val kotlinxExampleEncoder: ExampleEncoder = { type, example ->
    if (type is KTypeDescriptor) {
        val jsonString = Json.encodeToString(serializer(type.type), example)
        val jsonObj = jacksonObjectMapper().readValue(jsonString, object : TypeReference<Any>() {})
        jsonObj
    } else {
        example
    }
}

internal class ExampleConfigData(
    val sharedExamples: Map<String, ExampleDescriptor>,
    val securityExamples: SimpleBodyData?,
    val exampleEncoder: ExampleEncoder?
) {

    companion object {
        val DEFAULT = ExampleConfigData(
            sharedExamples = emptyMap(),
            securityExamples = null,
            exampleEncoder = null
        )
    }

}
