package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.maps.model.LatLng
import com.oslofjorden.oslofjordenturguide.MapView.InAppPurchaseInteractor
import com.oslofjorden.oslofjordenturguide.MapView.MapsActivity
import com.oslofjorden.oslofjordenturguide.MapView.data.*
import com.oslofjorden.oslofjordenturguide.MapView.model.MergedData

class MapsActivityViewModel(application: Application) : AndroidViewModel(Application()), PurchasesUpdatedListener {

    private val markerDataRepository = MarkerDataRepository(MarkerReader(application.applicationContext))
    private val polylineRepository = PolylineRepository(PolylineReader(application.applicationContext))
    private val sharedPreferencesRepository = SharedPreferencesRepository(SharedPreferencesReader(application.applicationContext))

    private val locationInteractor = LocationInteractor(AndroidLocationProvider(application.applicationContext))
    private val inAppPurchaseInteractor = InAppPurchaseInteractor
    private val billingClient = BillingClient.newBuilder(application).setListener(this).build()


    val inAppPurchased = MutableLiveData<Boolean>()
    val firstTimeLaunchingApp = MutableLiveData<Boolean>()
    val currentMapItems = MutableLiveData<BooleanArray>()
    val mapData = MediatorLiveData<MergedData?>()
    val currentLocation = MutableLiveData<LatLng>()
    val locationEnabled = MutableLiveData<Boolean>()

    init {
        loadMapData()
        sharedPreferencesRepository.getHasPurchasedRemoveAds(inAppPurchased)
        sharedPreferencesRepository.isFirstTimeLaunchingApp(firstTimeLaunchingApp)
        sharedPreferencesRepository.getCurrentMapItems(currentMapItems)
        currentLocation.value = LatLng(59.903765, 10.699610) // Oslo

        // todo: change to false when location works
        locationEnabled.value = true


        inAppPurchaseInteractor.startGooglePlayConnection(billingClient)
    }

    fun removeAd() {
        sharedPreferencesRepository.setHasPurchasedRemoveAds(inAppPurchased)
    }

    private fun loadMapData() {
        mapData.addSource(markerDataRepository.getMarkers()) {
            if (it != null) {
                mapData.value = it
            }
        }

        mapData.addSource(polylineRepository.getPolylines()) {
            if (it != null) {
                mapData.value = it
            }
        }
    }

    fun setInfoMessageShown() {
        sharedPreferencesRepository.setAppOpenedBefore(firstTimeLaunchingApp)
    }

    fun setMapItems(newMapItems: BooleanArray) {
        sharedPreferencesRepository.setCurrentMapItems(newMapItems, currentMapItems)
    }

    fun getLocationUpdates() {
        locationInteractor.enableLocationUpdates(currentLocation)
    }

    fun disableLocationUpdates() {
        locationInteractor.disableLocationUpdates()
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        when (responseCode) {
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> sharedPreferencesRepository.setHasPurchasedRemoveAds(inAppPurchased)
            BillingClient.BillingResponse.OK -> sharedPreferencesRepository.setHasPurchasedRemoveAds(inAppPurchased)
        }
    }

    fun purchase(activity: MapsActivity) {
        inAppPurchaseInteractor.queryPurchases(activity)
    }

}

