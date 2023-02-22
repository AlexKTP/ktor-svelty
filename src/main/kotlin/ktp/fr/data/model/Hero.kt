package ktp.fr.data.model

import org.jetbrains.exposed.sql.Table

@kotlinx.serialization.Serializable
data class Hero(
    val id: Int? = null,
    val username: String? = null,
    val login: String,
    val password: String
)

object Heroes : Table() {
    val id = integer("id").autoIncrement()
    val userName = varchar("username", 255).nullable()
    val login = varchar("login", 255)
    val password = varchar("password", 255)
    override val primaryKey = PrimaryKey(id, name = "PK_Hero_Id")
}