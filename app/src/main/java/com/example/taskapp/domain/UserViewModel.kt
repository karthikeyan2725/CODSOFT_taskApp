package com.example.taskapp.domain

import android.app.Application
import android.util.Log
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.taskapp.data.database.TaskDatabase
import com.example.taskapp.data.entity.Task
import com.example.taskapp.data.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.annotation.meta.When
import javax.inject.Inject

const val viewTag = "taskApp:ViewModel"
@HiltViewModel
class UserViewModel @Inject constructor(val db:TaskDatabase) :ViewModel() {
    private val _userState = MutableStateFlow(UserState())
    val userState = _userState.asStateFlow()
    private val _tasks = MutableStateFlow(listOf<Task>())
    val tasks = _tasks.asStateFlow()
    private val _sortType = MutableStateFlow(SortType.INS)
    val sortType = _sortType.asStateFlow()
    fun updateState(
      attribute:String,
      value:String
    ){
        _userState.update {currState->
            when(attribute){
                "name"-> currState.copy(name = value)
                "email"->currState.copy(email=value)
                "password"->currState.copy(password = value)
                "loggedIn"->currState.copy(loggedIn = value == "true")
                else -> return
            }
        }
    }

    fun loginUser(){
        viewModelScope.launch(Dispatchers.IO){
            _userState.value.email?.let {
                db.userDao().getUserWithEmail(it)
                    ?.let{user->
                        if(user.password == _userState.value.password){
                            updateState("name", user.name)
                            _userState.update {currState->
                                currState.copy(uid=user.uid.toLong())
                            }
                            updateState("loggedIn","true")
                            collectTasksToggle()
                        }
                    }
            }
        }

    }

    fun signUpUser(){
        var uid :Long = 0
        viewModelScope.launch(Dispatchers.IO){
            uid = db.userDao().insertUser(User(
                uid=0,
                name = _userState.value.name.toString(),
                email = _userState.value.email.toString(),
                password = _userState.value.password.toString()
            ))
            Log.d(viewTag,"Returned UID: $uid")
            _userState.update {
                it.copy(uid=uid)
            }
            updateState("loggedIn","true")
            collectTasksToggle()
        }
    }

    fun addTask(dueDate:LocalDateTime,description:String){
        viewModelScope.launch(Dispatchers.IO){
            db.taskDao().insertTask(
                Task(
                    tid=0,
                    uid= _userState.value.uid ?:0,
                    description = description,
                    done = false,
                    dueDate = dueDate
                )
            )
        }
    }

    fun removeTask(tid:Int){
        viewModelScope.launch (Dispatchers.IO){
            db.taskDao().deleteTask(tid)
        }
    }

    fun collectTasksToggle(){

        _sortType.value = toggleNextSortType(_sortType.value)
        viewModelScope.launch(Dispatchers.IO){
            db.taskDao().let {
                when(_sortType.value){
                    SortType.ASC -> it.getTasksOrderedByDateAsc(_userState.value.uid?.toInt()?:0)
                    SortType.DEC -> it.getTasksOrderedByDateDesc(_userState.value.uid?.toInt()?:0)
                    SortType.INS ->  it.getTasksOf(_userState.value.uid?.toInt()?:0)
                }
            }.collect{allTasks->
                Log.d(viewTag,"TASKS SORT TOGGLED: ${_sortType.value.name}")
                _tasks.value = allTasks
            }
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch(Dispatchers.IO) {
            db.taskDao().updateTask(task)
        }
    }
}

enum class SortType{
    INS,
    ASC,
    DEC
}

fun toggleNextSortType(sortType:SortType) : SortType{
    return when(sortType){
        SortType.INS -> SortType.ASC
        SortType.ASC -> SortType.DEC
        SortType.DEC -> SortType.INS
    }
}