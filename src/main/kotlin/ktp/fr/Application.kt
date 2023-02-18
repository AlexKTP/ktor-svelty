package ktp.fr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ktp.fr.data.model.dao.DatabaseFactory
import ktp.fr.plugins.configureRouting
import ktp.fr.plugins.configureSerialization

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args = args)).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureRouting()
}
