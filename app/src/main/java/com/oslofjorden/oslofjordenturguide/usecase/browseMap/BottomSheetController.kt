package com.oslofjorden.oslofjordenturguide.usecase.browseMap

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.model.Polyline
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.model.MarkerTypes
import com.oslofjorden.oslofjordenturguide.model.Marker

class BottomSheetController(val view: LinearLayout, val activity: MapsActivity) {
    private val behavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(view)

    private val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().addDefaultShareMenuItem().setToolbarColor(activity.resources.getColor(R.color.colorPrimary)).setShowTitle(true).build()

    fun expandBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setLoadingText() {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        titleTextview.text = activity.getString(R.string.bottom_sheet_loading_data)

        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        descriptionTextview.visibility = View.GONE

        val button = view.findViewById<Button>(R.id.url)
        button.visibility = View.GONE
    }

    fun finishLoading() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Enable the views that was disabled during loading
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        descriptionTextview.visibility = View.VISIBLE

        val button = view.findViewById<Button>(R.id.url)
        button.visibility = View.VISIBLE
    }

    fun setPolylineContent(polyline: Polyline) {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.url)

        // Get the google maps polyline and convert the tag object into our own Polyline object
        val ourPolylineType = (polyline.tag as com.oslofjorden.oslofjordenturguide.model.Polyline)

        val title = ourPolylineType.title
        val description = ourPolylineType.description
        val url = ourPolylineType.url

        // Do not show the button if there is no link
        setVisibility(url, button)

        titleTextview.text = title
        descriptionTextview.text = description

        button.setOnClickListener {
            // open link with custom tabs
            openCustomTab(url)
        }
    }

    private fun openCustomTab(url: String?) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }

    fun setMarkerContent(item: Marker) {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.url)

        val title = item.title
        val markertypes = item.markerTypes
        val url = item.link

        setVisibility(url, button)

        titleTextview.text = title
        titleTextview.visibility = View.VISIBLE

        val description = buildDescription(markertypes)

        descriptionTextview.text = description

        button.setOnClickListener {
            // open link with custom tabs
            openCustomTab(url)
        }
    }

    private fun buildDescription(markertypes: List<MarkerTypes>): StringBuilder {
        val builder = StringBuilder()
        for (i in 0 until markertypes.size) {
            if (i == markertypes.size - 1) {
                builder.append(markertypes[i].toString())
            } else {

                builder.append(markertypes[i].toString() + ", ")
            }
        }
        return builder
    }

    private fun setVisibility(url: String?, button: Button) {
        if (url == null) {
            button.visibility = View.GONE
        } else {
            button.visibility = View.VISIBLE
        }
    }
}