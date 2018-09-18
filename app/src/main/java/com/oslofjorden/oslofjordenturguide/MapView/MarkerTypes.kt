package com.oslofjorden.oslofjordenturguide.MapView

enum class MarkerTypes(val value: String) {
    PATHS("Kyststier"),
    BEACH("Badestrand"),
    RESTAURANT("Restaurant"),
    STORE("Butikk"),
    PARKING_TRANSPORT("Parkering"),
    POINT_OF_INTEREST("Interessante steder"),
    FISHING_SPOT("Fiskeplass"),
    GUEST_HARBOR("Gjestehavn"),
    OUT_HARBOR("Uthavn"),
    PETROL_STATION("Sted å fylle " + "bensin"),
    MARINA("Marina"),
    RAMP("Båtrampe"),
    CRANE("Kran/Truck"),
    TOILETT("Toalett"),
    LIGHTHOUSE("Fyr"),
    BOAT_STORE("Båtbutikk"),
    CAMPING("Campingplass");

    companion object {
        @JvmStatic
        fun getTypeFromIndex(index: Int): MarkerTypes {
            return when (index) {
                0 -> PATHS
                1 -> BEACH
                2 -> RESTAURANT
                3 -> STORE
                4 -> PARKING_TRANSPORT
                5 -> POINT_OF_INTEREST
                6 -> FISHING_SPOT
                7 -> GUEST_HARBOR
                8 -> OUT_HARBOR
                9 -> PETROL_STATION
                10 -> MARINA
                11 -> RAMP
                12 -> CRANE
                13 -> TOILETT
                14 -> LIGHTHOUSE
                15 -> BOAT_STORE
                16 -> CAMPING
                else -> {
                    throw IllegalArgumentException("Not legal index")
                }
            }
        }
    }

    override fun toString(): String {
        return value
    }

}