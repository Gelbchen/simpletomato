package com.example.simpletomato.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface PomodoroDao {
    @Insert
    suspend fun insert(record: PomodoroRecord)

    @Query("SELECT * FROM pomodoro_records ORDER BY date DESC")
    fun getAllRecords(): LiveData<List<PomodoroRecord>>

    @Query("SELECT * FROM pomodoro_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetweenDates(startDate: Date, endDate: Date): LiveData<List<PomodoroRecord>>

    @Query("SELECT COUNT(*) FROM pomodoro_records")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT SUM(focusDuration) FROM pomodoro_records")
    fun getTotalFocusTime(): LiveData<Int>

    @Query("SELECT SUM(focusDuration) FROM pomodoro_records WHERE date BETWEEN :startDate AND :endDate")
    fun getFocusTimeBetweenDates(startDate: Date, endDate: Date): LiveData<Int>
} 