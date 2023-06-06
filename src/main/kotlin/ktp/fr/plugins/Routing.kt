package ktp.fr.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktp.fr.data.model.Goal
import ktp.fr.data.model.Hero
import ktp.fr.data.model.Track
import ktp.fr.data.model.dao.DAOFacade
import ktp.fr.data.model.dao.DAOFacadeImpl
import ktp.fr.utils.hashPassword
import ktp.fr.validateToken
import org.mindrot.jbcrypt.BCrypt


fun Application.configureRouting() {

    val dao: DAOFacade = DAOFacadeImpl()

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    fun generateToken(login: String): String = JWT.create()
        .withIssuer(issuer)
        .withClaim("login", login)
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256(secret))


    suspend fun verifyPassword(password: String, hashedPassword: String): Boolean = withContext(Dispatchers.Default) {
        // Compare the provided password with the stored hashed password using BCrypt's checkpw function
        BCrypt.checkpw(password, hashedPassword)
    }

    fun stringToJson(message: String): String {
        val mapper = ObjectMapper()
        val jsonNode: ObjectNode = mapper.createObjectNode()
        return jsonNode.put("message", message).toPrettyString()
    }



    routing {

        /////////////////////////////////////////////////////////////
        // GENERAL
        /////////////////////////////////////////////////////////////

        get("/") {
            call.respondText(
                "## WELCOME on KTOR-SVELTY V.0.0.1\n" +
                        "To display the most useful CLI, please go to «/help»",
                ContentType.Text.Plain,
                HttpStatusCode.OK
            )
        }

        get("/help") {
            call.respondText(
                "## HELP\nIn this version: routes available are:\n" +
                        "- /track (GET) It requires an id as parameter.\n" +
                        "- /track (POST) To create a new Track.\n" +
                        "- /tracks (GET) To get all tracks recorded."
            )
        }

        /////////////////////////////////////////////////////////////
        // USERS
        /////////////////////////////////////////////////////////////


        post("/register") {
            val hero = call.receive<Hero>()
            val credentials: Pair<String?, String?> = Pair(hero.login, hero.password)
            if (credentials.first.isNullOrBlank() || credentials.second.isNullOrEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Oops, something went wrong. Please try with a valid login and password."
                )
            } else {
                val heroInserted: Hero? =
                    dao.insertNewHero(null, null, credentials.first!!, credentials.second!!.hashPassword())
                if (heroInserted?.id == null) call.respond(
                    HttpStatusCode.InternalServerError,
                    "Oops, something went wrong. Please, Try later."
                )
                else call.respond(
                    HttpStatusCode.OK,
                    stringToJson("Welcome. Never forget, Triumph without peril, brings no glory!")
                )
            }
        }

        post("/login") {
            val hero = call.receive<Hero>()
            if (dao.findHeroByLogin(hero.login) == null) call.respond(
                HttpStatusCode.NotFound,
                "Oops, Something goes wrong. Please, check your login and/or your password."
            )
            call.respond(hashMapOf("token" to generateToken(hero.login), "login" to hero.login))
        }

        post("/quit") {
            val hero = call.receive<Hero>()
            if (dao.findHeroByLogin(hero.login) == null) call.respond(
                HttpStatusCode.NotFound,
                "Oops, Something goes wrong. Please, check your login and/or your password."
            ) else {
                if (dao.deleteHeroByLogin(hero.login)) {
                    call.respond(HttpStatusCode.OK, "I hope you've reached your goal...")
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Oops, Something goes wrong... Please, try later..."
                    )
                }
            }
        }


        /////////////////////////////////////////////////////////////
        // USERS
        /////////////////////////////////////////////////////////////

        authenticate("auth-jwt") {

            intercept(ApplicationCallPipeline.Call) {
                call.validateToken()
            }

            get("/token") {
                call.respond(HttpStatusCode.OK)
            }

            get("/users") {
                val heroes = dao.getAllHeroes()
                if (heroes.isEmpty()) call.respond(HttpStatusCode.NoContent)
                else call.respond(dao.getAllHeroes())
            }






            post("/quit") {
                val hero = call.receive<Hero>()
                if (dao.findHeroByLogin(hero.login) == null) call.respond(
                    HttpStatusCode.NotFound,
                    "Oops, Something goes wrong. Please, check your login and/or your password."
                ) else {
                    if (dao.deleteHeroByLogin(hero.login)) {
                        call.respond(HttpStatusCode.OK, "I hope you've reached your goal...")
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Oops, Something goes wrong... Please, try later..."
                        )
                    }
                }
            }


            /////////////////////////////////////////////////////////////
            // TRACKS
            /////////////////////////////////////////////////////////////

            get("/track") {
                val id: Int? = call.parameters["id"]?.toInt()
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (id == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                val track = dao.getTrack(id!!, userId!!)
                if (track != null) {
                    call.respond(track)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            get("/tracks") {
                val userId: Int? = call.parameters["userId"]?.toInt()
                if (userId == null) call.respond(HttpStatusCode.BadRequest)
                else call.respond(dao.allTracks(userId))
            }

            post("/track") {
                val requestBody = call.receive<Track>()
                var track: Track? = dao.getTrack(requestBody.createdAt, requestBody.userId);
                if (track == null) {
                    track = dao.addNewTrack(
                        requestBody.weight,
                        requestBody.chest,
                        requestBody.abs,
                        requestBody.hip,
                        requestBody.bottom,
                        requestBody.leg,
                        requestBody.createdAt,
                        requestBody.toSynchronize,
                        requestBody.userId
                    )
                } else {
                    track = dao.updateTrack(
                        track.id!!,
                        requestBody.weight,
                        requestBody.chest,
                        requestBody.abs,
                        requestBody.hip,
                        requestBody.bottom,
                        requestBody.leg,
                        requestBody.userId,
                        requestBody.createdAt
                    )
                }
                if (track != null) {
                    call.respond(HttpStatusCode.OK, track)
                } else {
                    call.respond("Oops, something went wrong. Please, try again.")
                }
            }

            /////////////////////////////////////////////////////////////
            // GOAL
            /////////////////////////////////////////////////////////////

            get("/target") {
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                val target = dao.getTargetUser(userId!!)
                if (target != null) {
                    call.respond(HttpStatusCode.OK, target)
                } else {
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            post("/target") {
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                val requestBody = call.receive<Goal>()
                var goal: Goal? = dao.getTargetUser(userId!!);
                if (goal == null) {
                    goal = dao.insertNewGoal(
                        requestBody.weight,
                        requestBody.deadLine,
                        requestBody.userId
                    )
                } else {
                    goal = dao.updateGoal(
                        goal.id!!,
                        requestBody.weight,
                        requestBody.deadLine,
                        requestBody.userId
                    )
                }
                if (goal != null) {
                    call.respond(HttpStatusCode.OK, goal)
                } else {
                    call.respond("Oops, something went wrong. Please, try again.")
                }
            }
        }
    }
}

