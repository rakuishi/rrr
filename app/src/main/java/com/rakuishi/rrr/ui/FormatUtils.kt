package com.rakuishi.rrr.ui

import android.location.Location
import java.util.Locale

internal fun calculateDistance(points: List<com.rakuishi.rrr.data.db.Point>): Double {
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

internal fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

internal fun formatDistance(meters: Double): String {
    return if (meters >= 1000) {
        String.format(Locale.US, "%.2f km", meters / 1000)
    } else {
        String.format(Locale.US, "%.0f m", meters)
    }
}
