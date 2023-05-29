package ktp.fr.data.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

@kotlinx.serialization.Serializable
data class Hero(
    val id: Int? = null,
    val username: String,
    val login: String,
    val password: String,
    val creationDate: kotlinx.datetime.LocalDateTime?,
    val lastModificationDate: kotlinx.datetime.LocalDateTime?
)

object Heroes : Table() {
    val id = integer("id").autoIncrement()
    val userName = varchar("username", 255).nullable()
    val login = varchar("login", 255)
    val password = varchar("password", 255)
    val creationDate = datetime("creation_date").defaultExpression(CurrentDateTime)
    val lastModificationDate = datetime("last_mod_date").defaultExpression(CurrentDateTime)
    override val primaryKey = PrimaryKey(id, name = "PK_Hero_Id")
}

fun Hero.toJsonString():String{
    val mapper = ObjectMapper()
    val jsonNode: ObjectNode = mapper.createObjectNode()
    jsonNode.put("id", this.id)
    jsonNode.put("username", this.username)
    jsonNode.put("login", this.login)
    jsonNode.put("password", "")
    jsonNode.put("creationDate", this.creationDate.toString())
    jsonNode.put("lastModificationDate", this.lastModificationDate.toString())
    return jsonNode.toPrettyString()
}