package com.oslofjorden.oslofjordenturguide.data

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

interface LocationProvider {
    fun getLocation(currentLocation: MutableLiveData<LatLng>, locationEnabled: MutableLiveData<Boolean>)

    fun stopLocationUpdates(locationEnabled: MutableLiveData<Boolean>)
}