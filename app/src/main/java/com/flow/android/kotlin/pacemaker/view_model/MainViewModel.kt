package com.flow.android.kotlin.pacemaker.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private val today: Date by lazy {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        calendar.time
    }

    val todoList = repository.getAllByDateTime(today.time)

    fun insertToDo(content: String) {
        val toDo = ToDo(
            content = content,
            dateTime = today.time,
            priority = System.currentTimeMillis()
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertToDo(toDo)
            }
        }
    }

    fun insertToDoList(list: List<ToDo>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertToDoList(list)
            }
        }
    }

    fun updateToDo(toDo: ToDo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateToDo(toDo)
            }
        }
    }
}