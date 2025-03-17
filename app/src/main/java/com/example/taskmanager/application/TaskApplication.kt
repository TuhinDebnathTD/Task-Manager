package com.example.taskmanager.application

import android.app.Application
import com.example.taskmanager.database.TaskDatabase
import com.example.taskmanager.database.TaskRepository

class TaskApplication : Application() {

    val database by lazy { TaskDatabase.getDatabase(this) }
    val repository by lazy { TaskRepository(database.taskDao()) }
}