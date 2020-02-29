package org.trackit.database

import android.os.Build
import androidx.annotation.RequiresApi
import org.trackit.data.Habit
import org.trackit.data.HabitRepository
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
class HabitDbStore(private val appDatabase: AppDatabase) : HabitRepository {

    override fun getAll(): List<Habit> {
        return appDatabase.habitsDao().getAll().map { it.toHabit() }
    }

    override fun addHabit(habit: Habit) {
        appDatabase.habitsDao().add(habit.toHabitEntity())
    }

    override fun removeHabit(habit: Habit) {
        appDatabase.habitsDao().remove(habit.toHabitEntity())
    }

    override fun updateHabit(habit: Habit) {
        appDatabase.habitsDao().update(habit.toHabitEntity())
    }

    private fun HabitEntity.toHabit() = Habit(id, name, color, goal, LocalDate.ofEpochDay(start))
    private fun Habit.toHabitEntity() = HabitEntity(id, title, color, goal, start.toEpochDay())

}