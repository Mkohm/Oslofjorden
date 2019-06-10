package com.oslofjorden.data

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class LocationInteractor(private val locationProvider: LocationProvider) {

    fun enableLocationUpdates(currentLocation: MutableLiveData<LatLng>, locationEnabled: MutableLiveData<Boolean>) {
        locationProvider.getLocation(currentLocation, locationEnabled)
    }

    fun disableLocationUpdates(locationEnabled: MutableLiveData<Boolean>) {
        locationProvider.stopLocationUpdates(locationEnabled)
    }
}