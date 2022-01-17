package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.R

class SplashActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        IoGConfigurationManager.getSharedManager().applicationContext = this.applicationContext
        ConfigurationManager.setAppContext(applicationContext)
        val intent = Intent(this, GameCenterActivity::class.java)
        startActivity(intent)
        finish()
    }
}
