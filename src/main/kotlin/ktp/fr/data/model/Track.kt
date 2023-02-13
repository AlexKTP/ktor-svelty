package ktp.fr.data.model

@kotlinx.serialization.Serializable
data class Track(
    val id: Int? =null ,
    val weight: Double,
    val chest: Double?=null,
    val abs: Double?=null,
    val hip: Double?=null,
    val bottom: Double?=null,
    val leg: Double?=null,
    val createdAt: Long,
    val toSynchronize: Int
)