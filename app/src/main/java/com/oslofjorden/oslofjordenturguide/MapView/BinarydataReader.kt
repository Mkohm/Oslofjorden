package com.oslofjorden.oslofjordenturguide.MapView

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.ObjectInputStream

class BinarydataReader(val context: Context, val task: AsyncTask<Void, Int, Void>) {

    companion object {
        val TAG = "TAG"
    }

    fun readBinaryData(resource: Int): Map<List<DoubleArray>, Array<String>>? {

        var binaryPolylinesMap = HashMap<List<DoubleArray>, Array<String>>()

        val inputStream = context.resources.openRawResource(resource)
        val objectInputStream = ObjectInputStream(inputStream)

        var counter = 0

        // todo: get the number of objects
        while (counter < 400) {
            if (task.isCancelled) {
                Log.d(TAG, "getDataFromFileAndPutInDatastructure: stopp")
                return null
            }

            val polyline = objectInputStream.readObject() as SerializablePolyline


            val coord = objectInputStream.readObject() as List<DoubleArray>
            val info = objectInputStream.readObject() as Array<String>

            binaryPolylinesMap.put(coord, info)

            counter++
        }

        objectInputStream.close()


        return binaryPolylinesMap
    }
}