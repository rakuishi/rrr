package com.rakuishi.rrr.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.rakuishi.rrr.R
import com.rakuishi.rrr.service.TrackingService
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

private val DEFAULT_LOCATION = LatLng(35.6812, 139.7671)
private const val DEFAULT_ZOOM = 15f

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val trackingPoints by TrackingService.trackingPoints.collectAsState()
    val elapsedMs by TrackingService.elapsedMs.collectAsState()
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isFollowingUser by remember { mutableStateOf(false) }
    // カメラ追従のアニメーション回数を追跡し、ユーザー操作と区別する
    var programmaticMoveCount by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
    }

    val context = LocalContext.current
    LaunchedEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@LaunchedEffect
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    programmaticMoveCount++
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM)
                    )
                }
            }
        } catch (_: SecurityException) {
        }
    }

    val isRecording = uiState.mode == TrackingMode.RECORDING
    val routePoints = trackingPoints.map { LatLng(it.latitude, it.longitude) }
    val selectedPoints = uiState.selectedActivityPoints.map { LatLng(it.latitude, it.longitude) }
    val showingHistory = selectedPoints.isNotEmpty()

    // 記録開始時にカメラ追従を有効化
    LaunchedEffect(isRecording) {
        if (isRecording) {
            isFollowingUser = true
        }
    }

    // 記録中: 新しいポイントが追加されたらカメラを追従
    val scope = rememberCoroutineScope()
    LaunchedEffect(isRecording) {
        if (!isRecording) return@LaunchedEffect
        snapshotFlow { trackingPoints.lastOrNull() }
            .distinctUntilChanged()
            .drop(1) // 初回（既存リスト）をスキップ
            .collect { point ->
                if (point != null && isFollowingUser) {
                    val latLng = LatLng(point.latitude, point.longitude)
                    programmaticMoveCount++
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLng(latLng)
                    )
                }
            }
    }

    // ユーザーが地図を手動操作したら追従を停止
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving }
            .collect { isMoving ->
                if (!isMoving && programmaticMoveCount > 0) {
                    // プログラムによる移動が完了
                    programmaticMoveCount--
                } else if (isMoving && programmaticMoveCount == 0 && isFollowingUser) {
                    // ユーザー操作による移動
                    isFollowingUser = false
                }
            }
    }

    // 過去の軌跡選択時にカメラを移動
    LaunchedEffect(selectedPoints) {
        if (selectedPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.builder()
            selectedPoints.forEach { boundsBuilder.include(it) }
            programmaticMoveCount++
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
            )
        }
    }

    val density = LocalDensity.current
    val navBarHeight = with(density) { WindowInsets.navigationBars.getBottom(density).toDp() }
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
            ),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = statusBarHeight,
                bottom = navBarHeight,
            ),
        ) {
            // 記録中のリアルタイムルート
            if (routePoints.size >= 2) {
                Polyline(
                    points = routePoints,
                    color = Color(0xFF1976D2),
                    width = 12f,
                )
            }
            // 過去の軌跡表示
            if (selectedPoints.size >= 2) {
                Polyline(
                    points = selectedPoints,
                    color = Color(0xFF1976D2),
                    width = 12f,
                )
            }
        }

        // 記録中のオーバーレイ
        if (isRecording) {
            RecordingOverlay(
                elapsedMs = elapsedMs,
                distanceM = calculateDistance(trackingPoints),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // リストボタン（端に配置）: 待機モード → リストアイコン、軌跡表示中 → ×アイコン
        if (!isRecording) {
            SmallFloatingActionButton(
                onClick = {
                    if (showingHistory) {
                        viewModel.clearSelectedActivity()
                    } else {
                        viewModel.showBottomSheet()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(
                        if (showingHistory) R.drawable.ic_close else R.drawable.ic_list
                    ),
                    contentDescription = if (showingHistory) stringResource(R.string.content_description_close) else stringResource(R.string.content_description_history),
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // 待機モード: 記録開始 ExtendedFAB（軌跡表示中は非表示）
        if (!isRecording && !showingHistory) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startRecording() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(50),
                containerColor = Color(0xFFB4C5FF),
                contentColor = Color(0xFF1E3264),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.action_start),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        }

        // 記録中: 停止ボタン（角丸四角）
        if (isRecording) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.stopRecording() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .height(72.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = Color(0xFFFFB4A9),
                contentColor = Color(0xFF680003),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_stop),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.action_stop),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        }

        // 現在地ボタン（右下）
        if (hasLocationPermission) {
            SmallFloatingActionButton(
                onClick = {
                    // 記録中なら追従を再開
                    if (isRecording) {
                        isFollowingUser = true
                    }
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    try {
                        fusedClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                programmaticMoveCount++
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(location.latitude, location.longitude),
                                            DEFAULT_ZOOM,
                                        )
                                    )
                                }
                            }
                        }
                    } catch (_: SecurityException) {
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_my_location),
                    contentDescription = stringResource(R.string.content_description_my_location),
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // ボトムシート: 過去のアクティビティ一覧
        if (uiState.showBottomSheet) {
            ActivityBottomSheet(
                activities = activities,
                onActivityClick = { viewModel.selectActivity(it.id) },
                onActivityDelete = { viewModel.deleteActivity(it.id) },
                onDismiss = { viewModel.hideBottomSheet() },
            )
        }
    }
}
