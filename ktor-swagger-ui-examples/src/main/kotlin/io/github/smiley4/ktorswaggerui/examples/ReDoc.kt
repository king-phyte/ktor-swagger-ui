package io.github.smiley4.ktorswaggerui.examples

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.github.smiley4.ktorswaggerui.routing.HtmlSource
import io.github.smiley4.ktorswaggerui.routing.ResourceContent
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = 8081, host = "localhost", module = Application::myModule).start(wait = true)
}

private fun Application.myModule() {

    install(SwaggerUI) {
        info {
            title = "Example API"
            description = "An example api to showcase basic swagger-ui functionality."
        }
        externalDocs {
            url = "https://github.com/SMILEY4/ktor-swagger-ui/wiki"
            description = "Sample external documentation"
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        server {
            url = "https://www.example.com"
            description = "Production Server"
        }
    }

    routing {
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApiSpec()
        }


        route("redoc") {
            route({ hidden = true }) {
                get {
                    call.respondRedirect("${call.request.uri}/index.html")
                }
                get("index.html") {
                    val specUrl = "/api.json"
                    val scriptUrl = "/redoc/redoc.standalone.js"
                    call.respond(HtmlSource(
                        """
                            <!DOCTYPE html>
                            <html>
                              <head>
                                <title>Redoc</title>
                                <meta charset="utf-8"/>
                                <meta name="viewport" content="width=device-width, initial-scale=1">
                                <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
                                <style>
                                  body {
                                    margin: 0;
                                    padding: 0;
                                  }
                                </style>
                              </head>
                              <body>
                                <redoc spec-url='$specUrl'></redoc>
                                <script src="$scriptUrl"> </script>
                              </body>
                            </html>
                        """.trimIndent()
                    ))
                }
                get("{filename}") {
                    serveStaticResourceRedoc(call.parameters["filename"]!!, "2.1.5", call)
                }
            }
        }

        get("hello", {
            description = "A Hello-World route"
            request {
                queryParameter<String>("name") {
                    description = "the name to greet"
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    description = "successful request - always returns 'Hello World!'"
                }
            }
        }) {
            call.respondText("Hello ${call.request.queryParameters["name"]}")
        }

    }

}


private suspend fun serveStaticResourceRedoc(filename: String, webjarVersion: String, call: ApplicationCall) {
    val resourceName = "/META-INF/resources/webjars/redoc/$webjarVersion/$filename"
    val resource = SwaggerUI::class.java.getResource(resourceName)
    if (resource != null) {
        call.respond(ResourceContent(resource))
    } else {
        call.respond(HttpStatusCode.NotFound, "$filename could not be found")
    }
}
