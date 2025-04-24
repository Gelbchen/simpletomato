package com.example.simpletomato.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simpletomato.R
import com.example.simpletomato.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var adapter: PomodoroAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statisticsViewModel = ViewModelProvider(this).get(StatisticsViewModel::class.java)
        
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置RecyclerView
        adapter = PomodoroAdapter()
        binding.recyclerHistory.adapter = adapter
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        
        // 观察ViewModel数据变化
        statisticsViewModel.allRecords.observe(viewLifecycleOwner) { records ->
            records?.let {
                adapter.submitList(it)
            }
        }
        
        statisticsViewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.textCompletedPomodoros.text = count?.toString() ?: "0"
        }
        
        statisticsViewModel.totalFocusTime.observe(viewLifecycleOwner) { time ->
            val totalTime = time ?: 0
            val formattedTime = getString(R.string.minutes, totalTime)
            binding.textTotalFocusTime.text = formattedTime
        }
        
        statisticsViewModel.todayFocusTime.observe(viewLifecycleOwner) { time ->
            val todayTime = time ?: 0
            val formattedTime = getString(R.string.minutes, todayTime)
            binding.textTodayFocusTime.text = formattedTime
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 