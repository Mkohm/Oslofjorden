package com.oslofjorden.oslofjordenturguide.MapView


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.view.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.oslofjorden.oslofjordenturguide.R


//TODO: helgeroaferfgene link meld inn - fikset i fil, fix animation of infobar, back faast after removes kyststier
//Challenge in walking kyststier
//Infobar material design
//Menu - hamburgermenu
//Rapporter feil/tur/hvasomhelst
//instillinger oppdateringshastighet ++
//I nærheten
//Database implementation and search
//lagrer ikke


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        NoticeDialogListener, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private val PERMISSIONS_OK = 1
    private var currentPosition: LatLng = LatLng(59.903765, 10.699610) // Oslo
    private var currentCameraPosition: CameraPosition? = null
    private lateinit var clickedClusterItem: MarkerData

    private lateinit var bottomSheetController: BottomSheetController
    private var addInfoToMap: AddInfoToMap = AddInfoToMap()
    private lateinit var mClusterManager: ClusterManager<MarkerData>
    private var previousPolylineClicked: Polyline? = null
    private var defaultCheckedItems: BooleanArray = createDefaultCheckedArray()
    private var polylineData: List<PolylineData> = ArrayList()
    private var markerData: List<MarkerData>? = null
    private var dataLoaded = false
    private var mMap: GoogleMap? = null
    private val polylinesOnMap = ArrayList<Polyline>()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private var locationTrackingEnabled = false
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize lateinits
        bottomSheetController = BottomSheetController(findViewById<View>(R.id.bottom_sheet) as LinearLayout, this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //The first time the user launches the app, this message will be shown
        showInfomessageToUserIfFirstTime()

        //Removes the oslofjorden picture
        window.setBackgroundDrawableResource(R.drawable.graybackground)

        initMap()

        initToolbar()


        onofflocationbutton.isClickable = true
        onofflocationbutton.setOnClickListener {
            if (!locationTrackingEnabled) {
                enableMyLocation()
            } else {
                disableMyLocation()
                onofflocationbutton.setImageResource(R.drawable.ic_location_off)
            }
        }

    }


    fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap?.isMyLocationEnabled = true
            locationTrackingEnabled = true
            onofflocationbutton.setImageResource(R.drawable.ic_location_on)


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, this)


        }
    }


    /**
     * Is called when the result of the requestPermission() is ready
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // If the request code is something other than what we requested
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Display the missing permission error dialog when the fragments resume.
        }

    }


    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initLocationButton() {
        val onOffLocationButton = findViewById<View>(R.id.onofflocationbutton) as ImageButton
        onOffLocationButton.setImageResource(R.drawable.ic_location_off)
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
            if (checkedList[i]) {

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


    override fun onResume() {
        super.onResume()

        if (!dataLoaded) {
            addInfoToMap = AddInfoToMap()
            addInfoToMap.execute()
        }
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


        mMap?.setOnPolylineClickListener { polyline ->
            // Set the color of the previous polyline back to what it was
            setOriginalPolylineColor()

            polyline.color = Color.BLACK


            bottomSheetController.setPolylineContent(polyline)
            bottomSheetController.expandBottomSheet()


            previousPolylineClicked = polyline
        }

        mMap?.setOnMapClickListener { latLng ->
            bottomSheetController.hideBottomSheet()
            setOriginalPolylineColor()
        }
    }

    private fun disableMyLocation() {
        locationManager.removeUpdates(this)
        locationTrackingEnabled = false
    }

    private fun setOriginalPolylineColor() {
        val description = (previousPolylineClicked?.tag as PolylineData?)?.description
        previousPolylineClicked?.color = SelectPolylineColor.setPolylineColor(description ?: "")
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
        mClusterManager.algorithm = PreCachingAlgorithmDecorator(GridBasedAlgorithm())
        mClusterManager.renderer = OwnIconRendered(applicationContext, mMap, mClusterManager)


        mClusterManager.setOnClusterItemClickListener { item ->
            clickedClusterItem = item

            bottomSheetController.setMarkerContent(item)
            bottomSheetController.expandBottomSheet()

            // Navigate to the center of the marker when clicking on it
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(item.position))

            // Do not show the window that usually appears when clicking on a marker
            true
        }

        mMap?.setInfoWindowAdapter(mClusterManager.markerManager)

        // Point the map's listeners at the listeners implemented by the cluster manager.
        mMap?.setOnCameraIdleListener(mClusterManager)
        mMap?.setOnMarkerClickListener(mClusterManager)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, checkedItems: BooleanArray) {
        loadCheckedItems(checkedItems)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Log.d(TAG, "onDialogNegativeClick: gjør ingenting")
    }

    private fun setOnClusterItemClickListener() {

    }


    override fun onLocationChanged(location: Location) {
        val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude))
        mMap?.animateCamera(cameraUpdate)

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    private fun getDataFromFilesAndPutInDatastructure() {

        val binaryReader = BinarydataReader(applicationContext, addInfoToMap)
        polylineData = binaryReader.readBinaryData(R.raw.polylines_binary_file)


        val markerReader = MarkerReader(applicationContext, addInfoToMap)
        markerData = markerReader.readMarkers()

        setOnClusterItemClickListener()

    }

    internal inner class AddInfoToMap : AsyncTask<Void?, Void?, Void?>() {

        override fun onPreExecute() {

            //   bottomSheetController.expandBottomSheet()
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


            bottomSheetController.finishLoading()


            layers.isClickable = true
            layers.isEnabled = true
            layers.setImageResource(R.drawable.ic_layers_white_24dp)
            loading.visibility = View.GONE

        }
    }

    companion object {
        var TAG = "TAG"

    }

}