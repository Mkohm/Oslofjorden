package com.oslofjorden.oslofjordenturguide.MapView


import android.Manifest
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.View
import android.widget.ImageButton
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.oslofjorden.R
import com.oslofjorden.databinding.ActivityMainBinding
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData
import com.oslofjorden.oslofjordenturguide.viewmodels.MapsActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, NoticeDialogListener, LifecycleOwner, ActivityCompat.OnRequestPermissionsResultCallback, AppPurchasedListener {


    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentPosition: LatLng = LatLng(59.903765, 10.699610) // Oslo
    private var currentCameraPosition: CameraPosition? = null
    private lateinit var clickedClusterItem: MarkerData
    private lateinit var bottomSheetController: BottomSheetController
    private lateinit var mClusterManager: ClusterManager<MarkerData>
    private var previousPolylineClicked: Polyline? = null
    private var polylineData: List<PolylineData> = ArrayList()
    private var markerData: List<MarkerData>? = null
    private var mMap: GoogleMap? = null
    private val polylinesOnMap = ArrayList<Polyline>()
    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(MapsActivityViewModel(application)::class.java)


        viewModel.markers.observe(this, Observer { markers ->
            // Update UI when the marker changes
            markerData = markers

            updateUI()
        })

        viewModel.polylines.observe(this, Observer { polylines ->
            // todo: what?
            polylineData = polylines!!

            viewModel.enableLayersButton()

            updateUI()
        })


        // Inflate view and obtain an instance of the binding class.
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Assign the component to a property in the binding class.
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        initBottomSheetController()

        //        layers.isClickable = true
        //        layers.isEnabled = true
        //       layers.setImageResource(R.drawable.ic_layers_white_24dp)
        //      loading.visibility = View.GONE

        //The first time the user launches the app, this message will be shown
        showInfomessageToUserIfFirstTime()

        //Removes the oslofjorden picture that is used as splash screen
        window.setBackgroundDrawableResource(R.drawable.graybackground)

        val purchasedListener = InAppPurchaseHandler(this, this, this)

        initMap()

        AdHandler(this)

        initToolbar()

        onofflocationbutton.isClickable = true
        onofflocationbutton.setOnClickListener {

            if (!hasPermission()) {
                requestPermission()
                return@setOnClickListener
            }

            if (!myLocationListener.enabled) {
                myLocationListener.enableMyLocation()
            } else {
                myLocationListener.disableMyLocation()
            }
        }

        buyButton.setOnClickListener {
            purchasedListener.queryPurchases()
        }

    }

    fun initBottomSheetController() {
        bottomSheetController = BottomSheetController(bottom_sheet, this)
        bottomSheetController.setLoadingText()
        bottomSheetController.expandBottomSheet()
    }

    fun updateUI() {
        loadLastStateOfApplication()

    }

    override fun onPurchaseSuccess() {
        adLayout.visibility = View.GONE
    }

    private fun requestPermission() {
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // If the request code is something other than what we requested
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            myLocationListener.enableMyLocation()
            onofflocationbutton.setImageResource(R.drawable.ic_location_on)
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
        layersButton.isClickable = false
        layersButton.isEnabled = false
        layersButton.setImageResource(R.drawable.ic_layers_gray)

        layersButton.setOnClickListener {
            //Show the choose map info dialog
            val mapInfoDialog = ChooseMapInfoDialog()
            mapInfoDialog.show(supportFragmentManager, "test")
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
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

                    addPolylines()

                } else {
                    val type = MarkerTypes.getTypeFromIndex(i)
                    addMarkersToMap(type)
                }

            } else {
                if (i == 0) {

                    removePolylines()
                }
            }
        }
    }

    private fun removePolylines() {
        polylinesOnMap.forEach { it.remove() }
    }

    private fun addMarkersToMap(markerType: MarkerTypes) {
        val markersToAdd = markerData?.filter { marker -> marker.markerTypes.contains(markerType) }
        markersToAdd?.let {
            mClusterManager.addItems(markersToAdd)
        }
    }

    private fun addPolylines() {
        for (i in polylineData.indices) {

            val polyline = mMap?.addPolyline(polylineData[i].options)
            polyline?.tag = polylineData[i]

            // Store the polylines currently on the map to be able to remove them later
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


    // todo: remove?
    override fun onPause() {
        super.onPause()

        currentCameraPosition = mMap?.cameraPosition
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        myLocationListener = MyLocationListener(this, mMap, onofflocationbutton, lifecycle, OnLocationChangedListener { it ->
            // update ui
            val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(it.latitude, it.longitude))
            mMap?.animateCamera(cameraUpdate)
        })

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


    private fun setOriginalPolylineColor() {
        val description = (previousPolylineClicked?.tag as PolylineData?)?.description
        previousPolylineClicked?.color = SelectPolylineColor.setPolylineColor(description ?: "")
    }

    private fun loadLastStateOfApplication() {
        if (loadArray("userChecks", applicationContext).isEmpty()) {
            // Did not find any checked items in shared preferences
            val defaultCheckedItems = createDefaultCheckedArray()
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

        for (i in 0 until 6) {
            defaultChecked[i] = true
        }

        for (i in 7 until 17) {
            defaultChecked[i] = false
        }

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


    companion object {
        var TAG = "TAG"

    }

}