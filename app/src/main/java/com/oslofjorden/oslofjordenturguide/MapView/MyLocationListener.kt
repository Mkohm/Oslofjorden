package com.oslofjorden.oslofjordenturguide.MapView

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.oslofjorden.R

class MyLocationListener(context: Context, private val mMap: GoogleMap?, private val
onOffLocationButton: ImageButton, private val lifecycle: Lifecycle, private val callback: OnLocationChangedListener) : LifecycleObserver, LocationListener {


    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderEnabled(p0: String?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onLocationChanged(p0: Location?) = callback.onLocationChanged(p0)

    var enabled = false
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        if (enabled) {
            // Access to the location has been granted to the app.
            enableMyLocation()
        }
    }


    fun enableMyLocation() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            enabled = true
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this)
            onOffLocationButton.setImageResource(R.drawable.ic_location_on)
            mMap?.isMyLocationEnabled = true
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        disableMyLocation()
    }

    fun disableMyLocation() {
        locationManager.removeUpdates(this)
        enabled = false
        onOffLocationButton.setImageResource(R.drawable.ic_location_off)
        mMap?.isMyLocationEnabled = false
    }
}