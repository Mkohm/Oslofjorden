package com.oslofjorden.oslofjordenturguide.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.newBuilder
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.maps.model.LatLng
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.MapView.InAppPurchaseInteractor
import com.oslofjorden.oslofjordenturguide.MapView.MapsActivity
import com.oslofjorden.oslofjordenturguide.MapView.SingleLiveEvent
import com.oslofjorden.oslofjordenturguide.MapView.data.AndroidLocationProvider
import com.oslofjorden.oslofjordenturguide.MapView.data.LocationInteractor
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerDataRepository
import com.oslofjorden.oslofjordenturguide.MapView.data.MarkerReader
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineReader
import com.oslofjorden.oslofjordenturguide.MapView.data.PolylineRepository
import com.oslofjorden.oslofjordenturguide.MapView.data.SharedPreferencesReader
import com.oslofjorden.oslofjordenturguide.MapView.data.SharedPreferencesRepository
import com.oslofjorden.oslofjordenturguide.MapView.model.MergedData

class MapsActivityViewModel(private val myApplication: Application) : AndroidViewModel(Application()),
    PurchasesUpdatedListener {

    private val markerDataRepository = MarkerDataRepository(MarkerReader(myApplication.applicationContext))
    private val polylineRepository = PolylineRepository(PolylineReader(myApplication.applicationContext))
    private val sharedPreferencesRepository =
        SharedPreferencesRepository(SharedPreferencesReader(myApplication.applicationContext))

    private val locationInteractor = LocationInteractor(AndroidLocationProvider(myApplication.applicationContext))
    private val inAppPurchaseInteractor = InAppPurchaseInteractor
    private val billingClient = newBuilder(myApplication).setListener(this).build()

    val hasPurchasedRemoveAds = MutableLiveData<Boolean>()
    val firstTimeLaunchingApp = MutableLiveData<Boolean>()
    val currentMapItems = MutableLiveData<BooleanArray>()
    val mapData = MediatorLiveData<MergedData?>()
    val currentLocation = MutableLiveData<LatLng>()
    val locationEnabled = MutableLiveData<Boolean>()

    val inAppPurchaseStatus = SingleLiveEvent<String>()

    init {
        loadMapData()
        sharedPreferencesRepository.getHasPurchasedRemoveAds(hasPurchasedRemoveAds)
        sharedPreferencesRepository.isFirstTimeLaunchingApp(firstTimeLaunchingApp)
        sharedPreferencesRepository.getCurrentMapItems(currentMapItems)
        currentLocation.value = LatLng(59.903765, 10.699610) // Oslo

        locationEnabled.value = false

        inAppPurchaseInteractor.startGooglePlayConnection(billingClient)
    }

    fun removeAd() {
        sharedPreferencesRepository.setHasPurchasedRemoveAds(hasPurchasedRemoveAds)
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
        locationInteractor.enableLocationUpdates(currentLocation, locationEnabled)
    }

    fun disableLocationUpdates() {
        locationInteractor.disableLocationUpdates(locationEnabled)
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        when (responseCode) {
            BillingResponse.ITEM_ALREADY_OWNED -> removeAd()
            BillingResponse.OK -> removeAd()
            BillingResponse.DEVELOPER_ERROR -> showErrorMessage(getString(R.string.developer_error))
            BillingResponse.ERROR -> showErrorMessage(getString(R.string.error))
            BillingResponse.BILLING_UNAVAILABLE -> showErrorMessage(getString(R.string.billing_unavailable))
            BillingResponse.FEATURE_NOT_SUPPORTED -> showErrorMessage(getString(R.string.feature_not_supported))
            BillingResponse.SERVICE_DISCONNECTED -> showErrorMessage(getString(R.string.service_disconnected))
            BillingResponse.ITEM_UNAVAILABLE -> showErrorMessage(getString(R.string.item_unavailable))
            BillingResponse.USER_CANCELED -> showErrorMessage(getString(R.string.user_cancelled))
            BillingResponse.ITEM_NOT_OWNED -> showErrorMessage(getString(R.string.item_not_owned))
            BillingResponse.SERVICE_UNAVAILABLE -> showErrorMessage(getString(R.string.service_unavailable))
        }
    }

    private fun showErrorMessage(snackMessage: String?) {
        inAppPurchaseStatus.value = snackMessage
    }

    fun purchase(activity: MapsActivity) {
        inAppPurchaseInteractor.queryPurchases(activity)
    }

    private fun getString(id: Int): String? {
        return myApplication.resources.getString(id)
    }
}
