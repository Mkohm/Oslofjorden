package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerDataRepository
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerReader
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineReader
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineRepository
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData

class MapsActivityViewModel(application: Application) : AndroidViewModel(Application()) {
    private val markerDataRepository = MarkerDataRepository(MarkerReader(application.applicationContext))
    private val polylineRepository = PolylineRepository(PolylineReader(application.applicationContext))


    val markers = MutableLiveData<List<MarkerData>>()
    val polylines = MutableLiveData<List<PolylineData>>()
    val dataLoaded = MutableLiveData<Boolean>()
    val inAppPurchased = MutableLiveData<Boolean>()

    init {
        loadMarkers()
        loadPolylines()
        dataLoaded.value = false
        inAppPurchased.value = hasBoughtInAppPurchase(application)
    }

    private fun loadMarkers() {
        return markerDataRepository.getMarkers(markers)
    }

    private fun loadPolylines() {
        val polylines = polylineRepository.getPolylines(polylines)
        dataLoaded.value = true
        return polylines
    }

    fun hasBoughtInAppPurchase(application: Application): Boolean {
        val sharedPref = application.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val userHasBoughtRemoveAds = sharedPref.getBoolean("userHasBoughtRemoveAds", false)
        return userHasBoughtRemoveAds
    }
}

