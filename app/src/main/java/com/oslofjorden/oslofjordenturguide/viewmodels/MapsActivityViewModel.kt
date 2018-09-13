package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.MarkerData
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerDataRepository
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerReader

class MapsActivityViewModel(application: Application) : AndroidViewModel(Application()) {


    val markerDataRepository = MarkerDataRepository(MarkerReader(application.applicationContext))

    val markers = MutableLiveData<List<MarkerData>>()


    init {
        loadMarkers()
    }

    private fun loadMarkers() {
        return markerDataRepository.getMarkers(markers)
    }
}

