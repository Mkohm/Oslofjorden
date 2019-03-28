package com.oslofjorden.oslofjordenturguide.usecase.chooseMapData

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.oslofjorden.oslofjordenturguide.model.MarkerTypes

class ChooseMapInfoDialog : DialogFragment() {

    // Use this instance of the interface to deliver action events

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mListener: mapDataChangedListener = activity as mapDataChangedListener

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
