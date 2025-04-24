package com.example.simpletomato.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "pomodoro_records")
data class PomodoroRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val focusDuration: Int, // 单位：分钟
    val breakDuration: Int  // 单位：分钟
) 