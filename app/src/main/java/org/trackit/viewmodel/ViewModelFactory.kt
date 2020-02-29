package org.trackit.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.trackit.data.HabitRepository

class ViewModelFactory(private val habitRepository: HabitRepository) : ViewModelProvider.NewInstanceFactory() {
    @RequiresApi(Build.VERSION_CODES.N)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HabitsViewModel(habitRepository) as T
    }
}