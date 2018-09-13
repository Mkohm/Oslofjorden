package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineReader
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerDataRepository
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerReader
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineRepository

class MapsActivityViewModel(application: Application) : AndroidViewModel(Application()) {
    val markerDataRepository = MarkerDataRepository(MarkerReader(application.applicationContext))
    val polylineRepository = PolylineRepository(PolylineReader(application.applicationContext))


    val markers = MutableLiveData<List<MarkerData>>()
    val polylines = MutableLiveData<List<PolylineData>>()


    init {
        loadMarkers()
        loadPolylines()
    }

    private fun loadMarkers() {
        return markerDataRepository.getMarkers(markers)
    }

    private fun loadPolylines() {
        return polylineRepository.getPolylines(polylines)
    }
}

