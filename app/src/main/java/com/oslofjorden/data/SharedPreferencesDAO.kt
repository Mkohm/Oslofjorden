package com.oslofjorden.data

import androidx.lifecycle.MutableLiveData

interface SharedPreferencesDAO {
    fun hasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>)
    fun setHasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>)

    fun isFirstTimeLaunchingApp(isFirstTimeLaunchingApp: MutableLiveData<Boolean>)
    fun setAppOpenedBefore(isFirstTimeLaunchingApp: MutableLiveData<Boolean>)

    fun setCurrentMapItems(newMapItems: BooleanArray, currentMapItems: MutableLiveData<BooleanArray>)
    fun getCurrentMapItems(currentMapItems: MutableLiveData<BooleanArray>)
}
