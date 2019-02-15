package com.oslofjorden.oslofjordenturguide.usecase.chooseMapData

interface mapDataChangedListener {
    fun onDialogPositiveClick(newMapItems: BooleanArray)
    fun onDialogNegativeClick()
}
