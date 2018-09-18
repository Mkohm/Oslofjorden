package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import com.oslofjorden.oslofjordenturguide.MapView.data.*
import com.oslofjorden.oslofjordenturguide.MapView.model.MergedData

class MapsActivityViewModel(application: Application) : AndroidViewModel(Application()) {
    private val markerDataRepository = MarkerDataRepository(MarkerReader(application.applicationContext))
    private val polylineRepository = PolylineRepository(PolylineReader(application.applicationContext))
    private val sharedPreferencesRepository = SharedPreferencesRepository(SharedPreferencesReader(application.applicationContext))

    val inAppPurchased = MutableLiveData<Boolean>()
    val firstTimeLaunchingApp = MutableLiveData<Boolean>()
    val currentMapItems = MutableLiveData<BooleanArray>()
    val mapData = MediatorLiveData<MergedData?>()


    init {
        loadMapData()
        sharedPreferencesRepository.getHasPurchasedRemoveAds(inAppPurchased)
        sharedPreferencesRepository.isFirstTimeLaunchingApp(firstTimeLaunchingApp)
        sharedPreferencesRepository.getCurrentMapItems(currentMapItems)
    }

    fun removeAd() {
        sharedPreferencesRepository.setHasPurchasedRemoveAds(inAppPurchased)
    }


    private fun loadMapData() {
        mapData.addSource(markerDataRepository.getMarkers()) {
            if (it != null) {
                mapData.value = it
            }
        }

        mapData.addSource(polylineRepository.getPolylines()) {
            if (it != null) {
                mapData.value = it
            }
        }
    }

    fun setInfoMessageShown() {
        sharedPreferencesRepository.setAppOpenedBefore(firstTimeLaunchingApp)
    }

}

