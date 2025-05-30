package org.dsh.personal.sudoku.data.database.entiry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "statistic_entries",
    indices = [Index(value = ["gameId"], unique = true)] )
data class StatisticEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "gameId")
    val gameId: String,
    val difficulty: String,
    val isSolved: Boolean,
    val completionTimeMillis: Long,
    val mistakesMade: Int = 0,
    val stepsTaken: Int,
    val timeStarted: Long,
    val timeFinished: Long = System.currentTimeMillis()
)
