package com.infusionsofgrandeur.lootraider.UserInterface

import android.R
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.lootraider.Adapters.ControlSchemeAdapter
import com.infusionsofgrandeur.lootraider.Additions.RecyclerItemClickListener
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.databinding.ActivityControlSchemesBinding

class ControlSchemesActivity : AppCompatActivity()
{

    lateinit var binding: ActivityControlSchemesBinding

    private var listAdapter: ControlSchemeAdapter? = null

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityControlSchemesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        listAdapter = ControlSchemeAdapter(this)
        binding.controlSchemesList.adapter = listAdapter
        binding.controlSchemesList.layoutManager = LinearLayoutManager(this)
        binding.controlSchemesList.addOnItemTouchListener(RecyclerItemClickListener(this, binding.controlSchemesList, object : RecyclerItemClickListener.OnItemClickListener
        {

            override fun onItemClick(view: View, position: Int)
            {
                when (position)
                {
                    0 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Horizontal1)
                    1 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Horizontal2)
                    2 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Horizontal5)
                    3 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Horizontal3)
                    4 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Horizontal4)
                    5 -> ConfigurationManager.setLayoutScheme(ConfigurationManager.GameScreenLayoutScheme.Vertical)
                }
                firebaseAnalytics.logEvent("SetControlScheme", Bundle().apply {
                    putInt(FirebaseAnalytics.Param.VALUE, position)
                })
                finish()
            }
            override fun onItemLongClick(view: View?, position: Int)
            {
            }
        }))
        binding.controlSchemesCancelImageButton.setOnClickListener { finish() }
    }
}