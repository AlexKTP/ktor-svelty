package ktp.fr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ktp.fr.data.model.dao.DatabaseFactory
import ktp.fr.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureRouting()
}
