package com.rakuishi.rrr.ui

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.rakuishi.rrr.R
import com.rakuishi.rrr.data.db.Activity
import com.rakuishi.rrr.service.TrackingService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DEFAULT_LOCATION = LatLng(35.6812, 139.7671)
private const val DEFAULT_ZOOM = 15f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val trackingPoints by TrackingService.trackingPoints.collectAsState()
    val elapsedMs by TrackingService.elapsedMs.collectAsState()
    var hasLocationPermission by remember { mutableStateOf(false) }

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

    // 過去の軌跡選択時にカメラを移動
    LaunchedEffect(selectedPoints) {
        if (selectedPoints.size >= 2) {
            val boundsBuilder = LatLngBounds.builder()
            selectedPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
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

        // 記録中のオーバーレイ: 経過時間と走行距離
        if (isRecording) {
            val distanceM = calculateDistance(trackingPoints)

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = formatTime(elapsedMs),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = formatDistance(distanceM),
                    color = Color.White,
                    fontSize = 18.sp,
                )
            }
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
                    .padding(start = 16.dp, bottom = 48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(
                        if (showingHistory) R.drawable.ic_close else R.drawable.ic_list
                    ),
                    contentDescription = if (showingHistory) "閉じる" else "履歴",
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // 待機モード: 記録開始 FAB（軌跡表示中は非表示）
        if (!isRecording && !showingHistory) {
            FloatingActionButton(
                onClick = { viewModel.startRecording() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .size(64.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = "開始",
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // 記録中: 停止ボタン
        if (isRecording) {
            FloatingActionButton(
                onClick = { viewModel.stopRecording() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .size(64.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_stop),
                    contentDescription = "停止",
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        // ボトムシート: 過去の走行記録一覧
        if (uiState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideBottomSheet() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            ) {
                ActivityListContent(
                    activities = activities,
                    onActivityClick = { viewModel.selectActivity(it.id) },
                    onActivityDelete = { viewModel.deleteActivity(it.id) },
                )
            }
        }
    }
}

@Composable
private fun ActivityListContent(
    activities: List<Activity>,
    onActivityClick: (Activity) -> Unit,
    onActivityDelete: (Activity) -> Unit,
) {
    val listState = rememberLazyListState()

    // リストが先頭にいるときだけ上方向のスクロールをシートのドラッグに委譲する
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "アクティビティ",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        if (activities.isEmpty()) {
            Text(
                text = "アクティビティはまだありません",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection),
            ) {
                items(activities) { activity ->
                    ActivityRow(
                        activity = activity,
                        onClick = { onActivityClick(activity) },
                        onDelete = { onActivityDelete(activity) },
                    )
                    HorizontalDivider()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ActivityRow(
    activity: Activity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(Date(activity.createdAt)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = formatDistance(activity.totalDistanceM),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = formatTime(activity.totalTimeMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "削除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("アクティビティを削除") },
            text = { Text("このアクティビティを削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            },
        )
    }
}

private fun calculateDistance(points: List<com.rakuishi.rrr.data.db.Point>): Double {
    var total = 0.0
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val curr = points[i]
        val results = FloatArray(1)
        Location.distanceBetween(
            prev.latitude, prev.longitude,
            curr.latitude, curr.longitude,
            results,
        )
        total += results[0]
    }
    return total
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format(Locale.US, "%.2f km", meters / 1000)
    } else {
        String.format(Locale.US, "%.0f m", meters)
    }
}
