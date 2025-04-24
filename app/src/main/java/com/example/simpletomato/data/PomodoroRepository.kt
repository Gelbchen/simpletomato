package com.example.simpletomato.data

import androidx.lifecycle.LiveData
import java.util.*
import java.util.concurrent.TimeUnit

class PomodoroRepository(private val pomodoroDao: PomodoroDao) {
    
    // 获取所有番茄钟记录
    val allRecords: LiveData<List<PomodoroRecord>> = pomodoroDao.getAllRecords()
    
    // 获取今日的番茄钟记录
    val todayRecords: LiveData<List<PomodoroRecord>> by lazy {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // 设置为今天的结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        pomodoroDao.getRecordsBetweenDates(startOfDay, endOfDay)
    }
    
    // 获取番茄钟总数
    val totalCount: LiveData<Int> = pomodoroDao.getTotalCount()
    
    // 获取总专注时间（分钟）
    val totalFocusTime: LiveData<Int> = pomodoroDao.getTotalFocusTime()
    
    // 获取今日专注时间（分钟）
    val todayFocusTime: LiveData<Int> by lazy {
        val calendar = Calendar.getInstance()
        // 设置为今天的开始时间
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // 设置为今天的结束时间
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        pomodoroDao.getFocusTimeBetweenDates(startOfDay, endOfDay)
    }
    
    // 插入一条番茄钟记录
    suspend fun insert(record: PomodoroRecord) {
        pomodoroDao.insert(record)
    }
} 