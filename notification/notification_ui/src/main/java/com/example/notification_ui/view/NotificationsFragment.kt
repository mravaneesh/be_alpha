package com.example.notification_ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notification_ui.R
import com.example.notification_ui.adapter.NotificationAdapter
import com.example.notification_ui.databinding.FragmentNotificationsBinding
import com.example.notification_ui.viewmodel.NotificationViewModel
import com.example.utils.CommonFun
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationViewModel by activityViewModels
    private val adapter = NotificationAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNotifications()
    }

    private fun observeNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collectLatest {
                adapter.submitList(it)
            }
        }

        viewModel.sync(CommonFun.getCurrentUserId()!!)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}