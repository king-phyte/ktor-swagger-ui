package io.github.smiley4.ktorswaggerui.config

class SwaggerUIConfig {

    /**
     * Path to the static resources for swagger-ui in the jar-file.
     * Version must match the version of the swagger-ui-webjars dependency.
     */
    internal val staticResourcesPath: String = "/META-INF/resources/webjars/swagger-ui/5.17.11"

    /**
     * Swagger UI can attempt to validate specs against swagger.io's online validator.
     * You can use this parameter to set a different validator URL, for example for locally deployed validators.
     * Set to "null" to disable validation.
     * Validation is disabled when the url of the api-spec-file contains localhost.
     * (see https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/configuration.md#network)
     */
    var validatorUrl: String? = null

    fun disableSpecValidator() {
        validatorUrl = null
    }

    fun onlineSpecValidator() {
        validatorUrl = "https://validator.swagger.io/validator"
    }

    /**
     * Whether to show the operation-id of endpoints in the list
     */
    var displayOperationId: Boolean = false

    /**
     * Whether the top bar will show an edit box that you can use to filter the tagged operations.
     */
    var showTagFilterInput: Boolean = false

    /**
     * Apply a sort to the operation list of each API
     */
    var sort: SwaggerUISort = SwaggerUISort.NONE

    /**
     * Syntax coloring theme to use
     */
    var syntaxHighlight: SwaggerUISyntaxHighlight = SwaggerUISyntaxHighlight.AGATE

    /**
     * Bring cookies when initiating network requests
     */
    var withCredentials: Boolean = false

}