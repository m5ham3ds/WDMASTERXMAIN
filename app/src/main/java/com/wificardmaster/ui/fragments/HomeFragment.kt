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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wdmaster.app.R
import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.databinding.FragmentHomeBinding
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.TestServiceBridge
import com.wdmaster.app.ui.adapter.TerminalAdapter
import com.wdmaster.app.utils.TerminalLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private var testService: TestService? = null
    private lateinit var terminalAdapter: TerminalAdapter
    
    @Inject
    lateinit var terminalLogger: TerminalLogger
    
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTerminal()
        setupControls()
        setupSliders()
        updateStatusUI(false)
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
    
    private fun setupTerminal() {
        terminalAdapter = TerminalAdapter()
        binding.rvTerminal.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = terminalAdapter
        }
    }
    
    private fun setupControls() {
        binding.btnStart.setOnClickListener {
            val config = CardConfig(
                prefix = binding.etPrefix.text.toString(),
                length = binding.etLength.text.toString().toIntOrNull() ?: 16,
                allowedChars = binding.etChars.text.toString().takeIf { it.isNotEmpty() } ?: "0123456789ABCDEF",
                maxTries = binding.etMaxTries.text.toString().toLongOrNull() ?: 1000
            )
            
            if (!config.validate()) {
                Toast.makeText(requireContext(), "Invalid configuration", Toast.LENGTH_SHORT).show()
                return@setOnClickListener            }
            
            testService?.sendCommand(TestServiceBridge.ServiceCommand.UpdateConfig(config))
            testService?.sendCommand(TestServiceBridge.ServiceCommand.Start)
        }
        
        binding.btnStop.setOnClickListener {
            testService?.sendCommand(TestServiceBridge.ServiceCommand.Stop)
        }
        
        binding.btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Export functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupSliders() {
        binding.sliderDelay.addOnChangeListener { _, value, _ ->
            binding.tvDelayVal.text = "Delay: ${value.toInt()}ms"
            testService?.sendCommand(
                TestServiceBridge.ServiceCommand.UpdateSettings(
                    skipTested = binding.swSkipTested.isChecked,
                    stopOnSuccess = binding.swStopOnSuccess.isChecked,
                    delayMs = value.toLong()
                )
            )
        }
        
        binding.swSkipTested.setOnCheckedChangeListener { _, _ ->
            updateServiceSettings()
        }
        
        binding.swStopOnSuccess.setOnCheckedChangeListener { _, _ ->
            updateServiceSettings()
        }
    }
    
    private fun updateServiceSettings() {
        testService?.sendCommand(
            TestServiceBridge.ServiceCommand.UpdateSettings(
                skipTested = binding.swSkipTested.isChecked,
                stopOnSuccess = binding.swStopOnSuccess.isChecked,
                delayMs = binding.sliderDelay.value.toLong()
            )
        )
    }
    
    private fun bindServiceEvents() {
        testService?.observeEvents()?.onEach { event ->
            when (event) {
                is TestServiceBridge.ServiceEvent.LogMessage -> {                    terminalAdapter.addLog(event.msg, event.type)
                    binding.rvTerminal.smoothScrollToPosition(terminalAdapter.itemCount - 1)
                }
                is TestServiceBridge.ServiceEvent.StatsUpdate -> {
                    binding.statTested.text = "Tested:\n${event.stats.tested}"
                    binding.statSuccess.text = "Success:\n${event.stats.success}"
                    binding.statFailure.text = "Failed:\n${event.stats.failure}"
                    binding.progressBar.setProgressCompat((event.stats.progress * 100).toInt(), true)
                    binding.tvProgressPercent.text = "${(event.stats.progress * 100).toInt()}%"
                }
                is TestServiceBridge.ServiceEvent.ServiceStarted -> {
                    updateStatusUI(true)
                }
                is TestServiceBridge.ServiceEvent.ServiceStopped -> {
                    updateStatusUI(false)
                    Toast.makeText(requireContext(), "Service Stopped", Toast.LENGTH_SHORT).show()
                }
                is TestServiceBridge.ServiceEvent.ServicePaused -> {
                    binding.btnStart.text = "RESUME"
                }
                is TestServiceBridge.ServiceEvent.ServiceResumed -> {
                    binding.btnStart.text = "START"
                }
                else -> {}
            }
        }?.launchIn(viewLifecycleOwner.lifecycleScope)
    }
    
    private fun updateStatusUI(isRunning: Boolean) {
        binding.statusDot.setBackgroundResource(
            if (isRunning) R.drawable.bg_dot_green else R.drawable.bg_dot_red
        )
        binding.tvStatus.text = if (isRunning) "Running" else "Disconnected"
        binding.btnStart.isEnabled = !isRunning
        binding.btnStop.isEnabled = isRunning
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}