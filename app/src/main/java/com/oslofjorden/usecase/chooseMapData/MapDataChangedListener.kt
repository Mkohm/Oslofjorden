package com.oslofjorden.usecase.chooseMapData

interface MapDataChangedListener {
    fun onDialogPositiveClick(newMapItems: BooleanArray)
    fun onDialogNegativeClick()
}
