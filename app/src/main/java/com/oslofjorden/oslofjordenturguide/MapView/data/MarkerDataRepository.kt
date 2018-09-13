package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.MarkerData


class MarkerDataRepository(private val markerDataAccessObject: MarkerDataAccessObject) {

    fun getMarkers(markers: MutableLiveData<List<MarkerData>>) {
        markerDataAccessObject.readMarkers(markers)
    }
}