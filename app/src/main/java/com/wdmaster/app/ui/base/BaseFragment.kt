package com.wdmaster.app.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupUI()
        setupClickListeners()
    }

    open fun setupObservers() {}
    open fun setupUI() {}
    open fun setupClickListeners() {}

    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        context?.let {
            Toast.makeText(it, message, duration).show()
        }
    }

    protected fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        binding.root.let {
            Snackbar.make(it, message, duration).show()
        }
    }

    protected fun showSnackbar(message: String, action: String, onClick: () -> Unit) {
        binding.root.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction(action) { onClick() }
                .show()
        }
    }

    protected fun hideKeyboard() {
        view?.let {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}