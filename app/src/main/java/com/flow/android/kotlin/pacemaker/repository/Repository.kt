package com.flow.android.kotlin.pacemaker.repository

import com.flow.android.kotlin.pacemaker.model.database.LocalDatabase
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import javax.inject.Inject

class Repository @Inject constructor(localDatabase: LocalDatabase) {

    private val toDoDao = localDatabase.toDoDao()

    fun getAllByDateTime(dateTime: Long) = toDoDao.getAllByDateTime(dateTime)

    suspend fun insertToDo(toDo: ToDo) {
        toDoDao.insert(toDo)
    }

    suspend fun insertToDoList(list: List<ToDo>) {
        toDoDao.insertList(list)
    }

    suspend fun deleteToDo(toDo: ToDo) {
        toDoDao.delete(toDo)
    }

    suspend fun updateToDo(toDo: ToDo) {
        toDoDao.update(toDo)
    }
}