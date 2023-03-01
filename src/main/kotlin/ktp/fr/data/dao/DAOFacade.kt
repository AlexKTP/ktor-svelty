package ktp.fr.data.model.dao

import ktp.fr.data.model.Hero
import ktp.fr.data.model.Track

interface DAOFacade {
    suspend fun allTracks(userID: Int): List<Track>
    suspend fun getTrack(id: Int, userID: Int): Track?
    suspend fun addNewTrack(
        weight: Double,
        chest: Double?,
        abs: Double?,
        hip: Double?,
        bottom: Double?,
        leg: Double?,
        createdAt: Long,
        toSynchronize: Int,
        userID: Int
    ): Track?
    suspend fun deleteTrack(id: Int, userID: Int): Boolean
    suspend fun insertNewHero(id: Int?, username: String?, login: String, password: String): Hero?
    suspend fun getAllHeroes(): List<Hero>
    suspend fun findHeroByUsername(username: String): Hero?
    suspend fun findHeroByLogin(login: String): Hero?
    suspend fun deleteHeroByLogin(login: String): Boolean
}
