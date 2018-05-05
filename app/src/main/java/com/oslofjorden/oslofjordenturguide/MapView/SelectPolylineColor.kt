package com.oslofjorden.oslofjordenturguide.MapView

import android.graphics.Color

object SelectPolylineColor {

    private fun isSykkelvei(description: String): Boolean {
        return description.contains("Sykkel") || description.contains("sykkel")
    }

    private fun isFerge(description: String): Boolean {
        return (description.contains("Ferge") || description.contains("ferge")) && !description.contains("fergeleie")
    }

    private fun isVanskeligKyststi(description: String): Boolean {
        return description.contains("Vanskelig") || description.contains("vanskelig")
    }


    fun setPolylineColor(description: String): Int {
        return when {
            isSykkelvei(description) -> Color.GREEN
            isFerge(description) -> Color.parseColor("#980009")
            isVanskeligKyststi(description) -> Color.RED
            else -> Color.BLUE
        }
    }

}