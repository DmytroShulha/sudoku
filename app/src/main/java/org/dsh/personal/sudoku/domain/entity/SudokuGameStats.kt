package org.dsh.personal.sudoku.domain.entity

data class SudokuGameStats(
    val totalGamesPlayed: Int = 0,
    val totalGamesWon: Int = 0,
    val easyStats: DifficultyStats = DifficultyStats(),
    val mediumStats: DifficultyStats = DifficultyStats(),
    val hardStats: DifficultyStats = DifficultyStats(),
    val expertStats: DifficultyStats = DifficultyStats()
)

data class DifficultyStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val averageCompletionTimeMillis: Long = 0L, // Or Double if you use AVG returning Double
    val fastestCompletionTimeMillis: Long = Long.MAX_VALUE // Or nullable Long?
)