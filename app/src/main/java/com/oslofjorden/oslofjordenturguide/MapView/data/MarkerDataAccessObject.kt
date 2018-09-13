package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.MarkerData


interface MarkerDataAccessObject {
    fun readMarkers(users: MutableLiveData<List<MarkerData>>)

    // other methods for reading marking e.g single markers or list of markers filtered or something
}

