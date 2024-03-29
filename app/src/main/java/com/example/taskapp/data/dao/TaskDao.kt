package com.example.taskapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.taskapp.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Query("DELETE FROM task WHERE tid = :tid")
    suspend fun deleteTask(tid:Int)

    @Query("SELECT * from task WHERE uid=:uid")
    fun getTasksOf(uid:Int): Flow<List<Task>>

    @Query("SELECT * from task WHERE uid=:uid ORDER BY due_date ASC")
    fun getTasksOrderedByDateAsc(uid:Int): Flow<List<Task>>

    @Query("SELECT * from task WHERE uid=:uid ORDER BY due_date DESC")
    fun getTasksOrderedByDateDesc(uid:Int): Flow<List<Task>>

    @Query("SELECT * FROM task")
    suspend fun getAllTasks():List<Task>

    @Upsert
    suspend fun updateTask(task:Task)
}