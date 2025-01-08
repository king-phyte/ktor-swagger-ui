package io.github.smiley4.ktoropenapi.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.smiley4.ktoropenapi.data.*
import io.swagger.v3.oas.models.examples.Example
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Encoder to produce the final example value.
 * Return the unmodified example to fall back to the default encoder.
 */
typealias ExampleEncoder = (type: TypeDescriptor?, example: Any?) -> Any?

/**
 * [ExampleEncoder] using kotlinx-serialization to encode example objects.
 */
val kotlinxExampleEncoder: ExampleEncoder = { type, example ->
    if (type is KTypeDescriptor) {
        val jsonString = Json.encodeToString(serializer(type.type), example)
        val jsonObj = jacksonObjectMapper().readValue(jsonString, object : TypeReference<Any>() {})
        jsonObj
    } else {
        example
    }
}

/**
 * Configuration for examples
 */
@OpenApiDslMarker
class ExampleConfig {

    val sharedExamples = mutableMapOf<String, ExampleDescriptor>()
    var exampleEncoder: ExampleEncoder? = null


    /**
     * Add a shared example that can be referenced by all routes.
     * The name of the example has to be unique among all shared examples and acts as its id.
     * @param example the example data.
     */
    fun example(example: ExampleDescriptor) {
        sharedExamples[example.name] = example
    }

    /**
     * Add a shared example that can be referenced by all routes by the given name.
     * The provided name has to be unique among all shared examples and acts as its id.
     */
    fun example(name: String, example: Example) = example(SwaggerExampleDescriptor(name, example))

    /**
     * Add a shared example that can be referenced by all routes by the given name.
     * The provided name has to be unique among all shared examples and acts as its id.
     */
    fun example(name: String, example: ValueExampleDescriptorConfig.() -> Unit) = example(
        ValueExampleDescriptorConfig()
            .apply(example)
            .let { result ->
                ValueExampleDescriptor(
                    name = name,
                    value = result.value,
                    summary = result.summary,
                    description = result.description
                )
            }
    )


    /**
     * Specify a custom encoder for example objects
     */
    fun encoder(exampleEncoder: ExampleEncoder) {
        this.exampleEncoder = exampleEncoder
    }

    /**
     * Build the data object for this config.
     * @param securityConfig the data for security config that might contain additional examples
     */
    internal fun build(securityConfig: SecurityData) = ExampleConfigData(
        sharedExamples = sharedExamples,
        securityExamples = securityConfig.defaultUnauthorizedResponse?.body?.let {
            when (it) {
                is SimpleBodyData -> it
                is MultipartBodyData -> null
            }
        },
        exampleEncoder = exampleEncoder
    )
}
