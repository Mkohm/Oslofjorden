package com.oslofjorden.oslofjordenturguide.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.model.Marker
import com.oslofjorden.oslofjordenturguide.model.MarkerData
import com.oslofjorden.oslofjordenturguide.model.MarkerTypes
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import java.io.InputStreamReader
import java.io.Reader

class MarkerReaderFromGPX(private val context: Context) : MarkerDAO {
    override fun readMarkers(markers: MutableLiveData<MarkerData>) {
        doAsync {

            val result = read(context)

            uiThread {

                // Update the livedata with the loaded markers
                markers.postValue(result)
            }
        }
    }

    private fun read(context: Context): MarkerData {
        val markerList = ArrayList<Marker>()

        val inputStream = context.resources.openRawResource(R.raw.oslofjorden_db_22_november_2018)
        val reader = InputStreamReader(inputStream) as Reader

        val text = reader.readText()

        val doc = Jsoup.parse(text)
        val waypoints = doc.getElementsByTag("wpt")

        for (waypoint in waypoints) {
            val name = waypoint.getElementsByTag("name")[0].text()
            val latitude = waypoint.attr("lat").toDouble()
            val longitude = waypoint.attr("lon").toDouble()

            val link = try {
                waypoint.getElementsByTag("link")[0].attr("href")
            } catch (e: IndexOutOfBoundsException) {
                null
            }

            val categories = waypoint.getElementsByTag("gpxx:Category")
            val markerTypes = arrayListOf<MarkerTypes>()
            for (category in categories) {
                when (category.text()) {
                    "Badeplass" -> markerTypes.add(MarkerTypes.BEACH)
                    "Butikk" -> markerTypes.add(MarkerTypes.STORE)
                    "Spisested" -> markerTypes.add(MarkerTypes.RESTAURANT)
                    "Parkering transp" -> markerTypes.add(MarkerTypes.PARKING_TRANSPORT)
                    "Rampe" -> markerTypes.add(MarkerTypes.RAMP)
                    "Gjestehavn" -> markerTypes.add(MarkerTypes.GUEST_HARBOR)
                    "Uthavn" -> markerTypes.add(MarkerTypes.OUT_HARBOR)
                    "WC" -> markerTypes.add(MarkerTypes.TOILETT)
                    "Point of Interes" -> markerTypes.add(MarkerTypes.BEACH)
                    "Fiskeplass" -> markerTypes.add(MarkerTypes.FISHING_SPOT)
                    "Campingplass" -> markerTypes.add(MarkerTypes.CAMPING)
                    "Bunkers" -> markerTypes.add(MarkerTypes.PETROL_STATION)
                    "Fyr" -> markerTypes.add(MarkerTypes.LIGHTHOUSE)
                    "Kran/Truck" -> markerTypes.add(MarkerTypes.CRANE)
                    "Båtbutikk" -> markerTypes.add(MarkerTypes.BOAT_STORE)
                    "Marina" -> markerTypes.add(MarkerTypes.MARINA)
                }
            }

            val marker = createMarker(latitude, longitude, name, link, markerTypes)

            markerList.add(marker)
        }

        return MarkerData(markerList)
    }

    private fun createMarker(
        latitude: Double,
        longitude: Double,
        name: String,
        link: String?,
        markerTypes: ArrayList<MarkerTypes>
    ): Marker {
        val markerOption = MarkerOptions()
        markerOption.position(LatLng(latitude, longitude))
        markerOption.title(name)

        return Marker(markerOption, link, markerTypes)
    }
}
