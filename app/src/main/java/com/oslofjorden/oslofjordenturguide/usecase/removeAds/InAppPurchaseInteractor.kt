package com.oslofjorden.oslofjordenturguide.usecase.removeAds

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.SkuDetailsParams
import com.oslofjorden.oslofjordenturguide.usecase.broweMap.MapsActivity

// Starts a connection to google play to initiate the app purchaseOk and will receive a callback when a purchaseOk is completed.
object InAppPurchaseInteractor {
    private lateinit var billingClient: BillingClient

    fun startGooglePlayConnection(billingClient: BillingClient) {
        InAppPurchaseInteractor.billingClient = billingClient

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. We will now enable the buy button
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                billingClient.startConnection(this)
            }
        })
    }

    fun queryPurchases(activity: MapsActivity) {
        val skuList = ArrayList<String>()
        skuList.add("com.oslofjorden.removeads")

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
            // Process the result.
            val flowParams = BillingFlowParams.newBuilder().setSku("com.oslofjorden.removeads").setType(BillingClient.SkuType.INAPP).build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
        }
    }
}
