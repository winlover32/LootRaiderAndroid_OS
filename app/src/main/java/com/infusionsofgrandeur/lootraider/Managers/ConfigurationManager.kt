package com.infusionsofgrandeur.lootraider.Managers

import android.content.Context

import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager

import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceSource;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceDataType;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceLifespan;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceProtectionLevel;

object ConfigurationManager
{

    data class Coordinate(val xPos: Int, val yPos: Int)
    {}

    data class TilePosition(val x: Int, val y: Int, val tile: Int, val attributes: List<Int>)
    {}

    // TODO: Consider moving to the control view class
    enum class ControlType
    {
        Tap,
        Propel,
        Flick
    }

    // TODO: Consider moving to the game screen view
    enum class GameScreenLayoutScheme
    {
        Vertical,
        Horizontal1,        // Combined buttons, right
        Horizontal2,        // Combined buttons, left
        Horizontal3,        // Split buttons, horizontal buttons right
        Horizontal4,        // Split buttons, horizontal buttons left
        Horizontal5         // D-Pad style, directional controls on left, action buttons on right
    }

    // Assets
    val defaultGameboardFilename = "gameboard_set_1"
    val defaultSpriteFilename = "loot_raider_sprites"
    val alternateSpriteFilename = "loot_rider_sprites_large"
    val defaultIntroFilename = "Intro"
    val defaultPlayerGetGoldFilename = "PlayerGetGold"
    val defaultSentryGetGoldFilename = "SentryGetGold"
    val defaultRaiseStasisFieldFilename = "RaiseStasisField"
    val defaultLowerStasisFieldFilename = "LowerStasisField"
    val defaultTeleporterFilename = "Teleporter"
    val defaultPlayerCaughtFilename = "PlayerCaught"
    val defaultEscapeLadderFilename = "EscapeLadder"
    val defaultWinLevelFilename = "WinLevel"
    val defaultExtraLifeFilename = "ExtraLife"
    val defaultHighScoreFilename = "HighScore"

    // Spriteset
    val spriteSetDefaultHeader = byteArrayOf(0x45, 0x47, 0x47)
    val spriteDelineator = byteArrayOf(0x80.toByte(), 0x81.toByte(), 0x79, 0x82.toByte(), 0x78)
    val spriteSectionHeaderDelineator = byteArrayOf(0x26, 0x48)
    val spriteSectionIdentifierDelineator = byteArrayOf(0x26, 0x49)
    val spriteSectionAnimationDelineator = byteArrayOf(0x26, 0x41)
    val spriteSectionDimensionsDelineator = byteArrayOf(0x26, 0x44)
    val spriteSectionBackgroundColorDelineator = byteArrayOf(0x26, 0x42)
    val spriteSectionPixelMapDelineator = byteArrayOf(0x26, 0x50)
    val spriteSectionPixelMaskDelineator = byteArrayOf(0x26, 0x4D)

    // GameboadSet
    val gameboardSetDefaultHeader = byteArrayOf(0x45, 0x47, 0x43)
    val gameboardDelineator = byteArrayOf(0x78, 0x82.toByte(), 0x79, 0x81.toByte(), 0x80.toByte())
    val gameboardSectionHeaderDelineator = byteArrayOf(0x26, 0x48)
    val gameboardSectionIdentifierDelineator = byteArrayOf(0x26, 0x49)
    val gameboardSectionDimensionsDelineator = byteArrayOf(0x26, 0x44)
    val gameboardSectionTileMapDelineator = byteArrayOf(0x26, 0x50)

    // Basic game settings
    val defaultStartLevel = 1
    val defaultStartingNumberOfLives = 4
    val makeBlackPixelsTransparent = true
    val gameUpdateLoopTimerDelay: Long = 30L
    val postDeathDelay: Long = 1000L
    val postLevelWinDelay: Long = 3000L
    val postLastLevelWinDelay: Long = 1L
    val pauseOverlayAlpha = 0.70
    val pauseLabelFontSize = 17.0
    val gameboardImageViewLevelNumberTag = 77
    val gameboardImageViewLevelNameTag = 88
    val winLevelTimerDelay: Long = 1000L
    val gamepadDirectionalBuffer = 0.25

    // Game-specific sprite asset info
    val playerSpriteIndex = 19
    val guardSpriteIndex = 25
    val platformSpriteIndex = 7
    val teleporterSpriteIndex = 14
    val goldBarSpriteIndex = 33
    val stasisFieldSpriteIndex = 54
    val darkBackgroundTiles = intArrayOf(0, 3, 5)
    val lightBackgroundTiles = intArrayOf(1, 4, 6)
    val steelGirderTile = 2
    val ladderTileDarkBackground = 3
    val ladderTileLightBackground = 4
    val tileDarkBackground = 0
    val tileLightBackground = 1

    // Sprite characteristics
    val spriteHeaderTraversable = 0x01
    val spriteHeaderClimable = 0x02
    val spriteHeaderHangable = 0x04
    val spriteHeaderFallthroughable = 0x08

    // Sprite attributes
    val exitLadderBaseTileHeaderValue = 0x80
    val platformStoppableHeaderValue = 0x01
    val platformHorizontalHeaderValue = 0x20
    val platformVerticalHeaderValue = 0x40
    val platformSlowSpeedHeaderValue = 0x04
    val platformModerateSpeedHeaderValue = 0x08
    val platformFastSpeedHeaderValue = 0xf0
    val platformLongWaitHeaderValue = 0x20
    val platformModerateWaitHeaderValue = 0x40
    val platformShortWaitHeaderValue = 0x80
    val platformInitialDirectionLeft = 0x02
    val platformInitialDirectionUp = 0x01
    val teleporterRoundTrippableValue = 0x02
    val teleporterSendableHeaderValue = 0x04
    val teleporterReceivableHeaderValue = 0x08
    val teleporterPairableHeaderValue = 0x10

    // Platforms
    val platformSpeedAttributeMask = 0x18
    val platformWaitAttributeMask = 0xe0
    val platformDefaultWaitTime = 2.0
    val platformSpeedSlowMultiplier = 1
    val platformSpeedModerateMultiplier = 2
    val platformSpeedFastMultiplier = 3
    val platformWaitShortMultiplier = 1.0
    val platformWaitModerateMultiplier = 2.0
    val platformWaitLongMultiplier = 3.0

    // Teleporters
    val cyclesToSkipBetweenTeleporterFrames = 3

    // Character movement
    val playerXAxisSteps = 2
    val playerYAxisSteps = 2
    val playerFallingSteps = 1
    val guardXAxisSteps = 1
    val guardYAxisSteps = 1
    val platformXAxisSteps = 1
    val platformYAxisSteps = 1

    // Guard behaviors
    val optionsForGuardSmartBehavior = 20
    val guardPossibleRandomDirections = 4

    // Character control
    val defaultControlType = ControlType.Tap
    val defaultMultiControlButtonProximityPercent = 10
    val defaultControllerCenterDeadRadius = 40.0F
    val defaultControllerSideLength = 293.0F

    // Stasis fields
    val stasisFieldDuration = 4.0
    val stasisFieldBlockingStage = 3
    val stasisFieldAlpha = 191

    // Player Collision Detection
    val guardCollisionXAxisOverlap = 4
    val guardCollisionYAxisOverlap = 4

    // Game stats
    val pointsPerGoldBar = 100
    val pointsPerLevelBeaten = 1000
    val pointsPerAdditionalLife = 5000

    // High Scores
    val persistenceItemHighScores = "High Scores"
    val numberHighScoresDisplayed = 10
    val highScoreEntryFieldScore = "Score"
    val highScoreEntryFieldName = "Name"
    val highScoreAnimationFrameUpdateDelay = 0.03

    // Title
    val titleAlphaIncrementValue = 0.03F
    val titleAnimationTimerDelay: Long = 120L
    val titleEndTimerDelay: Long = 3000L

    // Reveal Curtain
    val revealCurtainTimerDelay: Long = 60L
    val revealImageWidth = 300
    val revealImageHeight = 200
    val revealCurtainWidth = 600
    val revealCurtainHeight = 400
    val revealSpotlightStartingWidth = 12
    val revealSpotlightStartingHeight = 8
    val revealSpotlightXAxisSteps = 6
    val revealSpotlightYAxisSteps = 4

    // Game Screen Layout
    val defaultLayoutType = GameScreenLayoutScheme.Horizontal1

    // Settings
    val preferencesFileName = "Settings"
    val persistenceItemControlScheme = "Control Scheme"
    val persistenceItemLayoutScheme = "Layout Scheme"
    val persistenceItemPlaySounds = "Play Sounds"
    val persistenceItemPlayIntro = "Play Intro"
    val persistenceItemSkipPlayedLevelsIntro = "Skip Played Levels Intro"
    val persistenceItemEasyMode = "Easy Mode"
    val persistenceItemStartLevel = "Start Level"
    val persistenceItemUnlockAll = "Unlock All Levels"
    val defaultPlaySoundsValue = true
    val defaultPlayIntroValue = true
    val defaultSkipPlayedLevelsIntro = false
    val defaultUnlockAllLevels = false
    val defaultEasyModeValue = false
    val defaultSettingsNumLayoutConfigurations = 6
    val defaultNumProductsToQuery = 18

    // Tutorial
    val numberTutorialSegments = 5
    val spriteAnimationLoopTimerDelay: Long = 40L

    // Dynamic Game Data
    val persistenceItemAuthorizedLevels = "Authorized Levels"
    val persistenceItemPlayedLevels = "Played Levels"
    val persistenceItemBeatenLevels = "Beaten Levels"
    val persistenceItemGameboardNumber = "Gameboard Number"
    val persistenceItemGameboardName = "Gameboard Name"

    // Contact
    val defaultContactEmail = "lootraider@infusionsofgrandeur.com"

    // In-App Purchases
    val persistenceItemPurchasedItems = "Purchased Items"
    val persistenceItemDownloadItem = "SKDownload"
    val unlockAllLevelsIdentifier = "lr_0002"
    val levels7Through12Identifier = "lr_0003"
    val levels13Through18Identifier = "lr_0004"
    val levels19Through24Identifier = "lr_0005"
    val levels25Through30Identifier = "lr_0006"
    val levels7Through30Identifier = "lr_0007"
    val levels7Through30AndOOPIdentifier = "lr_0008"
    val unlockAllLevelsPlusComboIdentifiers = arrayOf("lr_0002", "lr_0008")
    val comboIdentifiers = arrayOf("lr_0007", "lr_0008")
    val gameboardSet2ProductIdentifiers = arrayOf("lr_0003", "lr_0007", "lr_0008")
    val gameboardSet3ProductIdentifiers = arrayOf("lr_0004", "lr_0007", "lr_0008")
    val gameboardSet4ProductIdentifiers = arrayOf("lr_0005", "lr_0007", "lr_0008")
    val gameboardSet5ProductIdentifiers = arrayOf("lr_0006", "lr_0007", "lr_0008")

    lateinit var context: Context

    var currentControlScheme: ControlType
    var currentLayoutScheme: GameScreenLayoutScheme
    var playSounds = defaultPlaySoundsValue
        set(newValue)
        {
            field = newValue
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemPlaySounds, newValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
    var playIntros = defaultPlayIntroValue
        set(newValue)
        {
            field = newValue
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemPlayIntro, newValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
    var skipPlayedLevelIntros = defaultSkipPlayedLevelsIntro
        set(newValue)
        {
            field = newValue
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemSkipPlayedLevelsIntro, newValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
    var easyMode = defaultEasyModeValue
        set(newValue)
        {
            field = newValue
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemEasyMode, newValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
    var currentStartLevel = defaultStartLevel
    var unlockAllLevels = defaultUnlockAllLevels
        set(newValue)
        {
            field = newValue
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemUnlockAll, newValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }

    init
    {
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemControlScheme, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemControlScheme, defaultControlType.name, PersistenceDataType.String, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val controlResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemControlScheme, PersistenceSource.SharedPreferences)
        currentControlScheme = ControlType.valueOf(controlResponse.get(IoGConfigurationManager.persistenceReadResultValue) as String)
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemLayoutScheme, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemLayoutScheme, defaultLayoutType.name, PersistenceDataType.String, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val layoutResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemLayoutScheme, PersistenceSource.SharedPreferences)
        currentLayoutScheme = GameScreenLayoutScheme.valueOf(layoutResponse.get(IoGConfigurationManager.persistenceReadResultValue) as String)
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemPlaySounds, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemPlaySounds, defaultPlaySoundsValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val soundsResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemPlaySounds, PersistenceSource.SharedPreferences)
        playSounds = soundsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Boolean
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemPlayIntro, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemPlayIntro, defaultPlayIntroValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val introResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemPlayIntro, PersistenceSource.SharedPreferences)
        playIntros = introResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Boolean
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemSkipPlayedLevelsIntro, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemSkipPlayedLevelsIntro, defaultSkipPlayedLevelsIntro, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val playedLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemSkipPlayedLevelsIntro, PersistenceSource.SharedPreferences)
        skipPlayedLevelIntros = playedLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Boolean
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemEasyMode, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemEasyMode, defaultEasyModeValue, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val easyModeResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemEasyMode, PersistenceSource.SharedPreferences)
        easyMode = easyModeResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Boolean
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemStartLevel, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemStartLevel, defaultStartLevel, PersistenceDataType.Number, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val startLevelResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemStartLevel, PersistenceSource.SharedPreferences)
        currentStartLevel = (startLevelResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Double).toInt()
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemUnlockAll, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemUnlockAll, defaultUnlockAllLevels, PersistenceDataType.Boolean, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        val unlockAllLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(persistenceItemUnlockAll, PersistenceSource.SharedPreferences)
        unlockAllLevels = unlockAllLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as Boolean
    }

    fun setAppContext(ctxt: Context)
    {
        context = ctxt
    }

    fun getAppContext(): Context
    {
        return context
    }

    fun setControlType(newType: ControlType)
    {
        currentControlScheme = newType
        IoGPersistenceManager.getSharedManager().saveValue(persistenceItemControlScheme, newType.name, PersistenceDataType.String, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
    }

    fun getControlType(): ControlType
    {
        return currentControlScheme
    }

    fun setLayoutScheme(newScheme: GameScreenLayoutScheme)
    {
        currentLayoutScheme = newScheme
        IoGPersistenceManager.getSharedManager().saveValue(persistenceItemLayoutScheme, newScheme.name, PersistenceDataType.String, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
    }

    fun getLayoutScheme(): GameScreenLayoutScheme
    {
        return currentLayoutScheme
    }

    fun setStartLevel(level: Int)
    {
        currentStartLevel = level
        IoGPersistenceManager.getSharedManager().saveValue(persistenceItemStartLevel, level, PersistenceDataType.Number, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
    }

    fun getStartLevel(): Int
    {
        return currentStartLevel
    }

    fun getLastLevelNumber(): Int
    {
        val authorizedLevelsEntry = IoGPersistenceManager.getSharedManager().readValue(persistenceItemAuthorizedLevels, PersistenceSource.SharedPreferences)
        val authorizedLevels = authorizedLevelsEntry.get(IoGConfigurationManager.persistenceReadResultValue) as List<Map<String, Any>>
        var sortedAuthorizedLevelsList = mutableListOf<Map<String, Any>>()
        for (nextLevelEntry in authorizedLevels)
        {
            val entryLevel = (nextLevelEntry[persistenceItemGameboardNumber] as Double).toInt()
            var inserted = false
            var index = 0
            for (nextAuthEntry in sortedAuthorizedLevelsList)
            {
                val nextAuthEntryLevel = (nextAuthEntry[persistenceItemGameboardNumber] as Double).toInt()
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
        return (sortedAuthorizedLevelsList.last()[persistenceItemGameboardNumber] as Double).toInt()
    }

    fun addAuthorizedLevel(number : Int, name: String)
    {
        if (!IoGPersistenceManager.getSharedManager().checkForValue(persistenceItemAuthorizedLevels, PersistenceSource.SharedPreferences))
        {
            val authorizedLevel = mapOf(persistenceItemGameboardNumber to number, persistenceItemGameboardName to name)
            IoGPersistenceManager.getSharedManager().saveValue(persistenceItemAuthorizedLevels, mutableListOf(authorizedLevel), PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        else
        {
            val authorizedLevelsEntry = IoGPersistenceManager.getSharedManager().readValue(persistenceItemAuthorizedLevels, PersistenceSource.SharedPreferences)
            var authorizedLevels = authorizedLevelsEntry.get(IoGConfigurationManager.persistenceReadResultValue) as MutableList<Map<String, Any>>
            var addEntry = true
            for (nextEntry in authorizedLevels)
            {
                val entryNumber = (nextEntry[persistenceItemGameboardNumber] as Double).toInt()
                if (entryNumber == number)
                {
                    addEntry = false
                    break
                }
            }
            if (addEntry)
            {
                var authorizedLevel = mapOf(persistenceItemGameboardNumber to number, persistenceItemGameboardName to name)
                authorizedLevels.add(authorizedLevel)
                IoGPersistenceManager.getSharedManager().saveValue(persistenceItemAuthorizedLevels, authorizedLevels, PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
            }
        }
    }

    fun filenameForProductIdentifier(identifier: String): String
    {
        var filename = "gameboard_set_"
        if (gameboardSet2ProductIdentifiers.contains(identifier))
        {
            filename += "2"
        }
        else if (gameboardSet3ProductIdentifiers.contains(identifier))
        {
            filename += "3"
        }
        else if (gameboardSet4ProductIdentifiers.contains(identifier))
        {
            filename += "4"
        }
        else if (gameboardSet5ProductIdentifiers.contains(identifier))
        {
            filename += "5"
        }
        return filename
    }
}