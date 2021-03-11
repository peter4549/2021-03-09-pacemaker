package com.flow.android.kotlin.pacemaker.model.dao

import androidx.room.*
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import io.reactivex.Completable

@Dao
interface ToDoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(toDo: ToDo): Completable

    @Delete
    fun delete(toDo: ToDo): Completable

    @Update
    fun update(toDo: ToDo): Completable

    @Update
    suspend fun updateList(list: List<ToDo>)

    @Transaction
    @Query("SELECT * from to_do WHERE date_time = :date_time ORDER BY priority DESC")
    suspend fun getAllByDateTime(date_time: Long): List<ToDo>

    @Transaction
    @Query("SELECT COUNT(*) from to_do WHERE date_time = :date_time ORDER BY priority DESC")
    suspend fun getCountByDateTime(date_time: Long): Int
}