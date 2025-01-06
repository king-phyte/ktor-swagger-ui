package io.github.smiley4.ktoropenapi.data

/**
 * Information about a response for a status-code.
 */
data class OpenApiResponseData(
    val statusCode: String,
    val description: String?,
    val headers: Map<String, OpenApiHeaderData>,
    val body: OpenApiBaseBodyData?,
)
