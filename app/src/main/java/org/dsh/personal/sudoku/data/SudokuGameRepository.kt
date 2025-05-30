package org.dsh.personal.sudoku.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.dsh.personal.sudoku.data.database.EntryDao
import org.dsh.personal.sudoku.data.database.entiry.StatisticEntry
import org.dsh.personal.sudoku.domain.repository.SudokuGenerator
import org.dsh.personal.sudoku.domain.repository.SudokuRepository
import org.dsh.personal.sudoku.domain.entity.Difficulty
import org.dsh.personal.sudoku.domain.entity.DifficultyStats
import org.dsh.personal.sudoku.domain.entity.SudokuGameState
import org.dsh.personal.sudoku.domain.entity.SudokuGameStats

class SudokuGameRepository(
    private val gameStorage: CurrentGameStorage,
    private val entryDao: EntryDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val defaultDispatcher: CoroutineDispatcher,
    ) : SudokuRepository {
    override suspend fun generateGrid(
        generator: SudokuGenerator,
        difficulty: Difficulty
    ): Pair<Array<IntArray>, Array<IntArray>> = generator.generate(difficulty, defaultDispatcher)

    override suspend fun hasGame() =
        withContext(ioDispatcher) { gameStorage.currentGame.first() != null }

    override fun hasGameFlow() =
        gameStorage.currentGame.map { it != null }

    override suspend fun saveGame(game: SudokuGameState) =
        withContext(ioDispatcher) { gameStorage.saveThemeSettings(game) }

    override suspend fun loadGame(): SudokuGameState? = withContext(ioDispatcher){
        gameStorage.currentGame.first()
    }

    override suspend fun deleteGame() = withContext(ioDispatcher) {
        gameStorage.clearGameState()
    }

    override suspend fun storeStatistic() = withContext(ioDispatcher) {
        val state = loadGame()
        if(state != null) {
            entryDao.insertEntry(
                StatisticEntry(
                    gameId = state.gameId,
                    difficulty = state.difficulty.toString(),
                    isSolved = state.isSolved,
                    completionTimeMillis = state.duration.inWholeMilliseconds,
                    mistakesMade = state.mistakesMade,
                    stepsTaken = state.history.size,
                    timeStarted = state.timerMillis,
                    timeFinished = System.currentTimeMillis()
                )
            )
            deleteGame()
        }
    }

    override suspend fun clearStatistic() = withContext(ioDispatcher) {entryDao.clearStatistic() }

    override suspend fun readStatistic(): SudokuGameStats = withContext(ioDispatcher) {

        val totalGamesPlayed = entryDao.getTotalGamesPlayed().first() // Get the first value from the Flow
        val totalGamesWon = entryDao.getTotalGamesWon().first()

        // Function to get stats for a specific difficulty
        suspend fun getStatsForDifficulty(difficulty: Difficulty): DifficultyStats {
            val difficultyString = difficulty.toString()
            val gamesPlayed = entryDao.getGameStatisticsByDifficulty(difficultyString).first().size // Get the count
            val solvedGames = entryDao.getSolvedGameStatisticsByDifficulty(difficultyString).first()
            val gamesWon = solvedGames.size
            val averageTime =
                entryDao.getAverageCompletionTimeMillisByDifficulty(difficultyString).first() ?: 0L
            val fastestTime =
                entryDao.getFastestCompletionTimeMillisByDifficulty(difficultyString).first()
                    ?: Long.MAX_VALUE

            return DifficultyStats(
                gamesPlayed = gamesPlayed,
                gamesWon = gamesWon,
                averageCompletionTimeMillis = averageTime,
                fastestCompletionTimeMillis = fastestTime
            )
        }

        // Get stats for each difficulty level
        val easyStats = getStatsForDifficulty(Difficulty.EASY)
        val mediumStats = getStatsForDifficulty(Difficulty.MEDIUM)
        val hardStats = getStatsForDifficulty(Difficulty.HARD)
        val expertStats = getStatsForDifficulty(Difficulty.EXPERT)

        // Return the combined SudokuGameStats object
        SudokuGameStats(
            totalGamesPlayed = totalGamesPlayed,
            totalGamesWon = totalGamesWon,
            easyStats = easyStats,
            mediumStats = mediumStats,
            hardStats = hardStats,
            expertStats = expertStats
        )
    }
}
