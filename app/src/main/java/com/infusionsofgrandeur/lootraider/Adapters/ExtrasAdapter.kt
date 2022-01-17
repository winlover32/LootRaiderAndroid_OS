package com.infusionsofgrandeur.lootraider.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.IAPManager

import com.infusionsofgrandeur.lootraider.R

class ExtrasAdapter(private val context: Context) : RecyclerView.Adapter<ExtrasAdapter.ViewHolder>()
{

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        val buyImageButton: ImageButton
        val priceTextView: TextView
        val titleTextView: TextView
        val subtitleTextView: TextView
        lateinit var sku: String

        init
        {
            // Define click listener for the ViewHolder's View.
            buyImageButton = view.findViewById(R.id.extras_buy_button)
            priceTextView = view.findViewById(R.id.extras_price_text_view)
            titleTextView = view.findViewById(R.id.extras_title_text_view)
            subtitleTextView = view.findViewById(R.id.extras_subtitle_text_view)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
    {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.extras_entry, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
    {

        val extrasList = IAPManager.productList
        val extra = extrasList[position]
        val purchasedExtras = IAPManager.purcahsedProductList
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.titleTextView.text = extra.title
        viewHolder.subtitleTextView.text = extra.description
        viewHolder.priceTextView.text = extra.price
        viewHolder.buyImageButton.setOnClickListener { IAPManager.buyProduct(extra)}
        if (purchasedExtras != null)
        {
            for (product in purchasedExtras)
            {
                for (sku in product.skus)
                {
                    if (extra.sku == sku)
                    {
                        viewHolder.buyImageButton.isEnabled = false
                        viewHolder.buyImageButton.setImageResource(R.mipmap.game_control_button_owned)
                    }
                }
            }
        }
        if (IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPurchasedItems, IoGPersistenceManager.PersistenceSource.SharedPreferences))
        {
            val purchasedItemsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPurchasedItems, IoGPersistenceManager.PersistenceSource.SharedPreferences)
            val purchasedItemEntries = purchasedItemsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
            for (nextItem in purchasedItemEntries)
            {
                if (extra.sku == nextItem)
                {
                    viewHolder.buyImageButton.isEnabled = false
                    viewHolder.buyImageButton.setImageResource(R.mipmap.game_control_button_owned)
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = IAPManager.getNumberOfAvailableExtras()

}
