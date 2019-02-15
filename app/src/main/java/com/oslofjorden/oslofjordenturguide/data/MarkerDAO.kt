package com.oslofjorden.oslofjordenturguide.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.model.MarkerData

interface MarkerDAO {
    fun readMarkers(markers: MutableLiveData<MarkerData>)
}
