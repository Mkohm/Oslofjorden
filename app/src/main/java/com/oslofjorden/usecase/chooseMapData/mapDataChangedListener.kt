package com.oslofjorden.usecase.chooseMapData

interface mapDataChangedListener {
    fun onDialogPositiveClick(newMapItems: BooleanArray)
    fun onDialogNegativeClick()
}
