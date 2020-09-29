package com.krystofmacek.runtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.krystofmacek.runtracker.R
import com.krystofmacek.runtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.krystofmacek.runtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.krystofmacek.runtracker.other.Constants.MAP_ZOOM
import com.krystofmacek.runtracker.other.Constants.POLYLINE_COLOR
import com.krystofmacek.runtracker.other.Constants.POLYLINE_WIDTH
import com.krystofmacek.runtracker.other.TrackingUtility
import com.krystofmacek.runtracker.services.Polyline
import com.krystofmacek.runtracker.services.Polylines
import com.krystofmacek.runtracker.services.TrackingService
import com.krystofmacek.runtracker.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var currentTimeInMillis = 0L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }


    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUsersLocation()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMillis = it
            tvTimer.text = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
        })
    }

    private fun toggleRun() {
        if(isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /**
     * Update toggle btn based on state of tracking
     * */
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if(!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    /**
     * Animates the camera movement to keep user centered
     * */
    private fun moveCameraToUsersLocation() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /**
     * Adds all polylines
     * */
    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            map?.addPolyline(
                createPolylineOptions()
                    .addAll(polyline)
            )
        }
    }

    /**
     * Connects new pathPoint to the existing polyline
     * */
    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            // get second to last pathPoint (LatLong) from last polyline
            val secondToLastLL = pathPoints.last()[pathPoints.last().size -2]
            val lastLL = pathPoints.last().last()

            map?.addPolyline(
                createPolylineOptions()
                    .add(secondToLastLL)
                    .add(lastLL)
            )
        }
    }

    private fun createPolylineOptions() = PolylineOptions()
        .color(POLYLINE_COLOR)
        .width(POLYLINE_WIDTH)


    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also{
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}