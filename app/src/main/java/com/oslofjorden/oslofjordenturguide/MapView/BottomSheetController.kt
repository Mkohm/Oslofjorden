package com.oslofjorden.oslofjordenturguide.MapView

import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.model.Polyline
import com.oslofjorden.oslofjordenturguide.R
import com.oslofjorden.oslofjordenturguide.WebView.CustomTabActivityHelper
import com.oslofjorden.oslofjordenturguide.WebView.WebviewFallback


class BottomSheetController(val view: LinearLayout, val activity: MapsActivity) {
    private val behavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(view)

    private val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .setToolbarColor(activity.getResources().getColor(R.color.colorPrimary))
            .setShowTitle(true)
            .build()

    fun collapseBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setLoadingText() {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        titleTextview.text = "Oslofjorden laster inn - vennligst vent.."
    }

    fun setContent(polyline: Polyline) {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.url)

        val title = (polyline.tag as PolylineData).title
        val description = (polyline.tag as PolylineData).description
        val url = (polyline.tag as PolylineData).url

        if (url == null) {
            button.visibility = View.INVISIBLE
        } else {
            button.visibility = View.VISIBLE
        }

        titleTextview.text = title
        descriptionTextview.text = description


        button.setOnClickListener {
            // open link with custom tabs
        }


    }

    fun setMarkerContent(item: MarkerData) {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.url)

        val title = item.title
        val markertypes = item.markerTypes
        val url = item.link

        if (url == null) {
            button.visibility = View.INVISIBLE
        } else {
            button.visibility = View.VISIBLE
        }

        titleTextview.text = title
        titleTextview.visibility = View.VISIBLE

        val builder = StringBuilder()
        for (i in 0 until markertypes.size) {
            if (i != markertypes.size - 1) {

                builder.append(markertypes.get(i).toString() + ", ")
            }
        }

        descriptionTextview.text = builder



        button.setOnClickListener {
            // open link with custom tabs
            CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(url), WebviewFallback())
        }
    }

}