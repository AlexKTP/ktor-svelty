package ktp.fr.data.model

import org.jetbrains.exposed.sql.Table

@kotlinx.serialization.Serializable
data class Goal(
    val id: Int? = null,
    val weight: Double,
    val deadLine: Long,
    val userId: Int
)

object Goals : Table() {
    val id = integer("id").autoIncrement()
    val weight = double("weight")
    val deadLine = long("dead_line")
    val userID = integer("user_id").references(Heroes.id)
    override val primaryKey = PrimaryKey(id, name = "PK_Target_Id")
}