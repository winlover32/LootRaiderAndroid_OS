package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Intent
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.*
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceSource;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceDataType;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceLifespan;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceProtectionLevel;
import com.infusionsofgrandeur.lootraider.GameObjects.Gameboard
import com.infusionsofgrandeur.lootraider.R

import com.infusionsofgrandeur.lootraider.databinding.ActivityGameCenterBinding
import java.util.*

class GameCenterActivity : AppCompatActivity()
{

	lateinit var binding: ActivityGameCenterBinding
	lateinit var currentAnimatedHSRow: RelativeLayout

	private var timer: Timer? = null
	private var mostRecentHighScore = 0

	private var waitingOnHighScoreEntry = false

	private lateinit var firebaseAnalytics: FirebaseAnalytics

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		binding = ActivityGameCenterBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Obtain the FirebaseAnalytics instance.
		firebaseAnalytics = Firebase.analytics

		binding.gameCenterPlayImageButton.setOnClickListener { play() }
		binding.gameCenterSettingsImageButton.setOnClickListener { showSettings() }
		binding.gameCenterInstructionsImageButton.setOnClickListener { showTutorial() }
		binding.highScoreEntryCancelButton.setOnClickListener { cancelHighScoreEntry() }
		binding.highScoreEntryAcceptButton.setOnClickListener { acceptHighScoreEntry() }

		binding.highScoreEntryContainerView.visibility = View.INVISIBLE

		currentAnimatedHSRow = binding.gameCenterHighScoreEntries1.root

		SpriteManager.loadSprites()
		GameboardManager.loadGameboards()
		GameStateManager.arrangeAnimations()
	}

	override fun onResume()
	{
		super.onResume()
		if (!waitingOnHighScoreEntry)
		{
			SoundManager.playTheme()
			displayHighScores()
			startHighScoreAnimation()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	{
		val highScore = data?.getBooleanExtra("High Score", false)

		if (highScore != null)
		{
			if (highScore)
			{
				promptNewHighScore()
			}
			else
			{
				SoundManager.playTheme()
				displayHighScores()
				startHighScoreAnimation()
			}
		}
		else
		{
			SoundManager.playTheme()
			displayHighScores()
			startHighScoreAnimation()
		}

		super.onActivityResult(requestCode, resultCode, data)
	}

	fun play()
	{
		val intent = Intent(this,  GameScreenActivity::class.java)
		stopHighScoreAnimation()
		GameStateManager.setCurrentLevel(ConfigurationManager.getStartLevel())
		firebaseAnalytics.logEvent("StartGame", Bundle().apply {
			putInt(FirebaseAnalytics.Param.LEVEL, ConfigurationManager.getStartLevel())
		})
		startActivityForResult(intent, 1)
	}

	fun showSettings()
	{
		val intent = Intent(this,  SettingsActivity::class.java)
		firebaseAnalytics.logEvent("ViewSettings", null)
		startActivity(intent)
	}

	fun showTutorial()
	{
		val intent = Intent(this,  TutorialActivity::class.java)
		firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null)
		startActivity(intent)
	}

	fun promptNewHighScore()
	{
		SoundManager.playHighScore()
		waitingOnHighScoreEntry = true
		binding.gameCenterPlayImageButton.isEnabled = false
		binding.gameCenterSettingsImageButton.isEnabled = false
		binding.gameCenterInstructionsImageButton.isEnabled = false
		binding.highScoreEntryContainerView.visibility = View.VISIBLE
		binding.highScoreEntryViewNameEntryField.requestFocus()
		val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(binding.highScoreEntryViewNameEntryField, InputMethodManager.SHOW_IMPLICIT)
		firebaseAnalytics.logEvent("PromptHighScoreEntry", Bundle().apply {
			putInt(FirebaseAnalytics.Param.SCORE, GameStateManager.getCurrentHighScore())
		})
	}

	fun cancelHighScoreEntry()
	{
		binding.gameCenterPlayImageButton.isEnabled = true
		binding.gameCenterSettingsImageButton.isEnabled = true
		binding.gameCenterInstructionsImageButton.isEnabled = true
		binding.highScoreEntryContainerView.visibility = View.INVISIBLE
		binding.highScoreEntryViewNameEntryField.clearFocus()
		displayHighScores()
		startHighScoreAnimation()
		waitingOnHighScoreEntry = false
		GameStateManager.clearCurrentHighScore()
		firebaseAnalytics.logEvent("CancelHighScoreEntry", null)
	}

	fun acceptHighScoreEntry()
	{
		val name = binding.highScoreEntryViewNameEntryField.text.toString()
		val highScoresResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemHighScores, PersistenceSource.SharedPreferences)
		val highScores = highScoresResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Any>>
		var newHighScoreList = highScores.toMutableList()
		var index = 0
		val newHighScore = GameStateManager.getCurrentHighScore()
		for (nextHighScore in highScores)
		{
			val highScoreValue = (nextHighScore[ConfigurationManager.highScoreEntryFieldScore] as Double).toInt()
			if (newHighScore > highScoreValue)
			{
				var highScoreEntry = hashMapOf<String, Any>()
				highScoreEntry[ConfigurationManager.highScoreEntryFieldScore] = newHighScore
				highScoreEntry[ConfigurationManager.highScoreEntryFieldName] = name
				newHighScoreList.add(index, highScoreEntry)
				newHighScoreList.removeLast()
				mostRecentHighScore = index
				IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemHighScores, newHighScoreList, PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
				break
			}
			index += 1
		}
		binding.gameCenterPlayImageButton.isEnabled = true
		binding.gameCenterSettingsImageButton.isEnabled = true
		binding.gameCenterInstructionsImageButton.isEnabled = true
		binding.highScoreEntryContainerView.visibility = View.INVISIBLE
		binding.highScoreEntryViewNameEntryField.clearFocus()
		binding.highScoreEntryViewNameEntryField.setText("")
		displayHighScores()
		startHighScoreAnimation()
		waitingOnHighScoreEntry = false
		GameStateManager.clearCurrentHighScore()
		firebaseAnalytics.logEvent("AcceptHighScoreEntry", Bundle().apply {
			putString(FirebaseAnalytics.Param.ITEM_NAME, name)
		})
	}

	fun displayHighScores()
	{
		when (mostRecentHighScore)
		{
			0 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries1.root
			1 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries2.root
			2 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries3.root
			3 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries4.root
			4 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries5.root
			5 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries6.root
			6 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries7.root
			7 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries8.root
			8 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries9.root
			9 -> currentAnimatedHSRow = binding.gameCenterHighScoreEntries10.root
		}
		// Set up blank high scores first time through
		if (!IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemHighScores, PersistenceSource.SharedPreferences))
		{
			// Pre-populate with blank entries
			var highScores = mutableListOf<Map<String, Any>>()
			for (score in 1..ConfigurationManager.numberHighScoresDisplayed)
			{
				var highScoreEntry = hashMapOf<String, Any>()
				highScoreEntry[ConfigurationManager.highScoreEntryFieldScore] = 0
				highScoreEntry[ConfigurationManager.highScoreEntryFieldName] = ""
				highScores.add(highScoreEntry)
			}
			IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemHighScores, highScores, PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
		}
		val highScoresResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemHighScores, PersistenceSource.SharedPreferences)
		val highScores = highScoresResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Any>>
		var index = 1
		for (nextHighScore in highScores)
		{
			val score = (nextHighScore[ConfigurationManager.highScoreEntryFieldScore] as Double).toInt()
			val name = nextHighScore[ConfigurationManager.highScoreEntryFieldName] as String
			var highScoreRow: RelativeLayout
			when (index-1)
			{
				0 -> highScoreRow = binding.gameCenterHighScoreEntries1.root
				1 -> highScoreRow = binding.gameCenterHighScoreEntries2.root
				2 -> highScoreRow = binding.gameCenterHighScoreEntries3.root
				3 -> highScoreRow = binding.gameCenterHighScoreEntries4.root
				4 -> highScoreRow = binding.gameCenterHighScoreEntries5.root
				5 -> highScoreRow = binding.gameCenterHighScoreEntries6.root
				6 -> highScoreRow = binding.gameCenterHighScoreEntries7.root
				7 -> highScoreRow = binding.gameCenterHighScoreEntries8.root
				8 -> highScoreRow = binding.gameCenterHighScoreEntries9.root
				9 -> highScoreRow = binding.gameCenterHighScoreEntries10.root
				else -> highScoreRow = binding.gameCenterHighScoreEntries1.root
			}
			val rankTextView = highScoreRow.findViewById<TextView>(R.id.high_score_field_rank)
			val scoreTextView = highScoreRow.findViewById<TextView>(R.id.high_score_field_score)
			val nameTextView = highScoreRow.findViewById<TextView>(R.id.high_score_field_name)
			rankTextView.text = "$index"
			scoreTextView.text = "$score"
			nameTextView.text = "$name"
			index++
		}
	}

	fun startHighScoreAnimation()
	{
		val primaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_primary)
		val secondaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_secondary)
		val params = secondaryBackgroundLayout.layoutParams
		primaryBackgroundLayout.visibility = View.VISIBLE
		secondaryBackgroundLayout.visibility = View.VISIBLE
		params.height = 1
		secondaryBackgroundLayout.layoutParams = params
		primaryBackgroundLayout.setBackgroundColor(ConfigurationManager.getAppContext().resources.getColor(R.color.high_score_entry_background_primary_color, null))
		secondaryBackgroundLayout.setBackgroundColor(ConfigurationManager.getAppContext().resources.getColor(R.color.high_score_entry_background_secondary_color, null))
		if (timer == null)
		{
			timer = Timer()
		}
		timer?.scheduleAtFixedRate(object: TimerTask() {
			override fun run() {
				runOnUiThread {
					updateHSAnimation()
				}
			}
		}, ConfigurationManager.spriteAnimationLoopTimerDelay, ConfigurationManager.spriteAnimationLoopTimerDelay)
	}

	fun stopHighScoreAnimation()
	{
		val primaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_primary)
		val secondaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_secondary)
		timer?.cancel()
		timer = null
		primaryBackgroundLayout.visibility = View.INVISIBLE
		secondaryBackgroundLayout.visibility = View.INVISIBLE
	}

	fun updateHSAnimation()
	{
		val primaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_primary)
		val secondaryBackgroundLayout = currentAnimatedHSRow.findViewById<LinearLayout>(R.id.high_score_entry_background_secondary)
		val params = secondaryBackgroundLayout.layoutParams
		if (secondaryBackgroundLayout.height == primaryBackgroundLayout.height)
		{
			val backgroundDrawable: ColorDrawable = secondaryBackgroundLayout.background as ColorDrawable
			val topColor = backgroundDrawable.color
			val foregroundDrawable: ColorDrawable = primaryBackgroundLayout.background as ColorDrawable
			val bottomColor = foregroundDrawable.color
			primaryBackgroundLayout.setBackgroundColor(topColor)
			secondaryBackgroundLayout.setBackgroundColor(bottomColor)
			params.height = 1
		}
		else
		{
			params.height = secondaryBackgroundLayout.height + 1
		}
		secondaryBackgroundLayout.layoutParams = params
	}

}
