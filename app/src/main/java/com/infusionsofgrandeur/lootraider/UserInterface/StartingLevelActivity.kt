package com.infusionsofgrandeur.lootraider.UserInterface

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.lootraider.Adapters.StartingLevelAdapter
import com.infusionsofgrandeur.lootraider.Additions.RecyclerItemClickListener
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.databinding.ActivityStartingLevelBinding

class StartingLevelActivity : AppCompatActivity()
{

    lateinit var binding: ActivityStartingLevelBinding

    private var listAdapter: StartingLevelAdapter? = null
    private var sortedAuthorizedLevelsList = mutableListOf<Map<String, Int>>()
    private var unlockAllLevels = ConfigurationManager.unlockAllLevels

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityStartingLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        buildSortedList()

        listAdapter = StartingLevelAdapter(this)
        binding.startingLevelList.adapter = listAdapter
        binding.startingLevelList.layoutManager = LinearLayoutManager(this)
        binding.startingLevelList.addOnItemTouchListener(RecyclerItemClickListener(this, binding.startingLevelList, object : RecyclerItemClickListener.OnItemClickListener
        {

            override fun onItemClick(view: View, position: Int)
            {
                if (position == 0 || unlockAllLevels)
                {
                    val levelEntry = sortedAuthorizedLevelsList.get(position)
                    val levelNumber = (levelEntry[ConfigurationManager.persistenceItemGameboardNumber] as Double).toInt()
                    ConfigurationManager.setStartLevel(levelNumber)
                    firebaseAnalytics.logEvent("SetStartLevel", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, levelNumber)
                    })
                    finish()
                }
                else
                {
                    val selectedLevel = sortedAuthorizedLevelsList.get(position)
                    val selectedLevelName = selectedLevel.get(ConfigurationManager.persistenceItemGameboardName) as String
                    if (!IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPlayedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences))
                    {
                        return
                    }
                    else
                    {
                        val beatenLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPlayedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences)
                        val beatenLevelsEntries = beatenLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
                        for (beatenLevelName in beatenLevelsEntries)
                        {
                            if (beatenLevelName.equals(selectedLevelName, true))
                            {
                                ConfigurationManager.setStartLevel(position + 1)
                                firebaseAnalytics.logEvent("SetStartLevel", Bundle().apply {
                                    putInt(FirebaseAnalytics.Param.VALUE, position + 1)
                                })
                                finish()
                            }
                        }
                        return
                    }
                }
            }
            override fun onItemLongClick(view: View?, position: Int)
            {
            }
        }))
        binding.startingLevelCancelImageButton.setOnClickListener { finish() }
    }

    private fun buildSortedList()
    {
        val authorizedLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemAuthorizedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences)
        val authorizedLevels = authorizedLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Int>>
        if (sortedAuthorizedLevelsList.count() > 0)
        {
            sortedAuthorizedLevelsList.clear()
        }
        for (nextLevelEntry in authorizedLevels)
        {
            val entryLevel = (nextLevelEntry.get(ConfigurationManager.persistenceItemGameboardNumber) as Double).toInt()
            var inserted = false
            var index = 0
            for (nextAuthEntry in sortedAuthorizedLevelsList)
            {
                val nextAuthEntryLevel = (nextAuthEntry[ConfigurationManager.persistenceItemGameboardNumber] as Double).toInt()
                if (entryLevel < nextAuthEntryLevel)
                {
                    sortedAuthorizedLevelsList.add(index, nextLevelEntry)
                    inserted = true
                    break
                }
                index += 1
            }
            if (!inserted)
            {
                sortedAuthorizedLevelsList.add(nextLevelEntry)
            }
        }
    }
}