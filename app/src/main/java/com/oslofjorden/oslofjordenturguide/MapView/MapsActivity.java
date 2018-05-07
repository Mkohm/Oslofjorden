package com.oslofjorden.oslofjordenturguide.MapView;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.oslofjorden.oslofjordenturguide.R;
import com.oslofjorden.oslofjordenturguide.WebView.CustomTabActivityHelper;
import com.oslofjorden.oslofjordenturguide.WebView.WebviewFallback;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu
//Rapporter feil/tur/hvasomhelst
//instillinger oppdateringshastighet ++
//I nærheten
//Database implementation and search
//lagrer ikke


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback, CustomTabActivityHelper.ConnectionCallback, NoticeDialogListener {
    public static String TAG = "TAG";
    private static boolean stopAddingPolylines = false;

    AddInfoToMap addInfoToMap;

    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;


    float currentZoom = 13;
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


    private static ArrayList<Polyline> polylinesOnMap;
    private HashMap<LatLng, MyMarkerOptions> markersOnMap = new HashMap();

    private boolean[] checkedItems;
    private boolean[] defaultChecked;

    private boolean addingPolylines = false;


    private ImageButton layerButton;

    private boolean haslocationPermission;
    private boolean locationEnabled;
    private List<PolylineData> polylineData;
    private List<MyMarkerOptions> markerData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.i(TAG, "onCreate: oncreate");

        addedToDataStructure = false;
        infobarUp = false;
        polylinesOnMap = new ArrayList<>();


        //Add arraylists to arraylist of arraylists //the first element is empty and never used
        addArrayListsToArraylistOfArrayLists();
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


        //Create a new instance of GoogleAPIClient
        createInstanceOfGoogleAPIClient();

        //Something with running google play services safaly
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        setUpToolbar();


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
        for (int i = 0; i < categoryArrayList.size(); i++) {
            //If the markersonmap add list contains the element, do not add it

            if (markersOnMap.containsKey(categoryArrayList.get(i).getPosition())) {
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


        for (int i = 0; i < polylineData.size(); i++) {

            Polyline polyline = mMap.addPolyline(polylineData.get(i).getOptions());
            polyline.setTag(polylineData.get(i));

        }
        animateInfobarDown();
    }

    private void removePolylines() {
        Log.d(TAG, "removePolylines: fjerner kyststierh");
        if (addingPolylines) {
            stopAddingPolylines = true;
        }

        Log.d(TAG, "removePolylines: setter variabel stoppinnlasting til true");

        for (Polyline poly : polylinesOnMap) {
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
        super.onStart();

        mGoogleApiClient.connect();

        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }


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

        addInfoToMap = new AddInfoToMap();
        addInfoToMap.execute();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (addingPolylines) {
            clearItems();
            addedToDataStructure = false;
        }

        try {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();

        } catch (Exception e) {
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
        super.onPause();

        try {
            currentZoom = mMap.getCameraPosition().zoom;
            currentCameraPosition = mMap.getCameraPosition();


            stopLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "onPause: Noe gikk galt under pause");


        }

    }


    protected void stopLocationUpdates() {
        // TODO: error when trying to stop location updates before mgoogleapiclient is connected
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        if (polylineData != null) {
            loadLastStateOfApplication();
        }

        //enable zoom buttons, and remove toolbar when clicking on markers
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);


        setUpClusterer();


        final ImageButton onOffLocationButton = (ImageButton) findViewById(R.id.onofflocationbutton);
        onOffLocationButton.setImageResource(R.drawable.ic_location_off);
        locationUpdatesSwitch = false;


        onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
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
                    stopLocationUpdates();

                }

            }
        });

        final TextView kyststiInfo = (TextView) findViewById(R.id.infobar);


        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Set the color of the previous polyline back to what it was
                setOriginalPolylineColor();

                polyline.setColor(Color.BLACK);

                setKyststiInfoFromDescriptionAndName((PolylineData) polyline.getTag(), kyststiInfo);

                kyststiInfo.setVisibility(View.VISIBLE);

                animateInfobarUp();


                previousPolylineClicked = polyline;

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                currentMapClickPosition = latLng;


                int maxY = getDeviceMaxY();


                //Hvis kyststiinfo er oppe, lukk den

                if (addedToDataStructure) {
                    animateInfobarDown();
                }


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

        if (infobarUp) {
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


    private void setMarkerDescription(String title, String description, TextView txtMarkerDescription) {
        txtMarkerDescription.setMovementMethod(LinkMovementMethod.getInstance());

        String type = "Marker";
        String markerTitle = title;

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

    private void setKyststiInfoFromDescriptionAndName(PolylineData polylineData, TextView kyststiInfo) {
        String description = polylineData.getDescription();
        String title = polylineData.getTitle();
        String type = "Polyline";


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
        final Uri uri = Uri.parse(uriWithDesc + "?app=1");
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
        uriWithDesc = uriWithDesc.substring(span.getURL().indexOf("http"), span.getURL().length());
        return uriWithDesc;
    }

    protected void setTextViewHTML(TextView text, String description, String type, String title) {


        if (type.equals("Marker")) {
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
            if (urls.length != 0) {
                //Create the desired format of textview
                String link = "<a href=\"" + urls[0].getURL() + "\"><u>Mer info fra Oslofjorden.com</u></a>";
                CharSequence formattedText = Html.fromHtml(link);
                withCustomLinkLayout = new SpannableStringBuilder(formattedText);
                urls2 = withCustomLinkLayout.getSpans(0, formattedText.length(), URLSpan.class);
                withCustomLinkLayout.insert(0, descriptionWithoutLink);

                //If the link does not contain www return
                if (!urls[0].getURL().contains("www")) {
                    text.setText(description);
                    return;
                }

            }


            for (URLSpan span : urls2) {
                makeLinkClickable(withCustomLinkLayout, span);
            }
            text.setMovementMethod(LinkMovementMethod.getInstance());

            text.setText(withCustomLinkLayout);


        } catch (Exception e) {
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
        if (urls.length != 0) {
            //Create the desired format of textview
            String link = "<a href=\"" + urls[0].getURL() + "\"><u>Mer info fra Oslofjorden.com</u></a>";
            CharSequence formattedText = Html.fromHtml(link);
            withCustomLinkLayout = new SpannableStringBuilder(formattedText);
            urls2 = withCustomLinkLayout.getSpans(0, formattedText.length(), URLSpan.class);
            withCustomLinkLayout.insert(0, title + " " + typesOfMarker + "\n\n");
        }


        for (URLSpan span : urls2) {
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

    @Override
    public void onConnected(Bundle connectionHint) {
        //Connected to google play services: This is where the magic happens


        //Will find the the last known location only the first time
        if (!foundLastLocation) {
            findAndGoToLastKnownLocation();
            startLocationUpdates();
            foundLastLocation = true;

        } else {
            if (locationUpdatesSwitch) {
                startLocationUpdates();
            }
        }


    }


    protected void startLocationUpdates() {
        //If the user has the needed permissions
        if (haslocationPermission) {
            mLocationRequest = requestLocationUpdates();
        }


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

        Log.d(TAG, "requestLocationUpdates: lager ny request om oppdateringer");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
        return mLocationRequest;
    }

    private void handleUsersWithoutLocationEnabled() {

        LocationRequest settingsRequest = new LocationRequest();


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(settingsRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        //Make user add location
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }

    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    private void findAndGoToLastKnownLocation() throws SecurityException {

        Location mLastLocation = null;
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


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


        mClusterManager = new ClusterManager<MyMarkerOptions>(this, mMap);
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MyMarkerOptions>(new GridBasedAlgorithm<MyMarkerOptions>()));

        mClusterManager.setRenderer(new OwnIconRendered(getApplicationContext(), mMap, mClusterManager));

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);


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


    public boolean stopAsyncTaskIfOnStop() {
        if (addInfoToMap.isCancelled()) {
            Log.d(TAG, "Stopper task");
            addedToDataStructure = false;

            return true;
        }
        return false;
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

    private void setOnClusterItemClickListener(final TextView markerInfo) {
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMarkerOptions>() {
            @Override
            public boolean onClusterItemClick(MyMarkerOptions item) {
                markerInfo.setVisibility(View.VISIBLE);

                animateInfobarUp();


                clickedClusterItem = item;

             //   setMarkerDescription(item.getTitle(), item., markerInfo);


                return false;

            }
        });
    }

    private void getDataFromFilesAndPutInDatastructure() throws IOException, JSONException {

        BinarydataReader binaryReader = new BinarydataReader(getApplicationContext(), addInfoToMap);
        polylineData = binaryReader.readBinaryData(R.raw.polylines_binary_file);


        MarkerReader markerReader = new MarkerReader(getApplicationContext(), addInfoToMap);
        markerData = markerReader.readMarkers();

        final TextView markerInfo = (TextView) findViewById(R.id.infobar);
        setOnClusterItemClickListener(markerInfo);

    }

    class AddInfoToMap extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {

            TextView loading = (TextView) findViewById(R.id.infobar);
            loading.setVisibility(View.VISIBLE);

            loading.setText("Laster inn data...\n\nEtter innlasting kan du velge hva som skal vises med knappen oppe til høyre.");

            animateInfobarUp();

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

            animateInfobarDown();

            layerButton.setClickable(true);
            layerButton.setEnabled(true);
            layerButton.setImageResource(R.drawable.ic_layers_white_24dp);
            findViewById(R.id.loading).setVisibility(View.GONE);

        }
    }
}
