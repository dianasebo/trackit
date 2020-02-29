package org.trackit.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HabitEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitDao
}