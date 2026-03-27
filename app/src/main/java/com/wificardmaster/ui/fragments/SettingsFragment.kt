package com.wdmaster.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.wdmaster.app.R
import com.wdmaster.app.data.model.Router
import com.wdmaster.app.data.repository.RouterRepository
import com.wdmaster.app.data.repository.SettingsRepository
import com.wdmaster.app.databinding.FragmentSettingsBinding
import com.wdmaster.app.ui.adapter.RouterAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var routerAdapter: RouterAdapter

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var routerRepository: RouterRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDarkModeToggle()
        setupRouterList()
        setupAddRouterDialog()
        loadSettings()
    }

    private fun setupDarkModeToggle() {
        binding.swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                settingsRepository.toggleDarkMode(isChecked)

                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
    }

    private fun loadSettings() {
        settingsRepository.settingsFlow
            .onEach { settings ->
                binding.swDarkMode.isChecked = settings.darkModeEnabled
                binding.swVibrate.isChecked = settings.vibrateOnSuccess
                binding.swSound.isChecked = settings.soundOnSuccess
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupRouterList() {
        routerAdapter = RouterAdapter(
            onEdit = { router -> showEditRouterDialog(router) },
            onDelete = { router -> confirmDeleteRouter(router) },
            onToggle = { router, active -> toggleRouter(router, active) }
        )

        binding.rvRouters.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = routerAdapter
        }

        routerRepository.allRoutersFlow
            .onEach { routers ->
                routerAdapter.submitList(routers)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupAddRouterDialog() {
        binding.btnAddRouter.setOnClickListener {
            showEditRouterDialog(null)
        }
    }

    private fun showEditRouterDialog(router: Router?) {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_router_edit, binding.root, false)

        val etName = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName)
        val etPrefix = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPrefix)
        val etChipset = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etChipset)

        if (router != null) {
            etName.setText(router.name)
            etPrefix.setText(router.macPrefix)
            etChipset.setText(router.chipset)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (router == null) "Add Router" else "Edit Router")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val newRouter = Router(
                    id = router?.id ?: 0,
                    name = etName.text.toString(),
                    macPrefix = etPrefix.text.toString(),
                    chipset = etChipset.text.toString().takeIf { it.isNotBlank() },
                    isActive = router?.isActive ?: true
                )

                lifecycleScope.launch {
                    if (router == null) {
                        routerRepository.addRouter(newRouter)
                    } else {
                        routerRepository.updateRouter(newRouter)
                    }

                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteRouter(router: Router) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Router?")
            .setMessage("Are you sure you want to delete '${router.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    routerRepository.deleteRouter(router)
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleRouter(router: Router, active: Boolean) {
        lifecycleScope.launch {
            routerRepository.toggleActive(router.id, active)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
