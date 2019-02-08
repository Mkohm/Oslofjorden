package com.oslofjorden.binaryfilegenerator

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.ObjectOutputStream
import java.io.Serializable

// To run this first build the project separately and then run the file
fun main(args: Array<String>) {
    val generator = BinaryGeneratorFromKml()
    val data = generator.parseKMLAndOutputLists()
    generator.writeBinaryFile(data)
}

class BinaryGeneratorFromKml {

    fun parseKMLAndOutputLists(): Array<java.util.ArrayList<out Serializable?>> {

        val names = ArrayList<String>()
        val descriptions = ArrayList<String>()
        val colors = ArrayList<String>()
        val links = ArrayList<String?>()

        val coordinatesList = ArrayList<ArrayList<Pair<Double, Double>>>()

        val path = System.getProperty("user.dir")
        val reader =
            BufferedReader(InputStreamReader(File(path + "/binaryFileGenerator/src/main/res/mapData" + "/turer_oslofjorden.kml").inputStream()))
        val text = reader.readText()

        val doc = Jsoup.parse(text)

        val element = doc.select("Placemark")

        for (placemark in element) {
            val title = placemark.select("name").text()
            names.add(title)

            var description = placemark.select("description").text()

            val link = getLink(description)
            links.add(link)

            if (description.contains("<")) {
                description = description.substring(0, description.indexOf("<"))
            }

            descriptions.add(description)

            val color = getColor(placemark)
            colors.add(color ?: "ffffffff")

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

    private fun getLink(description: String): String {
        val regex = """<a href="(.*)">""".toRegex()
        return regex.find(description)?.groupValues?.get(1) ?: "null"
    }

    fun getColor(placemark: Element): String? {
        val style = placemark.select("Style").toString()
        val regex = """<color>(.{8})</color>""".toRegex()
        return regex.find(style)?.groupValues?.get(1)
    }

    fun getCoordinates(placemark: Element): ArrayList<Pair<Double, Double>> {
        val coordinateString = placemark.select("LineString").select("coordinates").text()

        val result = ArrayList<Pair<Double, Double>>()

        val coordinatePairs = coordinateString.split(" ")

        for (coordinatePair in coordinatePairs) {

            val coordinates = coordinatePair.split(",")

            val latitude = coordinates[0].toDouble()
            val longitude = coordinates[1].toDouble()

            val pair = Pair(latitude, longitude)

            result.add(pair)
        }

        return result
    }

    // Writes objects to the file, two objects at each iteration, so when reading it can be checked if the thread is cancelled
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