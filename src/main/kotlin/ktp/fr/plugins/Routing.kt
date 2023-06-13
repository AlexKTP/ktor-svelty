package ktp.fr.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.time.Instant
import java.util.Date
import javax.security.sasl.AuthenticationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ktp.fr.data.model.Goal
import ktp.fr.data.model.Hero
import ktp.fr.data.model.HeroProfileDTO
import ktp.fr.data.model.Track
import ktp.fr.data.model.dao.DAOFacade
import ktp.fr.data.model.dao.DAOFacadeImpl
import ktp.fr.data.model.toJsonString
import ktp.fr.utils.expireSoon
import ktp.fr.utils.generateToken
import ktp.fr.utils.hashPassword
import ktp.fr.utils.refreshToken
import ktp.fr.utils.validateToken
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun Application.configureRouting() {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    val dao: DAOFacade = DAOFacadeImpl()

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
            logger.debug("Registering a new user")
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
                if (heroInserted?.id == null) {
                    logger.debug("Error while registering a new user")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Oops, something went wrong. Please, Try later."
                    )
                } else {
                    logger.debug("New user registered", heroInserted.id)
                    call.respond(
                        HttpStatusCode.OK,
                        stringToJson("Welcome. Never forget, Triumph without peril, brings no glory!")
                    )
                }
            }
        }

        post("/login") {
            logger.debug("Login a user")
            val hero = call.receive<Hero>()
            val storedHero = dao.findHeroByLogin(hero.login)
            val check = verifyPassword(hero.password, storedHero!!.password)

            if (storedHero == null || !check) {
                logger.debug("Error while login a user")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    stringToJson("Oops, Something goes wrong. Please, check your login and/or your password.")
                )
            } else {
                logger.debug("User logged in", storedHero.id)
                call.respond(
                    hashMapOf(
                        "token" to call.generateToken(hero.login, null),
                        "hero" to storedHero.toJsonString()
                    )
                )
            }
        }

        post("/logout") {
            logger.debug("Logout a user")
            val userId: Int? = call.parameters["userId"]?.toInt()
            val isPresent = userId != null
            if (!isPresent) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    stringToJson("Oops, Something goes wrong. Please, check your login and/or your password.")
                )
            }
            val storedHero = dao.findHeroById(userId!!)
            if (storedHero != null) {
                logger.debug("User logged out", storedHero.id)
                call.respond(
                    HttpStatusCode.OK,
                    hashMapOf(
                        "token" to call.generateToken(storedHero.login, Instant.now().minusSeconds(60)),
                        "hero" to storedHero.toJsonString()
                    )
                )
            } else {
                logger.debug("Error while logout a user")
                call.respond(
                    HttpStatusCode.Unauthorized,
                    stringToJson("Oops, Something goes wrong. Please, check your login and/or your password.")
                )
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
                val id: Int? = call.parameters["id"]?.toInt()
                if(id ==null) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                val storedHero = dao.findHeroById(id!!)
                if(call.expireSoon(Date.from(Instant.now()))){
                    call.respond(HttpStatusCode.OK, hashMapOf("token" to call.refreshToken(storedHero!!.login)))
                }else call.respond(HttpStatusCode.OK)
            }

            get("/users") {
                logger.debug("Get all users")
                val heroes = dao.getAllHeroes()
                if (heroes.isEmpty()) call.respond(HttpStatusCode.NoContent)
                else call.respond(dao.getAllHeroes())
            }

            get("/user") {
                logger.debug("Get a user")
                val id: Int? = call.parameters["id"]?.toInt()
                if (id == null) call.respond(HttpStatusCode.BadRequest)
                else {
                    val hero = dao.findHeroById(id)
                    if (hero == null) call.respond(HttpStatusCode.NotFound)
                    else call.respond(HttpStatusCode.OK, hero)
                }
            }

            post("/updateUser") {
                logger.debug("Update a user")
                val hero = call.receive<HeroProfileDTO>()
                val storedHero = dao.findHeroById(hero.id!!)
                if (storedHero == null){
                    call.respond(
                        HttpStatusCode.NotFound,
                        "Oops, Something goes wrong. Please, check your login and/or your password."
                    )
                } else {
                    val heroUpdated = dao.updateHeroProfile(hero.id!!, hero.username, hero.goal!! )
                    if (heroUpdated == null) call.respond(
                        HttpStatusCode.InternalServerError,
                        "Oops, Something goes wrong... Please, try later..."
                    )
                    else call.respond(HttpStatusCode.OK, heroUpdated)

                }
            }

            get("/userProfile") {
                logger.debug("Get a user profile")
                val id: Int? = call.parameters["id"]?.toInt()
                if (id == null) call.respond(HttpStatusCode.BadRequest)
                else {
                    val hero = dao.getHeroProfile(id)
                    if (hero == null) call.respond(HttpStatusCode.NotFound)
                    else call.respond(HttpStatusCode.OK, hero)
                }
            }






            post("/quit") {
                logger.debug("Delete a user")
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
                logger.debug("Get a track")
                val id: Int? = call.parameters["id"]?.toInt()
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (id == null || userId == null) {
                    logger.debug("Error while getting a track")
                    call.respond(HttpStatusCode.BadRequest)
                }
                val track = dao.getTrack(id!!, userId!!)
                if (track != null) {
                    logger.debug("Track found", track.id)
                    call.respond(track)
                } else {
                    logger.debug("Track not found", id)
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            get("/tracks") {
                logger.debug("Get all tracks")
                val userId: Int? = call.parameters["userId"]?.toInt()
                if (userId == null) {
                    logger.debug("Error while getting all tracks")
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    logger.debug("All tracks found")
                    call.respond(dao.allTracks(userId))
                }
            }

            post("/track") {
                logger.debug("Create a new track")
                val requestBody = call.receive<Track>()
                var track: Track? = dao.getTrack(requestBody.createdAt, requestBody.userId)
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
                    logger.debug("Track created", track.id)
                    call.respond(HttpStatusCode.OK, track)
                } else {
                    logger.debug("Error while creating a new track")
                    call.respond("Oops, something went wrong. Please, try again.")
                }
            }

            /////////////////////////////////////////////////////////////
            // GOAL
            /////////////////////////////////////////////////////////////

            get("/target") {
                logger.debug("Get a target")
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (userId == null) {
                    logger.debug("Error while getting a target")
                    call.respond(HttpStatusCode.BadRequest)
                }
                val target = dao.getTargetUser(userId!!)
                if (target != null) {
                    logger.debug("Target found", target.id)
                    call.respond(HttpStatusCode.OK, target)
                } else {
                    logger.debug("Target not found", userId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }

            post("/target") {
                logger.debug("Create a new target")
                val userId: Int? = call.parameters["userid"]?.toInt()
                if (userId == null) {
                    logger.debug("Error while creating a new target")
                    call.respond(HttpStatusCode.BadRequest)
                }
                val requestBody = call.receive<Goal>()
                var goal: Goal? = dao.getTargetUser(userId!!)
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
                    logger.debug("Target created", goal.id)
                    call.respond(HttpStatusCode.OK, goal)
                } else {
                    logger.debug("Error while creating a new target")
                    call.respond("Oops, something went wrong. Please, try again.")
                }
            }
        }
    }
}



