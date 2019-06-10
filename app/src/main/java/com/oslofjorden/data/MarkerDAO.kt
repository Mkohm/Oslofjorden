package com.oslofjorden.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.model.MarkerData

interface MarkerDAO {
    fun readMarkers(markers: MutableLiveData<MarkerData>)
}
