package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class LocationInteractor(private val locationProvider: LocationProvider) {

    fun enableLocationUpdates(currentLocation: MutableLiveData<LatLng>) {
        locationProvider.getLocation(currentLocation)
    }

    fun disableLocationUpdates() {

    }

}