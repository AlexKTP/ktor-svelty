package ktp.fr.plugins

import io.ktor.server.application.Application
import ktp.fr.data.model.Goals
import ktp.fr.data.model.Heroes
import ktp.fr.data.model.Tracks
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDatabase() {
    val username: String = environment.config.property("db.user").getString()
    val pwd: String = environment.config.property("db.pwd").getString()
    val db: String = environment.config.property("db.name").getString()
    val driverClassName = "com.mysql.cj.jdbc.Driver"
    val jdbcURL = "jdbc:mysql://localhost:3306/${db}?serverTimezone=UTC&user=${username}&password=${pwd}&useSSL=false"
    val database = Database.connect(jdbcURL, driverClassName, username, pwd)
    transaction(database) {
        SchemaUtils.create(Tracks, Heroes, Goals)
    }
}