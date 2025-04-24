package com.example.simpletomato.ui.statistics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.simpletomato.R
import com.example.simpletomato.data.PomodoroRecord
import java.text.SimpleDateFormat
import java.util.*

class PomodoroAdapter : ListAdapter<PomodoroRecord, PomodoroAdapter.PomodoroViewHolder>(POMODORO_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PomodoroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pomodoro_history, parent, false)
        return PomodoroViewHolder(view)
    }

    override fun onBindViewHolder(holder: PomodoroViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class PomodoroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.text_date)
        private val durationTextView: TextView = itemView.findViewById(R.id.text_duration)
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(record: PomodoroRecord) {
            dateTextView.text = dateFormat.format(record.date)
            val durationText = itemView.context.getString(R.string.minutes, record.focusDuration)
            durationTextView.text = durationText
        }
    }

    companion object {
        private val POMODORO_COMPARATOR = object : DiffUtil.ItemCallback<PomodoroRecord>() {
            override fun areItemsTheSame(oldItem: PomodoroRecord, newItem: PomodoroRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PomodoroRecord, newItem: PomodoroRecord): Boolean {
                return oldItem.date.time == newItem.date.time &&
                        oldItem.focusDuration == newItem.focusDuration &&
                        oldItem.breakDuration == newItem.breakDuration
            }
        }
    }
} 