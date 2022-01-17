package com.infusionsofgrandeur.lootraider.Managers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object IAPManager: PurchasesUpdatedListener, PurchasesResponseListener
{

    data class ExtrasItem(val title: String, val description: String, val price: String, val sku: String, val skuDetails: SkuDetails)
    {}

    interface IAPDelegate
    {
        fun productsRetrieved(products: List<ExtrasItem>)
        fun purchasesUpdated(purchasedProducts: List<Purchase>)
        fun purchaseCancelled()
        fun purchaseFailed()
        fun purchasesRetrieved(purchasedProducts: List<Purchase>)
        fun purchaseRetrievalFailed()
    }

    private val billingClient: BillingClient
    private val purchaseUpdateListener: PurchasesUpdatedListener
    private var ready = false
    private var waitingForConnection = false
    private var waitingForPurchases = false
    var productList = mutableListOf<ExtrasItem>()
    var purcahsedProductList = mutableListOf<Purchase>()
    lateinit var activity: AppCompatActivity

    lateinit var delegate: IAPDelegate

    private var firebaseAnalytics: FirebaseAnalytics

    init
    {
        purchaseUpdateListener =  PurchasesUpdatedListener { billingResult, purchases ->
            processPurchase(billingResult, purchases)
        }

        billingClient = BillingClient.newBuilder(ConfigurationManager.getAppContext())
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

    }

    private fun initiateConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    ready = true
                    if (waitingForConnection)
                    {
                        waitingForConnection = false
                        retrieveProducts()
                    }
                    else if (waitingForPurchases)
                    {
                        waitingForPurchases = false
                        retrievePurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                ready = false
            }
        })
    }

    fun retrieveProducts()
    {
        if (!ready)
        {
            waitingForConnection = true
            initiateConnection()
        }
        else
        {
            val skuList = ArrayList<String>()
            val params = SkuDetailsParams.newBuilder()
            skuList.add(ConfigurationManager.unlockAllLevelsIdentifier)
            skuList.add(ConfigurationManager.levels7Through12Identifier)
            skuList.add(ConfigurationManager.levels13Through18Identifier)
            skuList.add(ConfigurationManager.levels19Through24Identifier)
            skuList.add(ConfigurationManager.levels25Through30Identifier)
            skuList.add(ConfigurationManager.levels7Through30Identifier)
            skuList.add(ConfigurationManager.levels7Through30AndOOPIdentifier)
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

            billingClient?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                // Process the result.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty())
                {
                    productList.clear()
                    for (skuDetails in skuDetailsList)
                    {
                        productList.add(ExtrasItem(skuDetails.title.replace("(Loot Raider)", "", true).trim(), skuDetails.description, skuDetails.price, skuDetails.sku, skuDetails))
                    }
                    retrievePurchases()
                    if (delegate != null)
                    {
                        delegate.productsRetrieved(productList)
                    }
                }
            }
        }
    }

    fun retrievePurchases()
    {
        if (!ready)
        {
            waitingForPurchases = true
            initiateConnection()
        }
        else
        {
            billingClient?.queryPurchasesAsync(BillingClient.SkuType.INAPP, this)
        }
    }

    fun getNumberOfAvailableExtras(): Int
    {
        return productList.size
    }

    fun buyProduct(extra: ExtrasItem)
    {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(extra.skuDetails)
            .build()
        firebaseAnalytics.logEvent("PurchaseInitiated", Bundle().apply {putString(FirebaseAnalytics.Param.ITEM_ID, extra.sku)})
        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
    }

    private suspend fun acknowledgePurchase(purchase: Purchase)
    {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
        billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?)
    {
        processPurchase(billingResult, purchases)
    }

    override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: MutableList<Purchase>)
    {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null)
        {
            purcahsedProductList.addAll(purchases.toList())
            for (purchase in purchases)
            {
                if (!purchase.isAcknowledged)
                {
                    runBlocking {launch {acknowledgePurchase(purchase)}}
                }
            }
            if (delegate != null)
            {
                delegate.purchasesRetrieved(purcahsedProductList)
            }
            else
            {
                for (purchase in purchases)
                {
                    for (sku in purchase.skus)
                    {
                        // First see if we need to enable unlock of all levels
                        if (sku == ConfigurationManager.unlockAllLevelsIdentifier)
                        {
                            ConfigurationManager.unlockAllLevels = true
                        }
                        else if (sku == ConfigurationManager.levels7Through30AndOOPIdentifier)
                        {
                            ConfigurationManager.unlockAllLevels = true
                        }
                        // Then add whatever extra was purchased to storage
                        if (IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPurchasedItems, IoGPersistenceManager.PersistenceSource.SharedPreferences))
                        {
                            val purchasedItemsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPurchasedItems, IoGPersistenceManager.PersistenceSource.SharedPreferences)
                            val purchasedItemEntries = purchasedItemsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
                            var addItem = true
                            for (nextItem in purchasedItemEntries)
                            {
                                if (nextItem == sku)
                                {
                                    addItem = false
                                    break
                                }
                            }
                            if (addItem)
                            {
                                var newEntries = purchasedItemEntries.toMutableList()
                                newEntries.add(sku)
                                IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemPurchasedItems, newEntries, IoGPersistenceManager.PersistenceDataType.Array, IoGPersistenceManager.PersistenceSource.SharedPreferences, IoGPersistenceManager.PersistenceProtectionLevel.Unsecured, IoGPersistenceManager.PersistenceLifespan.Immortal, null, true)
                            }
                        }
                        else
                        {
                            var newEntries = mutableListOf<String>()
                            newEntries.add(sku)
                            IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemPurchasedItems, newEntries, IoGPersistenceManager.PersistenceDataType.Array, IoGPersistenceManager.PersistenceSource.SharedPreferences, IoGPersistenceManager.PersistenceProtectionLevel.Unsecured, IoGPersistenceManager.PersistenceLifespan.Immortal, null, true)
                        }
                    }
                }
            }
        }
        else
        {
            if (delegate != null)
            {
                delegate.purchaseRetrievalFailed()
            }
        }
    }

    fun processPurchase(billingResult: BillingResult, purchases: List<Purchase>?)
    {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null)
        {
            purcahsedProductList.addAll(purchases.toList())
            for (purchase in purchases)
            {
                if (!purchase.isAcknowledged)
                {
                    runBlocking {launch {acknowledgePurchase(purchase)}}
                    firebaseAnalytics.logEvent("CompletedPurchase", Bundle().apply {putString(FirebaseAnalytics.Param.ITEM_ID, purchase.skus.toString())})
                }
            }
            delegate?.purchasesUpdated(purcahsedProductList)
        }
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED)
        {
            // Handle an error caused by a user cancelling the purchase flow.
            firebaseAnalytics.logEvent("PurchaseCancelled", null)
            delegate?.purchaseCancelled()
        }
        else
        {
            // Handle any other error codes.
            if (purchases != null)
            {
                for (purchase in purchases)
                {
                    firebaseAnalytics.logEvent("PurchaseFailed", Bundle().apply {
                        putString(FirebaseAnalytics.Param.ITEM_ID, purchase.skus.toString())
                        putInt("Error", billingResult.responseCode)})
                }
            }
            delegate?.purchaseFailed()
        }
    }
}