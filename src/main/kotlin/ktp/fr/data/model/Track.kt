package ktp.fr.data.model

import org.jetbrains.exposed.sql.Table

@kotlinx.serialization.Serializable
data class Track(
    val id: Int? = null,
    val weight: Double,
    val chest: Double? = null,
    val abs: Double? = null,
    val hip: Double? = null,
    val bottom: Double? = null,
    val leg: Double? = null,
    val createdAt: Long,
    val toSynchronize: Int,
    val userId: Int
)

object Tracks : Table() {
    val id = integer("id").autoIncrement()
    val weight = double("weight")
    val chest = double("chest").nullable()
    val abs = double("abs").nullable()
    val hip = double("hip").nullable()
    val bottom = double("bottom").nullable()
    val leg = double("leg").nullable()
    val createdAt = long("created_ad")
    val toSynchronize = integer("to_synchronize")
    val userID = integer("user_id").references(Heroes.id)
    override val primaryKey = PrimaryKey(id, name = "PK_Track_Id")
}