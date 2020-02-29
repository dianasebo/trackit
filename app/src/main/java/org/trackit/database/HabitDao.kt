package org.trackit.database

import androidx.room.*

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit")
    fun getAll(): List<HabitEntity>

    @Insert
    fun add(habit: HabitEntity)

    @Delete
    fun remove(habit: HabitEntity)

    @Update
    fun update(habit: HabitEntity)
}