package com.oslofjorden.oslofjordenturguide.MapView;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.oslofjorden.oslofjordenturguide.R;
import com.oslofjorden.oslofjordenturguide.WebView.CustomTabActivityHelper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu
//Rapporter feil/tur/hvasomhelst
//instillinger oppdateringshastighet ++
//I nærheten
//Database implementation and search
//lagrer ikke


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, ResultCallback, CustomTabActivityHelper.ConnectionCallback, NoticeDialogListener {
    public static String TAG = "TAG";
    private static boolean stopAddingPolylines = false;

    protected AddInfoToMap addInfoToMap;

    private static GoogleMap mMap;


    float currentZoom = 13;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;
    LatLng currentMapClickPosition;

    MarkerData clickedClusterItem;
    public ClusterManager<MarkerData> mClusterManager;


    private boolean infobarUp = false;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera onPause/onResume
    private boolean foundLastLocation = false;


    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;

    //is locationupdates enabled or not
    boolean locationUpdatesSwitch = true;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    private Polyline previousPolylineClicked;

    // Request code to use when launching the resolution activity
    public static final int REQUEST_RESOLVE_ERROR = 1001;

    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    public static final String STATE_RESOLVING_ERROR = "resolving_error";
    public static final String DIALOG_ERROR = "dialog_error";


    private final int PERMISSIONS_OK = 1;


    private CustomTabActivityHelper customTabActivityHelper;

    private static ArrayList<Polyline> polylinesOnMap = new ArrayList<>();
    private HashMap<LatLng, MarkerData> markersOnMap = new HashMap();

    private boolean[] checkedItems;
    private boolean[] defaultChecked;

    private ImageButton layerButton;

    private boolean haslocationPermission;
    private boolean locationEnabled;
    private List<PolylineData> polylineData;
    private List<MarkerData> markerData;
    BottomSheetController bottomSheetController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i(TAG, "onCreate: oncreate");
        createDefaultCheckedArray();


        //The first time the user launches the app, this message will be shown
        showInfomessageToUserIfFirstTime();

        //Set up custom tabs
        setUpCustomTabs();

        locationEnabled = isGPSEnabled(getApplicationContext());


        //Removes the oslofjorden picture
        getWindow().setBackgroundDrawableResource(R.drawable.graybackground);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Something with running google play services safaly
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        setUpToolbar();

        // init bottomsheet controller
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetController = new BottomSheetController(linearLayout, this);


        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getApplicationContext(), R.color.white),
                PorterDuff.Mode.SRC_IN);

        layerButton = (ImageButton) findViewById(R.id.layers);
        layerButton.setClickable(false);
        layerButton.setEnabled(false);
        layerButton.setImageResource(R.drawable.ic_layers_gray);


        layerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: klikk på knapp");

                //Show the choose map info dialog
                ChooseMapInfoDialog mapInfoDialog = new ChooseMapInfoDialog();
                mapInfoDialog.show(getSupportFragmentManager(), "test");

            }
        });

    }

    public boolean isGPSEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    private void loadCheckedItems(boolean[] checkedList) {
        markersOnMap.clear();

        for (int i = 0; i < checkedList.length; i++) {
            //Load items if the checkbox was checked
            if (checkedList[i] == true) {

                //Kyststier behandles spesielt
                if (i == 0) {

                    //Bare legg til om det ikke var der fra før
                    if (polylinesOnMap.size() == 0) {
                        Log.d(TAG, "loadCheckedItems: laster inn kyststier ");
                        addPolylines();
                    }
                } else {
                    MarkerTypes type = MarkerTypes.getTypeFromIndex(i);
                    addMarkersToMap(type);
                }

            } else {
                if (i == 0) {
                    if (polylinesOnMap.size() != 0) {
                        removePolylines();
                    }
                }
            }
        }

        //Show them by moving the map a bit
        CameraUpdate c = CameraUpdateFactory.zoomBy(0.001f);
        mMap.animateCamera(c);
    }

    private void removePolylines() {
        for (Polyline line : polylinesOnMap) {
            line.remove();
        }
    }

    private void addMarkersToMap(MarkerTypes markerType) {
        List<MarkerData> toAdd = markerData.stream().filter((marker) -> marker.getMarkerTypes().contains(markerType)).collect(Collectors.toList());
        mClusterManager.addItems(toAdd);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerData>() {
            @Override
            public boolean onClusterItemClick(MarkerData item) {
                item.getMarkerOptions().visible(false);

                bottomSheetController.expandBottomSheet();
                bottomSheetController.setMarkerContent(item);

                clickedClusterItem = item;

                //   setMarkerDescription(item.getTitle(), item., markerInfo);


                return false;

            }
        });
    }

    private void addPolylines() {
        //Adds the polylines to the map


        for (int i = 0; i < polylineData.size(); i++) {

            Polyline polyline = mMap.addPolyline(polylineData.get(i).getOptions());
            polyline.setTag(polylineData.get(i));
            polylinesOnMap.add(polyline);

        }
    }


    private void setUpCustomTabs() {
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
    }

    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Oslofjorden Båt - og Turguide");
        myToolbar.setTitleTextColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    @Override
    protected void onStart() {
        super.onStart();

        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCameraPosition));

        }

        addInfoToMap = new AddInfoToMap();
        addInfoToMap.execute();
    }


    @Override
    protected void onStop() {
        super.onStop();

        customTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            currentZoom = mMap.getCameraPosition().zoom;
            currentCameraPosition = mMap.getCameraPosition();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "onPause: Noe gikk galt under pause");


        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        if (polylineData != null) {
            loadLastStateOfApplication();
        }

        goToOsloLocation();

        //enable zoom buttons, and remove toolbar when clicking on markers
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);


        setUpClusterer();


        final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
        onOffLocationButton.setImageResource(R.drawable.ic_location_off);
        locationUpdatesSwitch = false;


        //  onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            /*@Override
            public void onClick(View v) {
                if (!haslocationPermission) {
                    checkPermission();

                }


                //Hvis location er enabled skal man kunne flicke swithchen akkurat sånn man vil
                if (locationEnabled) {

                    if (locationUpdatesSwitch == true) {

                        onOffLocationButton.setImageResource(R.drawable.ic_location_off);
                        Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
                        locationUpdatesSwitch = false;

                    } else if (locationUpdatesSwitch == false) {


                        onOffLocationButton.setImageResource(R.drawable.ic_location_on);
                        Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - på", Toast.LENGTH_SHORT).show();
                        locationUpdatesSwitch = true;

                    }


                }
                //Hvis location er på skal man få flicket switchen hvis det har skjedd en endring altså location er blitt skrudd på, dette må settingsrequest handle selv siden det er et callback som blir kallt
                else if (!locationEnabled) {
                    Log.d(TAG, "onClick: gps ikke på");
                    settingsrequest();

                }


                if (locationUpdatesSwitch == true) {
                    startLocationUpdates();
                } else {
                    // stopLocationUpdates();

                }

            }*/
        //   });


        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Set the color of the previous polyline back to what it was
                setOriginalPolylineColor();

                polyline.setColor(Color.BLACK);


                bottomSheetController.setContent(polyline);
                bottomSheetController.expandBottomSheet();


                previousPolylineClicked = polyline;

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                currentMapClickPosition = latLng;
                bottomSheetController.hideBottomSheet();
                setOriginalPolylineColor();
            }
        });
    }

    private void setOriginalPolylineColor() {
        // It will be null the first time a polyline is clicked
        if (previousPolylineClicked != null) {

            String description = ((PolylineData) previousPolylineClicked.getTag()).getDescription();
            previousPolylineClicked.setColor(SelectPolylineColor.INSTANCE.setPolylineColor(description));
        }
    }

    private void loadLastStateOfApplication() {
        if (loadArray("userChecks", getApplicationContext()).length == 0) {
            //Fant ingen checks på sharedpref
            Log.d(TAG, "loadLastStateOfApplication: fant ingen tidligere checks");
            checkedItems = defaultChecked;
        } else {
            checkedItems = loadArray("userChecks", getApplicationContext());
            Log.d(TAG, "loadLastStateOfApplication: fant checks");
        }
        loadCheckedItems(checkedItems);
        Log.d(TAG, "loadLastStateOfApplication: " + checkedItems[0]);
    }

    public boolean[] loadArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(arrayName, 0);

        int size = prefs.getInt(arrayName + "_17", 0);
        boolean[] array = new boolean[size];
        for (int i = 0; i < size; i++) {
            Log.d(TAG, "loadArray: " + i + " checked: " + prefs.getBoolean(arrayName + "_" + i, false));
            array[i] = prefs.getBoolean(arrayName + "_" + i, false);
        }
        return array;
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

    private void setGoogleMapUISettings() throws SecurityException {

        //enables location dot, and removes the standard google button


        //Depending on permissions UI will change
        if (haslocationPermission) {

            //Skru på location i googlemap ui
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


    }

    private void showInfomessageToUserIfFirstTime() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean firstTimeUserLaunchesApp = sharedPref.getBoolean("firstTimeUserLaunchesApp", true);

        if (firstTimeUserLaunchesApp) {
            //Saving that the user has opened the app before
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("firstTimeUserLaunchesApp", false);
            editor.commit();

            //Show message to the user
            InfoDialog infoDialog = new InfoDialog();
            infoDialog.show(getSupportFragmentManager(), "test");

        }
    }

    private int getDeviceMaxY() {
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        return mdispSize.y;
    }

    private void checkPermission() {
        Log.d(TAG, "checkPermission: sjekker for permission");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            //If we dont have permisson, request
            Log.d(TAG, "checkPermission: request permission");
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);


            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)

            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.


        } else {
            Log.d(TAG, "checkPermission: hadde permission fra før av ");

            haslocationPermission = true;
            setGoogleMapUISettings();


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) throws SecurityException {

        Log.d(TAG, "onRequestPermissionsResult: fikk svar om rettigheter");


        switch (requestCode) {
            case PERMISSIONS_OK: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Log.i(TAG, "onRequestPermissionsResult: Fikk tilgang kan nå skru på location");


                    CameraUpdate cameraUpdate2 = CameraUpdateFactory.newLatLngZoom(new LatLng(59.903079, 10.740479), 13);
                    mMap.moveCamera(cameraUpdate2);


                    haslocationPermission = true;
                    locationUpdatesSwitch = true;
                    final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
                    onOffLocationButton.setImageResource(R.drawable.ic_location_on);


                    setGoogleMapUISettings();


                } else {
                    Log.i(TAG, "onRequestPermissionsResult: Fikk ikke tilgang til location");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    haslocationPermission = false;
                    locationUpdatesSwitch = false;
                    final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
                    onOffLocationButton.setImageResource(R.drawable.ic_location_off);

                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }

        setGoogleMapUISettings();
    }


    private void setBottomsheetContents(Polyline polyline) {
        bottomSheetController.setContent(polyline);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //The result for request for turn on gps
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        locationEnabled = true;

                        ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
                        onOffLocationButton.setImageResource(R.drawable.ic_location_on);
                        locationUpdatesSwitch = true;


                        break;
                    case Activity.RESULT_CANCELED:
                        locationEnabled = false;
                        break;
                }
                break;
        }
    }

    private void goToOsloLocation() throws SecurityException {


        //Oslo sentrum
        LatLng lastLocation = new LatLng(59.903765, 10.699610);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 13);
        mMap.moveCamera(cameraUpdate);

    }

    private void setUpClusterer() {
        Log.d(TAG, "setUpClusterer: legger til markers");
        // Declare a variable for the cluster manager.


        // Position the map.

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)


        mClusterManager = new ClusterManager<MarkerData>(this, mMap);
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MarkerData>(new GridBasedAlgorithm<MarkerData>()));

        mClusterManager.setRenderer(new OwnIconRendered(getApplicationContext(), mMap, mClusterManager));

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);


    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    @Override
    public void onLocationChanged(Location location) {

        if (locationUpdatesSwitch) {
            //Updates current location
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom);
            mMap.animateCamera(cameraUpdate);
            Log.i(TAG, "OnLocationChanged: Location oppdatert");
        }


    }

    @Override
    public void onResult(@NonNull Result result) {

    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, boolean[] checkedItems) {
        Log.d(TAG, "onDialogPositiveClick: laster inn markerte items");

        this.checkedItems = checkedItems;

        loadCheckedItems(checkedItems);


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.d(TAG, "onDialogNegativeClick: gjør ingenting");
    }

    private void clearItems() {

        if (mMap != null) {
            mMap.clear();

            polylinesOnMap.clear();
            markersOnMap.clear();
        }

        if (mClusterManager != null) {

            mClusterManager.clearItems();

        }

    }

    private void setOnClusterItemClickListener() {

    }

    private void getDataFromFilesAndPutInDatastructure() throws IOException, JSONException {

        BinarydataReader binaryReader = new BinarydataReader(getApplicationContext(), addInfoToMap);
        polylineData = binaryReader.readBinaryData(R.raw.polylines_binary_file);


        MarkerReader markerReader = new MarkerReader(getApplicationContext(), addInfoToMap);
        markerData = markerReader.readMarkers();

        setOnClusterItemClickListener();

    }

    class AddInfoToMap extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {

            bottomSheetController.expandBottomSheet();
            bottomSheetController.setLoadingText();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                getDataFromFilesAndPutInDatastructure();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mMap != null) {
                loadLastStateOfApplication();
            }


            bottomSheetController.hideBottomSheet();


            layerButton.setClickable(true);
            layerButton.setEnabled(true);
            layerButton.setImageResource(R.drawable.ic_layers_white_24dp);
            findViewById(R.id.loading).setVisibility(View.GONE);

        }
    }
}
