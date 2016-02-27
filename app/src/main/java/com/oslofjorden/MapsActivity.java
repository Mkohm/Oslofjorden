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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


import java.util.Arrays;
import java.util.Observable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import java.util.List;
import java.util.Observer;
import java.util.Set;

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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;


//TODO: splashscreen with picture while the other things is loading, stop updating if you are moving on the map
//TODO:satelite, sporing icon, oslofjorden ikon, turn on location,
//TODO: on resume bugs with location?? rickroll

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback {
    //For debugging
    private String TAG = "TAG";


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    float currentZoom;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera onPause/onResume
    private boolean foundLastLocation = false;

    boolean mRequestingLocationUpdates;

    //is locationupdates enabled or not
    boolean locationUpdatesSwitch;

    //current link to link to
    String link = "EMPTY";

    public static HashMap<List<LatLng>, String> kyststiInfoMap = new HashMap<List<LatLng>, String>();
    GeoJsonRenderer renderer;
    public static String currentDescription = "Tom";
    public static ArrayList<String> descriptionList = new ArrayList<String>();
    public static int indexInDescriptionList = 0;

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


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Create a new instance of GoogleAPIClient
        createInstanceOfGoogleAPIClient();

        //Something with running google play services safaly
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);


        //Default value for locationupdates is on
        locationUpdatesSwitch = true;

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
        mMap.getUiSettings().setZoomControlsEnabled(true);
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
        kyststiInfo.setText("Dette er en kyststi.");

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if (currentPolyline != null){
                    currentPolyline.setColor(Color.BLUE);
                }

                currentPolyline = polyline;


                setKyststiInfoFromDescription(polyline, kyststiInfo);

                kyststiInfo.setVisibility(View.VISIBLE);
                currentPolyline.setColor(Color.BLACK);

            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                kyststiInfo.setVisibility(View.INVISIBLE);

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


        addGeoJsonLayerToMapAndAddData();

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

    private void addGeoJsonLayerToMapAndAddData() {
        try {


            GeoJsonLayer2 jsonLayer = new GeoJsonLayer2(mMap, R.raw.alle_kyststier, getApplicationContext());


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

                if (feature.getGeometry().getType().equals("LineString")) {
                    descriptionList.add(feature.getProperty("description"));
                }

                stringStyle.setClickable(true);
                stringStyle.setColor(Color.BLUE);


                feature.setLineStringStyle(stringStyle);

            }

            jsonLayer.addLayerToMap();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setMarkerDescription(Marker marker, TextView title) {
        title.setMovementMethod(LinkMovementMethod.getInstance());

        String markerTitle = "<p>" + marker.getTitle() + "</p>";

        //Hvis den ikke er tom og er en url så skal link vises
        if (marker.getSnippet() != null){
            String markerDescription = "";
            if (marker.getSnippet().matches("http[s]{0,1}:.{0,}")) {

                markerDescription = "\n <a href=" + marker.getSnippet() + "> <u> Klikk her for mer info </u></a>";
                String allMarkerDescription = markerTitle + markerDescription;

                title.setText(Html.fromHtml(allMarkerDescription));

            //Det er ikke en link, men kanskje noe annet interessant
            } else {
                markerDescription = "<br> <p>" + marker.getSnippet() + "</p>";
                title.setText(Html.fromHtml(markerDescription));
            }
        }

        else {
            title.setText(Html.fromHtml(markerTitle));
        }
    }

    private void setKyststiInfoFromDescription(Polyline polyline, TextView kyststiInfo) {
        String description = kyststiInfoMap.get(polyline.getPoints());

        //Setter teksten til description
        if (description.contains("<a href=")){

            String kyststiTitle = description.substring(0, description.indexOf("<a"));
            kyststiTitle = "<p>" + kyststiTitle + "</p> ";

            String kyststiLink = description.substring(description.indexOf("<a href=")+9, description.indexOf(">"));
            kyststiLink = "<a href=\"" + kyststiLink + "\"> <u> Klikk her for mer info</u></a>";


            kyststiInfo.setText(Html.fromHtml(kyststiTitle + kyststiLink));
            kyststiInfo.setMovementMethod(LinkMovementMethod.getInstance());

        } else {
            kyststiInfo.setText(description);
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
}



/**
 * A class that allows for GeoJsonLineString objects to be styled and for these styles to be
 * translated into a PolylineOptions object. {@see
 * <a href="https://developer.android.com/reference/com/google/android/gms/maps/model/PolylineOptions.html">
 * PolylineOptions docs</a> for more details about the options.}
 */
class GeoJsonLineStringStyle2 extends Observable implements GeoJsonStyle2 {

    private final static String[] GEOMETRY_TYPE = {"LineString", "MultiLineString",
            "GeometryCollection"};

    private final PolylineOptions mPolylineOptions;

    /**
     * Creates a new LineSringStyle object
     */
    public GeoJsonLineStringStyle2() {
        mPolylineOptions = new PolylineOptions();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the color of the GeoJsonLineString as a 32-bit ARGB color
     *
     * @return color of the GeoJsonLineString
     */
    public int getColor() {
        return mPolylineOptions.getColor();
    }



    /**
     * Sets the color of the GeoJsonLineString as a 32-bit ARGB color
     *
     * @param color color value of the GeoJsonLineString
     */
    public void setColor(int color) {
        mPolylineOptions.color(color);
        styleChanged();
    }

    public void setClickable(boolean clickable){
        mPolylineOptions.clickable(clickable);

        styleChanged();
    }

    public boolean isClickable() {
        return mPolylineOptions.isClickable();
    }

    /**
     * Gets whether the GeoJsonLineString is geodesic
     *
     * @return true if GeoJsonLineString is geodesic, false otherwise
     */
    public boolean isGeodesic() {
        return mPolylineOptions.isGeodesic();
    }

    /**
     * Sets whether the GeoJsonLineString is geodesic
     *
     * @param geodesic true if GeoJsonLineString is geodesic, false otherwise
     */
    public void setGeodesic(boolean geodesic) {
        mPolylineOptions.geodesic(geodesic);
        styleChanged();
    }

    /**
     * Gets the width of the GeoJsonLineString in screen pixels
     *
     * @return width of the GeoJsonLineString
     */
    public float getWidth() {
        return mPolylineOptions.getWidth();
    }

    /**
     * Sets the width of the GeoJsonLineString in screen pixels
     *
     * @param width width value of the GeoJsonLineString
     */
    public void setWidth(float width) {
        mPolylineOptions.width(width);
        styleChanged();
    }

    /**
     * Gets the z index of the GeoJsonLineString
     *
     * @return z index of the GeoJsonLineString
     */
    public float getZIndex() {
        return mPolylineOptions.getZIndex();
    }

    /**
     * Sets the z index of the GeoJsonLineString
     *
     * @param zIndex z index value of the GeoJsonLineString
     */
    public void setZIndex(float zIndex) {
        mPolylineOptions.zIndex(zIndex);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonLineString is visible
     *
     * @return true if the GeoJsonLineString visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mPolylineOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonLineString is visible
     *
     * @param visible true if the GeoJsonLineString is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mPolylineOptions.visible(visible);
        styleChanged();
    }

    /**
     * Notifies the observers, GeoJsonFeature objects, that the style has changed. Indicates to the
     * GeoJsonFeature that it should check whether a redraw is needed for the feature.
     */
    private void styleChanged() {
        setChanged();
        notifyObservers();
    }

    /**
     * Gets a new PolylineOptions object containing styles for the GeoJsonLineString
     *
     * @return new PolylineOptions object
     */
    public PolylineOptions toPolylineOptions() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(mPolylineOptions.getColor());
        polylineOptions.clickable(mPolylineOptions.isClickable());
        polylineOptions.geodesic(mPolylineOptions.isGeodesic());
        polylineOptions.visible(mPolylineOptions.isVisible());
        polylineOptions.width(mPolylineOptions.getWidth());
        polylineOptions.zIndex(mPolylineOptions.getZIndex());
        return polylineOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LineStringStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
        sb.append(",\n color=").append(getColor());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n width=").append(getWidth());
        sb.append(",\n z index=").append(getZIndex());
        sb.append("\n}\n");
        return sb.toString();
    }
}



/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
class GeoJsonFeature2 extends Observable implements Observer {

    private final String mId;

    private final LatLngBounds mBoundingBox;

    private final HashMap<String, String> mProperties;

    private GeoJsonGeometry2 mGeometry;

    private GeoJsonPointStyle2 mPointStyle;

    private GeoJsonLineStringStyle2 mLineStringStyle;

    private GeoJsonPolygonStyle2 mPolygonStyle;

    /**
     * Creates a new GeoJsonFeature object
     *
     * @param geometry    type of geometry to assign to the feature
     * @param id          common identifier of the feature
     * @param properties  hashmap of containing properties related to the feature
     * @param boundingBox bounding box of the feature
     */
    public GeoJsonFeature2(GeoJsonGeometry2 geometry, String id,
                          HashMap<String, String> properties, LatLngBounds boundingBox) {
        mGeometry = geometry;
        mId = id;
        mBoundingBox = boundingBox;
        if (properties == null) {
            mProperties = new HashMap<String, String>();
        } else {
            mProperties = properties;
        }
    }

    /**
     * Returns all the stored property keys
     *
     * @return iterable of property keys
     */
    public Iterable<String> getPropertyKeys() {
        return mProperties.keySet();
    }

    /**
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    public String getProperty(String property) {
        return mProperties.get(property);
    }

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    public String setProperty(String property, String propertyValue) {
        return mProperties.put(property, propertyValue);
    }

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    public boolean hasProperty(String property) {
        return mProperties.containsKey(property);
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    public String removeProperty(String property) {
        return mProperties.remove(property);
    }

    /**
     * Returns the style used to render GeoJsonPoints
     *
     * @return style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle2 getPointStyle() {
        return mPointStyle;
    }

    /**
     * Sets the style used to render GeoJsonPoints
     *
     * @param pointStyle style used to render GeoJsonPoints
     */
    public void setPointStyle(GeoJsonPointStyle2 pointStyle) {
        if (pointStyle == null) {
            throw new IllegalArgumentException("Point style cannot be null");
        }

        if (mPointStyle != null) {
            // Remove observer for previous style
            mPointStyle.deleteObserver(this);
        }
        mPointStyle = pointStyle;
        mPointStyle.addObserver(this);
        checkRedrawFeature(mPointStyle);
    }

    /**
     * Returns the style used to render GeoJsonLineStrings
     *
     * @return style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle2 getLineStringStyle() {
        return mLineStringStyle;
    }

    /**
     * Sets the style used to render GeoJsonLineStrings
     *
     * @param lineStringStyle style used to render GeoJsonLineStrings
     */
    public void setLineStringStyle(GeoJsonLineStringStyle2 lineStringStyle) {
        if (lineStringStyle == null) {
            throw new IllegalArgumentException("Line string style cannot be null");
        }

        if (mLineStringStyle != null) {
            // Remove observer for previous style
            mLineStringStyle.deleteObserver(this);
        }
        mLineStringStyle = lineStringStyle;
        mLineStringStyle.addObserver(this);
        checkRedrawFeature(mLineStringStyle);

    }

    /**
     * Returns the style used to render GeoJsonPolygons
     *
     * @return style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle2 getPolygonStyle() {
        return mPolygonStyle;
    }

    /**
     * Sets the style used to render GeoJsonPolygons
     *
     * @param polygonStyle style used to render GeoJsonPolygons
     */
    public void setPolygonStyle(GeoJsonPolygonStyle2 polygonStyle) {
        if (polygonStyle == null) {
            throw new IllegalArgumentException("Polygon style cannot be null");
        }

        if (mPolygonStyle != null) {
            // Remove observer for previous style
            mPolygonStyle.deleteObserver(this);
        }
        mPolygonStyle = polygonStyle;
        mPolygonStyle.addObserver(this);
        checkRedrawFeature(mPolygonStyle);

    }

    /**
     * Checks whether the new style that was set requires the feature to be redrawn. If the
     * geometry
     * and the style that was set match, then the feature is redrawn.
     *
     * @param style style to check if a redraw is needed
     */
    private void checkRedrawFeature(GeoJsonStyle2 style) {
        if (mGeometry != null && Arrays.asList(style.getGeometryType())
                .contains(mGeometry.getType())) {
            // Don't redraw objects that aren't on the map
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Gets the stored GeoJsonGeometry
     *
     * @return GeoJsonGeometry
     */
    public GeoJsonGeometry2 getGeometry() {
        return mGeometry;
    }

    /**
     * Sets the stored GeoJsonGeometry and redraws it on the layer if it has already been added
     *
     * @param geometry GeoJsonGeometry to set
     */
    public void setGeometry(GeoJsonGeometry2 geometry) {
        mGeometry = geometry;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the ID of the feature
     *
     * @return ID of the feature, if there is no ID then null will be returned
     */
    public String getId() {
        return mId;
    }

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    public boolean hasGeometry() {
        return (mGeometry != null);
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the feature. If
     * the feature did not have a bounding box then null will be returned.
     *
     * @return LatLngBounds containing bounding box of the feature, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feature{");
        sb.append("\n bounding box=").append(mBoundingBox);
        sb.append(",\n geometry=").append(mGeometry);
        sb.append(",\n point style=").append(mPointStyle);
        sb.append(",\n line string style=").append(mLineStringStyle);
        sb.append(",\n polygon style=").append(mPolygonStyle);
        sb.append(",\n id=").append(mId);
        sb.append(",\n properties=").append(mProperties);
        sb.append("\n}\n");
        return sb.toString();
    }

    /**
     * Update is called if the developer modifies a style that is stored in this feature
     *
     * @param observable GeoJsonStyle object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonStyle2) {
            checkRedrawFeature((GeoJsonStyle2) observable);
        }
    }
}

/**
 * A class that allows the developer to import GeoJSON data, style it and interact with the
 * imported data.
 *
 * To create a new GeoJsonLayer from a resource stored locally
 * {@code GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.resource,
 * getApplicationContext());}
 *
 * To render the imported GeoJSON data onto the layer
 * {@code layer.addLayerToMap();}
 *
 * To remove the rendered data from the layer
 * {@code layer.removeLayerFromMap();}
 */
class GeoJsonLayer2 {

    private final GeoJsonRenderer mRenderer;

    private LatLngBounds mBoundingBox;

    /**
     * Creates a new GeoJsonLayer object. Default styles are applied to the GeoJsonFeature objects.
     *
     * @param map         map where the layer is to be rendered
     * @param geoJsonFile GeoJSON data to add to the layer
     */
    public GeoJsonLayer2(GoogleMap map, JSONObject geoJsonFile) {
        if (geoJsonFile == null) {
            throw new IllegalArgumentException("GeoJSON file cannot be null");
        }

        mBoundingBox = null;
        GeoJsonParser parser = new GeoJsonParser(geoJsonFile);
        // Assign GeoJSON bounding box for FeatureCollection
        mBoundingBox = parser.getBoundingBox();
        HashMap<GeoJsonFeature2, Object> geoJsonFeatures = new HashMap<GeoJsonFeature2, Object>();
        for (GeoJsonFeature2 feature : parser.getFeatures()) {
            geoJsonFeatures.put(feature, null);
        }
        mRenderer = new GeoJsonRenderer(map, geoJsonFeatures);
    }

    /**
     * Creates a new GeoJsonLayer object. Default styles are applied to the GeoJsonFeature objects.
     *
     * @param map        map where the layer is to be rendered
     * @param resourceId GeoJSON file to add to the layer
     * @param context    context of the application, required to open the GeoJSON file
     * @throws IOException   if the file cannot be open for read
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     */
    public GeoJsonLayer2(GoogleMap map, int resourceId, Context context)
            throws IOException, JSONException {
        this(map, createJsonFileObject(context.getResources().openRawResource(resourceId)));
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream character input stream representing the GeoJSON file
     * @return JSONObject with the GeoJSON data
     * @throws IOException   if the file cannot be opened for read
     * @throws JSONException if the JSON file has poor structure
     */
    private static JSONObject createJsonFileObject(InputStream stream)
            throws IOException, JSONException {
        String line;
        StringBuilder result = new StringBuilder();
        // Reads from stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            // Read each line of the GeoJSON file into a string
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            reader.close();
        }
        // Converts the result string into a JSONObject
        return new JSONObject(result.toString());
    }

    /**
     * Gets an iterable of all GeoJsonFeature elements that have been added to the layer
     *
     * @return iterable of GeoJsonFeature elements
     */
    public Iterable<GeoJsonFeature2> getFeatures() {
        return mRenderer.getFeatures();
    }

    /**
     * Adds all the GeoJsonFeature objects parsed from the given GeoJSON data onto the map
     */
    public void addLayerToMap() {
        mRenderer.addLayerToMap();
    }

    /**
     * Adds a GeoJsonFeature to the layer. If the point, linestring or polygon style is set to
     * null, the relevant default styles are applied.
     *
     * @param feature GeoJsonFeature to add to the layer
     */
    public void addFeature(GeoJsonFeature2 feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        mRenderer.addFeature(feature);
    }

    /**
     * Removes the given GeoJsonFeature from the layer
     *
     * @param feature feature to remove
     */
    public void removeFeature(GeoJsonFeature2 feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        mRenderer.removeFeature(feature);
    }

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }

    /**
     * Removes all GeoJsonFeatures on the layer from the map
     */
    public void removeLayerFromMap() {
        mRenderer.removeLayerFromMap();
    }

    /**
     * Get whether the layer is on the map
     *
     * @return true if the layer is on the map, false otherwise
     */
    public boolean isLayerOnMap() {
        return mRenderer.isLayerOnMap();
    }

    /**
     * Gets the default style used to render GeoJsonPoints. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle2 getDefaultPointStyle() {
        return mRenderer.getDefaultPointStyle();
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle2 getDefaultLineStringStyle() {
        return mRenderer.getDefaultLineStringStyle();
    }

    /**
     * Gets the default style used to render GeoJsonPolygons. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle2 getDefaultPolygonStyle() {
        return mRenderer.getDefaultPolygonStyle();
    }

    /**
     * Gets the LatLngBounds containing the coordinates of the bounding box for the
     * FeatureCollection. If the FeatureCollection did not have a bounding box or if the GeoJSON
     * file did not contain a FeatureCollection then null will be returned.
     *
     * @return LatLngBounds containing bounding box of FeatureCollection, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append("\n Bounding box=").append(mBoundingBox);
        sb.append("\n}\n");
        return sb.toString();
    }
}

/**
 * Parses a JSONObject and places data into their appropriate GeoJsonFeature objects. Returns an
 * array of
 * GeoJsonFeature objects parsed from the GeoJSON file.
 */
/* package */ class GeoJsonParser {

    private static final String LOG_TAG = "GeoJsonParser";

    // Feature object type
    private static final String FEATURE = "Feature";

    // Feature object geometry member
    private static final String FEATURE_GEOMETRY = "geometry";

    // Feature object id member
    private static final String FEATURE_ID = "id";

    // FeatureCollection type
    private static final String FEATURE_COLLECTION = "FeatureCollection";

    // FeatureCollection features array member
    private static final String FEATURE_COLLECTION_ARRAY = "features";

    // Geometry coordinates member
    private static final String GEOMETRY_COORDINATES_ARRAY = "coordinates";

    // GeometryCollection type
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";

    // GeometryCollection geometries array member
    private static final String GEOMETRY_COLLECTION_ARRAY = "geometries";

    // Coordinates for bbox
    private static final String BOUNDING_BOX = "bbox";

    private static final String PROPERTIES = "properties";

    private static final String POINT = "Point";

    private static final String MULTIPOINT = "MultiPoint";

    private static final String LINESTRING = "LineString";

    private static final String MULTILINESTRING = "MultiLineString";

    private static final String POLYGON = "Polygon";

    private static final String MULTIPOLYGON = "MultiPolygon";

    private final JSONObject mGeoJsonFile;

    private final ArrayList<GeoJsonFeature2> mGeoJsonFeatures;

    private LatLngBounds mBoundingBox;


    /**
     * Creates a new GeoJsonParser
     *
     * @param geoJsonFile GeoJSON file to parse
     */
    /* package */ GeoJsonParser(JSONObject geoJsonFile) {
        mGeoJsonFile = geoJsonFile;
        mGeoJsonFeatures = new ArrayList<GeoJsonFeature2>();
        mBoundingBox = null;
        parseGeoJson();
    }

    private static boolean isGeometry(String type) {
        return type.matches(POINT + "|" + MULTIPOINT + "|" + LINESTRING + "|" + MULTILINESTRING +
                "|" + POLYGON + "|" + MULTIPOLYGON + "|" + GEOMETRY_COLLECTION);
    }

    /**
     * Parses a single GeoJSON feature which contains a geometry and properties member both of
     * which can be null. Also parses the bounding box and id members of the feature if they exist.
     *
     * @param geoJsonFeature feature to parse
     * @return GeoJsonFeature object
     */
    private static GeoJsonFeature2 parseFeature(JSONObject geoJsonFeature) {
        String id = null;
        LatLngBounds boundingBox = null;
        GeoJsonGeometry2 geometry = null;
        HashMap<String, String> properties = new HashMap<String, String>();

        try {
            if (geoJsonFeature.has(FEATURE_ID)) {
                id = geoJsonFeature.getString(FEATURE_ID);
            }
            if (geoJsonFeature.has(BOUNDING_BOX)) {
                boundingBox = parseBoundingBox(geoJsonFeature.getJSONArray(BOUNDING_BOX));
            }
            if (geoJsonFeature.has(FEATURE_GEOMETRY) && !geoJsonFeature.isNull(FEATURE_GEOMETRY)) {
                geometry = parseGeometry(geoJsonFeature.getJSONObject(FEATURE_GEOMETRY));
            }
            if (geoJsonFeature.has(PROPERTIES) && !geoJsonFeature.isNull(PROPERTIES)) {
                properties = parseProperties(geoJsonFeature.getJSONObject("properties"));
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Feature could not be successfully parsed " + geoJsonFeature.toString());
            return null;
        }
        return new GeoJsonFeature2(geometry, id, properties, boundingBox);
    }

    /**
     * Parses a bounding box given as a JSONArray of 4 elements in the order of lowest values for
     * all axes followed by highest values. Axes order of a bounding box follows the axes order of
     * geometries.
     *
     * @param coordinates array of 4 coordinates
     * @return LatLngBounds containing the coordinates of the bounding box
     * @throws JSONException if the bounding box could not be parsed
     */
    private static LatLngBounds parseBoundingBox(JSONArray coordinates) throws JSONException {
        // Lowest values for all axes
        LatLng southWestCorner = new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
        // Highest value for all axes
        LatLng northEastCorner = new LatLng(coordinates.getDouble(3), coordinates.getDouble(2));
        return new LatLngBounds(southWestCorner, northEastCorner);
    }

    /**
     * Parses a single GeoJSON geometry object containing a coordinates array or a geometries array
     * if it has type GeometryCollection
     *
     * @param geoJsonGeometry geometry object to parse
     * @return GeoJsonGeometry object
     */
    private static GeoJsonGeometry2 parseGeometry(JSONObject geoJsonGeometry) {
        try {

            String geometryType = geoJsonGeometry.getString("type");

            JSONArray geometryArray;
            if (geometryType.equals(GEOMETRY_COLLECTION)) {
                // GeometryCollection
                geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COLLECTION_ARRAY);
            } else if (isGeometry(geometryType)) {
                geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COORDINATES_ARRAY);
            } else {
                // No geometries or coordinates array
                return null;
            }
            return createGeometry(geometryType, geometryArray);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Converts a GeoJsonGeometry object into a GeoJsonFeature object. A geometry object has no ID,
     * properties or bounding box so it is set to null.
     *
     * @param geoJsonGeometry Geometry object to convert into a Feature object
     * @return new Feature object
     */
    private static GeoJsonFeature2 parseGeometryToFeature(JSONObject geoJsonGeometry) {
        GeoJsonGeometry2 geometry = parseGeometry(geoJsonGeometry);
        if (geometry != null) {
            return new GeoJsonFeature2(geometry, null, new HashMap<String, String>(), null);
        }
        Log.w(LOG_TAG, "Geometry could not be parsed");
        return null;

    }

    /**
     * Parses the properties of a GeoJSON feature into a hashmap
     *
     * @param properties GeoJSON properties member
     * @return hashmap containing property values
     * @throws JSONException if the properties could not be parsed
     */
    private static HashMap<String, String> parseProperties(JSONObject properties)
            throws JSONException {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        Iterator propertyKeys = properties.keys();
        while (propertyKeys.hasNext()) {
            String key = (String) propertyKeys.next();
            propertiesMap.put(key, properties.getString(key));
        }
        return propertiesMap;
    }

    /**
     * Creates a GeoJsonGeometry object from the given type of geometry and its coordinates or
     * geometries array
     *
     * @param geometryType  type of geometry
     * @param geometryArray coordinates or geometries of the geometry
     * @return GeoJsonGeometry object
     * @throws JSONException if the coordinates or geometries could be parsed
     */
    private static GeoJsonGeometry2 createGeometry(String geometryType, JSONArray geometryArray)
            throws JSONException {
        if (geometryType.equals(POINT)) {
            return createPoint(geometryArray);
        } else if (geometryType.equals(MULTIPOINT)) {
            return createMultiPoint(geometryArray);
        } else if (geometryType.equals(LINESTRING)) {
            return createLineString(geometryArray);
        } else if (geometryType.equals(MULTILINESTRING)) {
            return createMultiLineString(geometryArray);
        } else if (geometryType.equals(POLYGON)) {
            return createPolygon(geometryArray);
        } else if (geometryType.equals(MULTIPOLYGON)) {
            return createMultiPolygon(geometryArray);
        } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
            return createGeometryCollection(geometryArray);
        }
        return null;
    }

    /**
     * Creates a new GeoJsonPoint object
     *
     * @param coordinates array containing the coordinates for the GeoJsonPoint
     * @return GeoJsonPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonPoint2 createPoint(JSONArray coordinates) throws JSONException {
        return new GeoJsonPoint2(parseCoordinate(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiPoint object containing an array of GeoJsonPoint objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiPoint
     * @return GeoJsonMultiPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiPoint2 createMultiPoint(JSONArray coordinates) throws JSONException {
        ArrayList<GeoJsonPoint2> geoJsonPoints = new ArrayList<GeoJsonPoint2>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPoints.add(createPoint(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPoint2(geoJsonPoints);
    }

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates array containing the coordinates for the GeoJsonLineString
     * @return GeoJsonLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonLineString2 createLineString(JSONArray coordinates) throws JSONException {
        return new GeoJsonLineString2 (parseCoordinatesArray(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiLineString object containing an array of GeoJsonLineString objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiLineString
     * @return GeoJsonMultiLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiLineString2 createMultiLineString(JSONArray coordinates)
            throws JSONException {
        ArrayList<GeoJsonLineString2> geoJsonLineStrings = new ArrayList<GeoJsonLineString2>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonLineStrings.add(createLineString(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiLineString2(geoJsonLineStrings);
    }

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates array containing the coordinates for the GeoJsonPolygon
     * @return GeoJsonPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonPolygon2 createPolygon(JSONArray coordinates) throws JSONException {
        return new GeoJsonPolygon2(parseCoordinatesArrays(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiPolygon object containing an array of GeoJsonPolygon objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiPolygon
     * @return GeoJsonPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiPolygon2 createMultiPolygon(JSONArray coordinates)
            throws JSONException {
        ArrayList<GeoJsonPolygon2> geoJsonPolygons = new ArrayList<GeoJsonPolygon2>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPolygons.add(createPolygon(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPolygon2(geoJsonPolygons);
    }

    /**
     * Creates a new GeoJsonGeometryCollection object containing an array of GeoJsonGeometry
     * objects
     *
     * @param geometries array containing the geometries for the GeoJsonGeometryCollection
     * @return GeoJsonGeometryCollection object
     * @throws JSONException if geometries cannot be parsed
     */
    private static GeoJsonGeometryCollection2 createGeometryCollection(JSONArray geometries)
            throws JSONException {
        ArrayList<GeoJsonGeometry2> geometryCollectionElements
                = new ArrayList<GeoJsonGeometry2>();

        for (int i = 0; i < geometries.length(); i++) {
            JSONObject geometryElement = geometries.getJSONObject(i);
            GeoJsonGeometry2 geometry = parseGeometry(geometryElement);
            if (geometry != null) {
                // Do not add geometries that could not be parsed
                geometryCollectionElements.add(geometry);
            }
        }
        return new GeoJsonGeometryCollection2(geometryCollectionElements);
    }

    /**
     * Parses an array containing a coordinate into a LatLng object
     *
     * @param coordinates array containing the GeoJSON coordinate
     * @return LatLng object
     * @throws JSONException if coordinate cannot be parsed
     */
    private static LatLng parseCoordinate(JSONArray coordinates) throws JSONException {
        // GeoJSON stores coordinates as Lng, Lat so we need to reverse
        return new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
    }

    /**
     * Parses an array containing coordinates into an ArrayList of LatLng objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return ArrayList of LatLng objects
     * @throws JSONException if coordinates cannot be parsed
     */
    private static ArrayList<LatLng> parseCoordinatesArray(JSONArray coordinates)
            throws JSONException {
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinate(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    /**
     * Parses an array of arrays containing coordinates into an ArrayList of an ArrayList of LatLng
     * objects
     *
     * @param coordinates array of an array containing the GeoJSON coordinates
     * @return ArrayList of an ArrayList of LatLng objects
     * @throws JSONException if coordinates cannot be parsed
     */
    private static ArrayList<ArrayList<LatLng>> parseCoordinatesArrays(JSONArray coordinates)
            throws JSONException {
        ArrayList<ArrayList<LatLng>> coordinatesArray = new ArrayList<ArrayList<LatLng>>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinatesArray(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    /**
     * Parses the GeoJSON file by type and adds the generated GeoJsonFeature objects to the
     * mFeatures array. Supported GeoJSON types include feature, feature collection and geometry.
     */
    private void parseGeoJson() {
        try {
            GeoJsonFeature2 feature;
            String type = mGeoJsonFile.getString("type");

            if (type.equals(FEATURE)) {
                feature = parseFeature(mGeoJsonFile);
                if (feature != null) {
                    mGeoJsonFeatures.add(feature);
                }
            } else if (type.equals(FEATURE_COLLECTION)) {
                mGeoJsonFeatures.addAll(parseFeatureCollection(mGeoJsonFile));
            } else if (isGeometry(type)) {
                feature = parseGeometryToFeature(mGeoJsonFile);
                if (feature != null) {
                    // Don't add null features
                    mGeoJsonFeatures.add(feature);
                }
            } else {
                Log.w(LOG_TAG, "GeoJSON file could not be parsed.");
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "GeoJSON file could not be parsed.");
        }
    }

    /**
     * Parses the array of GeoJSON features in a given GeoJSON feature collection. Also parses the
     * bounding box member of the feature collection if it exists.
     *
     * @param geoJsonFeatureCollection feature collection to parse
     * @return array of GeoJsonFeature objects
     */
    private ArrayList<GeoJsonFeature2> parseFeatureCollection(JSONObject geoJsonFeatureCollection) {
        JSONArray geoJsonFeatures;
        ArrayList<GeoJsonFeature2> features = new ArrayList<GeoJsonFeature2>();
        try {
            geoJsonFeatures = geoJsonFeatureCollection.getJSONArray(FEATURE_COLLECTION_ARRAY);
            if (geoJsonFeatureCollection.has(BOUNDING_BOX)) {
                mBoundingBox = parseBoundingBox(
                        geoJsonFeatureCollection.getJSONArray(BOUNDING_BOX));
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Feature Collection could not be created.");
            return features;
        }

        for (int i = 0; i < geoJsonFeatures.length(); i++) {
            try {
                JSONObject feature = geoJsonFeatures.getJSONObject(i);
                if (feature.getString("type").equals(FEATURE)) {
                    GeoJsonFeature2 parsedFeature = parseFeature(feature);
                    if (parsedFeature != null) {
                        // Don't add null features
                        features.add(parsedFeature);
                    } else {
                        Log.w(LOG_TAG,
                                "Index of Feature in Feature Collection that could not be created: "
                                        + i);
                    }
                }
            } catch (JSONException e) {
                Log.w(LOG_TAG,
                        "Index of Feature in Feature Collection that could not be created: " + i);
            }
        }
        return features;
    }

    /**
     * Gets the array of GeoJsonFeature objects
     *
     * @return array of GeoJsonFeatures
     */
    /* package */ ArrayList<GeoJsonFeature2> getFeatures() {
        return mGeoJsonFeatures;
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return LatLngBounds object containing bounding box of FeatureCollection, null if no bounding
     * box
     */
    /* package */ LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

}


/**
 * Renders GeoJsonFeature objects onto the GoogleMap as Marker, Polyline and Polygon objects. Also
 * removes GeoJsonFeature objects and redraws features when updated.
 */
/* package */ class GeoJsonRenderer implements Observer {

    private final static int POLYGON_OUTER_COORDINATE_INDEX = 0;

    private final static int POLYGON_INNER_COORDINATE_INDEX = 1;

    private final static Object FEATURE_NOT_ON_MAP = null;

    /**
     * Value is a Marker, Polyline, Polygon or an array of these that have been created from the
     * corresponding key
     */
    private final HashMap<GeoJsonFeature2, Object> mFeatures;

    private final GeoJsonPointStyle2 mDefaultPointStyle;

    private final GeoJsonLineStringStyle2 mDefaultLineStringStyle;

    private final GeoJsonPolygonStyle2 mDefaultPolygonStyle;

    private boolean mLayerOnMap;

    private GoogleMap mMap;





    /**
     * Creates a new GeoJsonRender object
     *
     * @param map map to place GeoJsonFeature objects on
     */
    /* package */ GeoJsonRenderer(GoogleMap map, HashMap<GeoJsonFeature2, Object> features) {
        mMap = map;
        mFeatures = features;
        mLayerOnMap = false;
        mDefaultPointStyle = new GeoJsonPointStyle2();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle2();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle2();

        // Add default styles to features
        for (GeoJsonFeature2 feature : getFeatures()) {
            setFeatureDefaultStyles(feature);
        }
    }

    /**
     * Given a Marker, Polyline, Polygon or an array of these and removes it from the map
     *
     * @param mapObject map object or array of map objects to remove from the map
     */
    private static void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            ((Marker) mapObject).remove();
        } else if (mapObject instanceof Polyline) {
            ((Polyline) mapObject).remove();
        } else if (mapObject instanceof Polygon) {
            ((Polygon) mapObject).remove();
        } else if (mapObject instanceof ArrayList) {
            for (Object mapObjectElement : (ArrayList) mapObject) {
                removeFromMap(mapObjectElement);
            }
        }
    }

    /* package */ boolean isLayerOnMap() {
        return mLayerOnMap;
    }

    /**
     * Gets the GoogleMap that GeoJsonFeature objects are being placed on
     *
     * @return GoogleMap
     */
    /* package */ GoogleMap getMap() {
        return mMap;
    }

    /**
     * Changes the map that GeoJsonFeature objects are being drawn onto. Existing objects are
     * removed from the previous map and drawn onto the new map.
     *
     * @param map GoogleMap to place GeoJsonFeature objects on
     */
    /* package */ void setMap(GoogleMap map) {
        for (GeoJsonFeature2 feature : getFeatures()) {
            redrawFeatureToMap(feature, map);
        }
    }

    /**
     * Adds all of the stored features in the layer onto the map if the layer is not already on the
     * map.
     */
    /* package */ void addLayerToMap() {
        if (!mLayerOnMap) {
            mLayerOnMap = true;
            for (GeoJsonFeature2 feature : getFeatures()) {
                addFeature(feature);
            }
        }
    }

    /**
     * Gets a set containing GeoJsonFeatures
     *
     * @return set containing GeoJsonFeatures
     */
    /* package */ Set<GeoJsonFeature2> getFeatures() {
        return mFeatures.keySet();
    }

    /**
     * Checks for each style in the feature and adds a default style if none is applied
     *
     * @param feature feature to apply default styles to
     */
    private void setFeatureDefaultStyles(GeoJsonFeature2 feature) {
        if (feature.getPointStyle() == null) {
            feature.setPointStyle(mDefaultPointStyle);
        }
        if (feature.getLineStringStyle() == null) {
            feature.setLineStringStyle(mDefaultLineStringStyle);
        }
        if (feature.getPolygonStyle() == null) {
            feature.setPolygonStyle(mDefaultPolygonStyle);
        }
    }

    /**
     * Adds a new GeoJsonFeature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
    /* package */ void addFeature(GeoJsonFeature2 feature) {
        Object mapObject = FEATURE_NOT_ON_MAP;
        setFeatureDefaultStyles(feature);
        if (mLayerOnMap) {
            feature.addObserver(this);

            if (mFeatures.containsKey(feature)) {
                // Remove current map objects before adding new ones
                removeFromMap(mFeatures.get(feature));
            }

            if (feature.hasGeometry()) {
                // Create new map object
                mapObject = addFeatureToMap(feature, feature.getGeometry());
            }
        }
        mFeatures.put(feature, mapObject);
    }

    /**
     * Removes all GeoJsonFeature objects stored in the mFeatures hashmap from the map
     */
    /* package */ void removeLayerFromMap() {
        if (mLayerOnMap) {
            for (GeoJsonFeature2 feature : mFeatures.keySet()) {
                removeFromMap(mFeatures.get(feature));

                feature.deleteObserver(this);
            }
            mLayerOnMap = false;
        }
    }

    /**
     * Removes a GeoJsonFeature from the map if its geometry property is not null
     *
     * @param feature feature to remove from map
     */
    /* package */ void removeFeature(GeoJsonFeature2 feature) {
        // Check if given feature is stored
        if (mFeatures.containsKey(feature)) {
            removeFromMap(mFeatures.remove(feature));
            feature.deleteObserver(this);
        }
    }

    /**
     * Gets the default style used to render GeoJsonPoints
     *
     * @return default style used to render GeoJsonPoints
     */
    /* package */ GeoJsonPointStyle2 getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings
     *
     * @return default style used to render GeoJsonLineStrings
     */
    /* package */ GeoJsonLineStringStyle2 getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Gets the default style used to render GeoJsonPolygons
     *
     * @return default style used to render GeoJsonPolygons
     */
    /* package */ GeoJsonPolygonStyle2 getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Adds a new object onto the map using the GeoJsonGeometry for the coordinates and the
     * GeoJsonFeature for the styles.
     *
     * @param feature  feature to get geometry style
     * @param geometry geometry to add to the map
     */
    private Object addFeatureToMap(GeoJsonFeature2 feature, GeoJsonGeometry2 geometry) {
        String geometryType = geometry.getType();
        if (geometryType.equals("Point")) {
            return addPointToMap(feature.getPointStyle(), (GeoJsonPoint2) geometry);
        } else if (geometryType.equals("LineString")) {
            return addLineStringToMap(feature.getLineStringStyle(),
                    (GeoJsonLineString2) geometry);
        } else if (geometryType.equals("Polygon")) {
            return addPolygonToMap(feature.getPolygonStyle(),
                    (GeoJsonPolygon2) geometry);
        } else if (geometryType.equals("MultiPoint")) {
            return addMultiPointToMap(feature.getPointStyle(),
                    (GeoJsonMultiPoint2) geometry);
        } else if (geometryType.equals("MultiLineString")) {
            return addMultiLineStringToMap(feature.getLineStringStyle(),
                    ((GeoJsonMultiLineString2) geometry));
        } else if (geometryType.equals("MultiPolygon")) {
            return addMultiPolygonToMap(feature.getPolygonStyle(),
                    ((GeoJsonMultiPolygon2) geometry));
        } else if (geometryType.equals("GeometryCollection")) {
            return addGeometryCollectionToMap(feature,
                    ((GeoJsonGeometryCollection2) geometry).getGeometries());
        }
        return null;
    }

    /**
     * Adds a GeoJsonPoint to the map as a Marker
     *
     * @param pointStyle contains relevant styling properties for the Marker
     * @param point      contains coordinates for the Marker
     * @return Marker object created from the given GeoJsonPoint
     */
    private Marker addPointToMap(GeoJsonPointStyle2 pointStyle, GeoJsonPoint2 point) {
        MarkerOptions markerOptions = pointStyle.toMarkerOptions();
        markerOptions.position(point.getCoordinates());
        return mMap.addMarker(markerOptions);
    }

    /**
     * Adds all GeoJsonPoint objects in GeoJsonMultiPoint to the map as multiple Markers
     *
     * @param pointStyle contains relevant styling properties for the Markers
     * @param multiPoint contains an array of GeoJsonPoints
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonPointStyle2 pointStyle,
                                                 GeoJsonMultiPoint2 multiPoint) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (GeoJsonPoint2 geoJsonPoint : multiPoint.getPoints()) {
            markers.add(addPointToMap(pointStyle, geoJsonPoint));
        }
        return markers;
    }

    /**
     * Adds a GeoJsonLineString to the map as a Polyline
     *
     * @param lineStringStyle contains relevant styling properties for the Polyline
     * @param lineString      contains coordinates for the Polyline
     * @return Polyline object created from given GeoJsonLineString
     */
    private Polyline addLineStringToMap(GeoJsonLineStringStyle2 lineStringStyle,
                                        GeoJsonLineString2 lineString) {
        PolylineOptions polylineOptions = lineStringStyle.toPolylineOptions();
        // Add coordinates

        MapsActivity.kyststiInfoMap.put(lineString.getCoordinates(), MapsActivity.descriptionList.get(MapsActivity.indexInDescriptionList));
        MapsActivity.indexInDescriptionList++;

        polylineOptions.addAll(lineString.getCoordinates());



        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds all GeoJsonLineString objects in the GeoJsonMultiLineString to the map as multiple
     * Polylines
     *
     * @param lineStringStyle contains relevant styling properties for the Polylines
     * @param multiLineString contains an array of GeoJsonLineStrings
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(GeoJsonLineStringStyle2 lineStringStyle,
                                                        GeoJsonMultiLineString2 multiLineString) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (GeoJsonLineString2 geoJsonLineString : multiLineString.getLineStrings()) {
            polylines.add(addLineStringToMap(lineStringStyle, geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds a GeoJsonPolygon to the map as a Polygon
     *
     * @param polygonStyle contains relevant styling properties for the Polygon
     * @param polygon      contains coordinates for the Polygon
     * @return Polygon object created from given GeoJsonPolygon
     */
    private Polygon addPolygonToMap(GeoJsonPolygonStyle2 polygonStyle, GeoJsonPolygon2 polygon) {
        PolygonOptions polygonOptions = polygonStyle.toPolygonOptions();
        // First array of coordinates are the outline
        polygonOptions.addAll(polygon.getCoordinates().get(POLYGON_OUTER_COORDINATE_INDEX));
        // Following arrays are holes
        for (int i = POLYGON_INNER_COORDINATE_INDEX; i < polygon.getCoordinates().size();
             i++) {
            polygonOptions.addHole(polygon.getCoordinates().get(i));
        }
        return mMap.addPolygon(polygonOptions);
    }

    /**
     * Adds all GeoJsonPolygon in the GeoJsonMultiPolygon to the map as multiple Polygons
     *
     * @param polygonStyle contains relevant styling properties for the Polygons
     * @param multiPolygon contains an array of GeoJsonPolygons
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(GeoJsonPolygonStyle2 polygonStyle,
                                                    GeoJsonMultiPolygon2 multiPolygon) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (GeoJsonPolygon2 geoJsonPolygon : multiPolygon.getPolygons()) {
            polygons.add(addPolygonToMap(polygonStyle, geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Adds all GeoJsonGeometry objects stored in the GeoJsonGeometryCollection onto the map.
     * Supports recursive GeometryCollections.
     *
     * @param feature           contains relevant styling properties for the GeoJsonGeometry inside
     *                          the GeoJsonGeometryCollection
     * @param geoJsonGeometries contains an array of GeoJsonGeometry objects
     * @return array of Marker, Polyline, Polygons that have been added to the map
     */
    private ArrayList<Object> addGeometryCollectionToMap(GeoJsonFeature2 feature,
                                                         List<GeoJsonGeometry2> geoJsonGeometries) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        for (GeoJsonGeometry2 geometry : geoJsonGeometries) {
            geometries.add(addFeatureToMap(feature, geometry));
        }
        return geometries;
    }

    /**
     * Redraws a given GeoJsonFeature onto the map. The map object is obtained from the mFeatures
     * hashmap and it is removed and added.
     *
     * @param feature feature to redraw onto the map
     */
    private void redrawFeatureToMap(GeoJsonFeature2 feature) {
        redrawFeatureToMap(feature, mMap);
    }

    private void redrawFeatureToMap(GeoJsonFeature2 feature, GoogleMap map) {
        removeFromMap(mFeatures.get(feature));
        mFeatures.put(feature, FEATURE_NOT_ON_MAP);
        mMap = map;
        if (map != null && feature.hasGeometry()) {
            mFeatures.put(feature, addFeatureToMap(feature, feature.getGeometry()));
        }
    }

    /**
     * Update is called if the developer sets a style or geometry in a GeoJsonFeature object
     *
     * @param observable GeoJsonFeature object
     * @param data       null, no extra argument is passed through the notifyObservers method
     */
    public void update(Observable observable, Object data) {
        if (observable instanceof GeoJsonFeature2) {
            GeoJsonFeature2 feature = ((GeoJsonFeature2) observable);
            boolean featureIsOnMap = mFeatures.get(feature) != FEATURE_NOT_ON_MAP;
            if (featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature has been added to the map and its geometry is not null
                // TODO: change this so that we don't add and remove
                redrawFeatureToMap(feature);
            } else if (featureIsOnMap && !feature.hasGeometry()) {
                // Checks if feature is on map and geometry is null
                removeFromMap(mFeatures.get(feature));
                mFeatures.put(feature, FEATURE_NOT_ON_MAP);
            } else if (!featureIsOnMap && feature.hasGeometry()) {
                // Checks if the feature isn't on the map and geometry is not null
                addFeature(feature);
            }
        }
    }
}

/**
 * A GeoJsonPoint geometry contains a single {@link com.google.android.gms.maps.model.LatLng}.
 */
class GeoJsonPoint2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "Point";

    private final LatLng mCoordinates;

    /**
     * Creates a new GeoJsonPoint
     *
     * @param coordinate coordinate of GeoJsonPoint to store
     */
    public GeoJsonPoint2(LatLng coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }
        mCoordinates = coordinate;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the GeoJsonPoint
     *
     * @return coordinates of the GeoJsonPoint
     */
    public LatLng getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}


/**
 * A GeoJsonMultiPoint geometry contains a number of {@link GeoJsonPoint2}s.
 */
class GeoJsonMultiPoint2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "MultiPoint";

    private final List<GeoJsonPoint2> mGeoJsonPoints;

    /**
     * Creates a GeoJsonMultiPoint object
     *
     * @param geoJsonPoints list of GeoJsonPoints to store
     */
    public GeoJsonMultiPoint2(List<GeoJsonPoint2> geoJsonPoints) {
        if (geoJsonPoints == null) {
            throw new IllegalArgumentException("GeoJsonPoints cannot be null");
        }
        mGeoJsonPoints = geoJsonPoints;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of GeoJsonPoints
     *
     * @return list of GeoJsonPoints
     */
    public List<GeoJsonPoint2> getPoints() {
        return mGeoJsonPoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n points=").append(mGeoJsonPoints);
        sb.append("\n}\n");
        return sb.toString();
    }
}




/**
 *
 * A GeoJsonMultiLineString geometry contains a number of {@link GeoJsonLineString2}s.
 */
class GeoJsonMultiLineString2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "MultiLineString";

    private final List<GeoJsonLineString2> mGeoJsonLineStrings;

    /**
     * Creates a new GeoJsonMultiLineString object
     *
     * @param geoJsonLineStrings list of GeoJsonLineStrings to store
     */
    public GeoJsonMultiLineString2(List<GeoJsonLineString2> geoJsonLineStrings) {
        if (geoJsonLineStrings == null) {
            throw new IllegalArgumentException("GeoJsonLineStrings cannot be null");
        }
        mGeoJsonLineStrings = geoJsonLineStrings;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of GeoJsonLineStrings
     *
     * @return list of GeoJsonLineStrings
     */
    public List<GeoJsonLineString2> getLineStrings() {
        return mGeoJsonLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n LineStrings=").append(mGeoJsonLineStrings);
        sb.append("\n}\n");
        return sb.toString();
    }
}

/**
 * A GeoJsonLineString geometry represents a number of connected {@link
 * com.google.android.gms.maps.model.LatLng}s.
 */
class GeoJsonLineString2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "LineString";

    private final List<LatLng> mCoordinates;

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates list of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString2(List<LatLng> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the GeoJsonLineString
     *
     * @return list of coordinates of the GeoJsonLineString
     */
    public List<LatLng> getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}

/**
 * A GeoJsonPolygon geometry contains an array of arrays of {@link com.google.android.gms.maps.model.LatLng}s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */

class GeoJsonPolygon2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "Polygon";

    private final List<? extends List<LatLng>> mCoordinates;

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates list of list of coordinates of GeoJsonPolygon to store
     */
    public GeoJsonPolygon2(
            List<? extends List<LatLng>> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of a list of coordinates of the GeoJsonPolygons
     *
     * @return list of a list of coordinates of the GeoJsonPolygon
     */
    public List<? extends List<LatLng>> getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}


/**
 * A GeoJsonMultiPolygon geometry contains a number of {@link GeoJsonPolygon2}s.
 */
class GeoJsonMultiPolygon2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "MultiPolygon";

    private final List<GeoJsonPolygon2> mGeoJsonPolygons;

    /**
     * Creates a new GeoJsonMultiPolygon
     *
     * @param geoJsonPolygons list of GeoJsonPolygons to store
     */
    public GeoJsonMultiPolygon2(List<GeoJsonPolygon2> geoJsonPolygons) {
        if (geoJsonPolygons == null) {
            throw new IllegalArgumentException("GeoJsonPolygons cannot be null");
        }
        mGeoJsonPolygons = geoJsonPolygons;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets a list of GeoJsonPolygons
     *
     * @return list of GeoJsonPolygons
     */
    public List<GeoJsonPolygon2> getPolygons() {
        return mGeoJsonPolygons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Polygons=").append(mGeoJsonPolygons);
        sb.append("\n}\n");
        return sb.toString();
    }
}


/**
 * A GeoJsonGeometryCollection geometry contains a number of GeoJsonGeometry objects.
 */
class GeoJsonGeometryCollection2 implements GeoJsonGeometry2 {

    private final static String GEOMETRY_TYPE = "GeometryCollection";

    private final List<GeoJsonGeometry2> mGeometries;

    /**
     * Creates a new GeoJsonGeometryCollection object
     *
     * @param geometries array of GeoJsonGeometry objects to add to the GeoJsonGeometryCollection
     */
    public GeoJsonGeometryCollection2(
            List<GeoJsonGeometry2> geometries) {
        if (geometries == null) {
            throw new IllegalArgumentException("Geometries cannot be null");
        }
        mGeometries = geometries;
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the stored GeoJsonGeometry objects
     *
     * @return stored GeoJsonGeometry objects
     */
    public List<GeoJsonGeometry2> getGeometries() {
        return mGeometries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}


/**
 * A class that allows for GeoJsonPoint objects to be styled and for these styles to be translated
 * into a MarkerOptions object. {@see
 * <a href="https://developer.android.com/reference/com/google/android/gms/maps/model/MarkerOptions.html">
 * MarkerOptions docs</a> for more details about the options.}
 */
class GeoJsonPointStyle2 extends Observable implements GeoJsonStyle2 {

    private final static String[] GEOMETRY_TYPE = {"Point", "MultiPoint", "GeometryCollection"};

    private final MarkerOptions mMarkerOptions;


    /**
     * Creates a new PointStyle object
     */
    public GeoJsonPointStyle2() {
        mMarkerOptions = new MarkerOptions();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the alpha of the GeoJsonPoint. This is a value from 0 to 1, where 0 means the marker is
     * completely transparent and 1 means the marker is completely opaque.
     *
     * @return alpha of the GeoJsonPoint
     */
    public float getAlpha() {
        return mMarkerOptions.getAlpha();
    }

    /**
     * Sets the alpha of the GeoJsonPoint. This is a value from 0 to 1, where 0 means the marker is
     * completely transparent and 1 means the marker is completely opaque.
     *
     * @param alpha alpha value of the GeoJsonPoint
     */
    public void setAlpha(float alpha) {
        mMarkerOptions.alpha(alpha);
        styleChanged();
    }

    /**
     * Gets the Anchor U coordinate of the GeoJsonPoint. Normalized to [0, 1], of the anchor from
     * the left edge. This is equivalent to the same U value used in {@link
     * com.google.android.gms.maps.model.MarkerOptions#getAnchorU()}.
     *
     * @return Anchor U coordinate of the GeoJsonPoint
     */
    public float getAnchorU() {
        return mMarkerOptions.getAnchorU();
    }

    /**
     * Gets the Anchor V coordinate of the GeoJsonPoint. Normalized to [0, 1], of the anchor from
     * the top edge. This is equivalent to the same V value used in {@link
     * com.google.android.gms.maps.model.MarkerOptions#getAnchorV()}.
     *
     * @return Anchor V coordinate of the GeoJsonPoint
     */
    public float getAnchorV() {
        return mMarkerOptions.getAnchorV();
    }

    /**
     * Sets the Anchor U and V coordinates of the GeoJsonPoint. The anchor point is specified in
     * the
     * continuous space [0.0, 1.0] x [0.0, 1.0], where (0, 0) is the top-left corner of the image,
     * and (1, 1) is the bottom-right corner. The U &amp; V values are the same U &amp; V values
     * used in
     * {@link com.google.android.gms.maps.model.MarkerOptions#anchor(float, float)} ()}.
     *
     * @param anchorU Anchor U coordinate of the GeoJsonPoint
     * @param anchorV Anchor V coordinate of the GeoJsonPoint
     */
    public void setAnchor(float anchorU, float anchorV) {
        mMarkerOptions.anchor(anchorU, anchorV);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is draggable
     *
     * @return true if GeoJsonPoint is draggable, false if not draggable
     */
    public boolean isDraggable() {
        return mMarkerOptions.isDraggable();
    }

    /**
     * Sets the GeoJsonPoint to be draggable
     *
     * @param draggable true if GeoJsonPoint is draggable, false if not draggable
     */
    public void setDraggable(boolean draggable) {
        mMarkerOptions.draggable(draggable);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is flat
     *
     * @return true if GeoJsonPoint is flat, false if not flat
     */
    public boolean isFlat() {
        return mMarkerOptions.isFlat();
    }

    /**
     * Sets the GeoJsonPoint to be flat
     *
     * @param flat true if GeoJsonPoint is flat, false if not flat
     */
    public void setFlat(boolean flat) {
        mMarkerOptions.flat(flat);
        styleChanged();
    }

    /**
     * Gets a bitmap image for the GeoJsonPoint
     *
     * @return bitmap descriptor for the GeoJsonPoint
     */
    public BitmapDescriptor getIcon() {
        return mMarkerOptions.getIcon();
    }

    /**
     * Sets a bitmap image for the GeoJsonPoint
     *
     * @param bitmap bitmap descriptor for the GeoJsonPoint
     */
    public void setIcon(BitmapDescriptor bitmap) {
        mMarkerOptions.icon(bitmap);
        styleChanged();
    }

    /**
     * Gets the info window anchor U coordinate of the GeoJsonPoint. Normalized to [0, 1], of the
     * info window anchor from the left edge. This is equivalent to the same U value used in {@link
     * com.google.android.gms.maps.model.MarkerOptions#getInfoWindowAnchorU()}.
     *
     * @return info window anchor U coordinate of the GeoJsonPoint
     */
    public float getInfoWindowAnchorU() {
        return mMarkerOptions.getInfoWindowAnchorU();
    }

    /**
     * Gets the info window anchor V coordinate of the GeoJsonPoint. Normalized to [0, 1], of the
     * info window anchor from the top edge. This is equivalent to the same V value used in {@link
     * com.google.android.gms.maps.model.MarkerOptions#getInfoWindowAnchorV()}.
     *
     * @return info window anchor V coordinate of the GeoJsonPoint
     */
    public float getInfoWindowAnchorV() {
        return mMarkerOptions.getInfoWindowAnchorV();
    }

    /**
     * Sets the info window anchor U and V coordinates of the GeoJsonPoint. This is specified in
     * the same coordinate system as the anchor. The U &amp; V values are the same U &amp; V values
     * used in
     * {@link com.google.android.gms.maps.model.MarkerOptions#infoWindowAnchor(float, float)}.
     *
     * @param infoWindowAnchorU info window anchor U coordinate of the GeoJsonPoint
     * @param infoWindowAnchorV info window anchor V coordinate of the GeoJsonPoint
     */
    public void setInfoWindowAnchor(float infoWindowAnchorU, float infoWindowAnchorV) {
        mMarkerOptions.infoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV);
        styleChanged();
    }

    /**
     * Gets the rotation of the GeoJsonPoint in degrees clockwise about the marker's anchor point
     *
     * @return rotation of the GeoJsonPoint
     */
    public float getRotation() {
        return mMarkerOptions.getRotation();
    }


    /**
     * Sets the rotation of the GeoJsonPoint in degrees clockwise about the marker's anchor point
     *
     * @param rotation rotation value of the GeoJsonPoint
     */
    public void setRotation(float rotation) {
        mMarkerOptions.rotation(rotation);
        styleChanged();
    }

    /**
     * Gets the snippet of the GeoJsonPoint
     *
     * @return snippet of the GeoJsonPoint
     */
    public String getSnippet() {
        return mMarkerOptions.getSnippet();
    }

    /**
     * Sets the snippet of the GeoJsonPoint
     *
     * @param snippet sets the snippet value of the GeoJsonPoint
     */
    public void setSnippet(String snippet) {
        mMarkerOptions.snippet(snippet);
        styleChanged();
    }

    /**
     * Gets the title of the GeoJsonPoint
     *
     * @return title of the GeoJsonPoint
     */
    public String getTitle() {
        return mMarkerOptions.getTitle();
    }

    /**
     * Sets the title of the GeoJsonPoint
     *
     * @param title title value of the GeoJsonPoint
     */
    public void setTitle(String title) {
        mMarkerOptions.title(title);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPoint is visible
     *
     * @return true if GeoJsonPoint is visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mMarkerOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonPoint is visible
     *
     * @param visible true if GeoJsonPoint is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mMarkerOptions.visible(visible);
        styleChanged();
    }

    /**
     * Notifies the observers, GeoJsonFeature objects, that the style has changed. Indicates to the
     * GeoJsonFeature that it should check whether a redraw is needed for the feature.
     */
    private void styleChanged() {
        setChanged();
        notifyObservers();
    }

    /**
     * Gets a new MarkerOptions object containing styles for the GeoJsonPoint
     *
     * @return new MarkerOptions object
     */
    public MarkerOptions toMarkerOptions() {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.alpha(mMarkerOptions.getAlpha());
        markerOptions.anchor(mMarkerOptions.getAnchorU(), mMarkerOptions.getAnchorV());
        markerOptions.draggable(mMarkerOptions.isDraggable());
        markerOptions.flat(mMarkerOptions.isFlat());
        markerOptions.icon(mMarkerOptions.getIcon());
        markerOptions.infoWindowAnchor(mMarkerOptions.getInfoWindowAnchorU(),
                mMarkerOptions.getInfoWindowAnchorV());
        markerOptions.rotation(mMarkerOptions.getRotation());
        markerOptions.snippet(mMarkerOptions.getSnippet());
        markerOptions.title(mMarkerOptions.getTitle());
        markerOptions.visible(mMarkerOptions.isVisible());
        return markerOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PointStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
        sb.append(",\n alpha=").append(getAlpha());
        sb.append(",\n anchor U=").append(getAnchorU());
        sb.append(",\n anchor V=").append(getAnchorV());
        sb.append(",\n draggable=").append(isDraggable());
        sb.append(",\n flat=").append(isFlat());
        sb.append(",\n info window anchor U=").append(getInfoWindowAnchorU());
        sb.append(",\n info window anchor V=").append(getInfoWindowAnchorV());
        sb.append(",\n rotation=").append(getRotation());
        sb.append(",\n snippet=").append(getSnippet());
        sb.append(",\n title=").append(getTitle());
        sb.append(",\n visible=").append(isVisible());
        sb.append("\n}\n");
        return sb.toString();
    }
}



/**
 * A class that allows for GeoJsonPolygon objects to be styled and for these styles to be
 * translated into a PolygonOptions object. {@see
 * <a href="https://developer.android.com/reference/com/google/android/gms/maps/model/PolygonOptions.html">
 * PolygonOptions docs</a> for more details about the options.}
 */
class GeoJsonPolygonStyle2 extends Observable implements GeoJsonStyle2 {

    private final static String[] GEOMETRY_TYPE = {"Polygon", "MultiPolygon", "GeometryCollection"};

    private final PolygonOptions mPolygonOptions;

    /**
     * Creates a new PolygonStyle object
     */
    public GeoJsonPolygonStyle2() {
        mPolygonOptions = new PolygonOptions();
    }

    /** {@inheritDoc} */
    @Override
    public String[] getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the fill color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @return fill color of the GeoJsonPolygon
     */
    public int getFillColor() {
        return mPolygonOptions.getFillColor();
    }

    /**
     * Sets the fill color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @param fillColor fill color value of the GeoJsonPolygon
     */
    public void setFillColor(int fillColor) {
        mPolygonOptions.fillColor(fillColor);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPolygon is geodesic
     *
     * @return true if GeoJsonPolygon is geodesic, false if not geodesic
     */
    public boolean isGeodesic() {
        return mPolygonOptions.isGeodesic();
    }

    /**
     * Sets whether the GeoJsonPolygon is geodesic
     *
     * @param geodesic true if GeoJsonPolygon is geodesic, false if not geodesic
     */
    public void setGeodesic(boolean geodesic) {
        mPolygonOptions.geodesic(geodesic);
        styleChanged();
    }

    /**
     * Gets the stroke color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @return stroke color of the GeoJsonPolygon
     */
    public int getStrokeColor() {
        return mPolygonOptions.getStrokeColor();
    }

    /**
     * Sets the stroke color of the GeoJsonPolygon as a 32-bit ARGB color
     *
     * @param strokeColor stroke color value of the GeoJsonPolygon
     */
    public void setStrokeColor(int strokeColor) {
        mPolygonOptions.strokeColor(strokeColor);
        styleChanged();
    }

    /**
     * Gets the stroke width of the GeoJsonPolygon in screen pixels
     *
     * @return stroke width of the GeoJsonPolygon
     */
    public float getStrokeWidth() {
        return mPolygonOptions.getStrokeWidth();
    }

    /**
     * Sets the stroke width of the GeoJsonPolygon in screen pixels
     *
     * @param strokeWidth stroke width value of the GeoJsonPolygon
     */
    public void setStrokeWidth(float strokeWidth) {
        mPolygonOptions.strokeWidth(strokeWidth);
        styleChanged();
    }

    /**
     * Gets the z index of the GeoJsonPolygon
     *
     * @return z index of the GeoJsonPolygon
     */
    public float getZIndex() {
        return mPolygonOptions.getZIndex();
    }

    /**
     * Sets the z index of the GeoJsonPolygon
     *
     * @param zIndex z index value of the GeoJsonPolygon
     */
    public void setZIndex(float zIndex) {
        mPolygonOptions.zIndex(zIndex);
        styleChanged();
    }

    /**
     * Gets whether the GeoJsonPolygon is visible
     *
     * @return true if GeoJsonPolygon is visible, false if not visible
     */
    @Override
    public boolean isVisible() {
        return mPolygonOptions.isVisible();
    }

    /**
     * Sets whether the GeoJsonPolygon is visible
     *
     * @param visible true if GeoJsonPolygon is visible, false if not visible
     */
    @Override
    public void setVisible(boolean visible) {
        mPolygonOptions.visible(visible);
        styleChanged();
    }

    /**
     * Notifies the observers, GeoJsonFeature objects, that the style has changed. Indicates to the
     * GeoJsonFeature that it should check whether a redraw is needed for the feature.
     */
    private void styleChanged() {
        setChanged();
        notifyObservers();
    }

    /**
     * Gets a new PolygonOptions object containing styles for the GeoJsonPolygon
     *
     * @return new PolygonOptions object
     */
    public PolygonOptions toPolygonOptions() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(mPolygonOptions.getFillColor());
        polygonOptions.geodesic(mPolygonOptions.isGeodesic());
        polygonOptions.strokeColor(mPolygonOptions.getStrokeColor());
        polygonOptions.strokeWidth(mPolygonOptions.getStrokeWidth());
        polygonOptions.visible(mPolygonOptions.isVisible());
        polygonOptions.zIndex(mPolygonOptions.getZIndex());
        return polygonOptions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PolygonStyle{");
        sb.append("\n geometry type=").append(Arrays.toString(GEOMETRY_TYPE));
        sb.append(",\n fill color=").append(getFillColor());
        sb.append(",\n geodesic=").append(isGeodesic());
        sb.append(",\n stroke color=").append(getStrokeColor());
        sb.append(",\n stroke width=").append(getStrokeWidth());
        sb.append(",\n visible=").append(isVisible());
        sb.append(",\n z index=").append(getZIndex());
        sb.append("\n}\n");
        return sb.toString();
    }
}