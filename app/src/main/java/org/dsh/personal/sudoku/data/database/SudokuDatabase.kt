package org.dsh.personal.sudoku.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.dsh.personal.sudoku.data.database.entiry.StatisticEntry

@Database(entities = [StatisticEntry::class], version = 1, exportSchema = false)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {

        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getDatabase(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku_database"
                )
                    .addMigrations()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}