package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData


class MarkerDataRepository(private val markerDataAccessObject: MarkerDataAccessObject) {

    fun getMarkers(): LiveData<MarkerData> {
        val liveData = MutableLiveData<MarkerData>()
        markerDataAccessObject.readMarkers(liveData)
        return liveData
    }
}