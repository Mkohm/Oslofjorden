package com.oslofjorden.oslofjordenturguide.MapView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class ChooseMapInfoDialog extends DialogFragment {
    String[] mapInfo;
    boolean[] defaultChecked;

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    public static String TAG = "TAG";
    


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        createMapInfoArray();
        createDefaultCheckedArray();

        final boolean[] uselist = getUsersCheckListFromSharedPref();



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle("Velg kartinformasjon")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(mapInfo, uselist,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    Log.d(TAG, "onClick: " + which + isChecked);
                                    uselist[which] = isChecked;

                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("TAG", "onClick: ok");

                        //Save the users checks
                        saveArray(uselist, "userChecks", getActivity().getApplicationContext());
                        Log.d(TAG, "onCreateDialog: lagret array");

                        //Pass something to mapsactivity
                        mListener.onDialogPositiveClick(ChooseMapInfoDialog.this, uselist);

                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("TAG", "onClick: Avbrøt");
                        mListener.onDialogNegativeClick(ChooseMapInfoDialog.this);

                    }
                });



        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    private boolean[] getUsersCheckListFromSharedPref() {
        final boolean[] uselist;
        if (loadArray("userChecks", getActivity().getApplicationContext()).length == 0){
            Log.d("TAG", "onCreateDialog: null");
            //Ingenting er blitt lagret
            uselist = defaultChecked;
        } else {
            uselist = loadArray("userChecks", getActivity().getApplicationContext());
            Log.d(TAG, "onCreateDialog: fant liste" + uselist[2]);
        }
        return uselist;
    }

    private void createDefaultCheckedArray() {
        defaultChecked = new boolean[17];
        defaultChecked[0] = true;
        defaultChecked[1] = true;
        defaultChecked[2] = true;
        defaultChecked[3] = true;
        defaultChecked[4] = true;
        defaultChecked[5] = true;
        defaultChecked[6] = true;

        defaultChecked[7] = false;
        defaultChecked[8] = false;
        defaultChecked[9] = false;
        defaultChecked[10] = false;
        defaultChecked[11] = false;
        defaultChecked[12] = false;
        defaultChecked[13] = false;
        defaultChecked[14] = false;
        defaultChecked[15] = false;
        defaultChecked[16] = false;
    }

    private void createMapInfoArray() {
        mapInfo = new String[17];
        mapInfo[0] = "Kyststier";
        mapInfo[1] = "Badeplasser";
        mapInfo[2] = "Spisesteder";
        mapInfo[3] = "Butikker";
        mapInfo[4] = "Parkering og Transport";
        mapInfo[5] = "Interessante steder";
        mapInfo[6] = "Fiskeplasser";

        mapInfo[7] = "Gjestehavn";
        mapInfo[8] = "Uthavner";
        mapInfo[9] = "Bunkers - Steder å fylle bensin";
        mapInfo[10] = "Marinaer";



        mapInfo[11] = "Båtramper";
        mapInfo[12] = "Kran/Truck";



        mapInfo[13] = "Toaletter";
        mapInfo[14] = "Fyr";
        mapInfo[15] = "Båtbutikker";
        mapInfo[16] = "Campingplasser";

    }


    public boolean saveArray(boolean[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_17", array.length);

        for(int i=0;i<array.length;i++) {
            editor.putBoolean(arrayName + "_" + i, array[i]);
        }
        return editor.commit();
    }

    public boolean[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);

        int size = prefs.getInt(arrayName + "_17", 0);
        boolean[] array = new boolean[size];
        for(int i=0;i<size;i++) {
            Log.d(TAG, "loadArray: " + i + " checked: " + prefs.getBoolean(arrayName + "_" + i, false));
            array[i] = prefs.getBoolean(arrayName + "_" + i, false);
        }
        return array;
    }




}
