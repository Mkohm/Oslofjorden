package com.oslofjorden.oslofjordenturguide.MapView

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import java.io.EOFException
import java.io.ObjectInputStream

class BinarydataReader(val context: Context, val task: AsyncTask<Void?, Void?, Void?>) {

    companion object {
        val TAG = "TAG"
    }

    fun readBinaryData(resource: Int): ArrayList<PolylineData> {

        val polylineData = ArrayList<PolylineData>()

        val inputStream = context.resources.openRawResource(resource)
        val objectInputStream = ObjectInputStream(inputStream)


        // todo: get the number of objects
        while (true) {
            if (task.isCancelled) {
                Log.d(TAG, "getDataFromFileAndPutInDatastructure: stopp")
            }


            try {
                val name = objectInputStream.readObject() as String
                val description = objectInputStream.readObject() as String
                val url = objectInputStream.readObject() as String?
                val binaryCoordinates = objectInputStream.readObject() as ArrayList<DoubleArray>


                val coordinates = convertToLatLngObjects(binaryCoordinates)


                val color = SelectPolylineColor.setPolylineColor(description)

                polylineData.add(PolylineData(PolylineOptions().addAll(coordinates).clickable(true).color(color), name, description, url))

            } catch (e: EOFException) {
                objectInputStream.close()
                return polylineData
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