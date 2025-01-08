package io.github.smiley4.ktoropenapi.data

import io.swagger.v3.oas.models.OpenAPI

/**
 * Function executed after building the openapi-spec.
 */
typealias PostBuild = (openApi: OpenAPI, specName: String) -> Unit
