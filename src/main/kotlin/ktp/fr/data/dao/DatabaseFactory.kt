package ktp.fr.data.model.dao

import kotlinx.coroutines.Dispatchers
import ktp.fr.data.model.Heroes
import ktp.fr.data.model.Tracks
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Tracks, Heroes)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}