package io.github.smiley4.ktoropenapi.dsl.routes

import io.github.smiley4.ktoropenapi.data.KTypeDescriptor
import io.github.smiley4.ktoropenapi.data.OpenApiHeaderData
import io.github.smiley4.ktoropenapi.data.SwaggerTypeDescriptor
import io.github.smiley4.ktoropenapi.data.TypeDescriptor
import io.github.smiley4.ktoropenapi.dsl.OpenApiDslMarker
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Describes a single header.
 */
@OpenApiDslMarker
class OpenApiHeader {

    /**
     * A description of the header
     */
    var description: String? = null


    /**
     * The schema of the header
     */
    var type: TypeDescriptor? = null


    /**
     * The schema of the header
     */
    fun type(type: TypeDescriptor) {
        this.type = type
    }


    /**
     * The schema of the header
     */
    fun type(type: Schema<*>) = type(SwaggerTypeDescriptor(type))


    /**
     * The schema of the header
     */
    fun type(type: KType) = type(KTypeDescriptor(type))


    /**
     * The schema of the header
     */
    inline fun <reified T> type() = type(KTypeDescriptor(typeOf<T>()))


    /**
     * Determines whether this header is mandatory
     */
    var required: Boolean? = null


    /**
     * Specifies that a header is deprecated and SHOULD be transitioned out of usage
     */
    var deprecated: Boolean? = null


    /**
     * Specifies whether arrays and objects should generate separate parameters for each array item or object property.
     */
    var explode: Boolean? = null

    /**
     * Build the data object for this config.
     */
    fun build() = OpenApiHeaderData(
        description = description,
        type = type,
        required = required ?: false,
        deprecated = deprecated ?: false,
        explode = explode,
    )
}
