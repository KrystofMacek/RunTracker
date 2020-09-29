package com.krystofmacek.runtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.krystofmacek.runtracker.R
import com.krystofmacek.runtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.krystofmacek.runtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.krystofmacek.runtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.krystofmacek.runtracker.other.Constants.ACTION_STOP_SERVICE
import com.krystofmacek.runtracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.krystofmacek.runtracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.krystofmacek.runtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.krystofmacek.runtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.krystofmacek.runtracker.other.Constants.NOTIFICATION_ID
import com.krystofmacek.runtracker.other.Constants.TIMER_UPDATE_INTERVAL
import com.krystofmacek.runtracker.other.TrackingUtility
import com.krystofmacek.runtracker.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

// List of Lat Long coordinates
typealias Polyline = MutableList<LatLng>

// List of polylines
typealias Polylines = MutableList<Polyline>

/**
 * Service managing the tracking of users location and path taken during a run
 *  extending LifecycleService -> provides functionality of observable
 * */
class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
    }

    /**
     * used in onCreate() to initialize variables for tracking (isTracking and Polyline)
     *  sets isTracking boolean to false
     *  creates empty list used to store polyline (path of the run)
     * */
    private fun initTrackingValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        initTrackingValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        // Observe changes in LiveData object isTracking
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    /**
     * Called when isTracking changes (tracking starts / stops).
     *  false -> removes Location Updates of Location Provider
     *  true -> request Location Updates of Location Provider
     * */
    @SuppressLint("MissingPermission") // Permissions check is done with EasyPermissions
    private fun updateLocationTracking(isTracking: Boolean)  {
        if(isTracking) {
            // If the tracking is allowed
            if(TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     *  Crete LocationCallback object
     *      get PathPoints from location results and add them to polyline
     * */
    // get last location point and add it to the end of polyline
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION ${location.latitude} , ${location.longitude}")
                    }
                }
            }
        }
    }

    /**
     * Adds new position to pathPoints
     * */
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }




    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    /**
     * Override onStartCommand method of Lifecycle Service
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {

                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service")
                        startTimer()
                    }
                    Timber.d("Service Started / Resumed")
                }

                ACTION_STOP_SERVICE -> {
                    Timber.d("Service Stopped")
                }

                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Service Paused")

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun startForegroundService() {

        startTimer()
        isTracking.postValue(true)

        // Create Notification Manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create Notification Channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        // Build Notification
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Run Tracker")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    /**
     * Initialize path lists
     * */
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    // create pending intent that leads to Main Activity
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java)
            .also {
                // Tracking fragment will not show up by default, when the main activity is started -> extra action is necessary
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
        FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            //low importance so we dont get sound notification
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Stopwatch functionality
     * */

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        /**
         * Start coroutine, responsible for updating timer
         * */
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                // update every second
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

}