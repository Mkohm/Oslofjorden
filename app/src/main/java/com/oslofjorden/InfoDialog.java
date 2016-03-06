package com.oslofjorden;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class InfoDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Hei! \n\nOslofjorden anbefaler bruk av gps ved bruk av appen, slik at den kan følge deg under turen din.\n\n- Trykk på kyststier og markører for å få opp mer info.\n- Du kan skru av sporing med knappen øverst til høyre. \n- Mer funksjnalitet er på vei, stay tuned.\n\nGod tur!").setTitle("Oslofjorden");



        // Create the AlertDialog object and return it
        return builder.create();
    }
}