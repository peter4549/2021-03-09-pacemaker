package com.flow.android.kotlin.pacemaker.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "to_do")
data class ToDo (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String,
    @ColumnInfo(name = "date_time") val dateTime: Long,
    var done: Boolean = false,
    var priority: Long
) {
    fun copy(): ToDo = ToDo (
        id = this.id,
        content = this.content,
        dateTime = this.dateTime,
        done = this.done,
        priority = this.priority
    )
}