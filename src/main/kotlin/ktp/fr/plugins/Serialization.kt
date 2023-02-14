package ktp.fr.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ktp.fr.data.model.Track
import ktp.fr.data.model.dao.DAOFacade
import ktp.fr.data.model.dao.DAOFacadeImpl

fun Application.configureSerialization() {

    val dao: DAOFacade = DAOFacadeImpl()
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/track") {
            val id: Int? = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
            }
            val track = dao.getTrack(id!!)
            if (track != null) {
                call.respond(track)
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }

        get("/tracks"){
            call.respond(dao.allTracks())
        }

        post("/track") {
            val requestBody = call.receive<Track>()
            val track = dao.addNewTrack(
                requestBody.weight,
                requestBody.chest,
                requestBody.abs,
                requestBody.hip,
                requestBody.bottom,
                requestBody.leg,
                requestBody.createdAt,
                requestBody.toSynchronize
            )
            if (track != null) {
                call.respond(track)
            } else {
                call.respond("Une erreur a eu lieu lors de l'ajout de cette prise de poids")
            }
        }
    }
}
