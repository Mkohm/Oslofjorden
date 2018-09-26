package com.oslofjorden.oslofjordenturguide.MapView.data

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.RequiresPermission
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng

class AndroidLocationProvider(context: Context) : LocationProvider {
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    init {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest
    }

    @RequiresPermission(allOf = arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
    override fun getLocation(currentLocation: MutableLiveData<LatLng>) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                currentLocation.value = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
            }
        }

        fusedLocationProviderClient?.requestLocationUpdates(createLocationRequest(), locationCallback, null)
    }
}