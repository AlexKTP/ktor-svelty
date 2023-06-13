package ktp.fr.data.model.dao


import java.time.ZoneOffset
import kotlinx.datetime.toKotlinLocalDateTime
import ktp.fr.data.model.Goal
import ktp.fr.data.model.Goals
import ktp.fr.data.model.Hero
import ktp.fr.data.model.HeroProfileDTO
import ktp.fr.data.model.Heroes
import ktp.fr.data.model.Track
import ktp.fr.data.model.Tracks
import ktp.fr.data.model.dao.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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
        password = row[Heroes.password].toString(),
        creationDate = row[Heroes.creationDate].toKotlinLocalDateTime(),
        lastModificationDate = row[Heroes.lastModificationDate].toKotlinLocalDateTime()
    )

    private fun resultRowToGoal(row: ResultRow) = Goal(
        id = row[Goals.id],
        weight = row[Goals.weight],
        deadLine = row[Goals.deadLine],
        userId = row[Goals.userID]
    )

    override suspend fun allTracks(userID: Int): List<Track> = dbQuery {
        Tracks.select { Tracks.userID eq userID }.orderBy(Tracks.createdAt to SortOrder.ASC)
            .map { resultRow -> resultRowToTrack(resultRow) }
            .toList()
    }

    override suspend fun getTrack(id: Int, userID: Int): Track? = dbQuery {
        Tracks.select { Tracks.id.eq(id) and Tracks.userID.eq(userID) }
            .map { resultRow -> resultRowToTrack(resultRow) }
            .singleOrNull()
    }

    override suspend fun getTrack(createdAt: Long, userID: Int): Track? = dbQuery {
        Tracks.select { Tracks.createdAt.eq(createdAt) and Tracks.userID.eq(userID) }
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
        val insertion = insertStatement.resultedValues?.singleOrNull()?.let { resultRow -> resultRowToTrack(resultRow) }
        if (insertion != null) {
            Heroes.update({ Heroes.id eq insertion.userId }) {
                it[lastModificationDate] = java.time.LocalDateTime.now(ZoneOffset.UTC)
            }
        }
        insertion
    }

    override suspend fun updateTrack(
        id: Int,
        weight: Double,
        chest: Double?,
        abs: Double?,
        hip: Double?,
        bottom: Double?,
        leg: Double?,
        userID: Int,
        createdAt: Long
    ): Track? = dbQuery {

        Tracks.update({ Tracks.id.eq(id) and Tracks.userID.eq(userID) and Tracks.createdAt.eq(createdAt) }) {
            it[Tracks.weight] = weight
            it[Tracks.chest] = chest
            it[Tracks.abs] = abs
            it[Tracks.hip] = hip
            it[Tracks.bottom] = bottom
            it[Tracks.leg] = leg
        }
        getTrack(id, userID)
    }

    override suspend fun deleteTrack(id: Int, userID: Int) = dbQuery {
        Tracks.deleteWhere { Tracks.id.eq(id) and Tracks.userID.eq(userID) } > 0
    }

    override suspend fun insertNewHero(id: Int?, username: String?, login: String, password: String): Hero? = dbQuery {
        val insertStatement = Heroes.insert {
            it[Heroes.userName] = username
            it[Heroes.login] = login
            it[Heroes.password] = password
            it[Heroes.creationDate] = java.time.LocalDateTime.now(ZoneOffset.UTC)
            it[Heroes.lastModificationDate] = java.time.LocalDateTime.now(ZoneOffset.UTC)
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

    override suspend fun findHeroById(id: Int): Hero? = dbQuery {
        Heroes.select(where = Heroes.id eq id)
            .map { resultRow -> resultRowToHero(resultRow) }
            .firstOrNull()
    }

    override suspend fun updateHeroUsername(id: Int, username: String?): Hero? = dbQuery {
        Heroes.update({ Heroes.id eq id }) {
            it[Heroes.userName] = username
            it[lastModificationDate] = java.time.LocalDateTime.now(ZoneOffset.UTC)
        }
        findHeroById(id)
    }

    override suspend fun deleteHeroByLogin(login: String): Boolean = dbQuery {
        Heroes.deleteWhere { Heroes.login eq login } > -1
    }

    override suspend fun getTargetUser(userID: Int): Goal? = dbQuery {
        Goals.select { Goals.userID eq userID }
            .map { resultRow -> resultRowToGoal(resultRow) }
            .singleOrNull()
    }

    override suspend fun insertNewGoal(weight: Double, deadLine: Long, userID: Int): Goal? {
        return dbQuery {
            val insertStatement = Goals.insert {
                it[Goals.weight] = weight
                it[Goals.deadLine] = deadLine
                it[Goals.userID] = userID
            }
            insertStatement.resultedValues?.singleOrNull()?.let { resultRow ->
                resultRowToGoal(resultRow)
            }
        }
    }

    override suspend fun updateGoal(id: Int, weight: Double, deadLine: Long, userID: Int): Goal? {
        return dbQuery {
            Goals.update({ Goals.id eq id and (Goals.userID eq userID) }) {
                it[Goals.weight] = weight
                it[Goals.deadLine] = deadLine
            }
            getTargetUser(userID)
        }
    }

    override suspend fun deleteGoal(id: Int, userID: Int): Boolean = dbQuery {
        Goals.deleteWhere { Goals.id eq id and (Goals.userID eq userID) } > -1
    }

    override suspend fun getHeroGoal(userID: Int): Goal? = dbQuery {
        Goals.select { Goals.userID eq userID }
            .map { resultRow -> resultRowToGoal(resultRow) }
            .singleOrNull()
    }

    override suspend fun updateHeroProfile(id: Int, username: String?, goal: Goal): HeroProfileDTO? {
        transaction {
            try {
                if (username != null) {
                    Heroes.update({ Heroes.id eq id }) {
                        it[Heroes.userName] = username
                        it[lastModificationDate] = java.time.LocalDateTime.now(ZoneOffset.UTC)
                    }
                }
                if (goal.weight != null && goal.deadLine != null) {
                    Goals.deleteWhere { Goals.userID eq id }
                    Goals.insert(){
                        it[Goals.weight] = goal.weight
                        it[Goals.deadLine] = goal.deadLine /1000
                        it[Goals.userID] = id}
                }
                commit()
            } catch (e: Exception) {
                rollback()
                throw  Exception("Error updating hero profile")
            }
        }
        val heroSaved = findHeroById(id)
        val usernameStored = heroSaved?.username
        val goalStored = getHeroGoal(id)
        return HeroProfileDTO(id, usernameStored, goalStored)
    }

    override suspend fun getHeroProfile(id: Int): HeroProfileDTO = dbQuery {
        val hero = findHeroById(id)
        val username = hero?.username
        val goal = getHeroGoal(id)
        HeroProfileDTO(id, username, goal)
    }


}



