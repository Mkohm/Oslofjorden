package com.oslofjorden.oslofjordenturguide.usecase.browseMap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.newBuilder
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.maps.model.LatLng
import com.oslofjorden.R
import com.oslofjorden.oslofjordenturguide.usecase.removeAds.InAppPurchaseInteractor
import com.oslofjorden.oslofjordenturguide.data.AndroidLocationProvider
import com.oslofjorden.oslofjordenturguide.data.LocationInteractor
import com.oslofjorden.oslofjordenturguide.data.MarkerDataRepository
import com.oslofjorden.oslofjordenturguide.data.MarkerReaderFromGPX
import com.oslofjorden.oslofjordenturguide.data.PolylineReader
import com.oslofjorden.oslofjordenturguide.data.PolylineRepository
import com.oslofjorden.oslofjordenturguide.data.SharedPreferencesReader
import com.oslofjorden.oslofjordenturguide.data.SharedPreferencesRepository
import com.oslofjorden.oslofjordenturguide.model.MergedData

class MapsActivityViewModel(private val myApplication: Application) : AndroidViewModel(Application()),
    PurchasesUpdatedListener {

    private val markerDataRepository = MarkerDataRepository(MarkerReaderFromGPX(myApplication.applicationContext))
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

    private fun persistInAppPurchaseDone() {
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
            BillingResponse.ITEM_ALREADY_OWNED -> persistInAppPurchaseDone()
            BillingResponse.OK -> persistInAppPurchaseDone()
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