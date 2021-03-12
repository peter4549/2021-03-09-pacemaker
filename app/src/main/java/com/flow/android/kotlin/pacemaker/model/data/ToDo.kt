package com.flow.android.kotlin.pacemaker.model.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Entity(tableName = "to_do")
@Parcelize
data class ToDo (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String,
    @ColumnInfo(name = "date_time") val dateTime: Long,
    var done: Boolean = false,
    var priority: Long
): Parcelable {
    fun copy(): ToDo = ToDo (
        id = this.id,
        content = this.content,
        dateTime = this.dateTime,
        done = this.done,
        priority = this.priority
    )

    fun localDate(): LocalDate {
        val timeZone = Calendar.getInstance().timeZone
        val zoneId = timeZone.toZoneId() ?: ZoneId.systemDefault()
        return Instant.ofEpochMilli(dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}