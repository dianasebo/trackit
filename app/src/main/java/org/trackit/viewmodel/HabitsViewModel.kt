package org.trackit.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.trackit.data.Habit
import org.trackit.data.HabitRepository

@RequiresApi(Build.VERSION_CODES.N)
class HabitsViewModel(private val habitRepository: HabitRepository) : ViewModel() {
    val habitsLiveData = MutableLiveData<List<Habit>>()

    private fun refresh() {
        habitsLiveData.postValue(habitRepository.getAll())
    }

    fun addHabit(habit: Habit) {
        habitRepository.addHabit(habit)
        refresh()
    }

    fun updateHabit(habit: Habit) {
        habitRepository.updateHabit(habit)
        refresh()
    }

    fun removeHabit(habit: Habit) {
        habitRepository.removeHabit(habit)
        refresh()
    }
}