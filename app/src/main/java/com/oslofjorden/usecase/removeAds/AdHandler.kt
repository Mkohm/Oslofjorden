package com.oslofjorden.usecase.removeAds

import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.BELOW
import android.widget.RelativeLayout.CENTER_IN_PARENT
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.oslofjorden.R
import kotlinx.android.synthetic.main.activity_main.*

// Static helper class for creating an adview
object AdHandler {

    fun createAd(activity: AppCompatActivity) {
        // todo: Since both size and id have to be set programatically or via xml and we want to change
        // the id based on debug/release builds we have to do initialize it programmatically.
        // Since i now managed to change the google_ads_id with build variants this can now be
        // refactored to be in the xml file.

        val adView = AdView(activity)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = activity.getString(R.string.google_ads_id)

        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(BELOW, R.id.buyLayout)
        params.addRule(CENTER_IN_PARENT, R.id.buyLayout)
        adView.layoutParams = params

        activity.adLayout.addView(adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}
