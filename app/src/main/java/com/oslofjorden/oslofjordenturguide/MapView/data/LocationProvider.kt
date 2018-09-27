package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

interface LocationProvider {
    fun getLocation(currentLocation: MutableLiveData<LatLng>, locationEnabled: MutableLiveData<Boolean>)

    fun stopLocationUpdates(locationEnabled: MutableLiveData<Boolean>)
}