package com.krystofmacek.runtracker.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.krystofmacek.runtracker.R
import com.krystofmacek.runtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.krystofmacek.runtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.krystofmacek.runtracker.other.Constants.ACTION_STOP_SERVICE
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

    private var menu: Menu? = null

    private var currentTimeInMillis = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


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

    /**
     * Cancelling a run functionality
     * */
    /**
     * onCreate options menu - inflate menu
     * */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    /**
     * if the run is longer then 0ms show the options menu
     * */
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }
    /**
     * handle the selection of items in menu
     * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Displaying dialog to cancel run
     * */
    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run")
            .setMessage("Are you sure to cancel the current run and delete all its data")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _ , _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    /**
     * stop run and navigate to run fragment
     * */
    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    /**
     * Pausing the run
     * */
    private fun toggleRun() {
        if(isTracking) {
            menu?.getItem(0)?.isVisible = true
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
            menu?.getItem(0)?.isVisible = true
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