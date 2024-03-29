package ktp.fr

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import ktp.fr.plugins.configureRouting
import ktp.fr.plugins.configureSerialization
import ktp.fr.plugins.initDatabase

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args = args)).start(wait = true)
}

fun Application.module() {
    install(CORS) {
        allowCredentials = true
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Patch)
    }
    initDatabase()
    configureSerialization()
    configureRouting()
}