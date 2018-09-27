package com.oslofjorden.oslofjordenturguide.MapView.data

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.SelectPolylineColor
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
                val url = objectInputStream.readObject() as String?
                val binaryCoordinates = objectInputStream.readObject() as ArrayList<DoubleArray>
                val coordinates = convertToLatLngObjects(binaryCoordinates)
                val color = SelectPolylineColor.setPolylineColor(description)

                polylines.add(Polyline(PolylineOptions().addAll(coordinates).clickable(true).color(color), name, description, url))

            } catch (e: EOFException) {
                objectInputStream.close()
                return PolylineData(polylines)
            }
        }

    }

    private fun convertToLatLngObjects(binaryCoordinates: ArrayList<DoubleArray>): ArrayList<LatLng> {
        val coordinates = ArrayList<LatLng>()
        for (i in 0 until binaryCoordinates.size) {
            coordinates.add(LatLng(binaryCoordinates[i][0], binaryCoordinates[i][1]))
        }
        return coordinates
    }
}