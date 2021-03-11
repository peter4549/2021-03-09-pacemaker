package com.flow.android.kotlin.pacemaker.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flow.android.kotlin.pacemaker.model.dao.ToDoDao
import com.flow.android.kotlin.pacemaker.model.data.ToDo

@Database(entities = [ToDo::class], version = 1, exportSchema = false)
abstract class LocalDatabase: RoomDatabase() {

    abstract fun toDoDao(): ToDoDao

    companion object {
        const val name = "com.grand.duke.elliot.restaurantpost.database" +
                ".app_database.name:debug.1.1.2"
    }
}