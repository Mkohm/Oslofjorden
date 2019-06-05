package com.oslofjorden.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.model.PolylineData

interface PolylineDAO {

    fun readPolylines(liveData: MutableLiveData<PolylineData>)
}
