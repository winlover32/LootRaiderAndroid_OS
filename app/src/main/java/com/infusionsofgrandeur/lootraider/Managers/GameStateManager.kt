package com.infusionsofgrandeur.lootraider.Managers

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager

import com.infusionsofgrandeur.lootraider.GameObjects.StasisField
import com.infusionsofgrandeur.lootraider.GameObjects.Player
import com.infusionsofgrandeur.lootraider.GameObjects.Guard
import com.infusionsofgrandeur.lootraider.GameObjects.GoldBar
import com.infusionsofgrandeur.lootraider.GameObjects.Platform
import com.infusionsofgrandeur.lootraider.GameObjects.Teleporter

import  com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager.Coordinate

import java.util.*

object GameStateManager
{

	interface GameStateDelegate
	{
		fun freezeProcessing()
		fun playerDied(livesRemaining: Int)
		fun levelWon()
	}

	private val stasisFieldOne = StasisField()
	private val stasisFieldTwo = StasisField()
	private var tileWidth = 0
	private var tileHeight = 0
	private var currentLevel = ConfigurationManager.getStartLevel()
	lateinit private var escapeLadderBase: Coordinate
	lateinit private var escapeLadderTop: Coordinate
	lateinit private var playerPosition: Coordinate
	private var guards = mutableListOf<Guard>()
	private var player: Player? = null
	private var goldBars = mutableListOf<GoldBar>()
	private var platforms = mutableListOf<Platform>()
	private var teleporters = mutableListOf<Teleporter>()
	private var currentScore = 0
	private var currentHighScore = 0
	private var numberOfLives = ConfigurationManager.defaultStartingNumberOfLives
	private var levelEscapable = false
	private var nextExtraLifeBoundary = ConfigurationManager.pointsPerAdditionalLife

//	public var newHighScore = false

	var totalLevelGold = 0

	lateinit var delegate: GameStateDelegate

	private lateinit var firebaseAnalytics: FirebaseAnalytics

	fun setTileWidth(width: Int)
	{
		tileWidth = width
	}

	fun getTileWidth(): Int
	{
		return tileWidth
	}

	fun setTileHeight(height: Int)
	{
		tileHeight = height
	}

	fun getTileHeight(): Int
	{
		return tileHeight
	}

	fun setCurrentLevel(level: Int)
	{
		currentLevel = level
	}

	fun getCurrentLevel(): Int
	{
		return currentLevel
	}

	fun getNumberOfLives(): Int
	{
		return numberOfLives
	}

	fun setLevelEscapable(flag: Boolean)
	{
		levelEscapable = flag
	}

	fun getLevelEscapable(): Boolean
	{
		return levelEscapable
	}

	fun advanceLevel()
	{
		currentLevel += 1
	}

	fun getEscapeLadderBase(): Coordinate
	{
		return escapeLadderBase
	}

	fun getEscapeLadderTop(): Coordinate
	{
		return escapeLadderTop
	}

	fun setEscapeLadderBase(position: Coordinate)
	{
		escapeLadderBase = position
		escapeLadderTop = Coordinate(position.xPos, 0)
	}

	fun setPlayer(newPlayer: Player)
	{
		player = newPlayer
	}

	fun getPlayer(): Player
	{
		return player!!
	}

	fun getPlayerPosition(): Coordinate
	{
		return Coordinate(player!!.xTile, player!!.yTile)
	}

	fun getPlayerFalling(): Boolean
	{
        return player!!.falling
	}

	fun getGuards(): List<Guard>
	{
		return guards
	}

	fun addGuard(newGuard: Guard)
	{
		guards.add(newGuard)
	}

	fun removeLastGuard()
	{
		guards.removeLast()
	}

	fun getGoldBars(): List<GoldBar>
	{
		return goldBars
	}

	fun addGoldBar(newBar: GoldBar)
	{
		goldBars.add(newBar)
		totalLevelGold += 1
	}

	fun getPlatforms(): List<Platform>
	{
		return platforms
	}

	fun addPlatform(newPlatform: Platform)
	{
		platforms.add(newPlatform)
	}

	fun getTeleporters(): List<Teleporter>
	{
		return teleporters
	}

	fun getTeleporterForIdentifier(pair: Int): Teleporter?
	{
		for (teleporter in teleporters)
		{
            if (teleporter.identifier == pair)
            {
                return teleporter
            }
		}
		return null
	}

	fun addTeleporter(newTeleporter: Teleporter)
	{
		teleporters.add(newTeleporter)
	}

	fun getStasisFieldOne(): StasisField
	{
		return stasisFieldOne
	}

	fun getStasisFieldTwo(): StasisField
	{
		return stasisFieldTwo
	}

	fun getCurrentScore(): Int
	{
		return currentScore
	}

	fun getCurrentHighScore(): Int
	{
		return currentHighScore
	}

	fun saveCurrentHighScore()
	{
		currentHighScore = currentScore
	}

	fun clearCurrentHighScore()
	{
		currentHighScore = 0
	}

	fun getNextAdditionalLifeScore(): Int
	{
		return nextExtraLifeBoundary
	}

	fun advanceAdditionalLifeScore()
	{
		nextExtraLifeBoundary += ConfigurationManager.pointsPerAdditionalLife
		grantBonusLife()
	}

	fun arrangeAnimations()
	{

		// Obtain the FirebaseAnalytics instance.
		firebaseAnalytics = Firebase.analytics

		val sprites = SpriteManager.sprites
		var index = 0
		for (sprite in sprites)
		{
			if (index == ConfigurationManager.platformSpriteIndex)
			{
				for (endIndex in index until sprites.count())
				{
					val checkSprite = sprites[endIndex]
					if (checkSprite.lastFrame)
					{
						Platform.startFrame = index
						Platform.endFrame = endIndex
						break
					}
				}
			}
			else if (index == ConfigurationManager.teleporterSpriteIndex)
			{
				for (endIndex in index until sprites.count())
				{
					val checkSprite = sprites[endIndex]
					if (checkSprite.lastFrame)
					{
						Teleporter.startFrame = index
						Teleporter.endFrame = endIndex
						break
					}
				}
			}
			else if (index == ConfigurationManager.stasisFieldSpriteIndex)
			{
				for (endIndex in index until sprites.count())
				{
					val checkSprite = sprites[endIndex]
					if (checkSprite.lastFrame)
					{
						StasisField.startFrame = index
						StasisField.endFrame = endIndex
						break
					}
				}
			}
			index += 1
		}
	}

	fun resetLevel()
	{
		player = null
		totalLevelGold = 0
		guards.clear()
		goldBars.clear()
		platforms.clear()
		teleporters.clear()
		escapeLadderBase = Coordinate(0, 0)
		escapeLadderTop = Coordinate(0, 0)
		playerPosition = Coordinate(0, 0)
	}

	fun resetGameStats()
	{
		currentScore = 0
		numberOfLives = ConfigurationManager.defaultStartingNumberOfLives
	}

	fun addGoldBarToScore()
	{
		currentScore += ConfigurationManager.pointsPerGoldBar
	}

	fun addBeatenLevelToScore()
	{
		currentScore += ConfigurationManager.pointsPerLevelBeaten
	}

	fun playerDeath()
	{
		numberOfLives -= 1
		delegate?.freezeProcessing()
		Timer().schedule(object: TimerTask() {
			override fun run() {
				delegate?.playerDied(numberOfLives)
				if (numberOfLives == 0)
				{
					numberOfLives = ConfigurationManager.defaultStartingNumberOfLives
					currentScore = 0
				}
			}
		}, ConfigurationManager.postDeathDelay)
	}

	fun winLevel()
	{
		firebaseAnalytics.logEvent("BeatLevel", Bundle().apply {
			putInt(FirebaseAnalytics.Param.LEVEL, currentLevel)
		})
		addBeatenLevelToScore()
		SoundManager.playWinLevel()
		delegate?.freezeProcessing()
		advanceLevel()
		if (ConfigurationManager.getLastLevelNumber() > currentLevel)
		{
			ConfigurationManager.setStartLevel(currentLevel)
		}
		Timer().schedule(object: TimerTask() {
			override fun run() {
				delegate?.levelWon()
			}
		}, ConfigurationManager.winLevelTimerDelay)
	}

	fun grantBonusLife()
	{
		numberOfLives += 1
		firebaseAnalytics.logEvent("AwardedBonusLife", Bundle().apply {
			putInt(FirebaseAnalytics.Param.VALUE, numberOfLives)
		})
	}

	fun checkForHighScore(): Boolean
	{
		if (currentScore == 0)
		{
			return false
		}
		val highScoreResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemHighScores, IoGPersistenceManager.PersistenceSource.SharedPreferences)
		val highScores = highScoreResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Double>>
		val lowestHighScore = highScores.last()
		val lowestHighScoreValue = lowestHighScore[ConfigurationManager.highScoreEntryFieldScore]
		if (lowestHighScoreValue != null)
		{
			val lowestHighScoreIntValue = lowestHighScoreValue.toInt()
			if (lowestHighScoreIntValue > currentScore)
			{
				return false
			}
			else
			{
				return true
			}
		}
		else
		{
			return true
		}
	}
}