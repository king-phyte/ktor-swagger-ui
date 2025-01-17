package io.github.smiley4.ktoropenapi.examples

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.resources.delete
import io.github.smiley4.ktoropenapi.resources.get
import io.github.smiley4.ktoropenapi.resources.post
import io.github.smiley4.ktorredoc.redoc
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.github.smiley4.schemakenerator.serialization.processKotlinxSerialization
import io.github.smiley4.schemakenerator.swagger.compileReferencingRoot
import io.github.smiley4.schemakenerator.swagger.data.TitleType
import io.github.smiley4.schemakenerator.swagger.generateSwaggerSchema
import io.github.smiley4.schemakenerator.swagger.handleCoreAnnotations
import io.github.smiley4.schemakenerator.swagger.withTitle
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.resources.Resources
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost", module = Application::myModule).start(wait = true)
}

private fun Application.myModule() {

    install(Resources)

    install(OpenApi) {
        // enable automatically extracting documentation from resources-routes
        autoDocumentResourcesRoutes = true
        // schema-generator must use kotlinx-serialization to be compatible with resources
        schemas {
            generator = { type ->
                type
                    .processKotlinxSerialization()
                    .generateSwaggerSchema()
                    .withTitle(TitleType.SIMPLE)
                    .handleCoreAnnotations()
                    .compileReferencingRoot()
            }
        }
    }

    routing {

        route("api.json") {
            openApi()
        }
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("redoc") {
            redoc("/api.json")
        }

        get<PetsRoute.All>({
            description = "custom description"
            request {
                queryParameter<String>("tags") {
                    description = "sample description"
                }
            }
        }) { request ->
            println("..${request.tags}, ${request.limit}")
            call.respond(HttpStatusCode.NotImplemented, Unit)
        }

        get<PetsRoute.Id> { request ->
            println("..${request.id}")
            call.respond(HttpStatusCode.NotImplemented, Unit)
        }

        delete<PetsRoute.Id.Delete> { request ->
            println("..${request.parent.id}")
            call.respond(HttpStatusCode.NotImplemented, Unit)
        }

        post<PetsRoute.Id.New> { request ->
            println("..${request.parent.id}")
            call.respond(HttpStatusCode.NotImplemented, Unit)
        }

    }

}


@Resource("/pets")
class PetsRoute {

    @Resource("/")
    class All(
        val parent: PetsRoute,
        val tags: List<String> = emptyList(),
        val limit: Int = 100
    )


    @Resource("{id}")
    class Id(
        val parent: PetsRoute,
        val id: Long
    ) {

        @Resource("/")
        class Delete(
            val parent: Id
        )


        @Resource("/")
        class New(
            val parent: Id,
        )

    }

}


@Serializable
data class Pet(
    val name: String,
    val tag: String
)