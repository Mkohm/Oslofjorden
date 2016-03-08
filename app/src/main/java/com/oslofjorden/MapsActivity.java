package com.oslofjorden;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//TODO:link bug, strict mode, remove log


//Lage egen liste med kyststier i andre farger
//TODO: promote myself, handle links better
//TODO:farger kystst
//TODO: strings and translate to english
//skrudde av gps, spørsmål om du vil skru på igjen vent.. krøsj
//Adding custom tabs, sheet from material design

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback {
    //For debugging
    private static String TAG = "TAG";


    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    float currentZoom;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;
    LatLng currentMapClickPosition;

    boolean infoAddedToMap = false;

    Cluster clickedCluster;
    MyMarkerOptions clickedClusterItem;
    public ClusterManager<MyMarkerOptions> mClusterManager;

    public static GeoJsonLayer2 jsonLayer;

    private boolean infobarUp = false;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera onPause/onResume
    private boolean foundLastLocation = false;


    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    boolean userHasAnsweredLocationTurnOn = false;
    boolean userAcceptLocation = false;
    //is locationupdates enabled or not
    boolean locationUpdatesSwitch = true;

    //current link to link to
    String link = "EMPTY";

    public static HashMap<List<LatLng>, String[]> kyststiInfoMap = new HashMap<List<LatLng>, String[]>();
    public static String currentDescription = "Tom";
    public static ArrayList<String> descriptionList = new ArrayList<String>();
    public static int indexInDescriptionList = 0;
    public static ArrayList<String> nameList = new ArrayList<String>();
    public static int indexInNameList = 0;

    public static List<MarkerOptions> markersReadyToAdd = new ArrayList<MarkerOptions>();
    public static List<PolylineOptions> polylinesReadyToAdd = new ArrayList<PolylineOptions>();


    private Polyline currentPolyline;

    //Variables that the callback onconnectionfailed needs

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    //private static final String TAG = "TAG";
    
    private final int PERMISSIONS_OK = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        //Hvis man hadde location fra start av skal den ikke spørre mer
        if (isLocationEnabled(getApplicationContext())){
            userAcceptLocation = true;
        }

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


    }

    @Override
    protected void onStop() {
        try{
            stopLocationUpdates();
            mGoogleApiClient.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }


        super.onStop();
    }

    @Override
    protected void onPause() {

        try {
            currentZoom = mMap.getCameraPosition().zoom;
            currentCameraPosition = mMap.getCameraPosition();



            stopLocationUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }



        super.onPause();

    }


    protected void stopLocationUpdates() {
        //TODO:error when trying to stop location updates before mgoogleapiclient is connected
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }


    public boolean intersects(LatLngBounds bounds, LatLng southwest, LatLng northeast) {
        final boolean latIntersects =
                (bounds.northeast.latitude >= southwest.latitude) && (bounds.southwest.latitude <= northeast.latitude);
        final boolean lngIntersects =
                (bounds.northeast.longitude >= southwest.longitude) && (bounds.southwest.longitude <= northeast.longitude);

        return latIntersects && lngIntersects;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //enable zoom buttons, and remove toolbar when clicking on markers
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //enables location dot, and removes the standard google button
        if (checkPermission()) return;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        final FloatingActionButton onOffLocationButton = (FloatingActionButton) findViewById(R.id.onofflocationbutton);

        //Sets the initial icon depending on current settings
        if (isLocationEnabled(getApplicationContext())){
            onOffLocationButton.setImageResource(R.drawable.location_on_64px);
        } else {
            onOffLocationButton.setImageResource(R.drawable.location_off_64px);
        }

        onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationUpdatesSwitch == true) {
                    onOffLocationButton.setImageResource(R.drawable.location_off_64px);
                    Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
                    locationUpdatesSwitch = false;
                } else if (locationUpdatesSwitch == false) {


                    if (!userAcceptLocation || !isLocationEnabled(getApplicationContext())){
                        handleUsersWithoutLocationEnabled(mLocationRequest);

                    } else {
                        onOffLocationButton.setImageResource(R.drawable.location_on_64px);
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




        //The first time the user launches the app, this message will be shown


        final TextView kyststiInfo = (TextView) findViewById(R.id.infobar);
        kyststiInfo.setVisibility(View.INVISIBLE);


        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

                if (currentPolyline != null) {
                    currentPolyline.setColor(Color.BLUE);
                }

                currentPolyline = polyline;


                setKyststiInfoFromDescriptionAndName(polyline, kyststiInfo);

                kyststiInfo.setVisibility(View.VISIBLE);

                int maxY = getDeviceMaxY();

                //Hvis det allerede er et infovindu oppe skal det ikke animeres
                if (!infobarUp) {
                    animateInfobarUp(kyststiInfo);
                    infobarUp = true;
                }


                currentPolyline.setColor(Color.BLACK);

            }
        });



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {


                currentMapClickPosition = latLng;

                kyststiInfo.setVisibility(View.INVISIBLE);
                int maxY = getDeviceMaxY();


                //Hvis kyststiinfo er oppe, lukk den
                if (infobarUp) {

                    animateInfobarDown(kyststiInfo);

                    infobarUp = false;
                }



            }
        });

        //The event when you click on the info-window
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //Go to the current link, if the link is empty, do nothing
                if (!link.equals("EMPTY")) {

                    Intent i = new Intent(Intent.ACTION_VIEW);

                    if (link.matches("http[s]{0,1}:.{0,}")) {
                        i.setData(Uri.parse(link));
                        startActivity(i);
                    }

                }


            }
        });

        Log.d(TAG, "onMapReady: 2");
        
        try {
            if (! infoAddedToMap){
                AddInfoToMap addInfoToMap = new AddInfoToMap();
                addInfoToMap.execute();

            }
            infoAddedToMap = true;

        } catch (Exception e){
            e.printStackTrace();
        }



    }

    private void animateInfobarUp(TextView infobar) {
        int maxY = getDeviceMaxY();
        Animation animation = new TranslateAnimation(0, 0, maxY - infobar.getY(), 0);
        animation.setDuration(500);

        infobar.startAnimation(animation);
    }

    private void animateInfobarDown(TextView infobar) {
        int maxY = getDeviceMaxY();
        Animation animation = new TranslateAnimation(0, 0, 0, maxY);
        animation.setDuration(500);
        infobar.startAnimation(animation);

        infobar.setVisibility(View.INVISIBLE);
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


    public static void addGeoJsonLayerToDataStructure() {
        jsonLayer.addLayerToMap();
    }

    private void setMarkerDescription(String title, String description, TextView txtMarkerDescription) {
        txtMarkerDescription.setMovementMethod(LinkMovementMethod.getInstance());

        String markerTitle = "<p>" + title + "</p>";

        //Hvis den ikke er tom og er en url så skal link vises
        if (description != null) {
            String markerDescription = "";
            if (description.matches("http[s]{0,1}:.{0,}")) {

                markerDescription = "<a href=\"" + description + "\"> <u> Klikk her for mer info </u></a>";

                txtMarkerDescription.setText(Html.fromHtml(markerTitle + markerDescription));

                //Det er ikke en link, men kanskje noe annet interessant
            } else {
                markerDescription = "<p>" + description + "</p>";
                txtMarkerDescription.setText(Html.fromHtml(markerTitle + markerDescription));
            }
        } else {
            txtMarkerDescription.setText(Html.fromHtml(markerTitle));
        }
    }

    private void setKyststiInfoFromDescriptionAndName(Polyline polyline, TextView kyststiInfo) {
        String description;
        String name;

        try {
            name = kyststiInfoMap.get(polyline.getPoints())[1];
        } catch (Exception e) {
            name = null;

        }

        try {
            description = kyststiInfoMap.get(polyline.getPoints())[0];
        } catch (Exception e) {
            description = null;
        }


        //Fant ingenting, tekst settes til her var det ingenting.
        if (description == null && name == null) {
            kyststiInfo.setText("Her var det ingenting, gitt!");
        } else if (description == null && name != null) {
            //Setter navnet
            kyststiInfo.setText(name);
        } else if (description != null && name == null) {
            setKystiDescription(kyststiInfo, description);


        } else if (description != null && name != null) {
            setKystiDescription(kyststiInfo, description);
        }


    }

    private void setKystiDescription(TextView kyststiInfo, String description) {
        //Hvis description inneholder link settes denne som description
        if (description.contains("<a href=")) {

            String kyststiTitle = description.substring(0, description.indexOf("<a"));
            kyststiTitle = "<p>" + kyststiTitle + "</p> ";

            String kyststiLink = description.substring(description.indexOf("<a href=") + 9, description.indexOf(">"));
            kyststiLink = "<a href=\"" + kyststiLink + "\"> <u> Klikk her for mer info</u></a>";


            kyststiInfo.setText(Html.fromHtml(kyststiTitle + kyststiLink));
            kyststiInfo.setMovementMethod(LinkMovementMethod.getInstance());

            //Det er ingen link, da settes det som er der
        } else {
            kyststiInfo.setText(description);
        }
    }

    private void printdescriptions(HashMap<List<LatLng>, String> kyststiinfomap) {

        for (String desc : kyststiinfomap.values()) {
        }
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

        final FloatingActionButton onOffLocationButton = (FloatingActionButton) findViewById(R.id.onofflocationbutton);

        if (resultCode == 0){

            //Sporing button should be off
            Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
            onOffLocationButton.setImageResource(R.drawable.location_off_64px);

            locationUpdatesSwitch = false;
            userAcceptLocation = false;
        } else {
            Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - på", Toast.LENGTH_SHORT).show();
            onOffLocationButton.setImageResource(R.drawable.location_on_64px);

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
        LocationRequest mLocationRequest = requestLocationUpdates();

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
            lastLocation = new LatLng(59.908588, 10.741165);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 17);
        mMap.moveCamera(cameraUpdate);

    }

    private void setUpClusterer() {
        // Declare a variable for the cluster manager.


        // Position the map.

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)


        mClusterManager = new ClusterManager<MyMarkerOptions>(this, getMap());
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);

        mMap.setOnMarkerClickListener(mClusterManager);


        // Add cluster items (markers) to the cluster manager.
        addItems();

    }



    private void addItems() {
        for (MarkerOptions marker : markersReadyToAdd) {
            mClusterManager.addItem(new MyMarkerOptions(marker));
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

        //Updates current location
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom);
        mMap.animateCamera(cameraUpdate);
        Log.i(TAG, "OnLocationChanged: Location oppdatert");

    }

    @Override
    public void onResult(@NonNull Result result) {

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
            Log.d(TAG, "onPreExecute: ");
            TextView loading = (TextView) findViewById(R.id.infobar);
            loading.setVisibility(View.VISIBLE);

            loading.setText("Oslofjorden laster inn kyststier.. De vil poppe opp på kartet ditt snart :)");

        }

        @Override
        protected Void doInBackground(Void... params) {


            try {
                getDataFromFileAndPutInDatastructure();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {


        }

        @Override
        protected void onPostExecute(Void aVoid) {

          //  addPolylinesToMap();
            final Iterator<PolylineOptions> iterator = polylinesReadyToAdd.iterator();
            try {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (iterator.hasNext()){
                            mMap.addPolyline(iterator.next());
                            handler.postDelayed(this, 10);
                        }
                    }
                }, 100);
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Dette gikk dårlig, kyststier ble ikke lastet inn.", Toast.LENGTH_SHORT).show();
            }

            try {
                setUpClusterer();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Dette gikk dårlig, markers ble ikke lastet inn.", Toast.LENGTH_SHORT).show();
            }



            TextView loading = (TextView) findViewById(R.id.infobar);
            animateInfobarDown(loading);



            final TextView markerInfo = (TextView) findViewById(R.id.infobar);
            setOnClusterItemClickListener(markerInfo);


            Log.i(TAG, "onPostExecute: Kartinformasjon lastet inn!");
        }
    }

    private void setOnClusterItemClickListener(final TextView markerInfo) {
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyMarkerOptions>() {
            @Override
            public boolean onClusterItemClick(MyMarkerOptions item) {
                markerInfo.setVisibility(View.VISIBLE);
                if (! infobarUp){
                    animateInfobarUp(markerInfo);
                    infobarUp = true;
                }

    

                clickedClusterItem = item;

                setMarkerDescription(item.getTitle(), item.getDescription(), markerInfo);



                return false;

            }
        });
    }

    private void getDataFromFileAndPutInDatastructure() throws IOException, JSONException {
        jsonLayer = new GeoJsonLayer2(mMap, R.raw.alle_kyststier, MyApplication.getAppContext());

        for (GeoJsonFeature2 feature : jsonLayer.getFeatures()) {

            GeoJsonPointStyle2 pointStyle = new GeoJsonPointStyle2();
            GeoJsonLineStringStyle2 stringStyle;

            //Gets the name property from the json file
            pointStyle.setTitle(feature.getProperty("name"));
            feature.setPointStyle(pointStyle);


            //Gets the description property from the json file
            pointStyle.setSnippet(feature.getProperty("description"));

            stringStyle = feature.getLineStringStyle();

            //Hvis det er en kyststi legg til description og navn
            if (feature.getGeometry().getType().equals("LineString")) {
                descriptionList.add(feature.getProperty("description"));

                nameList.add(feature.getProperty("name"));
            }


            stringStyle.setClickable(true);
            stringStyle.setColor(Color.BLUE);


            feature.setLineStringStyle(stringStyle);
        }

        addGeoJsonLayerToDataStructure();
    }

    private void addPolylinesToMap() {
        for (PolylineOptions polylineOptions : polylinesReadyToAdd){
            mMap.addPolyline(polylineOptions);
        }
    }


    public class MyMarkerOptions implements ClusterItem {
        private String title;
        private String description;
        private final LatLng position;



        public MyMarkerOptions(MarkerOptions myMarkerOptions) {
            this.title = myMarkerOptions.getTitle();
            this.description = myMarkerOptions.getSnippet();
            this.position = myMarkerOptions.getPosition();

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


    }




}



