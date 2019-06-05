package com.oslofjorden.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oslofjorden.model.MarkerData

class MarkerDataRepository(private val markerDataAccessObject: MarkerDAO) {

    fun getMarkers(): LiveData<MarkerData> {
        val liveData = MutableLiveData<MarkerData>()
        markerDataAccessObject.readMarkers(liveData)
        return liveData
    }
}