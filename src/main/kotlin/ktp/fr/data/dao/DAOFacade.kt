package ktp.fr.data.model.dao

import ktp.fr.data.model.Track

interface DAOFacade {
    suspend fun allTracks(): List<Track>
    suspend fun getTrack(id: Int): Track?
    suspend fun addNewTrack(weight: Double, chest: Double?, abs: Double?, hip: Double?,
    bottom: Double?, leg: Double?, createdAt: Long, toSynchronize: Int): Track?
    suspend fun deleteTrack(id: Int): Boolean
}