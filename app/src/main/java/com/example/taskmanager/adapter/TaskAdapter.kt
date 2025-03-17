package com.example.taskmanager.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.databinding.ItemTaskBinding
import com.example.taskmanager.database.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(private val onTaskClick: (Task) -> Unit) :
    ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvTaskDueDate.text = "Due: ${
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    Date(task.dueDate)
                )
            }"
            binding.tvTaskPriority.text = task.priority

            // status of task (pending or completed?)
            if (task.isCompleted) {
                binding.tvTaskStatus.text = "Completed"
                binding.tvTaskStatus.setTextColor(binding.root.context.getColor(R.color.completed_color))
            } else {
                binding.tvTaskStatus.text = "Pending"
                binding.tvTaskStatus.setTextColor(binding.root.context.getColor(R.color.pending_color))
            }

            // Handle item click
            binding.root.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val newList = currentList.toMutableList()
        val item = newList.removeAt(fromPosition)
        newList.add(toPosition, item)
        submitList(newList)
    }

    fun undoDelete(task: Task, position: Int) {
        val newList = currentList.toMutableList()
        newList.add(position, task)
        submitList(newList)
    }
}