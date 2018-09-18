package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.MarkerTypes
import com.oslofjorden.oslofjordenturguide.MapView.model.Marker
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader


class MarkerReader(val context: Context) : MarkerDataAccessObject {
    override fun readMarkers(markers: MutableLiveData<MarkerData>) {
        doAsync {

            val result = read(context)

            uiThread {

                // Update the livedata with the loaded markers
                markers.postValue(result)

            }
        }
    }


    fun setMarkerPosition(line: String): LatLng {
        val indexOfStartCoordinate = line.indexOf("\"coordinates\": [ ") + 17
        val indexOfEndCoordinate = line.indexOf(" ] } }")
        val coordinates = line.substring(indexOfStartCoordinate, indexOfEndCoordinate)

        val longitude = java.lang.Double.valueOf(coordinates.substring(0, coordinates.indexOf(",")))
        val latitude = java.lang.Double.valueOf(coordinates.substring(coordinates.indexOf(",") + 1))


        return LatLng(latitude, longitude)
    }

    fun read(context: Context): MarkerData {
        val markerData = ArrayList<Marker>()

        val inputStream = context.resources.openRawResource(R.raw.points)
        val reader = BufferedReader(InputStreamReader(inputStream) as Reader)

        while (true) {
            val line = reader.readLine() ?: break
            if (!line.contains("gpxx_WaypointExtension")) {
                continue
            }

            val obj = createJsonObject(line)
            val propertiesObject = obj?.getJSONObject("properties")

            val name = propertiesObject?.getString("name")
            val markerTypes = propertiesObject?.getString("gpxx_WaypointExtension")

            var link = propertiesObject?.getString("link1_href")
            if (link == "null") {
                link = null
            }

            // Only add this suffix if the link is not null
            link = link?.let { it + "?app=1" }

            val markerTypesList = Regex("<gpxx:Category>.+?</gpxx:Category>").findAll(markerTypes!!).toList().map {
                when (it.value) {
                    "<gpxx:Category>Badeplass</gpxx:Category>" -> MarkerTypes.BEACH
                    "<gpxx:Category>Butikk</gpxx:Category>" -> MarkerTypes.STORE
                    "<gpxx:Category>Spisested</gpxx:Category>" -> MarkerTypes.RESTAURANT
                    "<gpxx:Category>Parkering transp</gpxx:Category>" -> MarkerTypes.PARKING_TRANSPORT
                    "<gpxx:Category>Rampe</gpxx:Category>" -> MarkerTypes.RAMP
                    "<gpxx:Category>Gjestehavn</gpxx:Category>" -> MarkerTypes.GUEST_HARBOR
                    "<gpxx:Category>Uthavn</gpxx:Category>" -> MarkerTypes.OUT_HARBOR
                    "<gpxx:Category>WC</gpxx:Category>" -> MarkerTypes.TOILETT
                    "<gpxx:Category>Point of Interes</gpxx:Category>" -> MarkerTypes.BEACH
                    "<gpxx:Category>Fiskeplass</gpxx:Category>" -> MarkerTypes.FISHING_SPOT
                    "<gpxx:Category>Campingplass</gpxx:Category>" -> MarkerTypes.CAMPING
                    "<gpxx:Category>Bunkers</gpxx:Category>" -> MarkerTypes.PETROL_STATION
                    "<gpxx:Category>Fyr</gpxx:Category>" -> MarkerTypes.LIGHTHOUSE
                    "<gpxx:Category>Kran/Truck</gpxx:Category>" -> MarkerTypes.CRANE
                    "<gpxx:Category>Båtbutikk</gpxx:Category>" -> MarkerTypes.BOAT_STORE
                    "<gpxx:Category>Marina</gpxx:Category>" -> MarkerTypes.MARINA
                    else -> {
                        "SHOULD NOT HAPPEN"
                    }
                }
            }

            val position = setMarkerPosition(line)

            val markerOption = MarkerOptions()
            markerOption.title(name)
            markerOption.position(position)

            val marker = Marker(markerOption, link, markerTypesList as List<MarkerTypes>, null)
            markerData.add(marker)


        }

        return MarkerData(markerData)
    }

    fun createJsonObject(line: String): JSONObject? {
        var obj: JSONObject? = null
        //For all the lines ending with ","
        if (line.matches(".{0,},".toRegex())) {
            obj = JSONObject(line.substring(0, line.length - 1))
            //The line does not end with ","
        } else if (line.matches(".{0,}[^,]".toRegex())) {
            obj = JSONObject(line)
        }
        return obj
    }

}
