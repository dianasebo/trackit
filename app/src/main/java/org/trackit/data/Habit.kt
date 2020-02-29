package org.trackit.data

import android.os.Build
import androidx.annotation.RequiresApi
import org.trackit.R
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class Habit (
    val id: Long = System.currentTimeMillis(),
    var title: String,
    var color: String,
    var goal: Int,
    val start: LocalDate
)
