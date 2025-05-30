package org.dsh.personal.sudoku.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.dsh.personal.sudoku.data.database.entiry.StatisticEntry

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: StatisticEntry): Long

    @Query("DELETE FROM statistic_entries")
    suspend fun clearStatistic()

    // Query to get statistics for a specific difficulty
    @Query(
        "SELECT * " +
                "FROM statistic_entries " +
                "WHERE difficulty = :difficulty " +
                "ORDER BY timeFinished DESC"
    )
    fun getGameStatisticsByDifficulty(difficulty: String): Flow<List<StatisticEntry>>

    // Query to get solved games for a specific difficulty, ordered by completion time (fastest first)
    @Query(
        "SELECT * " +
                "FROM statistic_entries " +
                "WHERE difficulty = :difficulty " +
                "AND isSolved = 1 " +
                "ORDER BY completionTimeMillis ASC"
    )
    fun getSolvedGameStatisticsByDifficulty(difficulty: String): Flow<List<StatisticEntry>>

    // Query to calculate the total number of games played
    @Query(
        "SELECT COUNT(*) " +
                "FROM statistic_entries"
    )
    fun getTotalGamesPlayed(): Flow<Int>

    // Query to calculate the total number of games won
    @Query("SELECT COUNT(*) FROM statistic_entries WHERE isSolved = 1")
    fun getTotalGamesWon(): Flow<Int>

    // Query to calculate the average completion time for solved games of a specific difficulty
    @Query("SELECT AVG(completionTimeMillis) FROM statistic_entries WHERE difficulty = :difficulty AND isSolved = 1")
    fun getAverageCompletionTimeMillisByDifficulty(difficulty: String): Flow<Long?> // Use Long? for average

    // Query to get the fastest completion time for a specific difficulty
    @Query("SELECT MIN(completionTimeMillis) FROM statistic_entries WHERE difficulty = :difficulty AND isSolved = 1")
    fun getFastestCompletionTimeMillisByDifficulty(difficulty: String): Flow<Long?> // Use Long? for min
}
