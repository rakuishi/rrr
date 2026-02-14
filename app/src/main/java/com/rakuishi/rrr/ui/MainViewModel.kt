package com.rakuishi.rrr.ui

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rakuishi.rrr.App
import com.rakuishi.rrr.data.db.Activity
import com.rakuishi.rrr.data.db.Point
import com.rakuishi.rrr.service.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TrackingMode {
    IDLE,
    RECORDING,
}

data class MainUiState(
    val mode: TrackingMode = TrackingMode.IDLE,
    val currentActivityId: Long = 0,
    val showBottomSheet: Boolean = false,
    val selectedActivityPoints: List<Point> = emptyList(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as App).repository

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val activities: StateFlow<List<Activity>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startRecording() {
        viewModelScope.launch {
            val activityId = repository.createActivity()
            _uiState.value = _uiState.value.copy(
                mode = TrackingMode.RECORDING,
                currentActivityId = activityId,
            )

            TrackingService.resetState()
            val intent = Intent(getApplication(), TrackingService::class.java).apply {
                action = TrackingService.ACTION_START
                putExtra(TrackingService.EXTRA_ACTIVITY_ID, activityId)
            }
            getApplication<App>().startForegroundService(intent)
        }
    }

    fun stopRecording() {
        val activityId = _uiState.value.currentActivityId

        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        getApplication<App>().startService(intent)

        viewModelScope.launch {
            val points = repository.getPoints(activityId)
            val activity = repository.getActivity(activityId)
            if (activity != null && points.size >= 2) {
                var totalDistance = 0.0
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val results = FloatArray(1)
                    Location.distanceBetween(
                        prev.latitude, prev.longitude,
                        curr.latitude, curr.longitude,
                        results,
                    )
                    totalDistance += results[0]
                }
                val totalTime = points.last().timestamp - points.first().timestamp
                repository.updateActivity(
                    activity.copy(
                        totalTimeMs = totalTime,
                        totalDistanceM = totalDistance,
                    )
                )
            }
            _uiState.value = _uiState.value.copy(mode = TrackingMode.IDLE)
        }
    }

    fun showBottomSheet() {
        _uiState.value = _uiState.value.copy(showBottomSheet = true)
    }

    fun hideBottomSheet() {
        _uiState.value = _uiState.value.copy(
            showBottomSheet = false,
            selectedActivityPoints = emptyList(),
        )
    }

    fun selectActivity(activityId: Long) {
        viewModelScope.launch {
            val points = repository.getPoints(activityId)
            _uiState.value = _uiState.value.copy(
                showBottomSheet = false,
                selectedActivityPoints = points,
            )
        }
    }

    fun clearSelectedActivity() {
        _uiState.value = _uiState.value.copy(selectedActivityPoints = emptyList())
    }

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            repository.deleteActivity(activityId)
        }
    }
}
