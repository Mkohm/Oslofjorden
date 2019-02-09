package com.oslofjorden.oslofjordenturguide.MapView.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData

interface MarkerDAO {
    fun readMarkers(markers: MutableLiveData<MarkerData>)
}
