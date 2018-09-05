package com.oslofjorden.oslofjordenturguide.MapView

import android.content.Context
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_maps.*

// Handles the purchaseOk of the in app purchaseOk.
// Starts a connection to google play to initiate the app purchaseOk and will receive a callback when a purchaseOk is completed.
// It will then notify the Mapsactivity that is listening for this class.
class InAppPurchaseHandler(private val inAppPurchasedListener: AppPurchasedListener, val context: Context, val activity: MapsActivity) : PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    init {
        startGooglePlayConnection()
    }


    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        when (responseCode) {
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> purchaseOk()
            BillingClient.BillingResponse.OK -> purchaseOk()
        }
    }


    fun startGooglePlayConnection() {
        billingClient = BillingClient.newBuilder(context).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. We will now enable the buy button
                    activity.buyButton.isEnabled = true
                } else {
                    val a = 1
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                billingClient.startConnection(this)
            }
        })

    }

    fun queryPurchases() {
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

    fun purchaseOk() {
        // Store in sharedpreferences that the user has bought removeads
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("userHasBoughtRemoveAds", true)
        editor.commit()

        inAppPurchasedListener.onPurchaseSuccess()

    }


}
