package com.oslofjorden.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.oslofjorden.model.PolylineData

class PolylineRepository(private val polylineDataAccessObject: PolylineDAO) {

    fun getPolylines(): LiveData<PolylineData> {
        val liveData = MutableLiveData<PolylineData>()
        polylineDataAccessObject.readPolylines(liveData)
        return liveData
    }
}
