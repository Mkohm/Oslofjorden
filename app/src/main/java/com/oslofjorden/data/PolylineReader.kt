package com.oslofjorden.data

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.oslofjorden.R
import com.oslofjorden.model.OslofjordenPolyline
import com.oslofjorden.model.PolylineData
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.EOFException
import java.io.ObjectInputStream

class PolylineReader(val context: Context) : PolylineDAO {
    override fun readPolylines(liveData: MutableLiveData<PolylineData>) {
        doAsync {

            val result = readBinaryData(R.raw.polylines_binary_file)

            uiThread {
                liveData.postValue(result)
            }
        }
    }

    fun readBinaryData(resource: Int): PolylineData {
        val polylines = HashMap<List<LatLng>, OslofjordenPolyline>()

        val inputStream = context.resources.openRawResource(resource)
        val objectInputStream = ObjectInputStream(inputStream)

        while (true) {

            try {
                val name = objectInputStream.readObject() as String
                val description = objectInputStream.readObject() as String
                var url = getUrl(objectInputStream)
                val color = objectInputStream.readObject() as String
                val binaryCoordinates = objectInputStream.readObject() as ArrayList<Pair<Double, Double>>
                val coordinates = convertToLatLngObjects(binaryCoordinates)

                polylines.put(coordinates, OslofjordenPolyline(PolylineOptions().addAll(coordinates).clickable(true).color(Color.parseColor(color)), name, description, url))
            } catch (e: EOFException) {
                objectInputStream.close()
                break
            }
        }

        return PolylineData(polylines)
    }

    private fun getUrl(objectInputStream: ObjectInputStream): String? {
        var url = objectInputStream.readObject() as String?

        // If there is no url, the url in the objectinputstream is "null".
        if (url == "null") {
            url = null
            // If there is an actual url we add the parameter to the url so that we will display
            // the website without the map.
        } else {
            url += "?app=1"
        }
        return url
    }

    private fun convertToLatLngObjects(binaryCoordinates: ArrayList<Pair<Double, Double>>): ArrayList<LatLng> {
        val coordinates = ArrayList<LatLng>()
        for (i in 0 until binaryCoordinates.size) {
            coordinates.add(LatLng(binaryCoordinates[i].first, binaryCoordinates[i].second))
        }
        return coordinates
    }
}