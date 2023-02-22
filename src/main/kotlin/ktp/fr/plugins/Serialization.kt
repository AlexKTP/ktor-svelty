package ktp.fr.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*


fun Application.configureSerialization() {

    install(ContentNegotiation) {
        json()
    }

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                if (credential.payload.getClaim("login").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _ , _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }

        }
    }

}
