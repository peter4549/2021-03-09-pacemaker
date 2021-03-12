package com.flow.android.kotlin.pacemaker.view_model

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.repository.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    private val calendar = Calendar.getInstance().apply {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }

    private val timeZone = calendar.timeZone
    private val zoneId = timeZone.toZoneId() ?: ZoneId.systemDefault()

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val today: LocalDate by lazy {
        LocalDateTime.ofInstant(calendar.toInstant(), zoneId).toLocalDate()
    }

    private var selectedDate = today

    fun selectedDate() = selectedDate

    fun setSelectedDate(localDate: LocalDate) {
        selectedDate = localDate

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _toDoList.postValue(repository.toDoDao.getAllByDateTime(selectedDate.time()))
            }
        }
    }

    private val _toDoList = MutableLiveData<List<ToDo>>()
    val toDoList: LiveData<List<ToDo>>
        get() = _toDoList

    private val _modifiedToDo = MutableLiveData<ToDo>()
    val modifiedToDo: LiveData<ToDo>
        get() = _modifiedToDo

    fun setModifiedToDo(toDo: ToDo) {
        _modifiedToDo.value = toDo
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _toDoList.postValue(repository.getAllByDateTime(requireNotNull(selectedDate.time())))
            }
        }
    }

    fun insertToDo(content: String, @MainThread onComplete: (ToDo) -> Unit) {
        val toDo = ToDo(
            content = content,
            dateTime = requireNotNull(selectedDate.time()),
            priority = System.currentTimeMillis()
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.toDoDao.insert(toDo).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onComplete.invoke(toDo)
                    }, {
                        Timber.e(it)
                    })
            }
        }
    }

    fun updateToDoList(toDoList: List<ToDo>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.toDoDao.updateList(toDoList)
            }
        }
    }

    fun deleteToDo(toDo: ToDo, @MainThread onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.toDoDao.delete(toDo).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onComplete.invoke()
                    }, {
                        Timber.e(it)
                    })
            }
        }
    }

    suspend fun getDoneListByLocalDate(localDate: LocalDate) = repository.toDoDao.getDoneListByDateTime(localDate.time())

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    private fun LocalDate.time() = this.atStartOfDay(zoneId).toInstant().toEpochMilli()
}