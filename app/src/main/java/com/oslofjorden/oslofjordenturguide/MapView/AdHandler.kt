package com.oslofjorden.oslofjordenturguide.MapView

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.ads.MobileAds
import com.oslofjorden.BuildConfig
import kotlinx.android.synthetic.main.activity_maps.*


class AdHandler(val activity: AppCompatActivity) {
    private val testID = "ca-app-pub-3940256099942544~3347511713"
    private val prodID = "ca-app-pub-8816231201193091/8419082785"


    init {


        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        val userHasBoughtRemoveAds = sharedPref.getBoolean("userHasBoughtRemoveAds", true)


        if (userHasBoughtRemoveAds) {
            activity.adLayout.visibility = View.GONE
        } else {

            val bannerID = if (BuildConfig.DEBUG) testID else prodID
            MobileAds.initialize(activity, bannerID)
        }

    }


}
