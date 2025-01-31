package io.github.smiley4.ktorswaggerui.misc

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.OutputFormat
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication
import kotlin.test.Test

class RoutingTests {

    @Test
    fun basicRouting() = swaggerUITestApplication {
        get("hello").also {
            it.status shouldBe HttpStatusCode.OK
            it.body shouldBe "Hello Test"
        }
        get("/").also {
            it.status shouldBe HttpStatusCode.NotFound
        }
        get("/swagger").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Html
            it.body.shouldNotBeEmpty()
        }
        get("/swagger/index.html").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Html
            it.body.shouldNotBeEmpty()
        }
        get("/swagger/swagger-initializer.js").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Application.JavaScript
            it.body shouldContain "url: \"/api.json\""
        }
        get("/api.json").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Application.Json
            it.body.shouldNotBeEmpty()
            it.body shouldStartWith "{\n  \"openapi\" : \"3.1.0\","
        }
    }


    @Test
    fun basicRoutingYml() = swaggerUITestApplication(OutputFormat.YAML) {
        get("hello").also {
            it.status shouldBe HttpStatusCode.OK
            it.body shouldBe "Hello Test"
        }
        get("/").also {
            it.status shouldBe HttpStatusCode.NotFound
        }
        get("/swagger").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Html
            it.body.shouldNotBeEmpty()
        }
        get("/swagger/index.html").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Html
            it.body.shouldNotBeEmpty()
        }
        get("/swagger/swagger-initializer.js").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Application.JavaScript
            it.body shouldContain "url: \"/api.yml\""
        }
        get("/api.yml").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Plain.withParameter("charset", "utf-8")
            it.body.shouldNotBeEmpty()
            it.body shouldStartWith "openapi: 3.1.0\n"
        }
    }


    @Test
    fun appRootPath() = swaggerUITestApplication(OutputFormat.YAML, "app" to "app/root/path") {
        get("/api.yml").also {
            it.status shouldBe HttpStatusCode.NotFound
        }
        get("/app/root/path/api.yml").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Plain.withParameter("charset", "utf-8")
            it.body.shouldNotBeEmpty()
            it.body shouldStartWith "openapi: 3.1.0\n"
            it.body shouldContain "app/root/path/hello:"
        }
    }


    @Test
    fun envRootPath() = swaggerUITestApplication(OutputFormat.YAML, "env" to "env/root/path") {
        // node: specifying rootPath via env in test does not apply to routes. It should still be added to routes in openapi documentation
        get("/api.yml").also {
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Plain.withParameter("charset", "utf-8")
            it.body.shouldNotBeEmpty()
            it.body shouldStartWith "openapi: 3.1.0\n"
            it.body shouldContain "env/root/path/hello:"
        }
    }

    @Test
    fun envAndAppRootPath() = swaggerUITestApplication(OutputFormat.YAML, "both" to "*/root/path") {
        get("/app/root/path/api.yml").also { // app root path takes priority
            it.status shouldBe HttpStatusCode.OK
            it.contentType shouldBe ContentType.Text.Plain.withParameter("charset", "utf-8")
            it.body.shouldNotBeEmpty()
            it.body shouldStartWith "openapi: 3.1.0\n"
            it.body shouldContain "app/root/path/hello:"
        }
    }

    private fun swaggerUITestApplication(
        format: OutputFormat = OutputFormat.JSON,
        rootPath: Pair<String, String>? = null,
        block: suspend TestContext.() -> Unit
    ) {
        testApplication {
            application {
                rootPath?.also {
                    if (it.first == "app") {
                        this.rootPath = it.second
                    }
                    if(it.first == "both") {
                        this.rootPath = it.second.replace("*", "app")
                    }
                }
            }
            environment {
                rootPath?.also {
                    if (it.first == "env") {
                        config = config.mergeWith(MapApplicationConfig("ktor.deployment.rootPath" to it.second))
                    }
                    if (it.first == "both") {
                        config = config.mergeWith(MapApplicationConfig("ktor.deployment.rootPath" to it.second.replace("*", "env")))
                    }
                }
            }
            val client = createClient {
                this.followRedirects = followRedirects
            }
            install(SwaggerUI) {
                outputFormat = format
            }
            routing {
                val routeSuffix = when (format) {
                    OutputFormat.JSON -> "json"
                    OutputFormat.YAML -> "yml"
                }
                route("api.$routeSuffix") {
                    openApiSpec()
                }
                route("swagger") {
                    swaggerUI("/api.$routeSuffix")
                }
                get("hello") {
                    call.respondText("Hello Test")
                }
            }
            TestContext(client).apply { block() }
        }
    }

    class TestContext(private val client: HttpClient) {

        suspend fun get(path: String): GetResult {
            return client.get(path)
                .let {
                    GetResult(
                        path = path,
                        status = it.status,
                        contentType = it.contentType(),
                        body = it.bodyAsText(),
                        redirect = it.headers["Location"]
                    )
                }
                .also { it.print() }
        }


        private fun GetResult.print() {
            println("GET ${this.path}  =>  ${this.status} (${this.contentType}): ${this.body}")
        }
    }

    data class GetResult(
        val path: String,
        val status: HttpStatusCode,
        val contentType: ContentType?,
        val body: String,
        val redirect: String?
    )

}
