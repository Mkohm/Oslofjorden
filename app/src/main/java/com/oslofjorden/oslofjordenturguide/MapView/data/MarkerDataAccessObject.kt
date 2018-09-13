package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData


interface MarkerDataAccessObject {
    fun readMarkers(markers: MutableLiveData<List<MarkerData>>)
}

