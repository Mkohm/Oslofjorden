package com.oslofjorden.binaryGenerator

import org.jsoup.Jsoup
import java.io.File
import java.io.InputStreamReader

fun main() {
    MarkerReaderGPX.readMarkers()
}

object MarkerReaderGPX {

    fun readMarkers() {
        val path = System.getProperty("user.dir")

        val reader = InputStreamReader(File(path + "/binaryGenerator/src/main/res/mapData/gpxpoints.gpx").inputStream())

        val text = reader.readText()

        val doc = Jsoup.parse(text)
        val trkpt = doc.getElementsByTag("trkpt")
        println(trkpt)
    }
}