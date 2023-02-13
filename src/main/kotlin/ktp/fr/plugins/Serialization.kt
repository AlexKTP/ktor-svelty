package ktp.fr.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ktp.fr.data.model.Track

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    val tracks = mutableListOf<Track>()

    routing {
        get("/track") {
                call.respond(tracks)
            }
        post("/track"){
            val requestBody = call.receive<Track>()
            tracks.add(requestBody)
            call.respond(requestBody)
        }
    }
}
