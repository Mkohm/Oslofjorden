package com.oslofjorden.usecase.chooseMapData

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.oslofjorden.model.MarkerTypes

class ChooseMapInfoDialog : DialogFragment() {

    // Use this instance of the interface to deliver action events

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mListener: MapDataChangedListener = activity as MapDataChangedListener

        val arguments = arguments

        val userChecks = arguments?.getBooleanArray("userChecks")
        val builder = AlertDialog.Builder(context!!)

        userChecks?.let {
            val options = MarkerTypes.values().map { it.value }.toTypedArray()

            builder.setTitle("Velg kartinformasjon").setMultiChoiceItems(options, userChecks) { dialog, which, isChecked ->
                if (isChecked) {
                    Log.d("TAG", "onClick: $which$isChecked")
                    userChecks[which] = isChecked
                }
            }.setPositiveButton("Ok") { dialog, id ->
                mListener.onDialogPositiveClick(userChecks)
                Log.i("TAG", "onClick: ok")
            }.setNegativeButton("Avbryt") { dialog, id ->
                mListener.onDialogNegativeClick()
                Log.i("TAG", "onClick: Avbrøt")
            }

            return builder.create()
        }

        return builder.create()
    }
}
