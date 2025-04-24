package com.example.simpletomato.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.simpletomato.MainActivity
import com.example.simpletomato.R
import com.example.simpletomato.data.AppDatabase
import com.example.simpletomato.data.PomodoroRecord
import com.example.simpletomato.util.VivoAodHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class TimerService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "SimpleTomatoChannel"
        
        const val ACTION_START = "com.example.simpletomato.ACTION_START"
        const val ACTION_STOP = "com.example.simpletomato.ACTION_STOP"
        const val EXTRA_FOCUS_TIME = "com.example.simpletomato.EXTRA_FOCUS_TIME"
        const val EXTRA_BREAK_TIME = "com.example.simpletomato.EXTRA_BREAK_TIME"
        
        const val TIMER_STATE_FOCUS = 0
        const val TIMER_STATE_BREAK = 1
        const val TIMER_STATE_IDLE = 2

        // AOD更新间隔（毫秒），避免频繁更新
        private const val AOD_UPDATE_INTERVAL = 60000L // 1分钟
    }
    
    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null
    private var timerState = TIMER_STATE_IDLE
    private var remainingTime: Long = 0
    private var focusTime: Int = 0
    private var breakTime: Int = 0
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    // 用于控制AOD更新频率
    private var lastAodUpdateTime: Long = 0
    
    // 回调接口，用于与Fragment通信
    private var timerCallbacks: TimerCallbacks? = null
    
    interface TimerCallbacks {
        fun onTimerTick(timeRemaining: Long)
        fun onFocusCompleted()
        fun onBreakCompleted()
        fun onTimerStateChanged(timerState: Int)
    }
    
    fun setTimerCallbacks(callbacks: TimerCallbacks?) {
        timerCallbacks = callbacks
    }
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                focusTime = intent.getIntExtra(EXTRA_FOCUS_TIME, 25)
                breakTime = intent.getIntExtra(EXTRA_BREAK_TIME, 5)
                startFocusTimer()
            }
            ACTION_STOP -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun startFocusTimer() {
        timerState = TIMER_STATE_FOCUS
        timerCallbacks?.onTimerStateChanged(timerState)
        
        val focusTimeInMillis = TimeUnit.MINUTES.toMillis(focusTime.toLong())
        remainingTime = focusTimeInMillis
        
        startForegroundService(getString(R.string.focus_notification_title))
        
        // 立即更新一次AOD显示
        updateAodDisplay()
        
        countDownTimer = object : CountDownTimer(focusTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateNotification()
                timerCallbacks?.onTimerTick(millisUntilFinished)
                
                // 定期更新AOD显示（每分钟一次）
                checkAndUpdateAod()
            }
            
            override fun onFinish() {
                timerCallbacks?.onFocusCompleted()
                startBreakTimer()
            }
        }.start()
    }
    
    private fun startBreakTimer() {
        timerState = TIMER_STATE_BREAK
        timerCallbacks?.onTimerStateChanged(timerState)
        
        val breakTimeInMillis = TimeUnit.MINUTES.toMillis(breakTime.toLong())
        remainingTime = breakTimeInMillis
        
        startForegroundService(getString(R.string.break_notification_title))
        
        // 立即更新一次AOD显示
        updateAodDisplay()
        
        countDownTimer = object : CountDownTimer(breakTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                updateNotification()
                timerCallbacks?.onTimerTick(millisUntilFinished)
                
                // 定期更新AOD显示（每分钟一次）
                checkAndUpdateAod()
            }
            
            override fun onFinish() {
                // 记录完成的番茄钟
                saveCompletedPomodoro()
                
                timerCallbacks?.onBreakCompleted()
                // 自动开始下一个循环
                startFocusTimer()
            }
        }.start()
    }
    
    /**
     * 检查是否需要更新AOD显示
     */
    private fun checkAndUpdateAod() {
        val currentTime = System.currentTimeMillis()
        // 限制更新频率，避免频繁更新AOD
        if (currentTime - lastAodUpdateTime >= AOD_UPDATE_INTERVAL) {
            updateAodDisplay()
            lastAodUpdateTime = currentTime
        }
    }
    
    /**
     * 更新vivo AOD显示
     */
    private fun updateAodDisplay() {
        if (VivoAodHelper.isVivoDevice()) {
            val title = if (timerState == TIMER_STATE_FOCUS) 
                getString(R.string.focus_notification_title)
            else 
                getString(R.string.break_notification_title)
                
            val content = VivoAodHelper.formatTimeForAod(remainingTime)
            
            VivoAodHelper.updateAodDisplay(
                this,
                title,
                content
            )
        }
    }
    
    private fun saveCompletedPomodoro() {
        serviceScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val pomodoroDao = database.pomodoroDao()
            
            val record = PomodoroRecord(
                date = Date(),
                focusDuration = focusTime,
                breakDuration = breakTime
            )
            pomodoroDao.insert(record)
        }
    }
    
    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        timerState = TIMER_STATE_IDLE
        timerCallbacks?.onTimerStateChanged(timerState)
    }
    
    private fun startForegroundService(title: String) {
        createNotificationChannel()
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(formatTime(remainingTime))
            .setSmallIcon(R.drawable.ic_tomato)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop_timer), stopPendingIntent)
            .setOngoing(true)
            // 增强锁屏显示
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val title = if (timerState == TIMER_STATE_FOCUS) {
            getString(R.string.focus_notification_title)
        } else {
            getString(R.string.break_notification_title)
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(formatTime(remainingTime))
            .setSmallIcon(R.drawable.ic_tomato)
            .setOngoing(true)
            // 增强锁屏显示
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val description = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH // 提高通知重要性
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
                // 启用锁屏显示
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // 启用通知灯
                enableLights(true)
                // 设置震动模式
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    // 获取当前计时器状态
    fun getTimerState(): Int = timerState
    
    // 获取剩余时间
    fun getRemainingTime(): Long = remainingTime
} 