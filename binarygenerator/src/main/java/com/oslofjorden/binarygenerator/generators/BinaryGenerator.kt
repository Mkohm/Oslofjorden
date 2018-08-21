package com.oslofjorden.binarygenerator.generators


import org.json.JSONArray
import org.json.JSONObject
import java.io.*


class BinaryGenerator {

    /**
     * Reads the geo-json files and creates lists of items ready for binary file creation
     */
    fun readFromJsonfilesCreateLists(): Array<java.util.ArrayList<out Serializable?>> {

        val names = ArrayList<String>()
        val descriptions = ArrayList<String>()
        val links = ArrayList<String?>()
        val coordinatesList = ArrayList<ArrayList<DoubleArray>>()


        val path = System.getProperty("user.dir")

        File(path + "/binarygenerator/src/main/java/com/oslofjorden/binarygenerator/mapData/").walk().forEach {

            if (it.isFile) {

                val reader = BufferedReader(InputStreamReader(it.inputStream()))


                while (true) {

                    val line = reader.readLine() ?: break
                    if (!line.contains("coordinates")) {
                        continue
                    }


                    val jsonObject = createJsonObject(line)

                    val propertiesObject = jsonObject?.getJSONObject("properties")
                    val name = propertiesObject?.get("Name") as String
                    var description = propertiesObject.optString("description", "Vi har desverre " +
                            "ingen beskrivelse av dette stedet")


                    val geometryObject = jsonObject.getJSONObject("geometry")
                    val jsonCoordinates = geometryObject.getJSONArray("coordinates")
                    val coordinates = getCoordinates(jsonCoordinates)

                    names.add(name)


                    val url = extractUrl(description)
                    links.add(url)

                    description = extractDescription(description)
                    descriptions.add(description)

                    coordinatesList.add(coordinates)

                }
            }
        }

        return arrayOf(names, descriptions, links, coordinatesList)
    }

    private fun extractUrl(description: String): String? {

        try {
            // todo: This is a little simple but will work good enough
            return "http://" + description.substring(description.indexOf("www"), description.indexOf
            (".html") + 5) + "?app=1"
        } catch (e: Exception) {
            return null
        }
    }

    private fun extractDescription(description: String): String {
        return try {
            description.substring(0, description.indexOf("<a ")).trim()
        } catch (e: Exception) {
            description.trim()
        }
    }

    private fun getCoordinates(jsonCoordinates: JSONArray): ArrayList<DoubleArray> {
        val coordinates = ArrayList<DoubleArray>()
        for (j in 0 until jsonCoordinates.length()) {

            var coord = jsonCoordinates.get(j).toString()
            val lng = coord.substring(1, coord.indexOf(",")).toDouble()

            val lat = coord.substring(coord.indexOf(",")+1, coord.length-1).toDouble()

            val latLng = doubleArrayOf(lat, lng)
            coordinates.add(latLng)
        }
        return coordinates
    }

    //Writes objects to the file, two objects at each iteration, so when reading it can be checked if the thread is cancelled
    fun writeBinaryFile(data: Array<java.util.ArrayList<out Serializable>>) {

        val fileout = FileOutputStream("polylines_binary_file.bin")
        val out = ObjectOutputStream(fileout)

        for (i in 0 until data[0].size) {
            out.writeObject(data[0].get(i))
            out.writeObject(data[1].get(i))
            out.writeObject(data[2].get(i))
            out.writeObject(data[3].get(i))
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
    val data = generator.readFromJsonfilesCreateLists()

    generator.writeBinaryFile(data)
}