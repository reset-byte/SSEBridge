package com.github.ssebridge.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.ssebridge.app.databinding.ItemEventLogBinding

/**
 * Event log RecyclerView adapter
 *
 * Displays SSE event logs in a list
 */
class EventLogAdapter : RecyclerView.Adapter<EventLogAdapter.EventLogViewHolder>() {

    private val logs = mutableListOf<EventLog>()

    /**
     * Add a new log entry
     */
    fun addLog(log: EventLog) {
        logs.add(log)
        notifyItemInserted(logs.size - 1)
    }

    /**
     * Clear all logs
     */
    fun clearLogs() {
        val size = logs.size
        logs.clear()
        notifyItemRangeRemoved(0, size)
    }

    /**
     * Get log count
     */
    fun getLogCount(): Int = logs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventLogViewHolder {
        val binding = ItemEventLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventLogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    /**
     * ViewHolder for event log item
     */
    class EventLogViewHolder(private val binding: ItemEventLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: EventLog) {
            binding.logTypeText.text = log.type
            binding.logTypeText.setTextColor(log.color)
            binding.logTimestampText.text = log.timestamp
            if (log.message.isNotEmpty()) {
                binding.logMessageText.text = log.message
                binding.logMessageText.visibility = android.view.View.VISIBLE
            } else {
                binding.logMessageText.visibility = android.view.View.GONE
            }
        }
    }
}

/**
 * Event log data class
 */
data class EventLog(
    val type: String,
    val message: String,
    val timestamp: String,
    val color: Int
)

