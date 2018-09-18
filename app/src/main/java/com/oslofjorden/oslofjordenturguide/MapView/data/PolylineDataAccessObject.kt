package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData

interface PolylineDataAccessObject {

    fun readPolylines(liveData: MutableLiveData<PolylineData>)
}
