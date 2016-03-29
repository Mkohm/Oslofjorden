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
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.EventLogTags;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Set different markers on different types of items
//Let user choose what type of info to see
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu

//Database implementation and search


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, ResultCallback, CustomTabActivityHelper.ConnectionCallback {
    //For debugging
    private static String TAG = "TAG";



    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;


    float currentZoom;
    LatLng currentPosition;
    CameraPosition currentCameraPosition;
    LatLng currentMapClickPosition;

    boolean infoAddedToMap;
    static boolean addedToDataStructure;

    MyMarkerOptions clickedClusterItem;
    public ClusterManager<MyMarkerOptions> mClusterManager;

    public static GeoJsonLayer2 jsonLayer;

    private boolean infobarUp;

    //If the last location was found, this variable is true, the app then swithches to use lastcameraposition to position the camera
    private boolean foundLastLocation = false;


    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    boolean userHasAnsweredLocationTurnOn = false;
    boolean userAcceptLocation = false;
    //is locationupdates enabled or not
    boolean locationUpdatesSwitch = true;



    public static HashMap<List<LatLng>, String[]> kyststiInfoMap = new HashMap<List<LatLng>, String[]>();
    public static ArrayList<String> descriptionList = new ArrayList<String>();
    public static int indexInDescriptionList = 0;
    public static ArrayList<String> nameList = new ArrayList<String>();
    public static int indexInNameList = 0;

    public static List<MarkerOptions> markersReadyToAdd = new ArrayList<MarkerOptions>();
    public static List<PolylineOptions> polylinesReadyToAdd = new ArrayList<PolylineOptions>();


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
    private boolean backGroundTaskRunning;
    private AddInfoToMap addInfoToMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addedToDataStructure = false;
        infoAddedToMap = false;
        infobarUp = false;
        backGroundTaskRunning = false;





        //Hvis man hadde location fra start av skal den ikke spørre mer
        if (isLocationEnabled(getApplicationContext())){
            userAcceptLocation = true;
        }


        //Set up custom tabs
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);




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


        Log.d(TAG, "onCreate: infoaddedtomap: " + infoAddedToMap);
/*
        if (savedInstanceState != null) {
            addedToDataStructure = savedInstanceState.getBoolean("addedToDataStructure");

        }*/
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
        //outState.putBoolean("addedToDataStructure", addedToDataStructure);


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
    protected void onStop() {

        Log.d(TAG, "onStop: ");

        if (backGroundTaskRunning) {
            addInfoToMap.cancel(true);

        }



        try{
            stopLocationUpdates();
            mGoogleApiClient.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }


        super.onStop();

        customTabActivityHelper.unbindCustomTabsService(this);

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onPause() {

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


        final FloatingActionButton onOffLocationButton = (FloatingActionButton) findViewById(R.id.onofflocationbutton);
        setOnOffLocationButtonStartstate(onOffLocationButton);


        onOffLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationUpdatesSwitch == true) {
                    onOffLocationButton.setImageResource(R.drawable.location_off_64px);
                    Toast.makeText(getApplicationContext(), "Oppdatering av posisjon - av", Toast.LENGTH_SHORT).show();
                    locationUpdatesSwitch = false;



                    //Debugtoast
                    Toast.makeText(getApplicationContext(), "addedtodatastructure: " + addedToDataStructure + " infoaddedtomap: " + infoAddedToMap + " backgroundtaskrunning: " + backGroundTaskRunning, Toast.LENGTH_LONG).show();



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

                Log.i(TAG, "onMapClick: Du klikket på kartet.");

                currentMapClickPosition = latLng;

                int maxY = getDeviceMaxY();


                //Hvis kyststiinfo er oppe, lukk den

                    animateInfobarDown();





            }
        });



    }

    private void setOnOffLocationButtonStartstate(FloatingActionButton onOffLocationButton) {
        //Sets the initial icon depending on current settings
        if (isLocationEnabled(getApplicationContext())){
            onOffLocationButton.setImageResource(R.drawable.location_on_64px);


        } else {
            onOffLocationButton.setImageResource(R.drawable.location_off_64px);
        }
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
        return currentPolylineDescription.contains("Sykkelvei") || currentPolylineDescription.contains("sykkelvei");
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


    public static void addGeoJsonLayerToDataStructure() {
        jsonLayer.addLayerToMap();
    }

    private void setMarkerDescription(String title, String description, TextView txtMarkerDescription) {
        txtMarkerDescription.setMovementMethod(LinkMovementMethod.getInstance());

       String type = "Marker";
       String markerTitle =title;

        //Hvis den ikke er tom og er en url så skal link vises
        if (description != null) {
            String markerDescription = "";
            if (description.matches("http[s]{0,1}:.{0,}")) {

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

    @NonNull
    private String extractUrlFromDescription(URLSpan span) {
        String uriWithDesc = span.getURL();
        uriWithDesc = uriWithDesc.substring(span.getURL().indexOf("http"),span.getURL().length());
        return uriWithDesc;
    }



    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        String uriWithDesc = extractUrlFromDescription(span);

        //May launch this link
        Log.i(TAG, "makeLinkClickable: Gjør link klar for å åpnes.");
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
                String link = "<a href=\"" + urls[0].getURL() + "\"><u>Klikk her for mer info</u></a>";
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

        String descriptionWithoutLink = description.substring(0, description.indexOf("http"));
        SpannableStringBuilder withCustomLinkLayout = new SpannableStringBuilder("Tomt");
        URLSpan[] urls2 = null;

        //If there is a link in the description
        if (urls.length != 0){
            //Create the desired format of textview
            String link = "<a href=\"" + urls[0].getURL() + "\"><u>Klikk her for mer info</u></a>";
            CharSequence formattedText = Html.fromHtml(link);
            withCustomLinkLayout = new SpannableStringBuilder(formattedText);
            urls2 = withCustomLinkLayout.getSpans(0, formattedText.length(), URLSpan.class);
            withCustomLinkLayout.insert(0, title + "\n\n");
        }


        for(URLSpan span : urls2) {
            makeLinkClickable(withCustomLinkLayout, span);
        }
        text.setMovementMethod(LinkMovementMethod.getInstance());

        text.setText(withCustomLinkLayout);
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

            if (addInfoToMap.isCancelled()){
                return;
            }

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



        try {
            if (!infoAddedToMap) {
                Log.d(TAG, "onResume: Try to run bakgrunnsprosess");

                addInfoToMap = new AddInfoToMap();

                Log.d(TAG, "onresume: Info var ikke lagt til");


                Log.d(TAG, "onResume: status er " + addInfoToMap.getStatus());

                if (mMap != null) {
                    mMap.clear();
                    Log.d(TAG, "onResume: Fjerner alt på kartet som eventuelt var der");
                } else {
                    Log.d(TAG, "onResume: Klarte ikke fjerne noe på kartet siden det ikke var noe der. ");
                }


                if (addInfoToMap.getStatus() == AsyncTask.Status.FINISHED) {
                    Log.d(TAG, "onResume: må lage en ny tråd, siden status var FINISHED");
                    addInfoToMap = new AddInfoToMap();
                    addInfoToMap.execute();

                } else if (addInfoToMap.getStatus() == AsyncTask.Status.PENDING) {
                    Log.d(TAG, "onResume: må kjøre, siden status var PENDING");
                    addInfoToMap.execute();


                } else if (addInfoToMap.getStatus() == AsyncTask.Status.RUNNING) {
                    Log.d(TAG, "onResume: Denne bakgrunnsprosess kjører allerede, gjør ingenting.");
                }

                backGroundTaskRunning = true;


            } else {
                Log.d(TAG, "onResume: Info var lastet inn");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onMapReady: Her gikk noe galt under innlastingen.");
            new RuntimeException("skjedde en feil med innlasting");
        }



    }


    class AddInfoToMap extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (addInfoToMap.isCancelled()){
                return;
            }

                TextView loading = (TextView) findViewById(R.id.infobar);
                loading.setVisibility(View.VISIBLE);

                loading.setText("Oslofjorden laster inn kyststier.. De vil poppe opp på kartet ditt snart :)");

                //Denne må ha executet
                animateInfobarUp();
                Log.d(TAG, "onPreExecute: animerer opp");

        }

        @Override
        protected Void doInBackground(Void... params) {


            try {
                if (! addedToDataStructure) {


                    getDataFromFileAndPutInDatastructure();
                    Log.i(TAG, "doInBackground: Lastet inn data til datastrukturen");
                }


            } catch (IOException e) {
                e.printStackTrace();
                new RuntimeException("datainnlasting skjedde en feil");
            } catch (JSONException e) {
                e.printStackTrace();
                new RuntimeException("datainnlasntingsfeil");
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

            //Adds the polylines to the map
            final Iterator<PolylineOptions> iterator = polylinesReadyToAdd.iterator();
            try {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        if (iterator.hasNext() && !infoAddedToMap){
                            if (addInfoToMap.isCancelled()){
                                Log.d(TAG, "run: stopper task");
                                infoAddedToMap = false;
                                backGroundTaskRunning = false;
                                return;
                            }



                            mMap.addPolyline(iterator.next());


                            handler.postDelayed(this, 1);



                          //  Log.d(TAG, "run: kyststi");
                        } else {
                            Log.d(TAG, "run : alle kyststier er lastet inn");
                            infoAddedToMap = true;

                            backGroundTaskRunning = false;

                                Log.d(TAG, "run: animerer ned");
                                animateInfobarDown();



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



            final TextView markerInfo = (TextView) findViewById(R.id.infobar);
            setOnClusterItemClickListener(markerInfo);

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
        jsonLayer = new GeoJsonLayer2(mMap, R.raw.alle_kyststier, MyApplication.getAppContext());

        for (GeoJsonFeature2 feature : jsonLayer.getFeatures()) {
            if (addInfoToMap.isCancelled()){
                return;
            }

            GeoJsonPointStyle2 pointStyle = new GeoJsonPointStyle2();
            GeoJsonLineStringStyle2 stringStyle;

            //Gets the name property from the json file
            pointStyle.setTitle(feature.getProperty("name"));
            feature.setPointStyle(pointStyle);

            //Gets the description property from the json file
            pointStyle.setSnippet(feature.getProperty("description"));

            stringStyle = feature.getLineStringStyle();

            String description = feature.getProperty("description");
            //Hvis det er en kyststi legg til description og navn
            if (feature.getGeometry().getType().equals("LineString")) {

                descriptionList.add(description);

                nameList.add(feature.getProperty("name"));
            }


            stringStyle.setClickable(true);




            feature.setLineStringStyle(stringStyle);
        }

        addGeoJsonLayerToDataStructure();

        addedToDataStructure = true;
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



