package com.oslofjorden.oslofjordenturguide.MapView.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.clustering.ClusterItem
import com.oslofjorden.oslofjordenturguide.MapView.MarkerTypes

sealed class MergedData

data class PolylineData(val polylines: List<Polyline>) : MergedData()
data class MarkerData(val markers: List<Marker>) : MergedData()

data class Polyline(val options: PolylineOptions?, val title: String, val description: String, val url: String?)

data class Marker(val markerOptions: MarkerOptions, val link: String?, val markerTypes: List<MarkerTypes>, val icon: BitmapDescriptor?) : ClusterItem {
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