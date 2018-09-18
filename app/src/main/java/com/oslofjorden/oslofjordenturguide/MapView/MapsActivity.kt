package com.oslofjorden.oslofjordenturguide.MapView

import android.Manifest
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextMenu
import android.view.View
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
import com.oslofjorden.oslofjordenturguide.MapView.model.Marker
import com.oslofjorden.oslofjordenturguide.MapView.model.MarkerData
import com.oslofjorden.oslofjordenturguide.MapView.model.PolylineData
import com.oslofjorden.oslofjordenturguide.viewmodels.MapsActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, NoticeDialogListener, LifecycleOwner, ActivityCompat.OnRequestPermissionsResultCallback, AppPurchasedListener {


    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentPosition: LatLng = LatLng(59.903765, 10.699610) // Oslo
    private var currentCameraPosition: CameraPosition? = null
    private lateinit var clickedClusterItem: Marker
    private lateinit var bottomSheetController: BottomSheetController
    private lateinit var mClusterManager: ClusterManager<Marker>
    private var previousPolylineClicked: Polyline? = null
    private var mMap: GoogleMap? = null
    private lateinit var myLocationListener: MyLocationListener

    private var polylineData: PolylineData? = null
    private var markerData: MarkerData? = null
    private var polylinesOnMap: ArrayList<Polyline>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(MapsActivityViewModel(application)::class.java)

        viewModel.mapData.observe(this, Observer {
            when (it) {
                is PolylineData -> polylineData = it
                is MarkerData -> markerData = it
            }

            val currentMapItems = viewModel.currentMapItems.value
            if (polylineData != null && markerData != null) {
                currentMapItems?.let { mapItems -> loadCheckedItems(mapItems) }
            }
        })


        viewModel.inAppPurchased.observe(this, Observer { inAppPurchased ->
            when (inAppPurchased) {
                false -> AdHandler.createAd(this)
                true -> viewModel.removeAd()
            }
        })

        viewModel.currentMapItems.observe(this, Observer { currentMapItems ->
            // When there is a change in the app items we want to reload the items on the map
            when (currentMapItems != null) {

                // loadCheckedItems(currentMapItems!! as BooleanArray)
            }
        })

        viewModel.firstTimeLaunchingApp.observe(this, Observer { firstTimeLaunchingApp ->
            when (firstTimeLaunchingApp) {
                true -> {
                    showWelcomeDialog()
                    viewModel.setInfoMessageShown()
                }
            }
        })

        // Inflate view and obtain an instance of the binding class.
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Assign the component to a property in the binding class.
        binding.setLifecycleOwner(this)
        binding.viewmodel = viewModel

        removeSplashScreen()

        val purchasedListener = InAppPurchaseHandler(this, this, this)

        initMap()

        initToolbar()

        showBottomSheetLoading()

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

    fun removeSplashScreen() {
        //Removes the oslofjorden picture that is used as splash screen
        window.setBackgroundDrawableResource(R.drawable.graybackground)
    }

    fun showBottomSheetLoading() {
        bottomSheetController = BottomSheetController(bottom_sheet, this)
        bottomSheetController.setLoadingText()
        bottomSheetController.expandBottomSheet()
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

    private fun initLayersButton() {
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
        this.polylinesOnMap?.forEach { it.remove() }
    }

    private fun addMarkersToMap(markerType: MarkerTypes) {
        val markersToAdd = markerData?.markers?.filter { marker -> marker.markerTypes.contains(markerType) }
        markersToAdd?.let {
            mClusterManager.addItems(markersToAdd)
        }
    }

    private fun addPolylines() {
        val polylineData = polylineData
        polylineData?.polylines?.forEachIndexed { index, polyline ->

            val polyline = mMap?.addPolyline(polyline.options)
            polyline?.tag = polylineData.polylines[index]

            // Store the polylines currently on the map to be able to remove them later
            polyline?.let { this.polylinesOnMap?.add(it) }
        }
    }


    private fun initToolbar() {
        setToolbar()
        initLayersButton()
    }

    private fun setToolbar() {
        setSupportActionBar(my_toolbar)
        supportActionBar?.title = getString(R.string.app_name)
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


        mMap?.setOnPolylineClickListener { polyline: Polyline ->
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
        val description = (previousPolylineClicked?.tag as com.oslofjorden.oslofjordenturguide.MapView.model.Polyline?)?.description
        previousPolylineClicked?.color = SelectPolylineColor.setPolylineColor(description ?: "")
    }

    private fun showWelcomeDialog() {
        //Show the welcome message to the user
        val welcomeDialog = WelcomeDialog()
        welcomeDialog.show(supportFragmentManager, "test")
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
        Log.i(TAG, "onDialogNegativeClick: gjør ingenting")
    }


    companion object {
        var TAG = "TAG"

    }

}