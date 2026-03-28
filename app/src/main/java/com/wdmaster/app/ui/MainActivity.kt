package com.wdmaster.app.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.wdmaster.app.R
import com.wdmaster.app.databinding.ActivityMainBinding
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.TestServiceBridge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var testService: TestService? = null
    private var serviceBridge: TestServiceBridge? = null

    // ✅ مهم جدًا لتفادي الكراش
    private var isServiceBound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TestService.LocalBinder
            testService = binder.getService()
            serviceBridge = testService

            isServiceBound = true

            bindServiceEvents()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            testService = null
            serviceBridge = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        loadDefaultFragment()
    }

    override fun onStart() {
        super.onStart()

        Intent(this, TestService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        // ✅ الحل الحقيقي للكراش
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_test -> {
                    loadFragment(TestFragment())
                    true
                }

                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadDefaultFragment() {
        loadFragment(HomeFragment())
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun bindServiceEvents() {
        serviceBridge?.observeEvents()
            ?.onEach { event ->
                when (event) {

                    is TestServiceBridge.ServiceEvent.StatsUpdate -> {
                        // TODO: update UI
                    }

                    is TestServiceBridge.ServiceEvent.TestResult -> {
                        // TODO: update results
                    }

                    else -> {}
                }
            }
            ?.launchIn(lifecycleScope)
    }

    fun getServiceBridge(): TestServiceBridge? = serviceBridge
}
