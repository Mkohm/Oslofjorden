package com.oslofjorden.usecase.browseMap

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.oslofjorden.R
import com.oslofjorden.data.SharedPreferencesReader
import com.oslofjorden.data.SharedPreferencesRepository
import com.oslofjorden.model.Marker
import com.oslofjorden.model.MarkerTypes
import com.oslofjorden.model.OslofjordenPolyline

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

        view.findViewById<TextView>(R.id.title).visibility = View.GONE
        view.findViewById<TextView>(R.id.description).visibility = View.GONE
        view.findViewById<TextView>(R.id.url).visibility = View.GONE
        view.findViewById<AppCompatCheckBox>(R.id.checkBox).visibility = View.GONE
    }

    fun finishLoadingAndShowPrivacyPolicy() {
        hideLoadingText()
        showPrivacyPolicyIfNotShownToUser()
    }

    private fun showPrivacyPolicyIfNotShownToUser() {
        val privacyPolicyShouldBeShown = !SharedPreferencesRepository(SharedPreferencesReader(activity)).getPrivacyPolicyShown()
        if (privacyPolicyShouldBeShown) {
            showAndHandlePrivacyPolicy()
        } else {
            animateDown()
        }
    }

    private fun showAndHandlePrivacyPolicy() {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        showTitleAndDescription(
                view.findViewById(R.id.title),
                activity.getString(R.string.privacy_policy),
                view.findViewById(R.id.description),
                activity.getString(R.string.privacy_policy_description)
        )

        view.findViewById<Button>(R.id.url).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://mkohm.github.io")))
            }
        }

        view.findViewById<AppCompatCheckBox>(R.id.checkBox).apply {
            visibility = View.VISIBLE

            // Write the current set value
            SharedPreferencesRepository(SharedPreferencesReader(activity)).setPrivacyPolicyShown(isChecked)

            // Write on update to new value
            setOnCheckedChangeListener { _, isChecked ->
                SharedPreferencesRepository(SharedPreferencesReader(activity)).setPrivacyPolicyShown(
                        isChecked
                )
            }
        }
    }

    private fun hideLoadingText() {
        view.findViewById<TextView>(R.id.loading_text).visibility = View.GONE
    }

    private fun animateDown() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun setPolylineContent(polyline: Polyline) {
        // Get the google maps polyline and convert the tag object into our own OslofjordenPolyline object
        val ourPolylineType = (polyline.tag as OslofjordenPolyline)

        showTitleAndDescription(
                view.findViewById(R.id.title), ourPolylineType.title, view.findViewById(R.id.description), ourPolylineType.description
        )

        val button = view.findViewById<Button>(R.id.url)
        val url = ourPolylineType.url
        // Do not show the button if there is no link
        setButtonVisibility(button, url)

        button.setOnClickListener {
            // open link with custom tabs
            openCustomTab(url)
        }
    }

    private fun showTitleAndDescription(titleTextview: TextView, title: String, descriptionTextview: TextView, description: String) {
        titleTextview.apply {
            text = title
            visibility = View.VISIBLE
        }

        descriptionTextview.apply {
            text = description
            visibility = View.VISIBLE
        }
    }

    private fun openCustomTab(url: String?) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }

    fun setMarkerContent(item: Marker) {
        val button = view.findViewById<Button>(R.id.url)
        val url = item.link
        setButtonVisibility(button, url)

        showTitleAndDescription(view.findViewById(R.id.title), item.title, view.findViewById(R.id.description), buildDescription(item.markerTypes))

        button.setOnClickListener {
            // open link with custom tabs
            openCustomTab(url)
        }
    }

    private fun setButtonVisibility(button: Button, url: String?) {
        button.visibility = if (url == null) View.GONE else View.VISIBLE
    }

    private fun buildDescription(markertypes: List<MarkerTypes>): String {
        val builder = StringBuilder()
        for (i in 0 until markertypes.size) {
            if (i == markertypes.size - 1) {
                builder.append(markertypes[i].toString())
            } else {
                builder.append(markertypes[i].toString() + ", ")
            }
        }
        return builder.toString()
    }
}