package com.oslofjorden.binarygenerator.generators

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
        val reader = BufferedReader(InputStreamReader(File(path + "/binarygenerator/src/main/java/com/oslofjorden/binarygenerator/mapData/turer_oslofjorden.kml").inputStream()))
        val text = reader.readText()

        val doc = Jsoup.parse(text)

        val element = doc.select("Placemark")


        for (placemark in element) {
            val title = placemark.select("name").text()
            val description = placemark.select("description").text()
            val color = getColor(placemark)
            val coordinates = getCoordinates(placemark)
            println()
        }


        return arrayOf(names, descriptions, links, coordinatesList)
    }

    fun getColor(placemark: Element): String? {
        val style = placemark.select("Style").toString()
        val regex = """<color>(.{8})</color>""".toRegex()
        return regex.find(style)?.groupValues?.get(1)

    }

    fun getCoordinates(placemark: Element) {
        val coordinateString = placemark.select("LineString").select("coordinates").text()
        val coordinatePairs =  coordinateString.split(" ")
        println(coordinatePairs)

        //coordinatePairs.fold()

       // coordinatePairs.reduceRight {
         //return it.split(",")



     //   }

        val regex = """(\d*\.\d*),(\d*\.\d*)""".toRegex()
        val result = regex.find(coordinateString)?.groupValues

        return
    }

}

fun main(args: Array<String>) {
    val generator = BinaryGeneratorFromKml()
    generator.parseKMLAndOutputLists()
}