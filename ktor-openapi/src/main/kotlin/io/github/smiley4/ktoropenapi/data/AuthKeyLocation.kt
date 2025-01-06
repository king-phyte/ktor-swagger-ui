package io.github.smiley4.ktoropenapi.data

import io.swagger.v3.oas.models.security.SecurityScheme

/**
 * The locations of the API key.
 */
enum class AuthKeyLocation(val swaggerType: SecurityScheme.In) {
    QUERY(SecurityScheme.In.QUERY),
    HEADER(SecurityScheme.In.HEADER),
    COOKIE(SecurityScheme.In.COOKIE)
}
