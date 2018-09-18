package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData

class PolylineRepository(private val polylineDataAccessObject: PolylineDataAccessObject) {

    fun getPolylines(): LiveData<PolylineData> {
        val liveData = MutableLiveData<PolylineData>()
        polylineDataAccessObject.readPolylines(liveData)
        return liveData
    }
}
