package com.krystofmacek.runtracker.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.krystofmacek.runtracker.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissions(context: Context) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            // if the app is running on Q check also Access background location
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)

        if(!includeMillis) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds:"
        }

        milliseconds /= 10

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds:"

    }

    /**
     * Calculate and return the length of a run
     * */
    fun calcPolylineLength(polyline: Polyline): Float {
        var distance = 0f
        for(i in 1..polyline.size-2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, result)

            distance += result[0]
        }

        return distance
    }

}