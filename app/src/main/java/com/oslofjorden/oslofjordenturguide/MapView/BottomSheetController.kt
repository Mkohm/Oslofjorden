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


class BottomSheetController(val view: LinearLayout, val activity: MapsActivity) {
    private val behavior: BottomSheetBehavior<LinearLayout> = BottomSheetBehavior.from(view)

    private val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder()
            .addDefaultShareMenuItem()
            .setToolbarColor(activity.resources.getColor(R.color.colorPrimary))
            .setShowTitle(true)
            .build()


    fun expandBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setLoadingText() {
        val titleTextview = view.findViewById<TextView>(R.id.title)
        titleTextview.text = "Oslofjorden laster inn - vennligst vent.."

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

        val title = (polyline.tag as PolylineData).title
        val description = (polyline.tag as PolylineData).description
        val url = (polyline.tag as PolylineData).url

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

    fun setMarkerContent(item: MarkerData) {
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