package io.github.smiley4.ktoropenapi.data

/**
 * See [OpenAPI Specification - Header Object](https://swagger.io/specification/#header-object).
 */
data class HeaderData(
    val description: String?,
    val type: TypeDescriptor?,
    val required: Boolean,
    val deprecated: Boolean,
    val explode: Boolean?,
)
