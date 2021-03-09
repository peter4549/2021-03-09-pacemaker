package com.flow.android.kotlin.pacemaker.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.flow.android.kotlin.pacemaker.model.data.ToDo

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDo: ToDo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: List<ToDo>)

    @Delete
    suspend fun delete(toDo: ToDo)

    @Update
    suspend fun update(toDo: ToDo)

    @Transaction
    @Query("SELECT * from to_do WHERE date_time = :date_time ORDER BY priority DESC")
    fun getAllByDateTime(date_time: Long): LiveData<List<ToDo>>
}