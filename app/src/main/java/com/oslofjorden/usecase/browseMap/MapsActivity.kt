package com.oslofjorden.usecase.browseMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.oslofjorden.R
import com.oslofjorden.databinding.ActivityMainBinding
import com.oslofjorden.model.*
import com.oslofjorden.permissions.PermissionUtils
import com.oslofjorden.usecase.chooseMapData.ChooseMapInfoDialog
import com.oslofjorden.usecase.chooseMapData.MapDataChangedListener
import com.oslofjorden.usecase.removeAds.AdHandler
import com.oslofjorden.usecase.welcomeUser.WelcomeDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet.*
import org.jetbrains.anko.longToast

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapDataChangedListener, LifecycleOwner, ActivityCompat.OnRequestPermissionsResultCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var clickedClusterItem: Marker
    private lateinit var bottomSheetController: BottomSheetController
    private var clusterManager: ClusterManager<Marker>? = null
    private var previousPolylineClicked: Polyline? = null
    private var mMap: GoogleMap? = null
    private var polylineData: PolylineData? = null
    private var markerData: MarkerData? = null
    private var polylinesOnMap = ArrayList<Polyline>()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var viewModel: MapsActivityViewModel

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFirebaseAnalytics()
        setupViewModel()
        removeSplashScreen()
        setToolbar()
        initMap()
        showBottomSheetLoading()
        observeViewModelValues()
        initButtons()
    }

    private fun initFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    @SuppressLint("MissingPermission")
    private fun initButtons() {
        onofflocationbutton.setOnClickListener {
            if (!hasPermission()) {
                requestPermission()
                return@setOnClickListener
            } else {

                if (viewModel.locationEnabled.value!!) {
                    disableLocationUpdates(viewModel, mMap)
                } else {
                    enableLocationUpdates()
                }
            }
        }

        buyButton.setOnClickListener {
            viewModel.purchase(this)
        }

        layersButton.setOnClickListener {
            // Show the choose map info dialog
            showMapInfoDialog(viewModel.currentMapItems)
        }
    }

    @SuppressLint("MissingPermission")
    private fun observeViewModelValues() {
        observeMapData()
        observeHasPurchasedRemovedAds()
        observeSelectedMapItems()
        observeFirstTimeLaunchingApp()
        observeCurrentLocation()
        observeInAppPurchaseStatus()
    }

    private fun observeInAppPurchaseStatus() {
        viewModel.inAppPurchaseStatus.observeOnce(this, Observer { statusMessage ->
            longToast(statusMessage)
        })
    }

    @SuppressLint("MissingPermission")
    private fun observeCurrentLocation() {
        viewModel.currentLocation.observe(this, Observer { currentLocation ->
            currentLocation?.let {
                updateCameraPosition(currentLocation)
                setLocationDot(mMap)
            }
        })
    }

    private fun observeFirstTimeLaunchingApp() {
        viewModel.firstTimeLaunchingApp.observe(this, Observer { firstTimeLaunchingApp ->
            when (firstTimeLaunchingApp) {
                true -> {
                    showWelcomeDialog()
                    viewModel.setWelcomeDialogShown()
                }
            }
        })
    }

    private fun observeSelectedMapItems() {
        viewModel.currentMapItems.observe(this, Observer { currentMapItems ->
            // When there is a change in the app items we want to reload the items on the map
            currentMapItems?.let { _ ->
                clusterManager?.let {
                    loadCheckedItems(currentMapItems)
                }
            }
        })
    }

    private fun observeHasPurchasedRemovedAds() {
        viewModel.hasPurchasedRemoveAds.observe(this, Observer { inAppPurchased ->
            when (inAppPurchased) {
                false -> AdHandler.createAd(this)
                // when inAppPurchased is true, the ad is removed by itself by data binding.
            }
        })
    }

    private fun observeMapData() {
        viewModel.mapData.observe(this, Observer {
            when (it) {
                is PolylineData -> polylineData = it
                is MarkerData -> markerData = it
            }

            // The polylines and markers are finished loading
            if (polylineData != null && markerData != null) {
                addDataToMap()
            }
        })
    }

    private fun addDataToMap() {
        viewModel.currentMapItems.value?.let { mapItems ->
            loadCheckedItems(mapItems)
            bottomSheetController.finishLoading()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun setLocationDot(mMap: GoogleMap?) {
        mMap?.isMyLocationEnabled = true
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun disableLocationUpdates(viewModel: MapsActivityViewModel, mMap: GoogleMap?) {
        viewModel.disableLocationUpdates()
        mMap?.isMyLocationEnabled = false
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun enableLocationUpdates() {
        viewModel.getLocationUpdates()
    }

    private fun setupViewModel() {
        viewModel = MapsActivityViewModel(application)

        // Inflate view and obtain an instance of the binding class.
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this
    }

    private fun removeSplashScreen() = window.setBackgroundDrawableResource(R.drawable.graybackground)

    private fun showBottomSheetLoading() {
        bottomSheetController = BottomSheetController(bottom_sheet, this)
        bottomSheetController.setLoadingText()
        bottomSheetController.expandBottomSheet()
    }

    private fun requestPermission() = PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true)

    private fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // If the request code is something other than what we requested
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            viewModel.getLocationUpdates()
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

    private fun showMapInfoDialog(userChecks: MutableLiveData<BooleanArray>) {
        val mapInfoDialog = ChooseMapInfoDialog()
        val bundle = Bundle()
        bundle.putBooleanArray("userChecks", userChecks.value)

        mapInfoDialog.arguments = bundle
        mapInfoDialog.show(supportFragmentManager, "test")
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_menu, menu)
    }

    private fun loadCheckedItems(checkedList: BooleanArray) {
        clusterManager?.clearItems()

        for (i in checkedList.indices) {
            // Load items if the checkbox was checked
            val type = MarkerTypes.getTypeFromIndex(i)

            if (checkedList[i]) {
                if (type == MarkerTypes.PATHS) {
                    addPolylines()
                } else {
                    addMarkersToMap(type)
                }
                // It was not checked
            } else {
                if (type == MarkerTypes.PATHS) {
                    removePolylines()
                }
            }
        }
    }

    private fun removePolylines() = this.polylinesOnMap.forEach { it.remove() }

    private fun addMarkersToMap(markerType: MarkerTypes) {
        val markersToAdd = markerData?.markers?.filter { marker -> marker.markerTypes.contains(markerType) }
        markersToAdd?.let {
            clusterManager?.addItems(markersToAdd)
            clusterManager?.cluster()
        }
    }

    private fun addPolylines() {
        val polylineData = polylineData
        polylineData?.polylines?.values?.forEach { polyline ->

            val addedPolyline = mMap?.addPolyline(polyline.options)
            addedPolyline?.tag = polylineData.polylines[polyline.options.points]

            // Store the polylines currently on the map to be able to remove them later
            addedPolyline?.let { this.polylinesOnMap.add(it) }
        }
    }

    private fun setToolbar() {
        setSupportActionBar(my_toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        my_toolbar.setTitleTextColor(Color.WHITE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setInitialZoomLevel()

        setUpClusterer(googleMap)

        // enable zoom buttons, and remove toolbar when clicking on markers
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isCompassEnabled = true

        googleMap.setOnPolylineClickListener { polyline: Polyline ->
            logPolylineTapped(polyline)

            setOriginalPolylineColor()

            polyline.color = Color.BLACK

            bottomSheetController.setPolylineContent(polyline)
            bottomSheetController.expandBottomSheet()

            previousPolylineClicked = polyline
        }

        googleMap.setOnMapClickListener {
            bottomSheetController.hideBottomSheet()
            setOriginalPolylineColor()
        }
    }

    private fun logPolylineTapped(polyline: Polyline) {
        Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, (polyline.tag as OslofjordenPolyline).title)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, this)
        }
    }

    private fun setInitialZoomLevel() {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.currentLocation.value, 13f))
    }

    private fun setOriginalPolylineColor() {
        previousPolylineClicked?.color = polylineData?.polylines?.get(previousPolylineClicked?.points)?.options!!.color
    }

    private fun showWelcomeDialog() = WelcomeDialog().show(supportFragmentManager, "test")

    private fun updateCameraPosition(coordinates: LatLng) = mMap?.animateCamera(CameraUpdateFactory.newLatLng(coordinates))

    private fun setUpClusterer(googleMap: GoogleMap) {
        clusterManager = ClusterManager(this, googleMap)

        clusterManager?.let { clusterManager ->

            clusterManager.algorithm = PreCachingAlgorithmDecorator(GridBasedAlgorithm())
            clusterManager.renderer = DefaultClusterRenderer(applicationContext, googleMap, this.clusterManager)

            clusterManager.setOnClusterItemClickListener { item ->
                logMarkerTapped(item)
                setOriginalPolylineColor()

                clickedClusterItem = item

                bottomSheetController.setMarkerContent(item)
                bottomSheetController.expandBottomSheet()

                // Navigate to the center of the marker when clicking on it
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(item.position))

                // Do not show the window that usually appears when clicking on a marker
                true
            }

            googleMap.setInfoWindowAdapter(clusterManager.markerManager)

            // Point the map's listeners at the listeners implemented by the cluster manager.
            googleMap.setOnCameraIdleListener(clusterManager)
            googleMap.setOnMarkerClickListener(clusterManager)
        }
    }

    private fun logMarkerTapped(item: Marker) {
        Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, item.title)
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, this)
        }
    }

    override fun onDialogPositiveClick(newMapItems: BooleanArray) = viewModel.setMapItems(newMapItems)

    override fun onDialogNegativeClick() {
        Log.i(TAG, "onDialogNegativeClick: gjør ingenting")
    }

    companion object {
        var TAG = "TAG"
    }
}