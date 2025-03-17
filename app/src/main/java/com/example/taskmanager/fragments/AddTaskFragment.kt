package com.example.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.application.TaskApplication
import com.example.taskmanager.database.Task
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.example.taskmanager.viewModel.TaskViewModel
import com.example.taskmanager.viewModel.TaskViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar

class AddTaskFragment : Fragment() {

    private lateinit var binding: FragmentAddTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private val args: AddTaskFragmentArgs by navArgs()

    private var selectedDueDate: Long = System.currentTimeMillis()
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireActivity().application as TaskApplication).repository

        val viewModelFactory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, viewModelFactory)[TaskViewModel::class.java]

        val taskId = args.taskId

        val priorities = listOf("Low", "Medium", "High")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = adapter

        binding.btnDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        if (taskId != 0) {
            // Editing an existing task
            taskViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
                binding.etTaskTitle.setText(task.title)
                binding.etTaskDescription.setText(task.description)
                binding.spinnerPriority.setSelection(priorities.indexOf(task.priority))
                selectedDueDate = task.dueDate
            }
        }

        binding.btnSaveTask.setOnClickListener {
            val title = binding.etTaskTitle.text.toString()
            val description = binding.etTaskDescription.text.toString()
            val priority = binding.spinnerPriority.selectedItem as String

            if (title.isNotEmpty()) {
                val task = if (taskId != 0) {
                    // Update existing task
                    Task(
                        id = taskId,
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = selectedDueDate
                    )
                } else {
                    // Create new task
                    Task(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = selectedDueDate
                    )
                }

                if (taskId != 0) {
                    taskViewModel.update(task)
                } else {
                    taskViewModel.insert(task)
                }

                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(calendar.timeInMillis)
            .setTitleText("Select Date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            calendar.timeInMillis = selection
            selectedDueDate = selection
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }
}