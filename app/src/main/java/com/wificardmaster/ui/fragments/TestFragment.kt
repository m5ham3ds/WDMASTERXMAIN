package com.wdmaster.app.ui

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wdmaster.app.databinding.FragmentTestBinding
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.TestServiceBridge
import com.wdmaster.app.ui.adapter.ResultsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class TestFragment : Fragment() {
    
    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!
    
    private var testService: TestService? = null
    private lateinit var resultsAdapter: ResultsAdapter
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TestService.LocalBinder
            testService = binder.getService()
            bindServiceEvents()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            testService = null
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        return binding.root    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupResultsList()
        setupFilters()
    }
    
    override fun onStart() {
        super.onStart()
        Intent(requireContext(), TestService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    
    override fun onStop() {
        super.onStop()
        requireContext().unbindService(connection)
    }
    
    private fun setupResultsList() {
        resultsAdapter = ResultsAdapter { result ->
            // Handle item click
        }
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultsAdapter
        }
    }
    
    private fun setupFilters() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipAll -> null
                R.id.chipSuccess -> true
                R.id.chipFailed -> false
                else -> null
            }
            resultsAdapter.filterBySuccess(filter)
        }
    }
    
    private fun bindServiceEvents() {
        testService?.observeEvents()?.onEach { event ->
            when (event) {
                is TestServiceBridge.ServiceEvent.TestResult -> {
                    // Add result to adapter
                }
                is TestServiceBridge.ServiceEvent.StatsUpdate -> {
                    binding.tvTotalCount.text = "Total: ${event.stats.tested}"                    binding.tvSuccessCount.text = "✓ ${event.stats.success}"
                    binding.tvFailedCount.text = "✗ ${event.stats.failure}"
                    binding.tvSuccessRate.text = "Rate: ${event.stats.getSuccessRate()}%"
                }
                else -> {}
            }
        }?.launchIn(viewLifecycleOwner.lifecycleScope)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}