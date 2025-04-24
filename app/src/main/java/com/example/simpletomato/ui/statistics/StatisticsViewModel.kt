package com.example.simpletomato.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.simpletomato.data.AppDatabase
import com.example.simpletomato.data.PomodoroRecord
import com.example.simpletomato.data.PomodoroRepository

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: PomodoroRepository
    
    val allRecords: LiveData<List<PomodoroRecord>>
    val totalCount: LiveData<Int>
    val totalFocusTime: LiveData<Int>
    val todayFocusTime: LiveData<Int>
    
    init {
        val database = AppDatabase.getDatabase(application)
        val pomodoroDao = database.pomodoroDao()
        repository = PomodoroRepository(pomodoroDao)
        
        allRecords = repository.allRecords
        totalCount = repository.totalCount
        totalFocusTime = repository.totalFocusTime
        todayFocusTime = repository.todayFocusTime
    }
} 