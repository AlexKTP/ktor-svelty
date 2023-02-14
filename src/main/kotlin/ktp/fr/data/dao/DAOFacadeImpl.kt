package ktp.fr.data.model.dao

import kotlinx.coroutines.runBlocking
import ktp.fr.data.model.Track
import ktp.fr.data.model.Tracks
import ktp.fr.data.model.dao.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class DAOFacadeImpl : DAOFacade {

    private fun resultRowToTrack(row: ResultRow) = Track(
        id = row[Tracks.id],
        weight = row[Tracks.weight],
        abs = row[Tracks.abs],
        hip = row[Tracks.hip],
        bottom = row[Tracks.bottom],
        leg = row[Tracks.leg],
        createdAt = row[Tracks.createdAt],
        toSynchronize = row[Tracks.toSynchronize]
    )

    override suspend fun allTracks(): List<Track> = dbQuery {
        Tracks.selectAll().map { resultRow -> resultRowToTrack(resultRow) }
    }

    override suspend fun getTrack(id: Int): Track? = dbQuery {
        Tracks.select { Tracks.id eq id }
            .map { resultRow -> resultRowToTrack(resultRow) }
            .singleOrNull()
    }

    override suspend fun addNewTrack(
        weight: Double,
        chest: Double?,
        abs: Double?,
        hip: Double?,
        bottom: Double?,
        leg: Double?,
        createdAt: Long,
        toSynchronize: Int
    ): Track? = dbQuery {
        val insertStatement = Tracks.insert {
            it[Tracks.weight] = weight
            it[Tracks.chest] = chest
            it[Tracks.abs] = abs
            it[Tracks.hip] = hip
            it[Tracks.bottom] = bottom
            it[Tracks.leg] = leg
            it[Tracks.createdAt] = createdAt
            it[Tracks.toSynchronize] = toSynchronize
        }
        insertStatement.resultedValues?.singleOrNull()?.let {resultRow ->  resultRowToTrack(resultRow)}

    }

    override suspend fun deleteTrack(id: Int): Boolean = dbQuery {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }
}



