package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData


interface MarkerDAO {
    fun readMarkers(markers: MutableLiveData<MarkerData>)
}

