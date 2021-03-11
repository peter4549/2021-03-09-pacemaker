package com.flow.android.kotlin.pacemaker.repository

import com.flow.android.kotlin.pacemaker.model.dao.ToDoDao
import com.flow.android.kotlin.pacemaker.model.database.LocalDatabase
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import javax.inject.Inject

class Repository @Inject constructor(localDatabase: LocalDatabase) {

    private val _toDoDao = localDatabase.toDoDao()
    val toDoDao: ToDoDao
        get() = _toDoDao

    suspend fun getAllByDateTime(dateTime: Long) = toDoDao.getAllByDateTime(dateTime)
}