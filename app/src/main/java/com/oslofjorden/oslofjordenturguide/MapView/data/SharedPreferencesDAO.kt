package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData

interface SharedPreferencesDAO {
    fun hasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>)
    fun setHasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>)

    fun isFirstTimeLaunchingApp(isFirstTimeLaunchingApp: MutableLiveData<Boolean>)
    fun setAppOpenedBefore(isFirstTimeLaunchingApp: MutableLiveData<Boolean>)

    fun setCurrentMapItems(currentMapItems: MutableLiveData<BooleanArray>)
    fun getCurrentMapItems(currentMapItems: MutableLiveData<BooleanArray>)
}
