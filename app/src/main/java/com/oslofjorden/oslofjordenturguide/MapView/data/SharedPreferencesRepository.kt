package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData

class SharedPreferencesRepository(private val sharedPreferencesReader: SharedPreferencesReader) {

    fun getHasPurchasedRemoveAds(inAppPurchased: MutableLiveData<Boolean>) {
        sharedPreferencesReader.hasPurchasedRemoveAds(inAppPurchased)
    }

    fun setHasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>) {
        sharedPreferencesReader.setHasPurchasedRemoveAds(hasPurchasedRemoveAds)
    }

    fun isFirstTimeLaunchingApp(firstTimeLaunchingApp: MutableLiveData<Boolean>) {
        sharedPreferencesReader.isFirstTimeLaunchingApp(firstTimeLaunchingApp)
    }

    fun getCurrentMapItems(currentMapItems: MutableLiveData<BooleanArray>) {
        sharedPreferencesReader.getCurrentMapItems(currentMapItems)
    }

    fun setAppOpenedBefore(isAppOpenedBefore: MutableLiveData<Boolean>) {
        sharedPreferencesReader.setAppOpenedBefore(isAppOpenedBefore)
    }
}