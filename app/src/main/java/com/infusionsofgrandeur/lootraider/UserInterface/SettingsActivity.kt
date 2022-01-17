package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.InputDevice
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.IAPManager
import com.infusionsofgrandeur.lootraider.R
import com.infusionsofgrandeur.lootraider.databinding.ActivitySettingsScreenBinding

import kotlin.experimental.and

class SettingsActivity : AppCompatActivity()
{

    lateinit var binding: ActivitySettingsScreenBinding
    var gamepadConnected = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val inputDevices = InputDevice.getDeviceIds()
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName: String = pInfo.versionName

        binding = ActivitySettingsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        binding.settingsControlSchemeImageButton.setOnClickListener { setControlScheme() }
        binding.settingsStartingLevelImageButton.setOnClickListener { setStartingLevel() }
        binding.settingsGetExtrasImageButton.setOnClickListener { getExtras() }
        binding.settingsContactUsImageButton.setOnClickListener { contact() }
        binding.settingsExitImageButton.setOnClickListener { exit() }
        binding.settingsPlaySoundsSwitch.setOnCheckedChangeListener{ _, isChecked ->
            ConfigurationManager.playSounds = isChecked
            firebaseAnalytics.logEvent("SetPlaySounds", Bundle().apply {
                putBoolean(FirebaseAnalytics.Param.VALUE, isChecked)
            })
        }
        binding.settingsPlayIntroAnimationsSwitch.setOnCheckedChangeListener{ _, isChecked ->
            ConfigurationManager.playIntros = isChecked
            firebaseAnalytics.logEvent("SetPlayIntros", Bundle().apply {
                putBoolean(FirebaseAnalytics.Param.VALUE, isChecked)
            })
        }
        binding.settingsPlaySkipPlayedIntrosSwitch.setOnCheckedChangeListener{ _, isChecked ->
            ConfigurationManager.skipPlayedLevelIntros = isChecked
            firebaseAnalytics.logEvent("SetSkipPlayedIntroAnimations", Bundle().apply {
                putBoolean(FirebaseAnalytics.Param.VALUE, isChecked)
            })
        }
        binding.settingsEasyModeSwitch.setOnCheckedChangeListener{ _, isChecked ->
            ConfigurationManager.easyMode = isChecked
            firebaseAnalytics.logEvent("SetEasyMode", Bundle().apply {
                putBoolean(FirebaseAnalytics.Param.VALUE, isChecked)
            })
        }
        binding.settingsPlaySoundsSwitch.isChecked = ConfigurationManager.playSounds
        binding.settingsPlayIntroAnimationsSwitch.isChecked = ConfigurationManager.playIntros
        binding.settingsPlaySkipPlayedIntrosSwitch.isChecked = ConfigurationManager.skipPlayedLevelIntros
        binding.settingsEasyModeSwitch.isChecked = ConfigurationManager.easyMode
        binding.settingsVersionTextView.text = this.resources.getString(R.string.settings_version_prefix) + versionName

        for (inputDeviceID in inputDevices)
        {
            val inputDevice = InputDevice.getDevice(inputDeviceID)
            val inputSources = inputDevice.sources
//            if (inputSources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD || inputSources and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD)
            if (inputSources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)
            {
                gamepadConnected = true
                break
            }
        }

        if (gamepadConnected)
        {
            binding.settingsGameControllerStateTextView.text = "GAME CONTROLLER DETECTED"
        }

        // Prep for checking in-app purchases
        IAPManager.retrieveProducts()
//        IAPManager.retrievePurchases()
    }

    fun setControlScheme()
    {
        val intent = Intent(this,  ControlSchemesActivity::class.java)
        startActivity(intent)
    }

    fun setStartingLevel()
    {
        val intent = Intent(this,  StartingLevelActivity::class.java)
        startActivity(intent)
    }

    fun getExtras()
    {
        val intent = Intent(this,  ExtrasActivity::class.java)
        startActivity(intent)
    }

    fun contact()
    {
        val intent = Intent(this,  ContactActivity::class.java)
        startActivity(intent)
    }

    fun exit()
    {
        finish()
    }

}