package com.flow.android.kotlin.pacemaker.view.util

import android.app.Activity
import android.content.Intent
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


/** Share */
fun shareToDo(activity: Activity, toDo: ToDo) {
    val intent = Intent(Intent.ACTION_SEND)


    val timeZone = Calendar.getInstance().timeZone
    val zoneId = timeZone.toZoneId() ?: ZoneId.systemDefault()
    val localDate = Instant.ofEpochMilli(toDo.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()

    val stringBuilder = StringBuilder()
    stringBuilder.append("${DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate)}\n")
    stringBuilder.append(toDo.content)

    intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString())
    intent.type = "text/plain"

    activity.startActivity(Intent.createChooser(intent, null))
}