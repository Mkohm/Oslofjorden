package com.oslofjorden.oslofjordenturguide.data

import androidx.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.model.PolylineData

interface PolylineDAO {

    fun readPolylines(liveData: MutableLiveData<PolylineData>)
}
