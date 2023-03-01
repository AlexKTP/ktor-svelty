package ktp.fr

import io.ktor.server.application.Application
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import ktp.fr.plugins.configureRouting
import ktp.fr.plugins.configureSerialization
import ktp.fr.plugins.initDatabase

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args = args)).start(wait = true)
}

fun Application.module() {
    initDatabase()
    configureSerialization()
    configureRouting()
}
