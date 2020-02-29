package org.trackit.database

import android.content.Context
import androidx.room.Room
import org.trackit.R

object RoomDatabase {

    private var appDatabase: AppDatabase? = null

    fun getDb(context: Context): AppDatabase {

        if(appDatabase == null)
            appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "trackit")
                    .allowMainThreadQueries()
                    .build()
        return appDatabase!!
    }

}