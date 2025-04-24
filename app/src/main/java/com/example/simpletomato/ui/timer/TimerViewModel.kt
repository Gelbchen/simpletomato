package com.example.simpletomato.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simpletomato.service.TimerService
import java.util.concurrent.TimeUnit

class TimerViewModel : ViewModel() {
    
    private val _focusTime = MutableLiveData<Int>().apply { value = 25 }
    val focusTime: LiveData<Int> = _focusTime
    
    private val _breakTime = MutableLiveData<Int>().apply { value = 5 }
    val breakTime: LiveData<Int> = _breakTime
    
    private val _timerRunning = MutableLiveData<Boolean>().apply { value = false }
    val timerRunning: LiveData<Boolean> = _timerRunning
    
    private val _timerState = MutableLiveData<Int>().apply { value = TimerService.TIMER_STATE_IDLE }
    val timerState: LiveData<Int> = _timerState
    
    private val _remainingTime = MutableLiveData<String>().apply { value = "00:00" }
    val remainingTime: LiveData<String> = _remainingTime
    
    fun setFocusTime(minutes: Int) {
        _focusTime.value = minutes
    }
    
    fun setBreakTime(minutes: Int) {
        _breakTime.value = minutes
    }
    
    fun setTimerRunning(running: Boolean) {
        _timerRunning.value = running
    }
    
    fun setTimerState(state: Int) {
        _timerState.value = state
    }
    
    fun updateRemainingTime(millis: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        _remainingTime.value = String.format("%02d:%02d", minutes, seconds)
    }
} 