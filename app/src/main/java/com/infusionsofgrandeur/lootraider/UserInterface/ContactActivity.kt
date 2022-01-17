package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.databinding.ActivityContactBinding
import java.lang.Exception

class ContactActivity : AppCompatActivity()
{

    lateinit var binding: ActivityContactBinding

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        binding.contactExitImageButton.setOnClickListener { finish() }
        binding.contactEmailImageButton.setOnClickListener { sendSupportEmail() }
    }

    fun sendSupportEmail()
    {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.type = "text/plain"
            intent.data = Uri.parse(ConfigurationManager.defaultContactEmail)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            firebaseAnalytics.logEvent("ComposeEmail", null)
            startActivity(Intent.createChooser(intent, "Send email..."))
        }
        catch (ex: Exception)
        {
            firebaseAnalytics.logEvent("EmailSendFailed", null)
        }
    }
}