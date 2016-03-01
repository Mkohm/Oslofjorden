package com.oslofjorden;


import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.app.Application;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Display;

import org.json.JSONException;

import android.text.Html;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


import java.util.List;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadFactory;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterItem;


//TODO: splashscreen with picture while the other things is loading, promote myself,
//TODO:satelite, sporing icon, oslofjorden ikon, turn on location, to big infoboxes, toast that recommends location, farger kyststi, ask user and no problem

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback {
    //For debugging
    private static String TAG = "TAG";


    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    float currentZoom;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;
    LatLng currentMapClickPosition;

    public static GeoJsonLayer2 jsonLayer;

    private boolean kyststiInfoUp = false;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera onPause/onResume
    private boolean foundLastLocation = false;

    boolean mRequestingLocationUpdates;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




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


        Log.d(TAG, "onStart: " + locationUpdatesSwitch);

        super.onStart();


    }

    @Override
    protected void onResume() {

        super.onResume();


        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            if (locationUpdatesSwitch == true){
                startLocationUpdates();
                Log.d(TAG, "onResume: starter location updates" + locationUpdatesSwitch);
            }

        }

        if (currentCameraPosition != null){
            Log.d(TAG, "onResume: flytter til forrige pos" + currentCameraPosition.target.latitude);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCameraPosition));

        }


    }

    @Override
    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();

        Log.d(TAG, "onStop: stopper locationupdates" + locationUpdatesSwitch);

        super.onStop();
    }

    @Override
    protected void onPause() {

        currentZoom = mMap.getCameraPosition().zoom;
        currentCameraPosition = mMap.getCameraPosition();
        Log.d(TAG, "onPause: " + currentCameraPosition.target.latitude);

        stopLocationUpdates();
        Log.d(TAG, "onPause: stopper locationupdates" + locationUpdatesSwitch);






        super.onPause();

    }



    protected void stopLocationUpdates() {
        //TODO:error when trying to stop location updates before mgoogleapiclient is connected
        Log.d(TAG, "stopLocationUpdates");
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

        final Button onOffLocationButton = (Button) findViewById(R.id.onofflocationbutton);
        onOffLocationButton.setAlpha(0.7f);
        onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationUpdatesSwitch == true) {
                    onOffLocationButton.setText("Sporing av");
                    locationUpdatesSwitch = false;
                } else if (locationUpdatesSwitch == false) {
                    onOffLocationButton.setText("Sporing på");
                    locationUpdatesSwitch = true;
                }

                if (locationUpdatesSwitch == true) {
                    startLocationUpdates();
                    Log.d("TAG", "starter locationupdates igjen.");
                } else {
                    Log.d("TAG", "Stopper location updates.");
                    stopLocationUpdates();

                }

            }
        });


        final TextView kyststiInfo = (TextView) findViewById(R.id.kyststiInfo);
        kyststiInfo.setVisibility(View.INVISIBLE);



        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {

                if (currentPolyline != null){
                    currentPolyline.setColor(Color.BLUE);
                }

                currentPolyline = polyline;


                setKyststiInfoFromDescriptionAndName(polyline, kyststiInfo);

                kyststiInfo.setVisibility(View.VISIBLE);

                int maxY = getDeviceMaxY();

                //Hvis det allerede er et infovindu oppe skal det ikke animeres
                if (! kyststiInfoUp) {
                    animateKyststiInfoUp(maxY, kyststiInfo);
                    kyststiInfoUp = true;
                }


                currentPolyline.setColor(Color.BLACK);

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                currentMapClickPosition  = latLng;

                kyststiInfo.setVisibility(View.INVISIBLE);
                int maxY = getDeviceMaxY();


                //Hvis kyststiinfo er oppe, lukk den
                if (kyststiInfoUp) {

                    animateKyststiInfoDown(maxY, kyststiInfo);

                    kyststiInfoUp = false;
                }

            }
        });




        //The info window that is popping up when clicking on a marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                // Getting reference to the TextView to set title
                TextView title = (TextView) v.findViewById(R.id.infoView);

                setMarkerDescription(marker, title);

                //Sets the global variable link to the link that is provided in the snippet
                if (marker.getSnippet() == null){
                    link = "EMPTY";
                } else {

                    //the validation will be taken care of later
                    link = marker.getSnippet();
                }

                // Returning the view containing InfoWindow contents
                return v;
            }
        });

        //The event when you click on the info-window
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                //Go to the current link, if the link is empty, do nothing
                if (! link.equals("EMPTY")){

                    Intent i = new Intent(Intent.ACTION_VIEW);

                    if (link.matches("http[s]{0,1}:.{0,}")){
                        Log.d("TAG", "går til linken: " + link);
                        i.setData(Uri.parse(link));
                        startActivity(i);
                    }

                }

            }
        });



        AddInfoToMap addInfoToMap = new AddInfoToMap();
        addInfoToMap.execute();


    }

    private void animateKyststiInfoUp(int maxY, TextView kyststiInfo) {
        Animation animation = new TranslateAnimation(0, 0, maxY-kyststiInfo.getY(), 0);
        animation.setDuration(500);

        kyststiInfo.startAnimation(animation);
    }

    private void animateKyststiInfoDown(int maxY, TextView kyststiInfo) {
        Animation animation = new TranslateAnimation(0, 0, 0, maxY);
        animation.setDuration(500);
        kyststiInfo.startAnimation(animation);
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
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return true;
        }
        return false;
    }

    public static void addGeoJsonLayerToMap() {
        jsonLayer.addLayerToMap();
    }

    public static void addMapData() throws IOException, JSONException {
        jsonLayer = new GeoJsonLayer2(mMap, R.raw.alle_kyststier, MyApplication.getAppContext());


        for (GeoJsonFeature2 feature : jsonLayer.getFeatures()) {



            GeoJsonPointStyle2 pointStyle = new GeoJsonPointStyle2();
            GeoJsonLineStringStyle2 stringStyle;

            //Gets the name property from the json file
            pointStyle.setTitle(feature.getProperty("name"));
            feature.setPointStyle(pointStyle);

            if (feature.getProperty("name").equals("Rodeløkka")){
                Log.d(TAG, "addGeoJsonLayerToMapAndAddData: rodeløkke");
            }

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

    }


    private void setMarkerDescription(Marker marker, TextView title) {
        title.setMovementMethod(LinkMovementMethod.getInstance());

        String markerTitle = "<p>" + marker.getTitle() + "</p>";
        Log.d(TAG, "setMarkerDescription: " + markerTitle);

        //Hvis den ikke er tom og er en url så skal link vises
        if (marker.getSnippet() != null){
            String markerDescription = "";
            if (marker.getSnippet().matches("http[s]{0,1}:.{0,}")) {

                markerDescription = "<a href=\"" + marker.getSnippet() + "\"> <u> Klikk her for mer info </u></a>";

                title.setText(Html.fromHtml(markerTitle + markerDescription));

            //Det er ikke en link, men kanskje noe annet interessant
            } else {
                markerDescription = "<p>" + marker.getSnippet() + "</p>";
                title.setText(Html.fromHtml(markerTitle + markerDescription));
            }
        }

        else {
            title.setText(Html.fromHtml(markerTitle));
        }
    }

    private void setKyststiInfoFromDescriptionAndName(Polyline polyline, TextView kyststiInfo) {
        String description;
        String name;

        try{
            name = kyststiInfoMap.get(polyline.getPoints())[1];
        } catch (Exception e) {
            Log.d(TAG, "setKyststiInfoFromDescriptionAndName: noe gikk dårlig. " + e);
            name = null;

        }

        try{
            description = kyststiInfoMap.get(polyline.getPoints())[0];
        } catch (Exception e) {
            Log.d(TAG, "setKyststiInfoFromDescriptionAndName: " + e);
            description = null;
        }


        //Fant ingenting, tekst settes til her var det ingenting.
        if (description == null && name == null) {
            kyststiInfo.setText("Her var det ingenting, gitt!");
        }

        else if (description == null && name != null){
            //Setter navnet
            kyststiInfo.setText(name);
        }

        else if (description != null && name == null){
            setKystiDescription(kyststiInfo, description);


        }

        else if (description != null && name != null){
            setKystiDescription(kyststiInfo, description);
        }


    }

    private void setKystiDescription(TextView kyststiInfo, String description) {
        //Hvis description inneholder link settes denne som description
        if (description.contains("<a href=")){

            String kyststiTitle = description.substring(0, description.indexOf("<a"));
            kyststiTitle = "<p>" + kyststiTitle + "</p> ";

            String kyststiLink = description.substring(description.indexOf("<a href=")+9, description.indexOf(">"));
            kyststiLink = "<a href=\"" + kyststiLink + "\"> <u> Klikk her for mer info</u></a>";


            kyststiInfo.setText(Html.fromHtml(kyststiTitle + kyststiLink));
            kyststiInfo.setMovementMethod(LinkMovementMethod.getInstance());

            //Det er ingen link, da settes det som er der
        } else {
           kyststiInfo.setText(description);
        }
    }

    private void printdescriptions(HashMap<List<LatLng>, String> kyststiinfomap){
        Log.d(TAG, "printdescriptions: " + kyststiinfomap.values());

        for (String  desc: kyststiinfomap.values()) {
            Log.d(TAG, "printdescriptions: " + desc);
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


        if (locationUpdatesSwitch){
            startLocationUpdates();
        }


    }


    protected void startLocationUpdates() {
        if (checkPermission()) return;

        Log.d(TAG, "startLocationUpdates: starter loctationupdates");

        //Request locationupdates
        LocationRequest mLocationRequest = requestLocationUpdates();

        //Handle users without location enabled
        handleUsersWithoutLocationEnabled(mLocationRequest);

    }

    @NonNull
    private LocationRequest requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        LocationRequest mLocationRequest = new LocationRequest();
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


                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MapsActivity.this,
                                    0);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

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
        Log.d(TAG, "findAndGoToLastKnownLocation: gikk til " +lastLocation.latitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 17);
        mMap.moveCamera(cameraUpdate);

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

        Log.d("TAG", "Oppdaterte posisjon");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom);
        mMap.animateCamera(cameraUpdate);

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



        }

        @Override
        protected Void doInBackground(Void... params) {


            int counter = 0;
            try {
                long start = System.nanoTime();
                //publishProgress(1);
                jsonLayer = new GeoJsonLayer2(mMap, R.raw.alle_kyststier, MyApplication.getAppContext());

                long elapsed = System.nanoTime() - start;
                Log.d(TAG, "doInBackground: " + elapsed);


                for (GeoJsonFeature2 feature : jsonLayer.getFeatures()) {
                    counter++;
                    //publishProgress(1);

                    GeoJsonPointStyle2 pointStyle = new GeoJsonPointStyle2();
                    GeoJsonLineStringStyle2 stringStyle;

                    //Gets the name property from the json file
                    pointStyle.setTitle(feature.getProperty("name"));
                    feature.setPointStyle(pointStyle);

                    if (feature.getProperty("name").equals("Rodeløkka")){
                        Log.d(TAG, "addGeoJsonLayerToMapAndAddData: rodeløkke");
                    }

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            addGeoJsonLayerToMap();


            //Set up the cluster manager







            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {


            //progress.incrementProgressBy(1);
            //Log.d(TAG, "onProgressUpdate: " + values[0] + " " + progress.getProgress());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //progress.setMessage("Legger masse fine kyststier til kart");
            //Try do add this in a separate onasynctask



            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    //Adds polylines to the map
                    //for (PolylineOptions polylines : polylinesReadyToAdd){

  //                      mMap.addPolyline(polylines);
//                    }

                    //Add markers to the map
                    for (MarkerOptions marker: markersReadyToAdd){
                        mMap.addMarker(marker);
                    }

                }
            }, 1);




        }
    }


    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }
}




