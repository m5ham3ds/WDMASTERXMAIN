package com.wdmaster.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wdmaster.app.data.model.TestResult
import com.wdmaster.app.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val resultRepository: ResultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private val _results = MutableStateFlow<List<TestResult>>(emptyList())
    val results: StateFlow<List<TestResult>> = _results.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        viewModelScope.launch {
            resultRepository.getRecentResults(100).collect { results ->
                _results.value = results
                updateStats(results)
            }
        }
    }

    private fun updateStats(results: List<TestResult>) {
        val success = results.count { it.success }
        val failure = results.count { !it.success }
        val rate = if (results.isNotEmpty()) (success * 100 / results.size) else 0

        _uiState.value = TestUiState(
            totalCount = results.size,
            successCount = success,
            failedCount = failure,
            successRate = rate
        )
    }

    fun addResult(result: TestResult) {
        val currentResults = _results.value.toMutableList()
        currentResults.add(0, result)

        if (currentResults.size > 100) {
            currentResults.removeAt(currentResults.size - 1)
        }

        _results.value = currentResults
        updateStats(currentResults)
    }

    fun filterResults(success: Boolean?) {
        viewModelScope.launch {
            val filtered = when (success) {
                true -> resultRepository.getSuccessfulResults().first()
                false -> resultRepository.getFailedResults().first()
                null -> resultRepository.getRecentResults(100).first()
            }

            _results.value = filtered
        }
    }

    fun clearResults() {
        viewModelScope.launch {
            resultRepository.clearAllResults()
            _results.value = emptyList()
            _uiState.value = TestUiState()
        }
    }

    fun exportResults(format: ExportFormat) {
        // TODO: implement export
    }
}

data class TestUiState(
    val totalCount: Int = 0,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val successRate: Int = 0,
    val currentFilter: Boolean? = null,
    val isLoading: Boolean = false
)

enum class ExportFormat {
    CSV, JSON, TXT
}
