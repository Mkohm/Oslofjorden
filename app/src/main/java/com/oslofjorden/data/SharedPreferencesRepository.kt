package com.oslofjorden.data

import androidx.lifecycle.MutableLiveData

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

    fun setCurrentMapItems(newMapItems: BooleanArray, currentMapItems: MutableLiveData<BooleanArray>) {
        sharedPreferencesReader.setCurrentMapItems(newMapItems, currentMapItems)
    }

    fun setAppOpenedBefore(isAppOpenedBefore: MutableLiveData<Boolean>) {
        sharedPreferencesReader.setAppOpenedBefore(isAppOpenedBefore)
    }
}