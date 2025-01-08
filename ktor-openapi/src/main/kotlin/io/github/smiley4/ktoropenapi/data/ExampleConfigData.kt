package io.github.smiley4.ktoropenapi.data

import io.github.smiley4.ktoropenapi.config.ExampleDescriptor
import io.github.smiley4.ktoropenapi.config.ExampleEncoder

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
