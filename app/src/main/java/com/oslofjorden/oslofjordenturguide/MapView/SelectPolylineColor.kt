package com.oslofjorden.oslofjordenturguide.MapView

import android.graphics.Color

object SelectPolylineColor {

    private fun isSykkelvei(description: String): Boolean {
        return description.toLowerCase().contains("Sykkel")
    }

    private fun isFerge(description: String): Boolean {
        return (description.toLowerCase().contains("Ferge")) && !description.contains("fergeleie")
    }

    private fun isVanskeligKyststi(description: String): Boolean {
        return description.toLowerCase().contains("Vanskelig")
    }

    private fun isKyststi(description: String): Boolean {
        return description.toLowerCase().contains("kyststi")
    }

    fun setPolylineColor(description: String): Int {
        return when {
            isKyststi(description) -> Color.BLUE
            isSykkelvei(description) -> Color.GREEN
            isFerge(description) -> Color.parseColor("#980009")
            isVanskeligKyststi(description) -> Color.RED

        // Det er en tursti
            else -> Color.GREEN
        }
    }

}