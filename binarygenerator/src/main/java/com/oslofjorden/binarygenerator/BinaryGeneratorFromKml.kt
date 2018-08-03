package com.oslofjorden.binarygenerator
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.Serializable


class BinaryGeneratorFromKml {

    fun readFromJsonfilesCreateLists(): Array<java.util.ArrayList<out Serializable?>> {

        val names = ArrayList<String>()
        val descriptions = ArrayList<String>()
        val links = ArrayList<String?>()
        val coordinatesList = ArrayList<ArrayList<DoubleArray>>()


        val path = System.getProperty("user.dir")
        val reader = BufferedReader(InputStreamReader(File(path + "/binarygenerator/src/main/java/com/oslofjorden/binarygenerator/mapData/turer_oslofjorden.kml").inputStream()))
        val text = reader.readText()

        val doc = Jsoup.parse(text)

        val element = doc.select("Placemark").select("description").first()
        println(element.text())





        return arrayOf(names, descriptions, links, coordinatesList)
    }

}

fun main(args: Array<String>) {
    val generator = BinaryGeneratorFromKml()
    generator.readFromJsonfilesCreateLists()
}