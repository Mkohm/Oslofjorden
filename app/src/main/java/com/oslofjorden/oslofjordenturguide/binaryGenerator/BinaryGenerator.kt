package com.oslofjorden.oslofjordenturguide.binaryGenerator

import com.oslofjorden.oslofjordenturguide.R
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.util.*

class BinaryGenerator {

    private fun readFromJsonfilesAndPutInBinaryMaps(): List<CustomPolyline> {

        val polylines = ArrayList<CustomPolyline>()

        val xmlfile = intArrayOf(R.raw.k1, R.raw.k2, R.raw.k3, R.raw.k4, R.raw.k5, R.raw.k6, R.raw.k7, R.raw.k8, R.raw.k9, R.raw.k10, R.raw.k11, R.raw.k12, R.raw.k13, R.raw.k14, R.raw.k15, R.raw.k16, R.raw.k17, R.raw.k18, R.raw.k19, R.raw.k20, R.raw.k21, R.raw.k21, R.raw.k22, R.raw.k23, R.raw.k24, R.raw.k25, R.raw.k26, R.raw.k27, R.raw.k28, R.raw.k29, R.raw.k30, R.raw.k31, R.raw.k32, R.raw.k33, R.raw.k34, R.raw.k35, R.raw.k36, R.raw.k37, R.raw.k38, R.raw.k39, R.raw.k40, R.raw.k41, R.raw.k42, R.raw.k43, R.raw.k44, R.raw.k45, R.raw.k46, R.raw.k47, R.raw.k48, R.raw.k49, R.raw.k50, R.raw.k51)

        for (i in xmlfile.indices) {
            val inputStream = File("test.txt").inputStream()


            val reader = BufferedReader(InputStreamReader(inputStream))

            while (true) {

                val line = reader.readLine() ?: break
                if (!line.contains("coordinates")) {
                    continue
                }

                val obj = createJsonObject(line)
                val properties = obj?.getString("properties")
                val obj2 = JSONObject(properties)
                val geometry = obj?.getString("geometry")
                val obj3 = JSONObject(geometry)


                val name = obj2.getString("Name")
                val description = obj2.getString("description")
                val jsonCoordinates = obj3.getJSONArray("coordinates")

                val coordinates = ArrayList<DoubleArray>()

                for (j in 0 until jsonCoordinates.length()) {

                    var coord = jsonCoordinates.get(j).toString()
                    val lng = java.lang.Double.valueOf(coord.substring(1, coord.indexOf(",")))!!

                    coord = coord.substring(coord.indexOf(",") + 1, coord.length)
                    val lat = java.lang.Double.valueOf(coord.substring(0, coord.indexOf(",")))!!

                    val latLng = doubleArrayOf(lat, lng)
                    coordinates.add(latLng)
                }


                val polyline = CustomPolyline(name, description, coordinates)
                polylines.add(polyline)
            }

        }

        return polylines
    }

    //Writes objects to the file, two objects at each iteration, so when reading it can be checked if the thread is cancelled
    fun writeBinaryFile(polylines: List<CustomPolyline>) {


        val fileout = FileOutputStream("polylines_binary_file")
        val out = ObjectOutputStream(fileout)
        //out.writeObject(binarykyststiInfoMap);

        for (polyline in polylines) {
            out.writeObject(polyline);
        }


        out.close()
    }

    @Throws(JSONException::class)
    private fun createJsonObject(line: String): JSONObject? {
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


fun main(args: Array<String>) {

    print("test")
    //  val polylines = readFromJsonfilesAndPutInBinaryMaps()

    //writeBinaryFile(polylines)
}