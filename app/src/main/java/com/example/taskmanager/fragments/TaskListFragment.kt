package com.example.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.application.TaskApplication
import com.example.taskmanager.adapter.TaskAdapter
import com.example.taskmanager.databinding.FragmentTaskListBinding
import com.example.taskmanager.viewModel.TaskViewModel
import com.example.taskmanager.viewModel.TaskViewModelFactory
import com.google.android.material.snackbar.Snackbar

class TaskListFragment : Fragment() {

    private lateinit var binding: FragmentTaskListBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this, TaskViewModelFactory((requireActivity().application as TaskApplication).repository))
            .get(TaskViewModel::class.java)

        taskAdapter = TaskAdapter { task ->
            val bundle = bundleOf("taskId" to task.id)
            findNavController().navigate(R.id.action_taskListFragment_to_taskDetailsFragment, bundle)
        }
        binding.recyclerViewTasks.adapter = taskAdapter
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())

        // Observe tasks
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.submitList(tasks)
        }

        binding.fabAddTask.setOnClickListener {
            // Bounce animation
            binding.fabAddTask.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction {
                    binding.fabAddTask.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        // For Drag-and-Drop and Swipe Gestures
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                //  drag for up and down directions
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                //  swipe for left and right directions
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Notifying adapter of item move
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                taskAdapter.moveItem(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = taskAdapter.currentList[position]

                when (direction) {
                    ItemTouchHelper.START -> {
                        // Swipe to delete
                        taskViewModel.delete(task)
                        taskAdapter.notifyItemRemoved(position)
                        showUndoSnackbar("Task deleted", "UNDO") {
                            taskViewModel.insert(task)
                            taskAdapter.undoDelete(task, position)
                        }
                    }
                    ItemTouchHelper.END -> {
                        // Swipe to complete
                        task.isCompleted = true
                        taskViewModel.update(task)
                        taskAdapter.notifyItemChanged(position) // Notify adapter immediately
                        showUndoSnackbar("Task completed", "UNDO") {
                            task.isCompleted = false
                            taskViewModel.update(task)
                            taskAdapter.notifyItemChanged(position) // Notify adapter on undo
                        }
                    }
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                // Provide haptic feedback when dragging starts
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.performHapticFeedback(
                        android.view.HapticFeedbackConstants.LONG_PRESS
                    )
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)
    }

    // snackbar with undo action
    private fun showUndoSnackbar(message: String, actionLabel: String, action: () -> Unit) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(actionLabel) {
                action()
            }
            .show()
    }

    // Sort tasks by priority
    fun sortTasksByPriority() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val sortedTasks = tasks.sortedBy { it.priority }
            taskAdapter.submitList(sortedTasks)
        }
    }

    // Sort tasks by due date
    fun sortTasksByDueDate() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val sortedTasks = tasks.sortedBy { it.dueDate }
            taskAdapter.submitList(sortedTasks)
        }
    }

    // Sort tasks alphabetically
    fun sortTasksAlphabetically() {
        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            val sortedTasks = tasks.sortedBy { it.title }
            taskAdapter.submitList(sortedTasks)
        }
    }
}