package com.oslofjorden.binarygenerator


import org.json.JSONObject
import java.io.*
import java.util.*

class BinaryGenerator {

    fun readFromJsonfilesAndPutInBinaryMaps(): List<CustomPolyline> {

        val polylines = ArrayList<CustomPolyline>()

        val path = System.getProperty("user.dir")


        //  for (i in xmlfile.indices) {
        val inputStream = File(path + "/binarygenerator/src/main/java/com/oslofjorden/binarygenerator/mapData/k3.json").inputStream()

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

        //    }

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


fun main(args: Array<String>) {

    val generator = BinaryGenerator()
    val polylines = generator.readFromJsonfilesAndPutInBinaryMaps()

    //writeBinaryFile(polylines)
}