package com.oslofjorden.oslofjordenturguide.MapView

import android.location.Location

interface LocationChangedListener {
    fun locationChanged(location: Location)
}