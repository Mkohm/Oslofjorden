package com.oslofjorden.oslofjordenturguide.MapView

class InAppPurchaseHandler(val appPurchasedListener: AppPurchasedListener) {

    fun purchase() {
        // implement purchase shit

        appPurchasedListener.onPurchaseSuccess()
    }


}
