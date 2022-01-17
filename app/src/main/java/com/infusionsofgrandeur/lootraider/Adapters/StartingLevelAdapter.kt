package com.infusionsofgrandeur.lootraider.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.R

class StartingLevelAdapter(private val context: Context) : RecyclerView.Adapter<StartingLevelAdapter.ViewHolder>()
{

	private var sortedAuthorizedLevelsList = mutableListOf<Map<String, Int>>()
	private var unlockAllLevels = ConfigurationManager.unlockAllLevels

	/**
	 * Provide a reference to the type of views that you are using
	 * (custom ViewHolder).
	 */
	class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
	{
		val levelThumbnailImageView: ImageView
		val levelNumberTextView: TextView
		val levelNameTextView: TextView
		val currentStartLevelTextView: TextView

		init
		{
			// Define click listener for the ViewHolder's View.
			levelThumbnailImageView = view.findViewById(R.id.starting_levels_entry_image_view)
			levelNumberTextView = view.findViewById(R.id.starting_level_entry_level_number_text_view)
			levelNameTextView = view.findViewById(R.id.starting_level_entry_level_name_text_view)
			currentStartLevelTextView = view.findViewById(R.id.starting_level_entry_current_start_text_view)
		}
	}

	// Create new views (invoked by the layout manager)
	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder
	{
		// Create a new view, which defines the UI of the list item
		val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.starting_level_entry, viewGroup, false)

		buildSortedList()

		return ViewHolder(view)
	}

	// Replace the contents of a view (invoked by the layout manager)
	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int)
	{

		val startingLevel = ConfigurationManager.currentStartLevel
		val authorizedLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemAuthorizedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences)
		val authorizedLevels = authorizedLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Int>>
		// Get element from your dataset at this position and replace the
		// contents of the view with that element
		var startLevelName = ""
		var startLevelNumber = 0
		viewHolder.currentStartLevelTextView.visibility = View.INVISIBLE
		val level = sortedAuthorizedLevelsList.get(position)
		startLevelName = level.get(ConfigurationManager.persistenceItemGameboardName) as String
		startLevelNumber = (level.get(ConfigurationManager.persistenceItemGameboardNumber) as Double).toInt()
		if (startingLevel == startLevelNumber)
		{
			viewHolder.currentStartLevelTextView.visibility = View.VISIBLE
		}
		else
		{
			viewHolder.currentStartLevelTextView.visibility = View.INVISIBLE
		}
		viewHolder.levelNameTextView.text = startLevelName
		viewHolder.levelNumberTextView.text = "Level ".plus(startLevelNumber)
		viewHolder.levelThumbnailImageView.setImageResource(android.R.color.transparent)
		viewHolder.levelThumbnailImageView.visibility = View.VISIBLE
		if (position == 0 || unlockAllLevels)
		{
			val imageName = "level".plus(startLevelNumber)
			val resourceID = ConfigurationManager.getAppContext().resources.getIdentifier(imageName, "mipmap", ConfigurationManager.getAppContext().packageName)
			viewHolder.levelThumbnailImageView.setImageResource(resourceID)
			viewHolder.levelThumbnailImageView.visibility = View.VISIBLE
		}
		else
		{
			if (IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPlayedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences))
			{
				val beatenLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPlayedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences)
				val beatenLevelsEntries = beatenLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
				for (beatenLevelName in beatenLevelsEntries)
				{
					if (beatenLevelName.equals(startLevelName, true))
					{
						val imageName = "level".plus(startLevelNumber)
						val resourceID = ConfigurationManager.getAppContext().resources.getIdentifier(imageName, "mipmap", ConfigurationManager.getAppContext().packageName)
						viewHolder.levelThumbnailImageView.setImageResource(resourceID)
						viewHolder.levelThumbnailImageView.visibility = View.VISIBLE
					}
				}
			}
		}
	}

	// Return the size of your dataset (invoked by the layout manager)
	override fun getItemCount(): Int
	{
		val authorizedLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemAuthorizedLevels, IoGPersistenceManager.PersistenceSource.SharedPreferences)
		val authorizedLevels = authorizedLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Int>>
		return authorizedLevels.count()
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
