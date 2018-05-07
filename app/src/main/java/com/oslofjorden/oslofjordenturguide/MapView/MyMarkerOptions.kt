package com.oslofjorden.oslofjordenturguide.MapView

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MyMarkerOptions(val markerTitle: String, val markerPosition: LatLng, val icon: BitmapDescriptor) : ClusterItem {
    override fun getTitle(): String {
        return markerTitle
    }

    override fun getPosition(): LatLng {
        return markerPosition
    }


    override fun getSnippet(): String {
        return ""
    }

}