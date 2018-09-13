package com.oslofjorden.binaryfilegenerator

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.Serializable


class BinaryGeneratorFromKml {

    fun parseKMLAndOutputLists(): Array<java.util.ArrayList<out Serializable?>> {

        val names = ArrayList<String>()
        val descriptions = ArrayList<String>()
        val links = ArrayList<String?>()
        val coordinatesList = ArrayList<ArrayList<DoubleArray>>()


        val path = System.getProperty("user.dir")
        val reader = BufferedReader(InputStreamReader(File(path + "/binaryFileGenerator/src/main/res/mapData" + "/turer_oslofjorden.kml").inputStream()))
        val text = reader.readText()

        val doc = Jsoup.parse(text)

        val element = doc.select("Placemark")

        for (placemark in element) {
            val title = placemark.select("name").text()
            val description = placemark.select("description").text()
            val color = getColor(placemark)
            val coordinates = getCoordinates(placemark)
            println(coordinates)
            println()
        }


        return arrayOf(names, descriptions, links, coordinatesList)
    }

    fun getColor(placemark: Element): String? {
        val style = placemark.select("Style").toString()
        val regex = """<color>(.{8})</color>""".toRegex()
        return regex.find(style)?.groupValues?.get(1)

    }

    fun getCoordinates(placemark: Element): List<List<Double>> {
        val coordinateString = placemark.select("LineString").select("coordinates").text()

        val result = ArrayList<ArrayList<Double>>()


        val coordinatePairs = coordinateString.split(" ")

        for (coordinatePair in coordinatePairs) {

            val coordinates = coordinatePair.split(",")

            val latitude = coordinates[0].toDouble()
            val longitude = coordinates[1].toDouble()

            val finalList = ArrayList<Double>()
            finalList.add(latitude)
            finalList.add(longitude)

            result.add(finalList)
        }


        return result
    }

}

fun main(args: Array<String>) {
    val generator = BinaryGeneratorFromKml()
    generator.parseKMLAndOutputLists()
}