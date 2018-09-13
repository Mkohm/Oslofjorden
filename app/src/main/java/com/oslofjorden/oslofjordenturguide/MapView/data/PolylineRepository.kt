package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData

class PolylineRepository(private val polylineDataAccessObject: PolylineDataAccessObject) {

    fun getPolylines(polylines: MutableLiveData<List<PolylineData>>) {
        polylineDataAccessObject.readPolylines(polylines)
    }
}
