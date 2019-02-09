package com.oslofjorden.binaryGenerator

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.ObjectOutputStream
import java.io.Serializable

class BinaryPolylineGenerator {
    fun parseKMLAndOutputLists(): Array<java.util.ArrayList<out Serializable?>> {

        val names = ArrayList<String>()
        val descriptions = ArrayList<String>()
        val colors = ArrayList<String>()
        val links = ArrayList<String?>()

        val coordinatesList = ArrayList<ArrayList<Pair<Double, Double>>>()

        val path = System.getProperty("user.dir")
        val reader = BufferedReader(InputStreamReader(File("$path/binaryGenerator/src/main/res/mapData/doc.kml").inputStream()))

        val text = reader.readText()
        val doc = Jsoup.parse(text)

        val polylineElements = doc.getElementsByTag("Placemark")

        for (placemark in polylineElements) {

            // First find the styleUrlId of the placemark element
            val color = getColor(placemark, doc)

            val title = placemark.select("name").text()
            names.add(title)

            var description = placemark.select("description").text()

            val link = getLink(description)
            links.add(link)

            if (description.contains("<")) {
                description = description.substring(0, description.indexOf("<"))
            }

            descriptions.add(description)

            colors.add(color)

            val coordinates = getCoordinates(placemark)
            coordinatesList.add(coordinates)
        }

        println("All these should be the same size or something will fail")
        println(names.size)
        println(descriptions.size)
        println(links.size)
        println(colors.size)
        println(coordinatesList.size)

        return arrayOf(names, descriptions, links, colors, coordinatesList)
    }

    private fun getColor(placemark: Element, doc: Document): String {
        val styleUrlId = placemark.getElementsByTag("styleUrl")[0].text().substring(1)

        // The corresponding stylemap with this id is
        val styleMap = doc.getElementsByAttributeValue("id", styleUrlId)

        // This style contains two StyleUrl's one for when the polyline is highlighted and one for when the polyline is not clicked. We want to select the one where it is not clicked.
        val styleNotClicked =
            styleMap.get(0).getElementsByTag("Pair")[0].getElementsByTag("styleUrl").text().substring(1)

        val style = doc.getElementsByAttributeValue("id", styleNotClicked)

        val styleText = style[0].toString()

        val regex = """<color>(.{8})</color>""".toRegex()

        val color = regex.find(styleText)?.groupValues?.get(1)

        if (color == null) {
            return "blue"
        } else {


            val rgbColor = convertFromAABBGGRRToRRGGBB(color)

            return "#$rgbColor"
        }
    }

    private fun convertFromAABBGGRRToRRGGBB(color: String): String {
        val blue = color.substring(2, 4)
        val green = color.substring(4, 6)
        val red = color.substring(6, 8)

        return "$red$green$blue"
    }

    private fun getLink(description: String): String {
        val regex = """<a href="(.*)">""".toRegex()
        return regex.find(description)?.groupValues?.get(1) ?: "null"
    }

    fun getColor(placemark: Element): String {
        val style = placemark.toString()

        val regex = """<color>(.{8})</color>""".toRegex()
        val colorFromString = regex.find(style)?.groupValues?.get(1)


        return if (colorFromString == null) "blue" else "#$colorFromString"
    }

    fun getCoordinates(placemark: Element): ArrayList<Pair<Double, Double>> {
        val coordinateString = placemark.select("LineString").select("coordinates").text()

        val result = ArrayList<Pair<Double, Double>>()

        val coordinatePairs = coordinateString.split(" ")

        for (coordinatePair in coordinatePairs) {

            val coordinates = coordinatePair.split(",")

            val longitude = coordinates[0].toDouble()
            val latitude = coordinates[1].toDouble()

            val pair = Pair(latitude, longitude)


            result.add(pair)
        }


        return result
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
            out.writeObject(data[4].get(i))
        }


        out.close()
    }
}