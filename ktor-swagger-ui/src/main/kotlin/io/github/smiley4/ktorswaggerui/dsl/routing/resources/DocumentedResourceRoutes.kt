package io.github.smiley4.ktorswaggerui.dsl.routing.resources

import io.github.smiley4.ktorswaggerui.dsl.routes.OpenApiRoute
import io.github.smiley4.ktorswaggerui.dsl.routing.documentation
import io.github.smiley4.ktorswaggerui.dsl.routing.method
import io.ktor.http.HttpMethod
import io.ktor.server.resources.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.serializer

//============================//
//             GET            //
//============================//

inline fun <reified T : Any> Route.get(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Get) {
                handle(body)
            }
        }
    }
}

//============================//
//            POST            //
//============================//

inline fun <reified T : Any> Route.post(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Post) {
                handle(body)
            }
        }
    }
}

//============================//
//             PUT            //
//============================//

inline fun <reified T : Any> Route.put(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Put) {
                handle(body)
            }
        }
    }
}

//============================//
//           DELETE           //
//============================//

inline fun <reified T : Any> Route.delete(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Delete) {
                handle(body)
            }
        }
    }
}

//============================//
//            PATCH           //
//============================//

inline fun <reified T : Any> Route.patch(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Patch) {
                handle(body)
            }
        }
    }
}

//============================//
//           OPTIONS          //
//============================//

inline fun <reified T : Any> Route.options(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Options) {
                handle(body)
            }
        }
    }
}

//============================//
//            HEAD            //
//============================//

inline fun <reified T : Any> Route.head(
    noinline builder: OpenApiRoute.() -> Unit = { },
    noinline body: suspend io.ktor.server.routing.RoutingContext.(T) -> Unit
): Route {
    return documentation(builder) {
        resource<T> {
            method(HttpMethod.Head) {
                handle(body)
            }
        }
    }
}


public inline fun <reified T : Any> Route.handle(
    noinline body: suspend RoutingContext.(T) -> Unit
) {
    val serializer = serializer<T>()
    handle(serializer, body)
}
