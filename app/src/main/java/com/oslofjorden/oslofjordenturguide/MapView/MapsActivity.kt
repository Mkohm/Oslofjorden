package com.oslofjorden.oslofjordenturguide.MapView


import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.gms.location.LocationListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.oslofjorden.oslofjordenturguide.R
import com.oslofjorden.oslofjordenturguide.WebView.CustomTabActivityHelper
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.view.*
import org.json.JSONException
import java.io.IOException


//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu
//Rapporter feil/tur/hvasomhelst
//instillinger oppdateringshastighet ++
//I nærheten
//Database implementation and search
//lagrer ikke


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
        CustomTabActivityHelper.ConnectionCallback, NoticeDialogListener {


    private val PERMISSIONS_OK = 1
    private var currentPosition: LatLng = LatLng(59.903765, 10.699610) // Oslo
    private var currentCameraPosition: CameraPosition? = null
    private lateinit var clickedClusterItem: MarkerData

    private var locationUpdatesSwitch = true
    private lateinit var bottomSheetController: BottomSheetController
    private var addInfoToMap: AddInfoToMap = AddInfoToMap()
    private lateinit var mClusterManager: ClusterManager<MarkerData>
    private var previousPolylineClicked: Polyline? = null
    private var customTabActivityHelper: CustomTabActivityHelper = CustomTabActivityHelper()
    private var defaultCheckedItems: BooleanArray = createDefaultCheckedArray()
    private var polylineData: List<PolylineData> = ArrayList()
    private var markerData: List<MarkerData>? = null
    private var dataLoaded = false
    private var mMap: GoogleMap? = null
    private val polylinesOnMap = ArrayList<Polyline>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize lateinits
        bottomSheetController = BottomSheetController(findViewById<View>(R.id.bottom_sheet) as LinearLayout, this)

        //The first time the user launches the app, this message will be shown
        showInfomessageToUserIfFirstTime()

        //Set up custom tabs
        setUpCustomTabs()

        //Removes the oslofjorden picture
        window.setBackgroundDrawableResource(R.drawable.graybackground)

        initMap()

        initToolbar()
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initLocationButton() {
        val onOffLocationButton = findViewById<View>(R.id.onofflocationbutton) as ImageButton
        onOffLocationButton.setImageResource(R.drawable.ic_location_off)
        locationUpdatesSwitch = false
    }

    private fun initLayersButton() {
        layers.isClickable = false
        layers.isEnabled = false
        layers.setImageResource(R.drawable.ic_layers_gray)

        layers.setOnClickListener {
            //Show the choose map info dialog
            val mapInfoDialog = ChooseMapInfoDialog()
            mapInfoDialog.show(supportFragmentManager, "test")
        }
    }

    fun isGPSEnabled(mContext: Context): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    private fun loadCheckedItems(checkedList: BooleanArray) {
        mClusterManager.clearItems()

        for (i in checkedList.indices) {
            //Load items if the checkbox was checked
            if (checkedList[i] == true) {

                //Kyststier behandles spesielt
                if (i == 0) {

                    //Bare legg til om det ikke var der fra før
                    if (polylinesOnMap.size == 0) {
                        Log.d(TAG, "loadCheckedItems: laster inn kyststier ")
                        addPolylines()
                    }
                } else {
                    val type = MarkerTypes.getTypeFromIndex(i)
                    addMarkersToMap(type)
                }

            } else {
                if (i == 0) {
                    if (polylinesOnMap.size != 0) {
                        removePolylines()
                    }
                }
            }
        }

        //Show them by moving the map a bit

        // todo : remove this workaround
        val c = CameraUpdateFactory.zoomBy(0.001f)
        mMap?.animateCamera(c)
    }

    private fun removePolylines() {
        polylinesOnMap.forEach { it.remove() }
    }

    private fun addMarkersToMap(markerType: MarkerTypes) {
        val toAdd = markerData?.filter { marker -> marker.markerTypes.contains(markerType) }
        mClusterManager.addItems(toAdd)
    }

    private fun addPolylines() {
        //Adds the polylines to the map

        for (i in polylineData.indices) {

            val polyline = mMap?.addPolyline(polylineData[i].options)
            polyline?.tag = polylineData[i]

            // Only add it to the list if the polyline was not null
            polyline?.let { polylinesOnMap.add(it) }

        }
    }

    private fun setUpCustomTabs() {
        customTabActivityHelper.setConnectionCallback(this)
    }

    private fun initToolbar() {
        setToolbar()
        initLayersButton()
        initLocationButton()
        initLoadingSpinner()
    }

    private fun initLoadingSpinner() {
        loading.indeterminateDrawable.setColorFilter(ContextCompat.getColor(applicationContext, R.color.white), PorterDuff.Mode.SRC_IN)
    }

    private fun setToolbar() {
        setSupportActionBar(my_toolbar)
        supportActionBar?.title = "Oslofjorden Båt - og Turguide"
        my_toolbar.setTitleTextColor(Color.WHITE)
    }

    override fun onDestroy() {
        super.onDestroy()
        customTabActivityHelper.setConnectionCallback(null)
    }

    override fun onStart() {
        super.onStart()

        customTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onResume() {
        super.onResume()

        if (!dataLoaded) {
            addInfoToMap = AddInfoToMap()
            addInfoToMap.execute()
        }
    }


    override fun onStop() {
        super.onStop()

        customTabActivityHelper.unbindCustomTabsService(this)
    }

    override fun onPause() {
        super.onPause()

        currentCameraPosition = mMap?.cameraPosition
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()

        goToInitialLocation()

        //enable zoom buttons, and remove toolbar when clicking on markers
        mMap?.uiSettings?.isZoomControlsEnabled = false
        mMap?.uiSettings?.isMapToolbarEnabled = false
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        mMap?.uiSettings?.isCompassEnabled = true


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


        mMap?.setOnPolylineClickListener { polyline ->
            // Set the color of the previous polyline back to what it was
            setOriginalPolylineColor()

            polyline.color = Color.BLACK


            bottomSheetController.setContent(polyline)
            bottomSheetController.expandBottomSheet()


            previousPolylineClicked = polyline
        }

        mMap?.setOnMapClickListener { latLng ->
            bottomSheetController.hideBottomSheet()
            setOriginalPolylineColor()
        }
    }

    private fun setOriginalPolylineColor() {
        val description = (previousPolylineClicked?.tag as PolylineData).description
        previousPolylineClicked?.color = SelectPolylineColor.setPolylineColor(description)
    }

    private fun loadLastStateOfApplication() {
        if (loadArray("userChecks", applicationContext).isEmpty()) {
            //Fant ingen checks på sharedpref
            loadCheckedItems(defaultCheckedItems)
        } else {
            val checkedItems = loadArray("userChecks", applicationContext)
            loadCheckedItems(checkedItems)
        }
    }

    fun loadArray(arrayName: String, mContext: Context): BooleanArray {
        val prefs = mContext.getSharedPreferences(arrayName, 0)

        val size = prefs.getInt(arrayName + "_17", 0)
        val array = BooleanArray(size)
        for (i in 0 until size) {
            Log.d(TAG, "loadArray: " + i + " checked: " + prefs.getBoolean(arrayName + "_" + i, false))
            array[i] = prefs.getBoolean(arrayName + "_" + i, false)
        }
        return array
    }

    private fun createDefaultCheckedArray(): BooleanArray {
        val defaultChecked = BooleanArray(17)
        defaultChecked[0] = true
        defaultChecked[1] = true
        defaultChecked[2] = true
        defaultChecked[3] = true
        defaultChecked[4] = true
        defaultChecked[5] = true
        defaultChecked[6] = true

        defaultChecked[7] = false
        defaultChecked[8] = false
        defaultChecked[9] = false
        defaultChecked[10] = false
        defaultChecked[11] = false
        defaultChecked[12] = false
        defaultChecked[13] = false
        defaultChecked[14] = false
        defaultChecked[15] = false
        defaultChecked[16] = false

        return defaultChecked
    }

    private fun showInfomessageToUserIfFirstTime() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        val firstTimeUserLaunchesApp = sharedPref.getBoolean("firstTimeUserLaunchesApp", true)

        if (firstTimeUserLaunchesApp) {
            //Saving that the user has opened the app before
            val editor = sharedPref.edit()
            editor.putBoolean("firstTimeUserLaunchesApp", false)
            editor.commit()

            //Show message to the user
            val infoDialog = InfoDialog()
            infoDialog.show(supportFragmentManager, "test")

        }
    }

    private fun goToInitialLocation() {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPosition, 13f)
        mMap?.moveCamera(cameraUpdate)
    }

    private fun setUpClusterer() {
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.setAlgorithm(PreCachingAlgorithmDecorator(GridBasedAlgorithm()))
        mClusterManager.setRenderer(OwnIconRendered(applicationContext, mMap, mClusterManager))


        mClusterManager.setOnClusterItemClickListener { item ->
            clickedClusterItem = item

            bottomSheetController.expandBottomSheet()
            bottomSheetController.setMarkerContent(item)

            false
        }

        mMap?.setInfoWindowAdapter(mClusterManager.markerManager)

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap?.setOnCameraIdleListener(mClusterManager)
        mMap?.setOnMarkerClickListener(mClusterManager)
    }


    override fun onLocationChanged(location: Location) {
        val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
        mMap?.animateCamera(cameraUpdate)
    }

    override fun onCustomTabsConnected() {

    }

    override fun onCustomTabsDisconnected() {

    }

    override fun onDialogPositiveClick(dialog: DialogFragment, checkedItems: BooleanArray) {
        loadCheckedItems(checkedItems)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Log.d(TAG, "onDialogNegativeClick: gjør ingenting")
    }

    private fun setOnClusterItemClickListener() {

    }

    @Throws(IOException::class, JSONException::class)
    private fun getDataFromFilesAndPutInDatastructure() {

        val binaryReader = BinarydataReader(applicationContext, addInfoToMap)
        polylineData = binaryReader.readBinaryData(R.raw.polylines_binary_file)


        val markerReader = MarkerReader(applicationContext, addInfoToMap)
        markerData = markerReader.readMarkers()

        setOnClusterItemClickListener()

    }

    internal inner class AddInfoToMap : AsyncTask<Void?, Void?, Void?>() {

        override fun onPreExecute() {

            bottomSheetController.expandBottomSheet()
            bottomSheetController.setLoadingText()
        }

        override fun doInBackground(vararg params: Void?): Void? {

            getDataFromFilesAndPutInDatastructure()

            return null
        }


        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)

            if (mMap != null) {
                loadLastStateOfApplication()
            }

            dataLoaded = true


            bottomSheetController.hideBottomSheet()


            layers.isClickable = true
            layers.isEnabled = true
            layers.setImageResource(R.drawable.ic_layers_white_24dp)
            loading.visibility = View.GONE

        }
    }

    companion object {
        // Request code to use when launching the resolution activity
        val REQUEST_RESOLVE_ERROR = 1001
        val STATE_RESOLVING_ERROR = "resolving_error"
        val DIALOG_ERROR = "dialog_error"
        protected val REQUEST_CHECK_SETTINGS = 0x1
        var TAG = "TAG"
    }
}