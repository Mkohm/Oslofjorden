package com.oslofjorden.usecase.browseMap

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oslofjorden.R
import com.oslofjorden.model.Marker
import com.oslofjorden.model.MarkerTypes

class BottomSheetController(private val view: LinearLayout, private val activity: MapsActivity) {
    private val behavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(view)

    fun expandBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setLoadingText() {
        val titleTextview = view.findViewById<TextView>(R.id.loading_text)
        titleTextview.text = activity.getString(R.string.bottom_sheet_loading_data)

        view.findViewById<TextView>(R.id.title).apply {
            text = activity.getString(R.string.privacy_policy)
            visibility = View.VISIBLE
        }

        // Enable the views that was disabled during loading
        view.findViewById<TextView>(R.id.description).apply {
            visibility = View.VISIBLE
            text = activity.getString(R.string.privacy_policy_description)
        }

        view.findViewById<Button>(R.id.url).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mkohm.github.io")))
            }
        }
    }

    fun finishLoading() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        view.findViewById<TextView>(R.id.loading_text).visibility = View.GONE
    }

    fun setPolylineContent(polyline: Polyline) {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        val descriptionTextview = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.url)

        // Get the google maps polyline and convert the tag object into our own Polyline object
        val ourPolylineType = (polyline.tag as com.oslofjorden.model.Polyline)

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