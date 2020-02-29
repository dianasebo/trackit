package org.trackit.data

interface HabitRepository {
    fun getAll() : List<Habit>
    fun addHabit(habit: Habit)
    fun removeHabit(habit: Habit)
    fun updateHabit(habit: Habit)
}