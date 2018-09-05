package com.oslofjorden.oslofjordenturguide.MapView;

import android.support.v4.app.DialogFragment;

/* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
public interface NoticeDialogListener {
    void onDialogPositiveClick(DialogFragment dialog, boolean[] checkedItems);

    void onDialogNegativeClick(DialogFragment dialog);

}
