package com.oslofjorden.oslofjordenturguide.MapView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class WelcomeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Applikasjonen vil vise deg veien på kyststiene rundt Oslofjorden " +
                "hvis" + " du skrur på GPS.\n\n- Trykk på kyststier og markører for å få opp mer "
                + "info\n- " + "Du kan velge hva som skal vises på kartet med knappen øverst til "
                + "høyre\n- Du kan" + " skru av sporing med knappen til venstre\n- Mer " +
                "funksjonalitet er på vei.\n\nGod" + " tur!").setTitle("Oslofjorden Turguide").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing, it disappears
            }
        });


        // Create the AlertDialog object and return it
        return builder.create();
    }
}