package com.oslofjorden;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Set different markers on different types of items
//Let user choose what type of info to see
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu
//Rapporter feil/tur/hvasomhelst
//instillinger oppdateringshastighet ++
//I nærheten
//Database implementation and search
//lagrer ikke


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback, CustomTabActivityHelper.ConnectionCallback, NoticeDialogListener {
    //For debugging
    public static String TAG = "TAG";
    private static boolean stopAddingPolylines = false;

    AddInfoToMap addInfoToMap;


    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;


    float currentZoom;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;
    LatLng currentMapClickPosition;

    static boolean addedToDataStructure = false;

    MyMarkerOptions clickedClusterItem;
    public ClusterManager<MyMarkerOptions> mClusterManager;


    private boolean infobarUp = false;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera onPause/onResume
    private boolean foundLastLocation = false;


    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    boolean userHasAnsweredLocationTurnOn = false;
    boolean userAcceptLocation = false;
    //is locationupdates enabled or not
    boolean locationUpdatesSwitch = true;


    private Polyline currentPolyline;
    private String currentPolylineDescription;
    //Variables that the callback onconnectionfailed needs

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private final int PERMISSIONS_OK = 0;



    private CustomTabActivityHelper customTabActivityHelper;
    private boolean backGroundTaskRunning = false;

    private static ArrayList<MyMarkerOptions> beachMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> rampeMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> fiskeplassMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> kranTruckMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> bunkersMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> butikkMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> spisestedMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> uthavnMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> fyrMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> baatbutikkMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> marinaMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> gjestehavnMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> parkeringTranspMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> WCMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> pointOfInterestMarkers = new ArrayList<>();
    private ArrayList<MyMarkerOptions> campingplassMarkers = new ArrayList<>();
    private ArrayList<ArrayList<MyMarkerOptions>> arrayListOfArrayLists = new ArrayList<>();

    //is used for finding the name and description when clicking a polyline
    public static HashMap<List<LatLng>, String[]> kyststiInfoMap = new HashMap<List<LatLng>, String[]>();

    public static List<MarkerOptions> markersReadyToAdd = new ArrayList<MarkerOptions>();
    public static List<PolylineOptions> polylinesReadyToAdd = new ArrayList<PolylineOptions>();


    private static ArrayList<Polyline> polylinesOnMap;
    private HashMap<LatLng, MyMarkerOptions> markersOnMap = new HashMap();


    //coordinate pair, name, description, color
    private Map<List<double[]>, String[]> binaryPolylinesMap = new HashMap();
    private Map<List<double[]>, String[]> binarykyststiInfoMap = new HashMap<>();


    private boolean[] checkedItems;
    private boolean[] defaultChecked;

    private boolean exit;
    private boolean addingPolylines = false;


    private ImageButton layerButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: oncreate");

        addedToDataStructure = false;
        infobarUp = false;
        backGroundTaskRunning = false;
        exit = false;
        polylinesOnMap = new ArrayList<>();





        //Add arraylists to arraylist of arraylists //the first element is empty and never used
        addArrayListsToArraylistOfArrayLists();
        createDefaultCheckedArray();




        //Hvis man hadde location fra start av skal den ikke spørre mer
        if (isLocationEnabled(getApplicationContext())){
            userAcceptLocation = true;
        }

        //Set up custom tabs
        setUpCustomTabs();




        setContentView(R.layout.activity_maps);

        //Removes the oslofjorden picture
        getWindow().setBackgroundDrawableResource(R.drawable.graybackground);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Create a new instance of GoogleAPIClient
        createInstanceOfGoogleAPIClient();

        //Something with running google play services safaly
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        setUpToolbar();


        ProgressBar progressBar = (ProgressBar) findViewById(R.id.loading);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getApplicationContext(), R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN);

        layerButton = (ImageButton) findViewById(R.id.layers);
        layerButton.setClickable(false);
        layerButton.setEnabled(false);
        layerButton.setImageResource(R.drawable.ic_layers_gray);


        layerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Show the choose map info dialog
                ChooseMapInfoDialog mapInfoDialog = new ChooseMapInfoDialog();
                mapInfoDialog.show(getSupportFragmentManager(), "test");


            }
        });

    }


    private void addArrayListsToArraylistOfArrayLists() {
        arrayListOfArrayLists.add(null);
        arrayListOfArrayLists.add(beachMarkers);
        arrayListOfArrayLists.add(spisestedMarkers);
        arrayListOfArrayLists.add(butikkMarkers);
        arrayListOfArrayLists.add(parkeringTranspMarkers);
        arrayListOfArrayLists.add(pointOfInterestMarkers);
        arrayListOfArrayLists.add(fiskeplassMarkers);
        arrayListOfArrayLists.add(gjestehavnMarkers);
        arrayListOfArrayLists.add(uthavnMarkers);
        arrayListOfArrayLists.add(bunkersMarkers);
        arrayListOfArrayLists.add(marinaMarkers);
        arrayListOfArrayLists.add(rampeMarkers);
        arrayListOfArrayLists.add(kranTruckMarkers);
        arrayListOfArrayLists.add(WCMarkers);
        arrayListOfArrayLists.add(fyrMarkers);
        arrayListOfArrayLists.add(baatbutikkMarkers);
        arrayListOfArrayLists.add(campingplassMarkers);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    private void loadCheckedItems(boolean[] checkedList) {
        mClusterManager.clearItems();
        markersOnMap.clear();
        Log.d(TAG, "loadCheckedItems: " + polylinesOnMap.size());

        for (int i = 0; i < checkedList.length; i++){

            Log.d(TAG, "loadCheckedItems: "+ i + " " + checkedList[i]);

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
                        addMarkersToMap(arrayListOfArrayLists.get(i));
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

    private void addMarkersToMap(ArrayList<MyMarkerOptions> categoryArrayList) {
        //Add markers to the map
        for (int i = 0; i < categoryArrayList.size(); i++){
            //If the markersonmap add list contains the element, do not add it

            if (markersOnMap.containsKey(categoryArrayList.get(i).getPosition())){
                Log.d(TAG, "addMarkersToMap: var her fra før");
                continue;
            } else {

                //Find the highest priority icon, and then add it to the cluster manager
                //categoryArrayList.get(i).setIcon();

                mClusterManager.addItem(categoryArrayList.get(i));
                markersOnMap.put(categoryArrayList.get(i).getPosition(), categoryArrayList.get(i));

            }
        }
    }

    private void addPolylines() {
        //Adds the polylines to the map

        addingPolylines = true;

        final Iterator<PolylineOptions> iterator = polylinesReadyToAdd.iterator();
        try {

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {




                    if (iterator.hasNext()){
                        if (stopAddingPolylines){
                            Log.d(TAG, "run: stopper task");

                            stopAddingPolylines = false;
                            Log.d(TAG, "run: stopaddingpolylines" + stopAddingPolylines);
                            backGroundTaskRunning = false;

                            return;
                        }


                        //Log.d(TAG, "run: legger til kyststi");
                        polylinesOnMap.add(mMap.addPolyline(iterator.next()));

                        handler.postDelayed(this, 1);



                    } else {
                        Log.d(TAG, "run : alle kyststier er lastet inn");
                        stopAddingPolylines = false;
                        addingPolylines = false;

                        Log.d(TAG, "run: stopaddingpolylines" + stopAddingPolylines);

                        Log.d(TAG, "run: animerer ned");
                        animateInfobarDown();


                    }
                }
            }, 100);


        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Dette gikk dårlig, kyststier ble ikke lastet inn.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removePolylines() {
        Log.d(TAG, "removePolylines: fjerner kyststierh");
        if (addingPolylines) {
            stopAddingPolylines = true;
        }

        Log.d(TAG, "removePolylines: setter variabel stoppinnlasting til true");

        for (Polyline poly : polylinesOnMap){
            Log.d(TAG, "removePolylines: ");
            poly.remove();
        }
        polylinesOnMap.clear();

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

    private void createInstanceOfGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }


    @Override
    protected void onStart() {

        mGoogleApiClient.connect();

        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }


        super.onStart();

        customTabActivityHelper.bindCustomTabsService(this);



    }

    @Override
    protected void onResume() {

        super.onResume();


        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            if (locationUpdatesSwitch == true) {
                startLocationUpdates();
            }

        }

        if (currentCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCameraPosition));

        }



        if (!addedToDataStructure) {


            //Ikke lag ny
            if (addInfoToMap != null && (addInfoToMap.getStatus() == AsyncTask.Status.RUNNING)){
                Log.d(TAG, "onResume: gjør ingenting, fortsetter");
            } else {
                clearItems();
                addInfoToMap = new AddInfoToMap();
                addInfoToMap.execute();
                Log.d(TAG, "onResume: kjører igjen");
            }



        } else {
            Log.d(TAG, "onResume: Info var lastet inn");
        }



    }


    @Override
    protected void onStop() {
        super.onStop();


        Log.d(TAG, "onStop: ");

        if (! addedToDataStructure) {
            addInfoToMap.cancel(true);
        }



        try{
            stopLocationUpdates();
            mGoogleApiClient.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }




        customTabActivityHelper.unbindCustomTabsService(this);

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: onpause");

        try {
            currentZoom = mMap.getCameraPosition().zoom;
            currentCameraPosition = mMap.getCameraPosition();



            stopLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "onPause: Noe gikk galt under pause");
        }



        super.onPause();

    }


    protected void stopLocationUpdates() {
        //TODO:error when trying to stop location updates before mgoogleapiclient is connected
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
       // mMap.clear();
        if (setGoogleMapUISettings()) return;


        final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
        setOnOffLocationButtonStartstate(onOffLocationButton);


        onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationUpdatesSwitch == true) {
                    onOffLocationButton.setImageResource(R.drawable.ic_location_off);
                    Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
                    locationUpdatesSwitch = false;
                } else if (locationUpdatesSwitch == false) {


                    if (!userAcceptLocation || !isLocationEnabled(getApplicationContext())){

                        handleUsersWithoutLocationEnabled(mLocationRequest);

                    } else {
                        onOffLocationButton.setImageResource(R.drawable.ic_location_on);
                        Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - på", Toast.LENGTH_SHORT).show();
                        locationUpdatesSwitch = true;
                    }

                }

                if (locationUpdatesSwitch == true) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();

                }

            }
        });

        //The first time the user launches the app, this message will be shown
        showInfomessageToUserIfFirstTime();

        final TextView kyststiInfo = (TextView) findViewById(R.id.infobar);


        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {


                //This is setting the polyline to blue color if the last polyline did not have a description
                if (currentPolylineDescription == null && currentPolyline != null){
                    currentPolyline.setColor(Color.BLUE);
                }

                //Sets the color back to what it was after clicking somewhere else, this is only working if the last polyline clicked had a description
                setKyststiColorsBack();

                //If the polyline did not have a description, the blue color is used


                currentPolyline = polyline;
                currentPolylineDescription = kyststiInfoMap.get(polyline.getPoints())[0];


                setKyststiInfoFromDescriptionAndName(polyline, kyststiInfo);

                kyststiInfo.setVisibility(View.VISIBLE);

                int maxY = getDeviceMaxY();


                animateInfobarUp();



                currentPolyline.setColor(Color.BLACK);

            }
        });



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                Log.i(TAG, "onMapClick: Du klikket på kartet med " + polylinesOnMap.size() + " kyststier og, " + markersOnMap.size() + " markers.");

                Log.d(TAG, "onPostExecute: beachmarkers: " + beachMarkers.size());
                Log.d(TAG, "onPostExecute: rampemarkers " + rampeMarkers.size());
                Log.d(TAG, "onPostExecute: spisested: " + spisestedMarkers.size());
                Log.d(TAG, "onPostExecute: butikk " + butikkMarkers.size());
                Log.d(TAG, "onPostExecute: parkering: " + parkeringTranspMarkers.size());
                Log.d(TAG, "onPostExecute: interest " + pointOfInterestMarkers.size());
                Log.d(TAG, "onPostExecute: fisk: " + fiskeplassMarkers.size());
                Log.d(TAG, "onPostExecute: gjestehavn " + gjestehavnMarkers.size());
                Log.d(TAG, "onPostExecute: uthavn: " + uthavnMarkers.size());
                Log.d(TAG, "onPostExecute: bunkers " + bunkersMarkers.size());
                Log.d(TAG, "onPostExecute: marina: " + marinaMarkers.size());
                Log.d(TAG, "onPostExecute: kran " + kranTruckMarkers.size());
                Log.d(TAG, "onPostExecute: toalett: " + WCMarkers.size());
                Log.d(TAG, "onPostExecute: fyr " + fyrMarkers.size());
                Log.d(TAG, "onPostExecute: baatbutikk: " + baatbutikkMarkers.size());
                Log.d(TAG, "onPostExecute: camping " + campingplassMarkers.size());

                currentMapClickPosition = latLng;


                int maxY = getDeviceMaxY();


                //Hvis kyststiinfo er oppe, lukk den

                if (addedToDataStructure) {
                    animateInfobarDown();
                }






            }
        });


        setUpClusterer();



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
        for(int i=0;i<size;i++) {
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

    private void setOnOffLocationButtonStartstate(ImageButton onOffLocationButton) {
        //Sets the initial icon depending on current settings
        if (isLocationEnabled(getApplicationContext())){
            onOffLocationButton.setImageResource(R.drawable.ic_location_on);


        } else {
            onOffLocationButton.setImageResource(R.drawable.location_off_64px);
        }
    }

    private void setKyststiColor(PolylineOptions polylineOptions, String description) {
        if (description != null) {
            if (isSykkelvei(description)) {
                polylineOptions.color(Color.GREEN);
            } else if (isFerge(description)) {
                polylineOptions.color(Color.parseColor("#980009"));
            } else if (isVanskeligKyststi(description)) {
                polylineOptions.color(Color.RED);
            } else {
                polylineOptions.color(Color.BLUE);
            }
        }

        if (description == null){
            polylineOptions.color(Color.BLUE);
        }
    }

    private boolean isSykkelvei(String description) {
        return description.contains("Sykkel") || description.contains("sykkel");
    }

    private boolean isFerge(String description) {
        return (description.contains("Ferge") || description.contains("ferge")) && !description.contains("fergeleie");
    }

    private boolean isVanskeligKyststi(String description) {
        return description.contains("Vanskelig") || description.contains("vanskelig");
    }

    private boolean setGoogleMapUISettings() {
        //enable zoom buttons, and remove toolbar when clicking on markers
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //enables location dot, and removes the standard google button
        if (checkPermission()) return true;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        return false;
    }

    private void showInfomessageToUserIfFirstTime() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean firstTimeUserLaunchesApp = sharedPref.getBoolean("firstTimeUserLaunchesApp", true);

        if (firstTimeUserLaunchesApp){
            //Saving that the user has opened the app before
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("firstTimeUserLaunchesApp", false);
            editor.commit();

            //Show message to the user
            InfoDialog infoDialog = new InfoDialog();
            infoDialog.show(getSupportFragmentManager(), "test");

        }
    }

    private void setKyststiColorsBack() {
        if (currentPolyline != null && currentPolylineDescription != null) {

            if (isSykkelvei()) {
                currentPolyline.setColor(Color.GREEN);
            } else if (isFerge()) {
                currentPolyline.setColor(Color.parseColor("#980009"));
            } else if (isVanskeligKyststi()) {
                currentPolyline.setColor(Color.RED);
            }
            else {
                currentPolyline.setColor(Color.BLUE);
            }
        }
    }

    private boolean isVanskeligKyststi() {
        return currentPolylineDescription.contains("Vanskelig") || currentPolylineDescription.contains("vanskelig");
    }

    private boolean isFerge() {
        return currentPolylineDescription.contains("Ferge") || currentPolylineDescription.contains("ferge") && !currentPolylineDescription.contains("fergeleie");
    }

    private boolean isSykkelvei() {
        return currentPolylineDescription.contains("Sykkel") || currentPolylineDescription.contains("sykkel");
    }

    private void animateInfobarUp() {
        TextView infobar = (TextView) findViewById(R.id.infobar);


        if (infobarUp) {
            return;
        }
        infobar.setVisibility(View.VISIBLE);
        int maxY = getDeviceMaxY();
        Animation animation = new TranslateAnimation(0, 0, maxY - infobar.getY(), 0);
        animation.setDuration(500);

        infobar.startAnimation(animation);
        infobarUp = true;
    }

    private void animateInfobarDown() {
        TextView infobar = (TextView) findViewById(R.id.infobar);

        if (infobarUp){
            int maxY = getDeviceMaxY();
            Animation animation = new TranslateAnimation(0, 0, 0, maxY);
            animation.setDuration(500);
            infobar.startAnimation(animation);

            infobar.setVisibility(View.INVISIBLE);
            infobarUp = false;
        } else {
            return;
        }

    }

    private int getDeviceMaxY() {
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        return mdispSize.y;
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return true;


        }
        //TODO: return true?
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {



        switch (requestCode) {
            case PERMISSIONS_OK: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    Log.i(TAG, "onRequestPermissionsResult: Fikk tilgang kan nå skru på location");
                    //Skru på location


                } else {
                    Log.i(TAG, "onRequestPermissionsResult: Fikk ikke tilgang til location");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void setMarkerDescription(String title, String description, TextView txtMarkerDescription) {
        txtMarkerDescription.setMovementMethod(LinkMovementMethod.getInstance());

       String type = "Marker";
       String markerTitle =title;

        //Hvis den ikke er tom og er en url så skal link vises
        if (description != null) {
            String markerDescription = "";
            if (description.matches(".{0,}http[s]{0,1}:.{0,}")) {

                setTextViewHTML(txtMarkerDescription, description, type, title);

                //Det er ikke en link, men kanskje noe annet interessant
            } else {
                markerDescription = description;
                txtMarkerDescription.setText(markerTitle + "\n\n" + markerDescription);
            }
        } else {
            txtMarkerDescription.setText(markerTitle);
        }



    }

    private void setKyststiInfoFromDescriptionAndName(Polyline polyline, TextView kyststiInfo) {
        String description;
        String title;
        String type = "Polyline";

        try {
            title = kyststiInfoMap.get(polyline.getPoints())[1];
        } catch (Exception e) {
            title = null;

        }

        try {
            description = kyststiInfoMap.get(polyline.getPoints())[0];
        } catch (Exception e) {
            description = null;
        }


        //Fant ingenting, tekst settes til her var det ingenting.
        if (description == null && title == null) {
            kyststiInfo.setText("Her var det ingenting, gitt!");
        } else if (description == null && title != null) {
            //Setter navnet
            kyststiInfo.setText(title);
        } else if (description != null && title == null) {



            setTextViewHTML(kyststiInfo, description, type, title);


        } else if (description != null && title != null) {
            setTextViewHTML(kyststiInfo, description, type, title);
        }


    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        String uriWithDesc = extractUrlFromDescription(span);

        //May launch this link
        Log.i(TAG, "makeLinkClickable: Gjør link klar for å åpnes." + span.getURL());
        final Uri uri  = Uri.parse(uriWithDesc + "?app=1");
        customTabActivityHelper.mayLaunchUrl(uri, null, null);

        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...


                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark)).setShowTitle(true);


                intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back));
                intentBuilder.setStartAnimations(getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);
                intentBuilder.setExitAnimations(MapsActivity.this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);



                //CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabActivityHelper.getSession()).build();
                CustomTabActivityHelper.openCustomTab(MapsActivity.this, intentBuilder.build(), uri, new WebviewFallback());
            }
        };


        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    @NonNull
    private String extractUrlFromDescription(URLSpan span) {
        String uriWithDesc = span.getURL();
        uriWithDesc = uriWithDesc.substring(span.getURL().indexOf("http"),span.getURL().length());
        return uriWithDesc;
    }

    protected void setTextViewHTML(TextView text, String description, String type, String title) {


        if (type.equals("Marker")){
            setMarkerInfoText(text, description, title);
        }

        if (type.equals("Polyline")) {
            setPolylineInfoText(text, description);
        }


    }

    private void setPolylineInfoText(TextView text, String description) {
        CharSequence sequence = Html.fromHtml(description);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);

        String descriptionWithoutLink;
        try {


            descriptionWithoutLink = description.substring(0, description.indexOf("<a href"));
            SpannableStringBuilder withCustomLinkLayout = new SpannableStringBuilder("Tomt");
            URLSpan[] urls2 = null;



            //If there is a link in the description
            if (urls.length != 0){
                //Create the desired format of textview
                String link = "<a href=\"" + urls[0].getURL() + "\"><u>Mer info fra Oslofjorden.com</u></a>";
                CharSequence formattedText = Html.fromHtml(link);
                withCustomLinkLayout = new SpannableStringBuilder(formattedText);
                urls2 = withCustomLinkLayout.getSpans(0, formattedText.length(), URLSpan.class);
                withCustomLinkLayout.insert(0, descriptionWithoutLink);

                //If the link does not contain www return
                if (! urls[0].getURL().contains("www")){
                    text.setText(description);
                    return;
                }

            }


            for(URLSpan span : urls2) {
                makeLinkClickable(withCustomLinkLayout, span);
            }
            text.setMovementMethod(LinkMovementMethod.getInstance());

            text.setText(withCustomLinkLayout);


        } catch (Exception e){
            e.printStackTrace();
            //Det var ingen link her
           // descriptionWithoutLink = "Her var det desverre ingen link";
            text.setText(description);
        }
    }

    private void setMarkerInfoText(TextView text, String description, String title) {
        CharSequence sequence = Html.fromHtml(description);
        URLSpan[] urls = {new URLSpan(description)};
        Log.d(TAG, "setMarkerInfoText: " + description);

        //The first part of the description "her finner du: ... .. "
        String typesOfMarker = description.substring(0, description.indexOf("http"));
        SpannableStringBuilder withCustomLinkLayout = new SpannableStringBuilder("Tomt");
        URLSpan[] urls2 = null;

        //If there is a link in the description
        if (urls.length != 0){
            //Create the desired format of textview
            String link = "<a href=\"" + urls[0].getURL() + "\"><u>Mer info fra Oslofjorden.com</u></a>";
            CharSequence formattedText = Html.fromHtml(link);
            withCustomLinkLayout = new SpannableStringBuilder(formattedText);
            urls2 = withCustomLinkLayout.getSpans(0, formattedText.length(), URLSpan.class);
            withCustomLinkLayout.insert(0, title + " " + typesOfMarker + "\n\n");
        }


        for(URLSpan span : urls2) {
            makeLinkClickable(withCustomLinkLayout, span);
        }
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text.setText(withCustomLinkLayout);
        Log.d(TAG, "setMarkerInfoText: ");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }

        final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);

        if (resultCode == 0){

            //Sporing button should be off
            Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
            onOffLocationButton.setImageResource(R.drawable.ic_location_off);

            locationUpdatesSwitch = false;
            userAcceptLocation = false;
        } else {
            Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - på", Toast.LENGTH_SHORT).show();
            onOffLocationButton.setImageResource(R.drawable.ic_location_on);

            locationUpdatesSwitch = true;
            userAcceptLocation = true;
        }


    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Connected to google play services: This is where the magic happens

        //check for permissions to get location
        if (checkPermission()) return;
        //Enable marker for current location
        mMap.setMyLocationEnabled(true);

        //Will finde the last known location only the first time
        if (!foundLastLocation) {
            findAndGoToLastKnownLocation();
            foundLastLocation = true;
        }


        if (locationUpdatesSwitch) {
            startLocationUpdates();
        }


    }


    protected void startLocationUpdates() {
        if (checkPermission()) return;


        //Request locationupdates
        mLocationRequest = requestLocationUpdates();

        if (! userHasAnsweredLocationTurnOn){
            //Handle users without location enabled
            handleUsersWithoutLocationEnabled(mLocationRequest);

        }

    }

    private GoogleMap getMap() {
        return mMap;
    }

    @NonNull
    private LocationRequest requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
        return mLocationRequest;
    }

    private void handleUsersWithoutLocationEnabled(LocationRequest mLocationRequest) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        //Make user add location
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {

                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        userHasAnsweredLocationTurnOn = true;

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.

                        //No is answered
                        userHasAnsweredLocationTurnOn = true;
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this,0);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        userHasAnsweredLocationTurnOn = true;

                        //Oslo sentrum
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(59.903079, 10.740479));
                        mMap.moveCamera(cameraUpdate);

                        break;
                }

            }


        });
    }

    private void findAndGoToLastKnownLocation() {
        //Find the last known location
        if (checkPermission()) return;

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        LatLng lastLocation;
        if (mLastLocation != null) {
            lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        } else {
            //Oslo sentrum
            lastLocation = new LatLng(59.903765, 10.699610);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 13);
        mMap.moveCamera(cameraUpdate);

    }

    private void setUpClusterer() {
        Log.d(TAG, "setUpClusterer: legger til markers");
        // Declare a variable for the cluster manager.


        // Position the map.

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)


        mClusterManager = new ClusterManager<MyMarkerOptions>(this, getMap());
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MyMarkerOptions>(new GridBasedAlgorithm<MyMarkerOptions>()));

        mClusterManager.setRenderer(new OwnIconRendered(getApplicationContext(), getMap(), mClusterManager));

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);





    }



    private void addItems() {
        //Find out which items to add
        //Create a list of added markers

        mClusterManager.addItem(new MyMarkerOptions(new MarkerOptions().position(new LatLng(0, 0))));



        Log.d(TAG, "addItems: " + markersReadyToAdd.size());
        for (MarkerOptions marker : markersReadyToAdd) {
            if (stopAsyncTaskIfOnStop()) return;

            mClusterManager.addItem(new MyMarkerOptions(marker));


            markersOnMap.put(new MyMarkerOptions(marker).getPosition(), new MyMarkerOptions(marker));

        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

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


    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapsActivity) getActivity()).onDialogDismissed();
        }

    }


    class AddInfoToMap extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (stopAsyncTaskIfOnStop()) {
                return;
            }


            TextView loading = (TextView) findViewById(R.id.infobar);
            loading.setVisibility(View.VISIBLE);

            loading.setText("Laster inn data...\n\nEtter innlasting kan du velge hva som skal vises med knappen oppe til høyre.");

            animateInfobarUp();

        }

        @Override
        protected Void doInBackground(Void... params) {

            if (!addedToDataStructure) {

                if (stopAsyncTaskIfOnStop()) {
                    return null;
                }

                try {
                    getDataFromFileAndPutInDatastructure();
                    if (exit) {
                        Log.d(TAG, "doInBackground: exit");
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (stopAsyncTaskIfOnStop()) {
                    return null;
                }



                addedToDataStructure = true;
                backGroundTaskRunning = false;

            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            loadLastStateOfApplication();

            animateInfobarDown();

            layerButton.setClickable(true);
            layerButton.setEnabled(true);
            layerButton.setImageResource(R.drawable.ic_layers_white_24dp);
            findViewById(R.id.loading).setVisibility(View.GONE);

           // writeBinaryFile();












        }
    }

    private void writeBinaryFile() {
        Log.d(TAG, "onPostExecute: start" + System.currentTimeMillis());
        long time = System.currentTimeMillis();

        try {

            File myFile = new File("/sdcard/binarykyststiinfomap.bin");

            FileOutputStream fileout = new FileOutputStream(myFile);
            ObjectOutputStream out = new ObjectOutputStream(fileout);
            out.writeObject(binarykyststiInfoMap);
            out.close();


            Log.d(TAG, "onPostExecute: finito");


        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onPostExecute: slutt skriving " + (System.currentTimeMillis()-time));
    }

    private void readBinaryFile() {
        Log.d(TAG, "onPostExecute: start lesing binær");
        long time = System.currentTimeMillis();

        Log.d(TAG, "onPostExecute: start");
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.binarypolylinesmap);
            ObjectInputStream is = new ObjectInputStream(inputStream);

            binaryPolylinesMap = (Map<List<double[]>, String[]>) is.readObject();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onPostExecute: slutt lesing" + (System.currentTimeMillis()-time));



        Log.d(TAG, "onPostExecute: start lesing binær");
        long time2 = System.currentTimeMillis();

        Log.d(TAG, "onPostExecute: start");
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.binarykyststiinfomap);
            ObjectInputStream is = new ObjectInputStream(inputStream);

            binarykyststiInfoMap = (Map<List<double[]>, String[]>) is.readObject();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onPostExecute: slutt lesing" + (System.currentTimeMillis()-time2));

    }


    public boolean stopAsyncTaskIfOnStop() {
        if (addInfoToMap.isCancelled()) {
            backGroundTaskRunning = false;
            Log.d(TAG, "Stopper task");
            addedToDataStructure = false;

            return true;
        }
        return false;
    }

    private void clearItems(){

        if (mMap != null) {
            Log.d(TAG, "onResume: fjerner kyststier");
            mMap.clear();
            kyststiInfoMap.clear();

            polylinesOnMap.clear();
            markersOnMap.clear();


            markersReadyToAdd.clear();
            polylinesReadyToAdd.clear();


            binarykyststiInfoMap.clear();
            binaryPolylinesMap.clear();

        }

        if (mClusterManager != null) {

            mClusterManager.clearItems();
            Log.d(TAG, "onResume: fjerna markers");

        }

    }

    private void setOnClusterItemClickListener(final TextView markerInfo) {
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMarkerOptions>() {
            @Override
            public boolean onClusterItemClick(MyMarkerOptions item) {
                markerInfo.setVisibility(View.VISIBLE);

                animateInfobarUp();


                clickedClusterItem = item;

                setMarkerDescription(item.getTitle(), item.getDescription(), markerInfo);



                return false;

            }
        });
    }

    private void getDataFromFileAndPutInDatastructure() throws IOException, JSONException {
        if (stopAsyncTaskIfOnStop()) {
            Log.d(TAG, "getDataFromFileAndPutInDatastructure: stopp");
            return;
        }


        readBinaryFile();


        //Add the polylines to the map with ready polylines
        for (List<double[]> coordinatelist : binaryPolylinesMap.keySet()) {

            List<LatLng> coordinates = new ArrayList<>();
            for (double[] coordinatepair: coordinatelist) {
                coordinates.add(new LatLng(coordinatepair[0], coordinatepair[1]));


            }



            //Make a new polyline
            PolylineOptions polyline = new PolylineOptions();
            polyline.addAll(coordinates);
            polyline.clickable(true);

            String desc = binaryPolylinesMap.get(coordinatelist)[1];
            String name = binaryPolylinesMap.get(coordinatelist)[0];

            setKyststiColor(polyline, binaryPolylinesMap.get(coordinatelist)[1]);

            polylinesReadyToAdd.add(polyline);


            String[] descnameArray = {desc, name};
            kyststiInfoMap.put(coordinates, descnameArray);
        }


        //Add markers
        Log.d(TAG, "getDataFromFileAndPutInDatastructure: start markerparsing");
        long time2 = System.currentTimeMillis();

        final TextView markerInfo = (TextView) findViewById(R.id.infobar);
        setOnClusterItemClickListener(markerInfo);

        InputStream inputStream = getResources().openRawResource(R.raw.interesting_points);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        while (true) {

            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (! line.contains("gpxx_WaypointExtension")) {
                continue;
            }

            JSONObject obj = createJsonObject(line);
            String properties = obj.getString("properties");


            JSONObject obj2 = new JSONObject(properties);

            String name = obj2.getString("name");
            String markerTypes = obj2.getString("gpxx_WaypointExtension");
            String link = obj2.getString("link1_href");

            //Contains the different types of the marker, index 0 is irrelevant, start from index 1
            String[] markerTypesArray = markerTypes.split("          ");

            for (int i = 0; i < markerTypesArray.length; i++) {
                //Gets only the part with the relevant information
                markerTypesArray[i] = markerTypesArray[i].substring(markerTypesArray[i].indexOf(">")+1, markerTypesArray[i].indexOf("</"));
            }


            //Create the description
            StringBuilder desc = createDescriptionFromLinkAndMarkerTypes(link, markerTypesArray);


            MarkerOptions options = setMarkerOptions(line, name, desc, markerTypesArray);


            putMarkerInLists(line, options);




        }

        Log.d(TAG, "getDataFromFileAndPutInDatastructure: ferdig parse markres" + (System.currentTimeMillis()-time2));

        addedToDataStructure = true;



    }

    private void readFromJsonfilesAndPutInBinaryMaps() throws IOException, JSONException {

        Log.d(TAG, "getDataFromFileAndPutInDatastructure: start filparsing");
        long time = System.currentTimeMillis();
        int[] xmlfile = { R.raw.k1, R.raw.k2, R.raw.k3, R.raw.k4, R.raw.k5, R.raw.k6, R.raw.k7, R.raw.k8, R.raw.k9, R.raw.k10,R.raw.k11, R.raw.k12, R.raw.k13, R.raw.k14, R.raw.k15,R.raw.k16, R.raw.k17, R.raw.k18, R.raw.k19, R.raw.k20,R.raw.k21, R.raw.k21, R.raw.k22, R.raw.k23, R.raw.k24,R.raw.k25, R.raw.k26, R.raw.k27, R.raw.k28, R.raw.k29,R.raw.k30, R.raw.k31, R.raw.k32, R.raw.k33, R.raw.k34,R.raw.k35, R.raw.k36, R.raw.k37, R.raw.k38, R.raw.k39,R.raw.k40, R.raw.k41, R.raw.k42, R.raw.k43, R.raw.k44,R.raw.k45};

        for (int i = 0; i < xmlfile.length; i++) {
            InputStream inputStream = getResources().openRawResource(xmlfile[i]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (true) {
                if (stopAsyncTaskIfOnStop()) return;


                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!line.contains("coordinates")) {
                    continue;
                }

                JSONObject obj = createJsonObject(line);
                String properties = obj.getString("properties");
                JSONObject obj2 = new JSONObject(properties);
                String geometry = obj.getString("geometry");
                JSONObject obj3 = new JSONObject(geometry);


                String name = obj2.getString("Name");

                String description = obj2.getString("description");

                JSONArray coordinates = obj3.getJSONArray("coordinates");

                //Put into list of latlng
                List<LatLng> buildCoordinates = new ArrayList<>();
                List<double[]> binaryBuildCoordinates = new ArrayList<>();


                final PolylineOptions poly = new PolylineOptions();


                for (int j = 0; j < coordinates.length(); j++) {

                    String coord = coordinates.get(j).toString();
                    double lng = Double.valueOf(coord.substring(1, coord.indexOf(",")));

                    coord = coord.substring(coord.indexOf(",")+1, coord.length());
                    double lat = Double.valueOf(coord.substring(0, coord.indexOf(",")));

                    double[] latLng = {lat, lng};
                    binaryBuildCoordinates.add(latLng);
                    buildCoordinates.add(new LatLng(lat, lng));


                }

                String[] polyOptions = {name, description};
                binaryPolylinesMap.put(binaryBuildCoordinates, polyOptions);

                String[] descNameArray = {description, name};
                kyststiInfoMap.put(buildCoordinates, descNameArray);
                binarykyststiInfoMap.put(binaryBuildCoordinates, descNameArray);


                poly.clickable(true);
                poly.addAll(buildCoordinates);
                setKyststiColor(poly, description);

                polylinesReadyToAdd.add(poly);
                //Log.d(TAG, "getDataFromFileAndPutInDatastructure: add a poly");


            }

        }

        Log.d(TAG, "getDataFromFileAndPutInDatastructure: ferdig parse kyststier" + (System.currentTimeMillis()-time));

    }

    @Nullable
    private JSONObject createJsonObject(String line) throws JSONException {
        JSONObject obj = null;
        //For all the lines ending with ","
        if (line.matches(".{0,},")) {
             obj = new JSONObject(line.substring(0, line.length()-1));
          //The line does not end with ","
        } else if (line.matches(".{0,}[^,]")) {
            obj = new JSONObject(line);
        }
        return obj;
    }

    private void putMarkerInLists(String line, MarkerOptions options) {
        if (line.contains("Rampe")){
            rampeMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Badeplass")){
            beachMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Kran/Truck")){
            kranTruckMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Bunkers")){
            bunkersMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Butikk")){
            butikkMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Spisested")){
            spisestedMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Uthavn")){
            uthavnMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Fyr")){
            fyrMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Båtbutikk")){
            baatbutikkMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Marina")){
            marinaMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Gjestehavn")){
            gjestehavnMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Parkering transp")){
            parkeringTranspMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("WC")){
            WCMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Point of Interes")){
            pointOfInterestMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Campingplass")){
            campingplassMarkers.add(new MyMarkerOptions(options));
        }
        if (line.contains("Fiskeplass")){
            fiskeplassMarkers.add(new MyMarkerOptions(options));
        }
    }

    @NonNull
    private MarkerOptions setMarkerOptions(String line, String name, StringBuilder desc, String[] markerTypes) {
        List<String> markerTypesArray = Arrays.asList(markerTypes);

        MarkerOptions options = new MarkerOptions();
        setMarkerPosition(line, options);
        options.title(name);
        options.snippet(""+ desc);

/*
        //ditching icons for now

        if (markerTypesArray.contains("Badeplass")) {

            //To create image
            //open the svg file with gimp
            //konverter "bildeinnholdet" til png
            //svg resize til 64x64 px / 75x75
            //legg til bildeinnholdet og lag transparent

            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.boat_ramp);
            Bitmap scaled = Bitmap.createScaledBitmap(image, image.getWidth()/2, image.getHeight()/2, false);


            options.icon(BitmapDescriptorFactory.fromBitmap(scaled));
        }
*/


        return options;
    }




    @NonNull
    private StringBuilder createDescriptionFromLinkAndMarkerTypes(String link, String[] markerTypesArray) {
        StringBuilder desc = new StringBuilder();
        desc.append("- ");
        for (int i = 1; i < markerTypesArray.length; i++){
            if (i == markerTypesArray.length-1){
                desc.append(markerTypesArray[i]);
                break;
            }

            desc.append(markerTypesArray[i] + ", ");
        }
        if (link != null && !link.equals("null")) {
            desc.append("" + link);
        }
        return desc;
    }

    private void setMarkerPosition(String line, MarkerOptions options) {
        int indexOfStartCoordinate = line.indexOf("\"coordinates\": [ ") + 17;
        int indexOfEndCoordinate = line.indexOf(" ] } }");
        String coordinates = line.substring(indexOfStartCoordinate, indexOfEndCoordinate);

        double longitude = Double.valueOf(coordinates.substring(0, coordinates.indexOf(",")));
        double latitude = Double.valueOf(coordinates.substring(coordinates.indexOf(",")+1));


        options.position(new LatLng(latitude, longitude));
    }


    public class MyMarkerOptions implements ClusterItem {
        private String title;
        private String description;
        private LatLng position;
        private BitmapDescriptor icon;



        public MyMarkerOptions(MarkerOptions myMarkerOptions) {
            this.title = myMarkerOptions.getTitle();
            this.description = myMarkerOptions.getSnippet();
            this.position = myMarkerOptions.getPosition();
            this.icon = myMarkerOptions.getIcon();

        }



        @Override
        public LatLng getPosition() {
            return position;
        }

        public String getDescription() {
            return description;
        }

        public String getTitle() {
            return title;
        }


        public BitmapDescriptor getIcon() {
            return icon;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setIcon(BitmapDescriptor icon) {
            this.icon = icon;
        }

        public void setPosition(LatLng position) {
            this.position = position;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    class OwnIconRendered extends DefaultClusterRenderer<MyMarkerOptions> {

        public OwnIconRendered(Context context, GoogleMap map, ClusterManager<MyMarkerOptions> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyMarkerOptions item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());

            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }


}



