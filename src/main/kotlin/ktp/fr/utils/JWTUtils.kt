package ktp.fr.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

fun ApplicationCall.validateToken() {

    val principal = authentication.principal<JWTPrincipal>()
    val now: Date = Date.from(Instant.now())

    if(principal == null || principal.payload.expiresAt < now){
        throw Exception("Token is not valid or has expired")
    }
}


fun ApplicationCall.generateToken(login: String, expirationDate: Instant?): String = JWT.create()
    .withIssuer(application.environment.config.property("jwt.issuer").getString())
    .withClaim("login", login)
    .withExpiresAt(Date.from(expirationDate ?: LocalDateTime.now().plusMinutes(5).toInstant(ZoneOffset.UTC)))
    .sign(Algorithm.HMAC256(application.environment.config.property("jwt.secret").getString()))


fun ApplicationCall.refreshToken(login: String): String = JWT.create()
    .withIssuer(application.environment.config.property("jwt.issuer").getString())
    .withClaim("login", login)
    .withExpiresAt(Date.from(LocalDateTime.now().plusMinutes(3).toInstant(ZoneOffset.UTC)))
    .sign(Algorithm.HMAC256(application.environment.config.property("jwt.secret").getString()))


fun ApplicationCall.expireSoon( now: Date): Boolean {
    val principal = authentication.principal<JWTPrincipal>() ?: throw Exception("Token is not valid or has expired")
    if(principal.expiresAt == null){
        throw Exception("Token is not valid or has expired")
    }
    val diff: Long = principal.expiresAt!!.time - now.time
    val diffMinutes = diff / (60 * 1000) % 60
    return diffMinutes in 0..2
}