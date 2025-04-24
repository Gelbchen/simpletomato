package com.example.simpletomato.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.simpletomato.R
import com.example.simpletomato.databinding.FragmentTimerBinding
import com.example.simpletomato.service.TimerService
import com.google.android.material.slider.Slider

class TimerFragment : Fragment(), TimerService.TimerCallbacks {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var timerViewModel: TimerViewModel
    private var timerService: TimerService? = null
    private var bound = false
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            timerService?.setTimerCallbacks(this@TimerFragment)
            bound = true
            
            // 服务已连接，恢复UI状态
            val currentState = timerService?.getTimerState() ?: TimerService.TIMER_STATE_IDLE
            timerViewModel.setTimerState(currentState)
            if (currentState != TimerService.TIMER_STATE_IDLE) {
                timerViewModel.setTimerRunning(true)
                updateTimerUI(currentState)
                timerViewModel.updateRemainingTime(timerService?.getRemainingTime() ?: 0)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        timerViewModel = ViewModelProvider(this).get(TimerViewModel::class.java)
        
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置滑块监听器
        binding.focusTimeSlider.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            binding.focusTimeValue.text = minutes.toString()
            timerViewModel.setFocusTime(minutes)
        }
        
        binding.breakTimeSlider.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            binding.breakTimeValue.text = minutes.toString()
            timerViewModel.setBreakTime(minutes)
        }
        
        // 设置按钮监听器
        binding.buttonStart.setOnClickListener {
            startTimer()
        }
        
        binding.buttonStop.setOnClickListener {
            stopTimer()
        }
        
        // 观察ViewModel数据变化
        timerViewModel.timerRunning.observe(viewLifecycleOwner) { running ->
            updateButtonsVisibility(running)
        }
        
        timerViewModel.timerState.observe(viewLifecycleOwner) { state ->
            updateTimerUI(state)
        }
        
        timerViewModel.remainingTime.observe(viewLifecycleOwner) { time ->
            binding.textTimer.text = time
        }
    }
    
    override fun onStart() {
        super.onStart()
        // 绑定服务
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    
    override fun onStop() {
        super.onStop()
        // 解绑服务
        if (bound) {
            timerService?.setTimerCallbacks(null)
            requireContext().unbindService(connection)
            bound = false
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun startTimer() {
        val focusTime = timerViewModel.focusTime.value ?: 25
        val breakTime = timerViewModel.breakTime.value ?: 5
        
        Intent(requireContext(), TimerService::class.java).also { intent ->
            intent.action = TimerService.ACTION_START
            intent.putExtra(TimerService.EXTRA_FOCUS_TIME, focusTime)
            intent.putExtra(TimerService.EXTRA_BREAK_TIME, breakTime)
            requireContext().startService(intent)
        }
        
        timerViewModel.setTimerRunning(true)
    }
    
    private fun stopTimer() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            intent.action = TimerService.ACTION_STOP
            requireContext().startService(intent)
        }
        
        timerViewModel.setTimerRunning(false)
        timerViewModel.setTimerState(TimerService.TIMER_STATE_IDLE)
    }
    
    private fun updateButtonsVisibility(running: Boolean) {
        if (running) {
            binding.buttonStart.visibility = View.GONE
            binding.buttonStop.visibility = View.VISIBLE
            binding.focusTimeSlider.isEnabled = false
            binding.breakTimeSlider.isEnabled = false
            binding.textTimer.visibility = View.VISIBLE
            binding.textTimerStatus.visibility = View.VISIBLE
        } else {
            binding.buttonStart.visibility = View.VISIBLE
            binding.buttonStop.visibility = View.GONE
            binding.focusTimeSlider.isEnabled = true
            binding.breakTimeSlider.isEnabled = true
            binding.textTimer.visibility = View.GONE
            binding.textTimerStatus.visibility = View.GONE
        }
    }
    
    private fun updateTimerUI(state: Int) {
        when (state) {
            TimerService.TIMER_STATE_FOCUS -> {
                binding.textTimerStatus.text = getString(R.string.focus_notification_title)
                binding.textTimerStatus.setTextColor(resources.getColor(R.color.primary, null))
                binding.textTimer.setTextColor(resources.getColor(R.color.primary, null))
            }
            TimerService.TIMER_STATE_BREAK -> {
                binding.textTimerStatus.text = getString(R.string.break_notification_title)
                binding.textTimerStatus.setTextColor(resources.getColor(R.color.accent, null))
                binding.textTimer.setTextColor(resources.getColor(R.color.accent, null))
            }
        }
    }
    
    // TimerService.TimerCallbacks实现
    override fun onTimerTick(timeRemaining: Long) {
        timerViewModel.updateRemainingTime(timeRemaining)
    }
    
    override fun onFocusCompleted() {
        // 不需要特殊处理，服务会自动开始休息计时
    }
    
    override fun onBreakCompleted() {
        // 不需要特殊处理，服务会自动开始下一个专注计时
    }
    
    override fun onTimerStateChanged(timerState: Int) {
        timerViewModel.setTimerState(timerState)
        if (timerState == TimerService.TIMER_STATE_IDLE) {
            timerViewModel.setTimerRunning(false)
        } else {
            timerViewModel.setTimerRunning(true)
        }
    }
} 