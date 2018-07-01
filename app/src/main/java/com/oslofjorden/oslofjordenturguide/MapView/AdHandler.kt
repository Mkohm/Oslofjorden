package com.oslofjorden.oslofjordenturguide.MapView

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.oslofjorden.BuildConfig

class AdHandler(val context: Context) {
    private val testID = "ca-app-pub-3940256099942544~3347511713"
    private val prodID = "ca-app-pub-8816231201193091/8419082785"


    init {

        val bannerID = if (BuildConfig.DEBUG) testID else prodID

        MobileAds.initialize(context, bannerID)
    }


}
