package com.rakuishi.rrr.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

// 東京駅をデフォルトの初期位置として使う
private val DEFAULT_LOCATION = LatLng(35.6812, 139.7671)
private const val DEFAULT_ZOOM = 15f

@Composable
fun MainScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    )
}
