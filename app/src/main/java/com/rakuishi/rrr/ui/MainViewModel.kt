package com.rakuishi.rrr.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TrackingMode {
    IDLE,
    RECORDING,
}

data class MainUiState(
    val mode: TrackingMode = TrackingMode.IDLE,
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun startRecording() {
        _uiState.value = _uiState.value.copy(mode = TrackingMode.RECORDING)
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(mode = TrackingMode.IDLE)
    }
}
