package com.oslofjorden.oslofjordenturguide.MapView

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem

class MarkerData(val markerOptions: MarkerOptions, val link: String?, val markerTypes: List<MarkerTypes>, val icon: BitmapDescriptor?) : ClusterItem {
    override fun getTitle(): String {
        return markerOptions.title
    }

    override fun getPosition(): LatLng {
        return markerOptions.position
    }


    override fun getSnippet(): String {
        return ""
    }

}