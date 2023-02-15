package ktp.fr.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import ktp.fr.data.model.Track
import ktp.fr.data.model.dao.DAOFacade
import ktp.fr.data.model.dao.DAOFacadeImpl

fun Application.configureRouting() {

    val dao: DAOFacade = DAOFacadeImpl()

    routing {

        get("/") {
            call.respondText("## WELCOME on KTOR-SVELTY V.0.0.1\n" +
                    "To display the most useful CLI, please go to «/help»", ContentType.Text.Plain, HttpStatusCode.OK)
        }

        get("/help") {
            call.respondText("## HELP\nIn this version: routes available are:\n" +
                    "- /track (GET) It requires an id as parameter.\n" +
                    "- /track (POST) To create a new Track.\n" +
                    "- /tracks (GET) To get all tracks recorded.")
        }

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
