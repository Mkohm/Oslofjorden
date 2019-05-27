package com.oslofjorden.oslofjordenturguide.data

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.oslofjorden.oslofjordenturguide.model.MarkerTypes
import com.oslofjorden.oslofjordenturguide.usecase.browseMap.MapsActivity

class SharedPreferencesReader(private val context: Context) : SharedPreferencesDAO {
    override fun isFirstTimeLaunchingApp(isFirstTimeLaunchingApp: MutableLiveData<Boolean>) {

        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val firstTimeUserLaunchesApp = sharedPref.getBoolean("firstTimeUserLaunchesApp", true)

        isFirstTimeLaunchingApp.value = firstTimeUserLaunchesApp
    }

    override fun setAppOpenedBefore(isFirstTimeLaunchingApp: MutableLiveData<Boolean>) {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("firstTimeUserLaunchesApp", false)
        editor.apply()

        isFirstTimeLaunchingApp.value = false
    }

    override fun setCurrentMapItems(newMapItems: BooleanArray, currentMapItems: MutableLiveData<BooleanArray>) {
        val arrayName = "userChecks"

        newMapItems.let {

            val prefs = context.getSharedPreferences(arrayName, 0)
            val editor = prefs.edit()
            editor.putInt(arrayName + "_18", newMapItems.size)

            for (i in newMapItems.indices) {
                editor.putBoolean(arrayName + "_" + i, newMapItems[i])
            }
            editor.apply()

            currentMapItems.value = newMapItems
        }
    }

    override fun getCurrentMapItems(currentMapItems: MutableLiveData<BooleanArray>) {
        val arrayName = "userChecks"
        val prefs = context.getSharedPreferences(arrayName, 0)

        val size = prefs.getInt(arrayName + "_18", 0)
        val array = BooleanArray(size)
        for (i in 0 until size) {
            Log.d(MapsActivity.TAG, "loadArray: " + i + " checked: " + prefs.getBoolean(arrayName + "_" + i, false))
            array[i] = prefs.getBoolean(arrayName + "_" + i, false)
        }

        if (array.isEmpty()) {
            val defaultChecked = BooleanArray(18)
            for (i in 0 until 6) {
                defaultChecked[i] = true
            }

            for (i in 7 until 18) {
                defaultChecked[i] = false
            }

            currentMapItems.value = defaultChecked
        } else {
            currentMapItems.value = array
        }
    }

    override fun setHasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>) {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("userHasBoughtRemoveAds", true)
        editor.apply()

        hasPurchasedRemoveAds.value = true
    }

    override fun hasPurchasedRemoveAds(hasPurchasedRemoveAds: MutableLiveData<Boolean>) {
        val sharedPref = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        hasPurchasedRemoveAds.value = sharedPref.getBoolean("userHasBoughtRemoveAds", false)
    }
}