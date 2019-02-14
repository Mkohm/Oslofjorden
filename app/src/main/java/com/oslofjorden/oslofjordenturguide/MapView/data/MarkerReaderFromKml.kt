package com.oslofjorden.oslofjordenturguide.MapView.data

import androidx.lifecycle.MutableLiveData
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.MarkerTypes
import com.oslofjorden.oslofjordenturguide.MapView.model.Marker
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.io.Reader

class MarkerReaderFromKml(val context: Context) : MarkerDAO {
    override fun readMarkers(markers: MutableLiveData<MarkerData>) {
        doAsync {

            val result = read(context)

            uiThread {

                // Update the livedata with the loaded markers
                markers.postValue(result)
            }
        }
    }

    fun read(context: Context): MarkerData {
        val markerList = ArrayList<Marker>()

        val inputStream = context.resources.openRawResource(R.raw.doc)
        val reader = InputStreamReader(inputStream) as Reader

        val text = reader.readText()

        val doc = Jsoup.parse(text)
        val placemarks = doc.getElementsByTag("Placemark")

        for (placemark in placemarks) {
            val names = placemark.getElementsByTag("name")[0].text()

            var descriptions: String? = null
            if (placemark.getElementsByTag("description").size != 0) {
                descriptions = placemark.getElementsByTag("description")[0].text()
            }

            val coordinates = placemark.getElementsByTag("coordinates")[0].text()
            val longitude = coordinates.split(",")[0].toDouble()
            val latitude = coordinates.split(",")[1].toDouble()

            val markerOption = MarkerOptions()
            markerOption.position(LatLng(latitude, longitude))
            markerOption.title(names)

            val marker = Marker(markerOption, descriptions, listOf(MarkerTypes.BEACH))

            markerList.add(marker)
        }

        return MarkerData(markerList)
    }
}
