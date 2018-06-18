package com.oslofjorden.oslofjordenturguide.MapView

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.location.Location
import android.location.LocationListener

import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener

class MyLocationListener(val context: Context, val lifecycle: Lifecycle, val callback: OnLocationChangedListener) : LifecycleObserver, LocationListener {
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
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager




    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        if (enabled) {
            // connect
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this)
        }
    }

    fun enable() {
        enabled = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // connect if not connected
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        // disconnect if connected


    }
}