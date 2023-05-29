package ktp.fr.data.model.dao

import kotlinx.datetime.toKotlinLocalDateTime
import ktp.fr.data.model.Hero
import ktp.fr.data.model.Heroes
import ktp.fr.data.model.Track
import ktp.fr.data.model.Tracks
import ktp.fr.data.model.dao.DatabaseFactory.dbQuery
import ktp.fr.utils.hashPassword
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class DAOFacadeImpl : DAOFacade {

    private fun resultRowToTrack(row: ResultRow) = Track(
        id = row[Tracks.id],
        weight = row[Tracks.weight],
        abs = row[Tracks.abs],
        hip = row[Tracks.hip],
        bottom = row[Tracks.bottom],
        leg = row[Tracks.leg],
        createdAt = row[Tracks.createdAt],
        toSynchronize = row[Tracks.toSynchronize],
        userId = row[Tracks.userID]
    )

    private fun resultRowToHero(row: ResultRow) = Hero(
        id = row[Heroes.id],
        username = row[Heroes.userName].toString(),
        login = row[Heroes.login].toString(),
        password = "",
        creationDate = row[Heroes.creationDate].toKotlinLocalDateTime(),
        lastModificationDate = row[Heroes.lastModificationDate].toKotlinLocalDateTime()
    )

    override suspend fun allTracks(userID: Int): List<Track> = dbQuery {
        Tracks.select { Tracks.userID eq userID }
            .map { resultRow -> resultRowToTrack(resultRow) }
            .toList()
    }

    override suspend fun getTrack(id: Int, userID: Int): Track? = dbQuery {
        Tracks.select { Tracks.id.eq(id) and Tracks.userID.eq(userID) }
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
        toSynchronize: Int,
        userID: Int
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
            it[Tracks.userID] = userID
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRow -> resultRowToTrack(resultRow) }
    }

    override suspend fun deleteTrack(id: Int, userID: Int) = dbQuery {
        Tracks.deleteWhere { Tracks.id.eq(id) and Tracks.userID.eq(userID) } > 0
    }

    override suspend fun insertNewHero(id: Int?, username: String?, login: String, password: String): Hero? = dbQuery {
        val insertStatement = Heroes.insert {
            it[Heroes.userName] = username
            it[Heroes.login] = login
            it[Heroes.password] = password.hashPassword()
            it[Heroes.creationDate] = java.time.LocalDateTime.now()
            it[Heroes.lastModificationDate] = java.time.LocalDateTime.now()
        }
        insertStatement.resultedValues?.singleOrNull()?.let { resultRow ->
            resultRowToHero(resultRow)
        }
    }

    override suspend fun getAllHeroes(): List<Hero> = dbQuery {
        Heroes.selectAll()
            .map { resultRow -> resultRowToHero(resultRow) }
            .toList()
    }

    override suspend fun findHeroByUsername(username: String): Hero? = dbQuery {
        Heroes.select(where = Heroes.userName eq username)
            .map { resultRow -> resultRowToHero(resultRow) }
            .firstOrNull()
    }

    override suspend fun findHeroByLogin(login: String): Hero? = dbQuery {
        Heroes.select(where = Heroes.login eq login)
            .map { resultRow -> resultRowToHero(resultRow) }
            .firstOrNull()
    }

    override suspend fun deleteHeroByLogin(login: String): Boolean = dbQuery {
        Heroes.deleteWhere { Heroes.login eq login } >-1
    }

}



