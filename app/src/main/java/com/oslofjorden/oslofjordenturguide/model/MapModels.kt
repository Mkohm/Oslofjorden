package com.oslofjorden.oslofjordenturguide.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.ClusterItem

sealed class MergedData

data class PolylineData(val polylines: HashMap<List<LatLng>, Polyline>) : MergedData()
data class MarkerData(val markers: List<Marker>) : MergedData()

data class Polyline(val options: PolylineOptions, val title: String, val description: String, val url: String?)

data class Marker(val markerOptions: MarkerOptions, val link: String?, val markerTypes: List<MarkerTypes>) : ClusterItem {
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