package io.github.smiley4.ktoropenapi.data

import io.ktor.http.ContentType

/**
 * The common information for request and response bodies.
 */
sealed class BaseBodyData(
    val description: String?,
    val required: Boolean,
    val mediaTypes: Set<ContentType>,
)


/**
 * Information for a "simple" request or response body.
 */
class OpenApiSimpleBodyData(
    description: String?,
    required: Boolean,
    mediaTypes: Set<ContentType>,
    val type: TypeDescriptor,
    val examples: List<ExampleDescriptor>
) : BaseBodyData(description, required, mediaTypes)


/**
 * Information for a multipart request or response body.
 */
class OpenApiMultipartBodyData(
    description: String?,
    required: Boolean,
    mediaTypes: Set<ContentType>,
    val parts: List<OpenApiMultipartPartData>
) : BaseBodyData(description, required, mediaTypes)
