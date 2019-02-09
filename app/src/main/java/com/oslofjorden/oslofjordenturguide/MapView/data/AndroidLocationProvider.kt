package com.oslofjorden.oslofjordenturguide.MapView.data

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.lifecycle.MutableLiveData
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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

    @RequiresPermission(allOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    override fun getLocation(currentLocation: MutableLiveData<LatLng>, locationEnabled: MutableLiveData<Boolean>) {

        // We set the value to true even before the first location update to make sure that the
        // icon showing if location is giving feedback of a press
        locationEnabled.value = true

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                currentLocation.value =
                    LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
            }
        }

        fusedLocationProviderClient?.requestLocationUpdates(createLocationRequest(), locationCallback, null)
    }

    override fun stopLocationUpdates(locationEnabled: MutableLiveData<Boolean>) {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
        locationEnabled.value = false
    }
}