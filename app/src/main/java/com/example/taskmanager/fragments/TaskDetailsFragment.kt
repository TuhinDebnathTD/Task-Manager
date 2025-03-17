package com.example.taskmanager.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.R
import com.example.taskmanager.application.TaskApplication
import com.example.taskmanager.databinding.FragmentTaskDetailsBinding
import com.example.taskmanager.viewModel.TaskViewModel
import com.example.taskmanager.viewModel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class TaskDetailsFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailsBinding
    private lateinit var taskViewModel: TaskViewModel
    private var taskId: Int = 0
    private val args: AddTaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireActivity().application as TaskApplication).repository

        val viewModelFactory = TaskViewModelFactory(repository)
        taskViewModel = ViewModelProvider(this, viewModelFactory)[TaskViewModel::class.java]

        val taskId = args.taskId

        // Fetching the task details using taskId
        taskViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvTaskDueDate.text = "Due: ${
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(Date(task.dueDate))
            }"
            binding.tvTaskPriority.text = task.priority

            binding.btnMarkCompleted.setOnClickListener {
                task.isCompleted = true
                taskViewModel.update(task)
                findNavController().popBackStack()
            }

            binding.btnEditTask.setOnClickListener {
                val bundle = bundleOf("taskId" to taskId)
                findNavController().navigate(
                    R.id.action_taskDetailsFragment_to_addTaskFragment,
                    bundle
                )
            }

            val centerX = view.width / 2
            val centerY = view.height / 2
            val initialRadius = max(view.width, view.height).toFloat()

            /* val animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, initialRadius, 0f)
             animator.duration = 500
             animator.start()*/
            // Reverse circular reveal when closing
            binding.btnDeleteTask.setOnClickListener {
                val reverseAnimator = ViewAnimationUtils.createCircularReveal(
                    view,
                    centerX,
                    centerY,
                    0f,
                    initialRadius
                )
                reverseAnimator.duration = 1000
                reverseAnimator.start()

                reverseAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        taskViewModel.delete(task)
                        findNavController().popBackStack()
                    }
                })
            }
        }
    }
}