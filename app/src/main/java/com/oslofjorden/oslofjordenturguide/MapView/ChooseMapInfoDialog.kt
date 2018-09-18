package com.oslofjorden.oslofjordenturguide.MapView

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log

class ChooseMapInfoDialog : DialogFragment() {

    // Use this instance of the interface to deliver action events

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mListener: NoticeDialogListener = activity as NoticeDialogListener

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
