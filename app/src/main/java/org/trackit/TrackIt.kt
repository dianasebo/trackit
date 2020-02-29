package org.trackit

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.trackit.*

class TrackIt : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trackit)
        if (main_layout != null) {
            if (savedInstanceState != null)
                return
            supportFragmentManager.beginTransaction()
                .add(R.id.main_layout, MainFragment()).commit()
        }

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_main -> {
                    supportFragmentManager.beginTransaction(). apply {
                        replace(R.id.main_layout, MainFragment())
                        addToBackStack(null)
                    }.commit()

                    true
                }
                R.id.navigation_habits -> {
                    supportFragmentManager.beginTransaction(). apply {
                        replace(R.id.main_layout, HabitsFragment())
                        addToBackStack(null)
                    }.commit()
                    true
                }
                R.id.navigation_statistics -> {
                    supportFragmentManager.beginTransaction(). apply {
                        replace(R.id.main_layout, StatisticsFragment())
                        addToBackStack(null)
                    }.commit()
                    true
                }
                else -> false
            }
        }

    }
}