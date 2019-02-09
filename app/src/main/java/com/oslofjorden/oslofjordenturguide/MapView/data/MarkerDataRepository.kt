package com.oslofjorden.oslofjordenturguide.MapView.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData

class MarkerDataRepository(private val markerDataAccessObject: MarkerDAO) {

    fun getMarkers(): LiveData<MarkerData> {
        val liveData = MutableLiveData<MarkerData>()
        markerDataAccessObject.readMarkers(liveData)
        return liveData
    }
}