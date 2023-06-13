package ktp.fr.data.model

@kotlinx.serialization.Serializable
data class HeroProfileDTO(
    val id: Int? = null,
    val username: String? = null,
    val goal: Goal? = null
)
