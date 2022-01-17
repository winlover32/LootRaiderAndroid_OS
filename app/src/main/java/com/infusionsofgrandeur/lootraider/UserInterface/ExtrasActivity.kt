package com.infusionsofgrandeur.lootraider.UserInterface

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.Purchase
import com.google.firebase.analytics.FirebaseAnalytics
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.lootraider.Adapters.ExtrasAdapter
import com.infusionsofgrandeur.lootraider.GameObjects.Gameboard
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.IAPManager
import com.infusionsofgrandeur.lootraider.Managers.IAPManager.IAPDelegate
import com.infusionsofgrandeur.lootraider.R
import com.infusionsofgrandeur.lootraider.databinding.ActivityExtrasBinding

class ExtrasActivity : AppCompatActivity(), IAPDelegate
{

    lateinit var binding: ActivityExtrasBinding

    private var listAdapter: ExtrasAdapter? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityExtrasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        IAPManager.delegate = this
        IAPManager.activity = this

        listAdapter = ExtrasAdapter(this)
        binding.extrasList.adapter = listAdapter
        binding.extrasList.layoutManager = LinearLayoutManager(this)
        binding.extrasExitImageButton.setOnClickListener { finish() }
    }

    // In-App Purchases Manager Delegate

    override fun productsRetrieved(products: List<IAPManager.ExtrasItem>)
    {
        listAdapter?.notifyDataSetChanged()
    }

    override fun purchasesUpdated(purchasedProducts: List<Purchase>)
    {
        AlertDialog.Builder(this)
            .setTitle(R.string.extras_screen_transaction_succeeded_title)
            .setMessage(R.string.extras_screen_transaction_succeeded_description)
            .setPositiveButton(R.string.button_title_ok, null)
            .create()
            .show()
        updateAuthorizedExtras(purchasedProducts)
        listAdapter?.notifyDataSetChanged()
    }

    override fun purchaseCancelled()
    {
        AlertDialog.Builder(this)
            .setTitle(R.string.extras_screen_transaction_cancelled_title)
            .setMessage(R.string.extras_screen_transaction_cancelled_description)
            .setPositiveButton(R.string.button_title_ok, null)
            .create()
            .show()
    }

    override fun purchaseFailed()
    {
        AlertDialog.Builder(this)
            .setTitle(R.string.extras_screen_transaction_failed_title)
            .setMessage(R.string.extras_screen_transaction_failed_description)
            .setPositiveButton(R.string.button_title_ok, null)
            .create()
            .show()
    }

    override fun purchasesRetrieved(purchasedProducts: List<Purchase>)
    {
        updateAuthorizedExtras(purchasedProducts)
    }

    override fun purchaseRetrievalFailed()
    {
        AlertDialog.Builder(this)
            .setTitle(R.string.extras_screen_product_retrieval_failed_title)
            .setMessage(R.string.extras_screen_product_retrieval_failed_description)
            .setPositiveButton(R.string.button_title_ok, null)
            .create()
            .show()
    }

    private fun updateAuthorizedExtras(purchasedProducts: List<Purchase>)
    {
        for (purchase in purchasedProducts)
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
            GameboardManager.loadGameboards()
        }
    }
}