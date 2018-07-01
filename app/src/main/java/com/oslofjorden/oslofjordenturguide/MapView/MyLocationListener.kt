package com.oslofjorden.oslofjordenturguide.MapView

import android.Manifest
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener

import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.oslofjorden.R
import kotlinx.android.synthetic.main.activity_maps.*

class MyLocationListener(private val context: Context, private val activity: AppCompatActivity, private val mMap: GoogleMap?, private val onOffLocationButton: ImageButton, private val lifecycle: Lifecycle, private val callback: OnLocationChangedListener) : LifecycleObserver, LocationListener {


    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(p0: Location?) {
        callback.onLocationChanged(p0)
    }

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
            mMap?.isMyLocationEnabled = true
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        // disconnect if connected
        disableMyLocation()
    }


    fun disableMyLocation() {
        locationManager.removeUpdates(this)
        enabled = false
        onOffLocationButton.setImageResource(R.drawable.ic_location_off)
        mMap?.isMyLocationEnabled = false
    }
}