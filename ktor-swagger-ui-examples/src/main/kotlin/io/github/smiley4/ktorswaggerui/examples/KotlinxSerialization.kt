package io.github.smiley4.ktorswaggerui.examples

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.data.kotlinxExampleEncoder
import io.github.smiley4.ktoropenapi.dsl.routing.get
import io.github.smiley4.ktoropenapi.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::myModule).start(wait = true)
}

private fun Application.myModule() {

    install(OpenApi) {
        schemas {
            // configure the schema generator to use kotlinx-serializer
            // (see https://github.com/SMILEY4/schema-kenerator/wiki for more information)
            generator = { type ->
                type
                    .processKotlinxSerialization()
                    .generateSwaggerSchema()
                    .withTitle(TitleType.SIMPLE)
                    .compileReferencingRoot()
            }
        }
        examples {
            // configure the example encoder to encode kotlin objects using kotlinx-serializer
            exampleEncoder = kotlinxExampleEncoder
        }
    }
    install(SwaggerUI)

    routing {

        // add the routes for swagger-ui and api-spec
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApiSpec()
        }

        // a documented route
        get("hello", {
            description = "A Hello-World route"
            request {
                queryParameter<String>("name") {
                    description = "the name to greet"
                    example("Name Parameter") {
                        value = "Mr. Example"
                    }
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "successful request - always returns 'Hello World!'"
                    body<TestResponse> {
                        example("Success Response") {
                            value = TestResponse(
                                name = "Mr. Example",
                                length = 11
                            )
                        }
                    }
                }
            }
        }) {
            call.respondText("Hello ${call.request.queryParameters["name"]}")
        }

    }

}


@Serializable
data class TestResponse(
    val name: String,
    val length: Int,
)