package com.oslofjorden.oslofjordenturguide.MapView.data

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.model.Polyline
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData
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
        val polylines = ArrayList<Polyline>()

        val inputStream = context.resources.openRawResource(resource)
        val objectInputStream = ObjectInputStream(inputStream)

        while (true) {

            try {
                val name = objectInputStream.readObject() as String
                val description = objectInputStream.readObject() as String
                val url = objectInputStream.readObject() as String
                val color = objectInputStream.readObject() as String
                val binaryCoordinates = objectInputStream.readObject() as ArrayList<Pair<Double, Double>>
                val coordinates = convertToLatLngObjects(binaryCoordinates)

                polylines.add(Polyline(PolylineOptions().addAll(coordinates).clickable(true).color(Color.parseColor(color)), name, description, url))
            } catch (e: EOFException) {
                objectInputStream.close()
                break
            }
        }

        return PolylineData(polylines)
    }

    private fun convertToLatLngObjects(binaryCoordinates: ArrayList<Pair<Double, Double>>): ArrayList<LatLng> {
        val coordinates = ArrayList<LatLng>()
        for (i in 0 until binaryCoordinates.size) {
            coordinates.add(LatLng(binaryCoordinates[i].first, binaryCoordinates[i].second))
        }
        return coordinates
    }
}