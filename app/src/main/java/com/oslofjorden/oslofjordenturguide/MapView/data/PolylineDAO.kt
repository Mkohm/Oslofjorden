package com.oslofjorden.oslofjordenturguide.MapView.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData

interface PolylineDAO {

    fun readPolylines(liveData: MutableLiveData<PolylineData>)
}
