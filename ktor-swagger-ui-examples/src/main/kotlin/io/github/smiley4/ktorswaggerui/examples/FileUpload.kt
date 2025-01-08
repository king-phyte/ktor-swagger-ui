package io.github.smiley4.ktorswaggerui.examples

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.dsl.routing.post
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.swagger.v3.oas.models.media.Schema
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::myModule).start(wait = true)
}

private fun Application.myModule() {

    // Install the "SwaggerUI"-Plugin and use the default configuration
    install(OpenApi) {
        schemas {
            // overwrite type "File" with custom schema for binary data
            overwrite<File>(Schema<Any>().also {
                it.type = "string"
                it.format = "binary"
            })
        }
    }
    install(SwaggerUI)

    routing {

        // add the routes for swagger-ui and api-spec
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApi()
        }

        // upload a single file, either as png, jpeg or svg
        post("single", {
            request {
                body<File> {
                    mediaTypes(
                        ContentType.Image.PNG,
                        ContentType.Image.JPEG,
                        ContentType.Image.SVG,
                    )
                }
            }
        }) {
            call.respond(HttpStatusCode.NotImplemented, "...")
        }

        // upload multiple files
        post("multipart", {
            request {
                multipartBody {
                    mediaTypes(ContentType.MultiPart.FormData)
                    part<File>("first-image",) {
                        mediaTypes(
                            ContentType.Image.PNG,
                            ContentType.Image.JPEG,
                            ContentType.Image.SVG
                        )
                    }
                    part<File>("second-image") {
                        mediaTypes(
                            ContentType.Image.PNG,
                            ContentType.Image.JPEG,
                            ContentType.Image.SVG
                        )
                    }
                }
            }
        }) {
            call.respond(HttpStatusCode.NotImplemented, "...")
        }

    }

}
