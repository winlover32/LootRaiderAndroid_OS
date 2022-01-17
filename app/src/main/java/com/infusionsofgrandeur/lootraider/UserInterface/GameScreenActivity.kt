package com.infusionsofgrandeur.lootraider.UserInterface

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo

import kotlin.experimental.and
import kotlin.experimental.or

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager

import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceSource;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceDataType;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceLifespan;
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager.PersistenceProtectionLevel;
import com.infusionsofgrandeur.lootraider.GameObjects.*

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager.Coordinate
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager.TilePosition
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.SoundManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.GameObjects.Player.ControlDirection
import com.infusionsofgrandeur.lootraider.UserInterface.ControlView.ControllerDirection
import com.infusionsofgrandeur.lootraider.UserInterface.ControlView.ControlSet

import com.infusionsofgrandeur.lootraider.R

import com.infusionsofgrandeur.lootraider.databinding.ActivityGameScreenBinding
import java.util.*

import android.util.Log
import android.view.View.*

class GameScreenActivity : AppCompatActivity(), SurfaceHolder.Callback, ControlView.ControlDelegate, GameStateManager.GameStateDelegate, Player.PlayerActionDelegate
{

    data class OnscreenSprite(var xPos: Int, var yPos: Int, var imageView: ImageView)
    {}

    lateinit var binding: ActivityGameScreenBinding

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    // UI elements
    private var viewStub: ViewStub? = null
    private var pauseButton: ImageButton? = null
    private var quitButton: ImageButton? = null
    private var stasisFieldLeftButton: ImageButton? = null
    private var stasisFieldRightButton: ImageButton? = null
    private var scoreTextView: TextView? = null
    private var livesTextView: TextView? = null
    private var levelNumberTextView: TextView? = null
    private var levelNameTextView: TextView? = null
    private var pauseTextView: TextView? = null
    private var controlImageView: ImageView? = null
    private var horizontalControlImageView: ImageView? = null
    private var verticalControlImageView: ImageView? = null
    private var controlOverlayView: ControlView? = null
    private var horizontalControlOverlayView: ControlView? = null
    private var verticalControlOverlayView: ControlView? = null
    private var gameScreenView: SurfaceView? = null
    private var pauseScreenView: ImageView? = null
    private var surfaceHolder: SurfaceHolder? = null

    // State variables
    lateinit private var currentLevel : Gameboard
    private var gameLoopTimer: Timer? = null
    private var entityIdentifier = 0
    private var imageViewsForEntities = mutableMapOf<Int, OnscreenSprite>()
    private var escapeLadderRevealed = false
    private var revealCurtainProgressTimer : Timer? = null
    private var revealTitleProgressTimer: Timer? = null
    private var revealCurtainPullLevel = 0
    private var levelNumberAlpha = 0.0F
    private var levelNameAlpha = 0.0F
    private var ignoreInput = false
    private var xAxisMultiplicationFactor = 1
    private var yAxisMultiplicationFactor = 1
    private var spriteWidth = 0
    private var spriteHeight = 0
    private lateinit var sourceRect: Rect
    private lateinit var gameboardRect: Rect
    private lateinit var gameboardBitmap: Bitmap
    private var scale = 0.0F
    private var scaledGameBoardWidthInPixels = 0.0F
    private var scaledGameBoardHeightInPixels = 0.0F
    private var scaledSpriteWidthInPixels = 0
    private var scaledSpriteHeightInPixels = 0
    private var paused = false
    private var lastUpdatedScore = -1
    private var lastUpdatedNumberOfLives = -1
    private var escapeLadderOriginalTileCoordinates = mutableListOf<Coordinate>()
    private var escapeLadderOriginalTileNumbers = mutableListOf<Int>()
    private var escapeLadderOriginalTileAttributes = mutableListOf<MutableList<Int>>()
    private var skipGuardUpdate = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        val layoutScheme = ConfigurationManager.getLayoutScheme()
        var baseLayout = ConstraintLayout(this)
        val level = GameStateManager.getCurrentLevel() - 1
        viewStub = binding.gameScreenViewStub
        currentLevel = GameboardManager.getGameboard(level)
        when (layoutScheme)
        {
            ConfigurationManager.GameScreenLayoutScheme.Vertical -> {
                viewStub?.layoutResource = R.layout.game_screen_vertical
                baseLayout = viewStub?.inflate() as ConstraintLayout
                controlImageView = baseLayout.findViewById(R.id.game_screen_controlpad_view)
                controlOverlayView = baseLayout.findViewById(R.id.game_screen_controlpad_overlay)
                controlOverlayView?.controlSet = ControlSet.BothAxes
                controlOverlayView?.delegate = this
            }
            ConfigurationManager.GameScreenLayoutScheme.Horizontal1 -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewStub?.layoutResource = R.layout.game_screen_horizontal_1
                baseLayout = viewStub?.inflate() as ConstraintLayout
                controlImageView = baseLayout.findViewById(R.id.game_screen_controlpad_view)
                controlOverlayView = baseLayout.findViewById(R.id.game_screen_controlpad_overlay)
                controlOverlayView?.controlSet = ControlSet.BothAxes
                controlOverlayView?.delegate = this
            }
            ConfigurationManager.GameScreenLayoutScheme.Horizontal2 -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewStub?.layoutResource = R.layout.game_screen_horizontal_2
                baseLayout = viewStub?.inflate() as ConstraintLayout
                controlImageView = baseLayout.findViewById(R.id.game_screen_controlpad_view)
                controlOverlayView = baseLayout.findViewById(R.id.game_screen_controlpad_overlay)
                controlOverlayView?.controlSet = ControlSet.BothAxes
                controlOverlayView?.delegate = this
            }
            ConfigurationManager.GameScreenLayoutScheme.Horizontal3 -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewStub?.layoutResource = R.layout.game_screen_horizontal_3
                baseLayout = viewStub?.inflate() as ConstraintLayout
                horizontalControlImageView = baseLayout.findViewById(R.id.game_screen_horizontal_controlpad_view)
                verticalControlImageView = baseLayout.findViewById(R.id.game_screen_vertical_controlpad_view)
                horizontalControlOverlayView = baseLayout.findViewById(R.id.game_screen_horizontal_controlpad_overlay)
                verticalControlOverlayView = baseLayout.findViewById(R.id.game_screen_vertical_controlpad_overlay)
                horizontalControlOverlayView?.controlSet = ControlSet.Horizontal
                verticalControlOverlayView?.controlSet = ControlSet.Vertical
                horizontalControlOverlayView?.delegate = this
                verticalControlOverlayView?.delegate = this
            }
            ConfigurationManager.GameScreenLayoutScheme.Horizontal4 -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewStub?.layoutResource = R.layout.game_screen_horizontal_4
                baseLayout = viewStub?.inflate() as ConstraintLayout
                horizontalControlImageView = baseLayout.findViewById(R.id.game_screen_horizontal_controlpad_view)
                verticalControlImageView = baseLayout.findViewById(R.id.game_screen_vertical_controlpad_view)
                horizontalControlOverlayView = baseLayout.findViewById(R.id.game_screen_horizontal_controlpad_overlay)
                verticalControlOverlayView = baseLayout.findViewById(R.id.game_screen_vertical_controlpad_overlay)
                horizontalControlOverlayView?.controlSet = ControlSet.Horizontal
                verticalControlOverlayView?.controlSet = ControlSet.Vertical
                horizontalControlOverlayView?.delegate = this
                verticalControlOverlayView?.delegate = this
            }
            ConfigurationManager.GameScreenLayoutScheme.Horizontal5 -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                viewStub?.layoutResource = R.layout.game_screen_horizontal_5
                baseLayout = viewStub?.inflate() as ConstraintLayout
                controlImageView = baseLayout.findViewById(R.id.game_screen_controlpad_view)
                controlOverlayView = baseLayout.findViewById(R.id.game_screen_controlpad_overlay)
                controlOverlayView?.controlSet = ControlSet.BothAxes
                controlOverlayView?.delegate = this
            }
        }
        pauseButton = baseLayout.findViewById(R.id.game_screen_pause_image_button)
        quitButton = baseLayout.findViewById(R.id.game_screen_quit_image_button)
        stasisFieldLeftButton = baseLayout.findViewById(R.id.game_screen_stasis_field_left_image_button)
        stasisFieldRightButton = baseLayout.findViewById(R.id.game_screen_stasis_field_right_image_button)
        scoreTextView = baseLayout.findViewById(R.id.game_screen_score_text_view)
        livesTextView = baseLayout.findViewById(R.id.game_screen_lives_text_view)
        levelNumberTextView = baseLayout.findViewById(R.id.game_screen_level_number_text_view)
        levelNameTextView = baseLayout.findViewById(R.id.game_screen_level_name_text_view)
        pauseTextView = baseLayout.findViewById(R.id.game_screen_paused_text_view)
        gameScreenView = baseLayout.findViewById(R.id.game_screen_gameboard_view)
        pauseScreenView = baseLayout.findViewById(R.id.game_screen_pause_view)
        pauseButton?.setOnClickListener { pauseGame() }
        quitButton?.setOnClickListener { quitGame() }
        stasisFieldLeftButton?.setOnClickListener { fireStasisFieldLeft() }
        stasisFieldRightButton?.setOnClickListener { fireStasisFieldRight() }

        surfaceHolder = gameScreenView?.holder
        surfaceHolder?.addCallback(this)

        gameScreenView?.setOnTouchListener(OnTouchListener { v, event ->
            if (levelNumberAlpha < 1.0 || levelNameAlpha < 1.0)
            {
                levelNumberAlpha = 1.0F
                levelNameAlpha = 1.0F
                revealCurtainPullLevel = 80
            }
            else
            {
                revealCurtainPullLevel = 80
            }
            true
        })
        GameStateManager.delegate = this
        startLevel(false)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View?
    {
        return super.onCreateView(name, context, attrs)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder)
    {
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int)
    {
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder)
    {
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean
    {
        val padXAxis: Float = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val padYAxis: Float = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
        val stickXAxis: Float = event.getAxisValue(MotionEvent.AXIS_X)
        val stickYAxis: Float = event.getAxisValue(MotionEvent.AXIS_Y)
        var xaxis: Float = 0.0F
        var yaxis: Float = 0.0F
        if (padXAxis != 0.0F || padYAxis != 0.0F)
        {
            xaxis = padXAxis
            yaxis = padYAxis
        }
        else if (stickXAxis != 0.0F || stickYAxis != 0.0F)
        {
            xaxis = stickXAxis
            yaxis = stickYAxis
        }
        if (xaxis == 0.0F && yaxis == 0.0F)
        {
            val player = GameStateManager.getPlayer()
            if (player.direction != ControlDirection.Still)
            {
                controlReleased()
            }
            return true
        }

        when (event.action)
        {
            MotionEvent.ACTION_MOVE -> {
                if (yaxis < (-1.0 * ConfigurationManager.gamepadDirectionalBuffer))
                {
                    directionTapped(ControllerDirection.Up)
                }
                else if (yaxis > ConfigurationManager.gamepadDirectionalBuffer)
                {
                    directionTapped(ControllerDirection.Down)
                }
                else if (xaxis < (-1.0 * ConfigurationManager.gamepadDirectionalBuffer))
                {
                    directionTapped(ControllerDirection.Left)
                }
                else if (xaxis > (1.0 * ConfigurationManager.gamepadDirectionalBuffer))
                {
                    directionTapped(ControllerDirection.Right)
                }
                else
                {
                    directionTapped(ControllerDirection.Center)
                }
            }
            else -> {
                directionTapped(ControllerDirection.Center)
            }
        }
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean
    {

        when (event.keyCode)
        {
            KeyEvent.KEYCODE_DPAD_UP -> {
                directionTapped(ControllerDirection.Up)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                directionTapped(ControllerDirection.Down)
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                directionTapped(ControllerDirection.Left)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                directionTapped(ControllerDirection.Right)
            }
            KeyEvent.KEYCODE_BUTTON_X -> {
                fireStasisFieldLeft()
            }
            KeyEvent.KEYCODE_BUTTON_B -> {
                fireStasisFieldRight()
            }
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean
    {
        when (keyCode)
        {

        }
        return true
    }
    // MARK: Game Logic

    fun pauseGame()
    {
        if (!paused)
        {
            paused = true
            pauseScreenView?.visibility = View.VISIBLE
            pauseTextView?.visibility = View.VISIBLE
            gameLoopTimer?.cancel()
            gameLoopTimer = null
            firebaseAnalytics.logEvent("PauseGame", null)
        }
        else
        {
            paused = false
            pauseScreenView?.visibility = View.INVISIBLE
            pauseTextView?.visibility = View.INVISIBLE
            gameLoopTimer = Timer()
            gameLoopTimer?.schedule(object: TimerTask() {
                override fun run() {
                    gameLoopTimer = null
                    runOnUiThread {
                        processChanges()
                    }
                }
            }, ConfigurationManager.spriteAnimationLoopTimerDelay)
            firebaseAnalytics.logEvent("UnpauseGame", null)
        }
    }

    fun quitGame()
    {
        if (paused)
        {
            pauseScreenView?.visibility = View.INVISIBLE
            pauseTextView?.visibility = View.INVISIBLE
        }
        endLevel()
        updateScore(0)
        updateLives(ConfigurationManager.defaultStartingNumberOfLives)
        GameStateManager.resetGameStats()
        firebaseAnalytics.logEvent("QuitGame", null)
        if (gameLoopTimer != null)
        {
            gameLoopTimer?.cancel()
            gameLoopTimer = null
        }
        finish()
    }

    fun startLevel(skipReveal: Boolean)
    {
        prepareLevel()
        pauseScreenView?.visibility = View.INVISIBLE
        pauseTextView?.visibility = View.INVISIBLE
        stasisFieldLeftButton?.isEnabled = true
        stasisFieldRightButton?.isEnabled = true
        pauseButton?.isEnabled = true
        quitButton?.isEnabled = true
        if (!IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPlayedLevels, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemPlayedLevels, arrayOf(currentLevel.identifier), PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        else
        {
            val playedLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPlayedLevels, PersistenceSource.SharedPreferences)
            val levelsList = playedLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
            var playedLevels = mutableListOf<String>()
            playedLevels.addAll(levelsList)
            if (!playedLevels.contains(currentLevel.identifier))
            {
                playedLevels.add(currentLevel.identifier)
                IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemPlayedLevels, playedLevels, PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
            }
        }
        if (!skipReveal)
        {
            revealLevel()
        }
        else
        {
            runOnUiThread {
                gameLoopTimer = Timer()
                gameLoopTimer?.schedule(object: TimerTask() {
                    override fun run() {
                        gameLoopTimer = null
                        runOnUiThread {
                            processChanges()
                        }
                    }
                }, ConfigurationManager.spriteAnimationLoopTimerDelay)
            }
        }
    }

    fun fireStasisFieldLeft()
    {
        if (ignoreInput)
        {
            return
        }
        val stasisFieldOne = GameStateManager.getStasisFieldOne()
        val stasisFieldTwo = GameStateManager.getStasisFieldTwo()
        val player = GameStateManager.getPlayer()
        val playerX = player.xTile
        val playerY = player.yTile
        var availableStasisField : StasisField? = null
        if (!stasisFieldOne.activated)
        {
            if (!(stasisFieldTwo.activated && playerX - 1 == stasisFieldTwo.xTile && playerY == stasisFieldTwo.yTile))
            {
                availableStasisField = stasisFieldOne
            }
        }
        else if (!stasisFieldTwo.activated)
        {
            if (!(playerX - 1 == stasisFieldOne.xTile && playerY == stasisFieldOne.yTile))
            {
                availableStasisField = stasisFieldTwo
            }
        }
        if (availableStasisField != null)
        {
            if (playerX > 0)
            {
                val targetTileNumber = currentLevel.tileMap[playerY][playerX - 1]
                val baseTargetTileNumber = currentLevel.tileMap[playerY + 1][playerX - 1]
                val tileSprite = SpriteManager.getSprite(targetTileNumber)
                val baseTileSprite = SpriteManager.getSprite(baseTargetTileNumber)
                val spriteCharacteristic = tileSprite?.header?.get(0)
                val baseSpriteCharacteristic = baseTileSprite?.header?.get(0)
                // First, check to see if it's the right kind of tile
                if ((spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderTraversable == ConfigurationManager.spriteHeaderTraversable && (spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderHangable == 0 && (spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderClimable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderTraversable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderHangable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderClimable == 0)
                {
                    // The check to make sure there's no platform occupying the space
                    val platforms = GameStateManager.getPlatforms()
                    for (nextPlatform in platforms)
                    {
                        if (nextPlatform.xTile == playerX - 1 && nextPlatform.yTile == playerY)
                        {
                            return
                        }
                    }
                    // All clear
                    availableStasisField.activate(playerX - 1, playerY, xAxisMultiplicationFactor, yAxisMultiplicationFactor)
                    if (player.xPos < player.xTile * GameStateManager.getTileWidth())
                    {
                        player.xPos = player.xTile * GameStateManager.getTileWidth()
                        if (player.direction == ControlDirection.Left || player.direction == ControlDirection.UpLeft || player.direction == ControlDirection.DownLeft)
                        {
                            player.direction = ControlDirection.Still
                        }
                    }
                }
            }
        }
    }
    fun fireStasisFieldRight()
    {
        if (ignoreInput)
        {
            return
        }
        val stasisFieldOne = GameStateManager.getStasisFieldOne()
        val stasisFieldTwo = GameStateManager.getStasisFieldTwo()
        val player = GameStateManager.getPlayer()
        val playerX = player.xTile
        val playerY = player.yTile
        var availableStasisField : StasisField? = null
        if (!stasisFieldOne.activated)
        {
            if (!(stasisFieldTwo.activated && playerX + 1 == stasisFieldTwo.xTile && playerY == stasisFieldTwo.yTile))
            {
                availableStasisField = stasisFieldOne
            }
        }
        else if (!stasisFieldTwo.activated)
        {
            if (!(playerX + 1 == stasisFieldOne.xTile && playerY == stasisFieldOne.yTile))
            {
                availableStasisField = stasisFieldTwo
            }
        }
        if (availableStasisField != null)
        {
            if (playerX < currentLevel.width - 1)
            {
                val targetTileNumber = currentLevel.tileMap[playerY][playerX + 1]
                val baseTargetTileNumber = currentLevel.tileMap[playerY + 1][playerX + 1]
                val tileSprite = SpriteManager.getSprite(targetTileNumber)
                val baseTileSprite = SpriteManager.getSprite(baseTargetTileNumber)
                val spriteCharacteristic = tileSprite?.header?.get(0)
                val baseSpriteCharacteristic = baseTileSprite?.header?.get(0)
                // First, check to see if it's the right kind of tile
                if ((spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderTraversable == ConfigurationManager.spriteHeaderTraversable && (spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderHangable == 0 && (spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderClimable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderTraversable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderHangable == 0 && (baseSpriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderClimable == 0)
                {
                    // The check to make sure there's no platform occupying the space
                    val platforms = GameStateManager.getPlatforms()
                    for (nextPlatform in platforms)
                    {
                        if (nextPlatform.xTile == playerX + 1 && nextPlatform.yTile == playerY)
                        {
                            return
                        }
                    }
                    // All clear
                    availableStasisField.activate(playerX + 1, playerY, xAxisMultiplicationFactor, yAxisMultiplicationFactor)
                    if (player.xPos > player.xTile * GameStateManager.getTileWidth())
                    {
                        player.xPos = player.xTile * GameStateManager.getTileWidth()
                        if (player.direction == ControlDirection.Left || player.direction == ControlDirection.UpLeft || player.direction == ControlDirection.DownLeft)
                        {
                            player.direction = ControlDirection.Still
                        }
                    }
                }
            }
        }
    }
    private fun processChanges()
    {
        var platforms = GameStateManager.getPlatforms()
        var teleporters = GameStateManager.getTeleporters()
        var guards = GameStateManager.getGuards()
        var goldBars = GameStateManager.getGoldBars()
        var player = GameStateManager.getPlayer()
        var processGuards = true    // For Easy Mode

        ignoreInput = false

        for (nextTeleporter in teleporters)
        {
            var onScreenSprite = imageViewsForEntities[nextTeleporter.entityID]
            var imageView = onScreenSprite?.imageView
            if (imageView != null)
            {
                nextTeleporter.runCycle(imageView)
                if (xAxisMultiplicationFactor != 1 || yAxisMultiplicationFactor != 1)
                {
                    imageViewsForEntities[nextTeleporter.entityID]?.xPos = nextTeleporter.xPos * xAxisMultiplicationFactor
                    imageViewsForEntities[nextTeleporter.entityID]?.yPos = nextTeleporter.yPos * yAxisMultiplicationFactor
                }
            }
        }

        for (nextPlatform in platforms)
        {
            var onScreenSprite = imageViewsForEntities[nextPlatform.entityID]
            var imageView = onScreenSprite?.imageView
            if (imageView != null)
            {
                nextPlatform.runCycle(imageView)
                onScreenSprite?.xPos = imageView.left
                onScreenSprite?.yPos = imageView.top
                if (xAxisMultiplicationFactor != 1 || yAxisMultiplicationFactor != 1)
                {
                    imageViewsForEntities[nextPlatform.entityID]?.xPos = nextPlatform.xPos * xAxisMultiplicationFactor
                    imageViewsForEntities[nextPlatform.entityID]?.yPos = nextPlatform.yPos * yAxisMultiplicationFactor
                }
            }
        }

        if (ConfigurationManager.easyMode)
        {
            if (skipGuardUpdate)
            {
                processGuards = false
            }
            skipGuardUpdate = !skipGuardUpdate
        }
        if (processGuards)
        {
            for (nextGuard in guards)
            {
                var onScreenSprite = imageViewsForEntities[nextGuard.entityID]
                var imageView = onScreenSprite?.imageView
                if (imageView != null)
                {
                    nextGuard.runChasePattern(imageView)
                    if (nextGuard.inStasis)
                    {
                        onScreenSprite?.xPos = nextGuard.xPos
                        onScreenSprite?.yPos = nextGuard.yPos
                    }
                    else
                    {
                        onScreenSprite?.xPos = imageView.left
                        onScreenSprite?.yPos = imageView.top
                    }
                    nextGuard.detectCollisions(imageView)
                    if (xAxisMultiplicationFactor != 1 || yAxisMultiplicationFactor != 1)
                    {
                        imageViewsForEntities[nextGuard.entityID]?.xPos = nextGuard.xPos * xAxisMultiplicationFactor
                        imageViewsForEntities[nextGuard.entityID]?.yPos = nextGuard.yPos * yAxisMultiplicationFactor
                    }
                }
            }
        }

        var onScreenSprite = imageViewsForEntities[player.entityID]
        var playerImageView = onScreenSprite?.imageView
        if (playerImageView != null)
        {
            player.updatePosition(playerImageView)
            onScreenSprite?.xPos = playerImageView.left
            onScreenSprite?.yPos = playerImageView.top
            player.detectCollisions(playerImageView)
            if (xAxisMultiplicationFactor != 1 || yAxisMultiplicationFactor != 1)
            {
                imageViewsForEntities[player.entityID]?.xPos = player.xPos * xAxisMultiplicationFactor
                imageViewsForEntities[player.entityID]?.yPos = player.yPos * yAxisMultiplicationFactor
            }
        }

        for (nextBar in goldBars)
        {
            var onScreenSprite = imageViewsForEntities[nextBar.entityID]
            var imageView = onScreenSprite?.imageView
            if (imageView != null)
            {
                if (nextBar.possessedBy != null)
                {
                    imageView.visibility = View.GONE
                }
                else
                {
                    imageView.visibility = View.VISIBLE
                }
                if (xAxisMultiplicationFactor != 1 || yAxisMultiplicationFactor != 1)
                {
                    imageViewsForEntities[nextBar.entityID]?.xPos = nextBar.xPos * xAxisMultiplicationFactor
                    imageViewsForEntities[nextBar.entityID]?.yPos = nextBar.yPos * yAxisMultiplicationFactor
                }
            }
        }

        if (GameStateManager.getStasisFieldOne().activated)
        {
            GameStateManager.getStasisFieldOne().advance(xAxisMultiplicationFactor, yAxisMultiplicationFactor)
        }
        if (GameStateManager.getStasisFieldTwo().activated)
        {
            GameStateManager.getStasisFieldTwo().advance(xAxisMultiplicationFactor, yAxisMultiplicationFactor)
        }
        updateSurface()
        if (GameStateManager.getCurrentScore() != lastUpdatedScore)
        {
            updateScore(GameStateManager.getCurrentScore())
        }
        if (GameStateManager.getNumberOfLives() != lastUpdatedNumberOfLives)
        {
            updateLives(GameStateManager.getNumberOfLives())
        }
    }

    fun updateSurface()
    {
        if (paused)
        {
            return
        }
        val startTime = Calendar.getInstance().time.time
        var surface = surfaceHolder?.surface
        if (surface != null && surface.isValid)
        {
            val canvas = surfaceHolder?.lockCanvas()

            canvas?.drawBitmap(gameboardBitmap, null, gameboardRect, null)

            // Now draw the game objects
            val keys = imageViewsForEntities.keys
            for (nextKey in keys) {
                val entityOnscreenSprite = imageViewsForEntities[nextKey]
                val imageView = entityOnscreenSprite!!.imageView
                if (imageView.visibility == View.GONE)
                {
                    continue
                }
                else
                {
                    if (scale > 1.0)
                    {
                        val startX = (scale * entityOnscreenSprite!!.xPos).toInt()
                        val startY = (scale * entityOnscreenSprite!!.yPos).toInt()
                        val endX = startX + scaledSpriteWidthInPixels
                        val endY = startY + scaledSpriteHeightInPixels
                        val bitmapDrawable = imageView.drawable as BitmapDrawable
                        val bitmap = bitmapDrawable.bitmap
                        val destRect = Rect(startX, startY, endX, endY)
                        canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                    }
                    else
                    {
                        val startX = entityOnscreenSprite!!.xPos
                        val startY = entityOnscreenSprite!!.yPos
                        val endX = startX + spriteWidth
                        val endY = startY + spriteHeight
                        val bitmapDrawable = imageView.drawable as BitmapDrawable
                        val bitmap = bitmapDrawable.bitmap
                        val destRect = Rect(startX, startY, endX, endY)
                        canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                    }
                }
            }

            // And finally, if the stasis fields need to be displayed
            if (GameStateManager.getStasisFieldOne().activated)
            {
                val stasisField = GameStateManager.getStasisFieldOne()
                val imageView = stasisField.imageView
                if (scale > 1.0)
                {
                    val startX = (scale * (stasisField.xTile * spriteWidth)).toInt()
                    val startY = (scale * (stasisField.yTile * spriteHeight)).toInt()
                    val endX = startX + scaledSpriteWidthInPixels
                    val endY = startY + scaledSpriteHeightInPixels
                    val bitmapDrawable = imageView!!.drawable as BitmapDrawable
                    val bitmap = bitmapDrawable.bitmap
                    val destRect = Rect(startX, startY, endX, endY)
                    canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                }
                else
                {
                    val startX = stasisField.xTile * spriteWidth
                    val startY = stasisField.yTile * spriteHeight
                    val endX = startX + spriteWidth
                    val endY = startY + spriteHeight
                    val bitmapDrawable = imageView!!.drawable as BitmapDrawable
                    val bitmap = bitmapDrawable.bitmap
                    val destRect = Rect(startX, startY, endX, endY)
                    canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                }
            }
            if (GameStateManager.getStasisFieldTwo().activated)
            {
                val stasisField = GameStateManager.getStasisFieldTwo()
                val imageView = stasisField.imageView
                if (scale > 1.0)
                {
                    val startX = (scale * (stasisField.xTile * spriteWidth)).toInt()
                    val startY = (scale * (stasisField.yTile * spriteHeight)).toInt()
                    val endX = startX + scaledSpriteWidthInPixels
                    val endY = startY + scaledSpriteHeightInPixels
                    val bitmapDrawable = imageView!!.drawable as BitmapDrawable
                    val bitmap = bitmapDrawable.bitmap
                    val destRect = Rect(startX, startY, endX, endY)
                    canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                }
                else
                {
                    val startX = stasisField.xTile * spriteWidth
                    val startY = stasisField.yTile * spriteHeight
                    val endX = startX + spriteWidth
                    val endY = startY + spriteHeight
                    val bitmapDrawable = imageView!!.drawable as BitmapDrawable
                    val bitmap = bitmapDrawable.bitmap
                    val destRect = Rect(startX, startY, endX, endY)
                    canvas?.drawBitmap(bitmap, sourceRect, destRect, null)
                }
            }

            if (canvas != null)
            {
                surfaceHolder?.unlockCanvasAndPost(canvas)
            }

            val endTime = Calendar.getInstance().time.time
            val elapsedTime = endTime - startTime
            val timerDelay = if (elapsedTime < ConfigurationManager.spriteAnimationLoopTimerDelay) ConfigurationManager.spriteAnimationLoopTimerDelay - elapsedTime else 1
            // Somehow after a few seconds the frame rate speeds up no matter what timer delay we put in, so trying methods to keep that from happening
            if (gameLoopTimer == null) {
                gameLoopTimer = Timer()
                gameLoopTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        gameLoopTimer = null
                        runOnUiThread {
                            processChanges()
                        }
                    }
                }, timerDelay)
            }
        }
        else
        {
            val endTime = Calendar.getInstance().time.time
            val elapsedTime = endTime - startTime
            val timerDelay = if (elapsedTime < ConfigurationManager.spriteAnimationLoopTimerDelay) ConfigurationManager.spriteAnimationLoopTimerDelay - elapsedTime else 1
            gameLoopTimer = Timer()
            gameLoopTimer?.schedule(object : TimerTask() {
                override fun run() {
                    gameLoopTimer = null
                    runOnUiThread {
                        processChanges()
                    }
                }
            }, timerDelay)
        }
    }

    fun endLevel()
    {
        val player = GameStateManager.getPlayer()
        val stasis1 = GameStateManager.getStasisFieldOne()
        val stasis2 = GameStateManager.getStasisFieldTwo()
        ignoreInput = true
        if (controlImageView != null)
        {
            controlImageView?.setImageResource(R.mipmap.buttons_none_pressed)
        }
        else
        {
            horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_right_empty)
            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_down_empty)
        }
        stasisFieldLeftButton?.isEnabled = false
        stasisFieldRightButton?.isEnabled = false
        pauseButton?.isEnabled = false
        quitButton?.isEnabled = false
        player.direction = ControlDirection.Still
        player.desiredDirection = ControlDirection.Still
        if (gameLoopTimer != null)
        {
            gameLoopTimer?.cancel()
            gameLoopTimer = null
        }
        if (escapeLadderRevealed)
        {
            retractEscapeLadder()
        }
        if (stasis1.activated)
        {
            stasis1.dissipate()
        }
        if (stasis2.activated)
        {
            stasis2.dissipate()
        }
    }

    private fun prepareForNextLevel()
    {
        val level = GameStateManager.getCurrentLevel() - 1
        currentLevel = GameboardManager.getGameboard(level)
        gameScreenView?.visibility = View.INVISIBLE
    }

    private fun resetSprites()
    {
        imageViewsForEntities.clear()
    }

    private fun prepareLevel()
    {
        val tile0 = SpriteManager.getSprite(0)
        GameStateManager.setTileWidth(tile0!!.width)
        GameStateManager.setTileHeight(tile0!!.height)
        GameStateManager.resetLevel()
        spriteWidth = tile0!!.width
        spriteHeight = tile0!!.height
        scale = resources.displayMetrics.density
        scaledGameBoardWidthInPixels = scale * ((currentLevel.width * spriteWidth) / 160)
        scaledGameBoardHeightInPixels = scale * ((currentLevel.height * spriteHeight) / 160)
        scaledSpriteWidthInPixels = (scale * spriteWidth).toInt()
        scaledSpriteHeightInPixels = (scale * spriteHeight).toInt()
        sourceRect = Rect(0, 0, spriteWidth, spriteHeight)
        gameboardRect = Rect(0, 0, (currentLevel.width * spriteWidth * scale).toInt(), (currentLevel.height * spriteHeight * scale).toInt())
        resetSprites()
        for (row in 0 until currentLevel.height)
        {
            for (column in 0 until currentLevel.width)
            {
                var attribute = 0
                val spriteNumber = currentLevel.spriteMap[row][column]
                if (currentLevel.attributeMap[row].size > column && currentLevel.attributeMap[row][column].size > 0)
                {
                    attribute = currentLevel.attributeMap[row][column][0]
                }
                if (spriteNumber == ConfigurationManager.playerSpriteIndex)
                {
                    val sprite = SpriteManager.getSprite(spriteNumber)
                    if (sprite != null)
                    {
                        val image = SpriteManager.bitmapForSprite(sprite)
                        val imageView = ImageView(this)
                        imageView.setImageBitmap(image)
                        var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                        imageView.left = onscreenSprite.xPos
                        imageView.top = onscreenSprite.yPos
                        val newPlayer = Player(column * GameStateManager.getTileWidth (), row * GameStateManager.getTileHeight(), column, row, Entity.Motion.Still, ConfigurationManager.playerSpriteIndex)
                        newPlayer.delegate = this;
                        GameStateManager.setPlayer(newPlayer)
                        imageViewsForEntities[newPlayer.entityID] = onscreenSprite
                    }
                }
                else if (spriteNumber == ConfigurationManager.guardSpriteIndex)
                {
                    val sprite = SpriteManager.getSprite(spriteNumber)
                    if (sprite != null)
                    {
                        val image = SpriteManager.bitmapForSprite(sprite)
                        val imageView = ImageView(this)
                        imageView.setImageBitmap(image)
                        var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                        imageView.left = onscreenSprite.xPos
                        imageView.top = onscreenSprite.yPos
                        val newGuard = Guard(column * GameStateManager.getTileWidth(), row * GameStateManager.getTileHeight (), column, row, Entity.Motion.Still, ConfigurationManager.guardSpriteIndex)
                        GameStateManager.addGuard(newGuard)
                        imageViewsForEntities[newGuard.entityID] = onscreenSprite
                    }
                }
                else if (spriteNumber == ConfigurationManager.platformSpriteIndex)
                {
                    val speedAttribute = currentLevel.attributeMap[row][column][1] and ConfigurationManager.platformSpeedAttributeMask
                    val waitAttribute = currentLevel.attributeMap[row][column][1] and ConfigurationManager.platformWaitAttributeMask
                    var speed = Platform.PlatformSpeed.Slow
                    var wait = Platform.PlatformWait.Long
                    var direction = Entity.Motion.Still
                    if (speedAttribute and ConfigurationManager.platformSlowSpeedHeaderValue == ConfigurationManager.platformSlowSpeedHeaderValue)
                    {
                        speed = Platform.PlatformSpeed.Slow
                    }
                    else if (speedAttribute and ConfigurationManager.platformModerateSpeedHeaderValue == ConfigurationManager.platformModerateSpeedHeaderValue)
                    {
                        speed = Platform.PlatformSpeed.Moderate
                    }
                    else if (speedAttribute and ConfigurationManager.platformFastSpeedHeaderValue == ConfigurationManager.platformFastSpeedHeaderValue)
                    {
                        speed = Platform.PlatformSpeed.Fast
                    }
                    if (waitAttribute and ConfigurationManager.platformLongWaitHeaderValue == ConfigurationManager.platformLongWaitHeaderValue)
                    {
                        wait = Platform.PlatformWait.Long
                    }
                    else if (waitAttribute and ConfigurationManager.platformModerateWaitHeaderValue == ConfigurationManager.platformModerateWaitHeaderValue)
                    {
                        wait = Platform.PlatformWait.Moderate
                    }
                    else if (waitAttribute and ConfigurationManager.platformShortWaitHeaderValue == ConfigurationManager.platformShortWaitHeaderValue)
                    {
                        wait = Platform.PlatformWait.Short
                    }
                    if (attribute and ConfigurationManager.platformHorizontalHeaderValue == ConfigurationManager.platformHorizontalHeaderValue)
                    {
                        val sprite = SpriteManager.getSprite(spriteNumber)
                        if (sprite != null)
                        {
                            val image = SpriteManager.bitmapForSprite(sprite)
                            val imageView = ImageView(this)
                            imageView.setImageBitmap(image)
                            var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                            imageView.left = onscreenSprite.xPos
                            imageView.top = onscreenSprite.yPos
                            if (currentLevel.attributeMap[row][column][1] and ConfigurationManager.platformInitialDirectionLeft == ConfigurationManager.platformInitialDirectionLeft)
                            {
                                direction = Entity.Motion.PlatformLeft
                            }
                            else
                            {
                                direction = Entity.Motion.PlatformRight
                            }
                            val newPlatform = Platform(column * GameStateManager.getTileWidth(), row * GameStateManager.getTileHeight (), column, row, direction, ConfigurationManager.platformSpriteIndex, Platform.TravelAxis.Horizontal, speed, wait)
                            GameStateManager.addPlatform(newPlatform)
                            imageViewsForEntities[newPlatform.entityID] = onscreenSprite
                        }
                    }
                    else if (attribute and ConfigurationManager.platformVerticalHeaderValue == ConfigurationManager.platformVerticalHeaderValue)
                    {
                        val sprite = SpriteManager.getSprite(spriteNumber)
                        if (sprite != null)
                        {
                            val image = SpriteManager.bitmapForSprite(sprite)
                            val imageView = ImageView(this)
                            imageView.setImageBitmap(image)
                            var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                            imageView.left = onscreenSprite.xPos
                            imageView.top = onscreenSprite.yPos
                            if (currentLevel.attributeMap[row][column][1] and ConfigurationManager.platformInitialDirectionUp == ConfigurationManager.platformInitialDirectionUp)
                            {
                                direction = Entity.Motion.PlatformUp
                            }
                            else
                            {
                                direction = Entity.Motion.PlatformDown
                            }
                            val newPlatform = Platform(column * GameStateManager.getTileWidth(), row * GameStateManager.getTileHeight(), column, row, direction, ConfigurationManager.platformSpriteIndex, Platform.TravelAxis.Vertical, speed, wait)
                            GameStateManager.addPlatform(newPlatform)
                            imageViewsForEntities[newPlatform.entityID] = onscreenSprite
                        }
                    }
                }
                else if (spriteNumber == ConfigurationManager.teleporterSpriteIndex)
                {
                    val sprite = SpriteManager.getSprite(spriteNumber)
                    if (sprite != null)
                    {
                        val image = SpriteManager.bitmapForSprite(sprite)
                        val imageView = ImageView(this)
                        imageView.setImageBitmap(image)
                        var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                        imageView.left = onscreenSprite.xPos
                        imageView.top = onscreenSprite.yPos
                        val sendable = if (attribute and ConfigurationManager.teleporterSendableHeaderValue == ConfigurationManager.teleporterSendableHeaderValue) true else false
                        val receivable = if (attribute and ConfigurationManager.teleporterReceivableHeaderValue == ConfigurationManager.teleporterReceivableHeaderValue) true else false
                        val roundtrippable = if (attribute and ConfigurationManager.teleporterRoundTrippableValue == ConfigurationManager.teleporterRoundTrippableValue) true else false
                        val identifier = if (attribute and ConfigurationManager.teleporterPairableHeaderValue == ConfigurationManager.teleporterPairableHeaderValue) currentLevel.attributeMap[row][column][1] else null
                        val newTeleporter = Teleporter(column * GameStateManager.getTileWidth(), row * GameStateManager.getTileHeight(), column, row, Entity.Motion.Still, ConfigurationManager.teleporterSpriteIndex, sendable, receivable, roundtrippable, identifier)
                        GameStateManager.addTeleporter(newTeleporter)
                        imageViewsForEntities[newTeleporter.entityID] = onscreenSprite
                    }
                }
                else if (spriteNumber == ConfigurationManager.goldBarSpriteIndex)
                {
                    val sprite = SpriteManager.getSprite(spriteNumber)
                    if (sprite != null)
                    {
                        val image = SpriteManager.bitmapForSprite(sprite)
                        val imageView = ImageView(this)
                        imageView.setImageBitmap(image)
                        var onscreenSprite = OnscreenSprite(column * sprite.width, row * sprite.height, imageView)
                        imageView.left = onscreenSprite.xPos
                        imageView.top = onscreenSprite.yPos
                        val newGoldBar = GoldBar(column * GameStateManager.getTileWidth(), row * GameStateManager.getTileHeight(), column, row, Entity.Motion.Still, ConfigurationManager.goldBarSpriteIndex)
                        GameStateManager.addGoldBar(newGoldBar)
                        imageViewsForEntities[newGoldBar.entityID] = onscreenSprite
                    }
                 }
                if (attribute and ConfigurationManager.exitLadderBaseTileHeaderValue == ConfigurationManager.exitLadderBaseTileHeaderValue)
                {
                    GameStateManager.setEscapeLadderBase(Coordinate(column, row))
                }
            }
        }
        // If in Easy Mode, we need to pull out the last guard
        if (ConfigurationManager.easyMode)
        {
            val guards = GameStateManager.getGuards()
            val lastGuard = guards.last()
            if (lastGuard != null)
            {
                imageViewsForEntities.remove(lastGuard.entityID)
                GameStateManager.removeLastGuard()
            }
        }
        // Go back through and assign teleporter pairs
        var nextTeleporterEntry = 0
        for (row in 0 until currentLevel.height)
        {
            for (column in 0 until currentLevel.width)
            {
                val spriteNumber = currentLevel.spriteMap[row][column]
                if (spriteNumber == ConfigurationManager.teleporterSpriteIndex)
                {
                    val teleporter = GameStateManager.getTeleporters()[nextTeleporterEntry]
                    val attribute = currentLevel.attributeMap[row][column][0]
                    nextTeleporterEntry = nextTeleporterEntry + 1
                    if (attribute and ConfigurationManager.teleporterPairableHeaderValue == ConfigurationManager.teleporterPairableHeaderValue)
                    {
                        val pairIdentifier = currentLevel.attributeMap[row][column][2]
                        teleporter.pair = GameStateManager.getTeleporterForIdentifier(pairIdentifier)
                    }
                }
            }
        }
        // Build background gameboard image
        gameboardBitmap = Bitmap.createBitmap(currentLevel.width * spriteWidth, currentLevel.height * spriteHeight, Bitmap.Config.ARGB_8888)
        gameboardBitmap.setHasAlpha(false)
        var canvas  = Canvas(gameboardBitmap)
        for (row in 0 until currentLevel.height)
        {
            for (column in 0 until currentLevel.width)
            {
                val tileNumber = currentLevel.tileMap[row][column]
                val sprite = SpriteManager.getSprite(tileNumber)
                if (tileNumber > -1 && sprite != null)
                {
                    val startX = column * spriteWidth
                    val startY = row * spriteHeight
                    val endX = startX + spriteWidth
                    val endY = startY + spriteHeight
                    val destRect = Rect(startX, startY, endX, endY)
                    canvas.drawBitmap(SpriteManager.bitmapForSprite(sprite), sourceRect, destRect, null)
                }
                else
                {
                    continue
                }

            }
        }
    }

    private fun revealLevel()
    {
        if (shouldBypassReveal())
        {
            levelNameAlpha = 1.0F
            levelNumberAlpha = 1.0F
            gameScreenView?.visibility = View.VISIBLE
            runOnUiThread {
                dismissRevealCurtain()
            }
        }
        else if (!ConfigurationManager.playIntros)
        {
            val title = currentLevel.identifier
            val levelNumber = GameStateManager.getCurrentLevel()
            Timer().schedule(object: TimerTask() {
                override fun run() {
                    runOnUiThread {
                        transitionTitleToCurtain()
                    }
                }
            }, ConfigurationManager.titleEndTimerDelay)
            levelNameAlpha = 1.0F
            levelNumberAlpha = 1.0F
            levelNameTextView?.alpha = levelNameAlpha
            levelNumberTextView?.alpha = levelNumberAlpha
            levelNameTextView?.visibility = View.VISIBLE
            levelNumberTextView?.visibility = View.VISIBLE
            levelNumberTextView?.text = "Level $levelNumber"
            levelNameTextView?.text = "$title"
            gameScreenView?.visibility = View.VISIBLE
        }
        else
        {
            startTitleReveal()
        }
    }

    private fun startTitleReveal()
    {
        val title = currentLevel.identifier
        val levelNumber = GameStateManager.getCurrentLevel()
        levelNumberAlpha = 0.0F
        levelNameAlpha = 0.0F
        levelNameTextView?.alpha = levelNameAlpha
        levelNumberTextView?.alpha = levelNumberAlpha
        levelNameTextView?.visibility = View.VISIBLE
        levelNumberTextView?.visibility = View.VISIBLE
        levelNumberTextView?.text = "Level $levelNumber"
        levelNameTextView?.text = "$title"
        gameScreenView?.visibility = View.VISIBLE
        revealTitleProgressTimer = Timer()
        revealTitleProgressTimer?.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                runOnUiThread {
                    brightenTitle()
                }
            }
        }, ConfigurationManager.titleAnimationTimerDelay, ConfigurationManager.titleAnimationTimerDelay)
    }

    private fun brightenTitle()
    {
        if (levelNumberAlpha >= 1.0)
        {
            levelNameAlpha += ConfigurationManager.titleAlphaIncrementValue
            levelNameTextView?.alpha = levelNameAlpha
        }
        else
        {
            levelNumberAlpha += ConfigurationManager.titleAlphaIncrementValue
            levelNumberTextView?.alpha = levelNumberAlpha
        }
        if (levelNameAlpha >= 1.0)
        {
            revealTitleProgressTimer?.cancel()
            revealTitleProgressTimer = null
            Timer().schedule(object: TimerTask() {
                override fun run() {
                    runOnUiThread {
                        transitionTitleToCurtain()
                    }
                }
            }, ConfigurationManager.titleAnimationTimerDelay)
        }
    }

    private fun dismissRevealCurtain()
    {
        runOnUiThread {
            levelNameTextView?.visibility = View.GONE
            levelNumberTextView?.visibility = View.GONE
        }
        gameLoopTimer = Timer()
        gameLoopTimer?.schedule(object: TimerTask() {
            override fun run() {
                gameLoopTimer = null
                runOnUiThread {
                    processChanges()
                }
            }
        }, ConfigurationManager.spriteAnimationLoopTimerDelay)
    }

    private fun transitionTitleToCurtain()
    {
        // Hide title
        levelNameTextView?.visibility = View.INVISIBLE
        levelNumberTextView?.visibility = View.INVISIBLE
        // Set up the animation
        runOnUiThread {
            if (shouldBypassReveal() || !ConfigurationManager.playIntros)
            {
                dismissRevealCurtain()
            }
            else
            {
                pullCurtain()
            }
        }
    }

    private fun pullCurtain()
    {
        if (revealCurtainPullLevel == 80)
        {
            dismissRevealCurtain()
            return
        }
        val spotlight = BitmapFactory.decodeResource(resources, R.mipmap.spotlight)
        // First, build the curtain bitmap at twice the width and twice the height of the actual gameboard
        val curtainBackgroundBitmap = Bitmap.createBitmap(currentLevel.width * spriteWidth * 2, currentLevel.height * spriteHeight * 2, Bitmap.Config.ARGB_8888)
        curtainBackgroundBitmap.setHasAlpha(false)
        var curtainCanvas  = Canvas(curtainBackgroundBitmap)
        // Then draw the full gameboard onto the curtain canvas
        val curtainGameboardDestRect = Rect((currentLevel.width * spriteWidth / 2).toInt(), (currentLevel.height * spriteHeight / 2).toInt(), (currentLevel.width * spriteWidth / 2).toInt() + (currentLevel.width * spriteWidth).toInt(), (currentLevel.height * spriteHeight / 2).toInt() + (currentLevel.height * spriteHeight).toInt())
        curtainCanvas.drawRGB(0, 0, 0)
        curtainCanvas.drawBitmap(gameboardBitmap, null, curtainGameboardDestRect, null)
        revealCurtainPullLevel = 1
        revealCurtainProgressTimer = Timer()
        revealCurtainProgressTimer?.scheduleAtFixedRate(object: TimerTask() {
            override fun run() {
                var destRectOriginX = (currentLevel.width * spriteWidth * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightXAxisSteps / 2).toInt()) * scale).toInt()
                var destRectOriginY = (currentLevel.height * spriteHeight * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightYAxisSteps / 2).toInt()) * scale).toInt()
                if (destRectOriginX < -75 || destRectOriginY < -50)
                {
                    revealCurtainProgressTimer?.cancel()
                    revealCurtainProgressTimer = null
                    dismissRevealCurtain()
                }
                runOnUiThread {
                    var surface = surfaceHolder?.surface
                    if (surface != null && surface.isValid)
                    {
                        var canvas = surfaceHolder?.lockCanvas()
                        var sourceRect = Rect((currentLevel.width * spriteWidth) - (revealCurtainPullLevel * (ConfigurationManager.revealSpotlightXAxisSteps / 2).toInt()), (currentLevel.height * spriteHeight) - (revealCurtainPullLevel * (ConfigurationManager.revealSpotlightYAxisSteps / 2).toInt()), ((currentLevel.width * spriteWidth) - (revealCurtainPullLevel * (ConfigurationManager.revealSpotlightXAxisSteps / 2).toInt())) + (revealCurtainPullLevel * ConfigurationManager.revealSpotlightXAxisSteps), ((currentLevel.height * spriteHeight) - (revealCurtainPullLevel * (ConfigurationManager.revealSpotlightYAxisSteps / 2).toInt())) + (revealCurtainPullLevel * ConfigurationManager.revealSpotlightYAxisSteps))
                        var destRect = Rect((currentLevel.width * spriteWidth * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightXAxisSteps / 2).toInt()) * scale).toInt(), (currentLevel.height * spriteHeight * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightYAxisSteps / 2).toInt()) * scale).toInt(), ((currentLevel.width * spriteWidth * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightXAxisSteps / 2).toInt()) * scale).toInt()) + ((revealCurtainPullLevel * ConfigurationManager.revealSpotlightXAxisSteps * scale).toInt()), ((currentLevel.height * spriteHeight * scale / 2).toInt() - ((revealCurtainPullLevel * (ConfigurationManager.revealSpotlightYAxisSteps / 2).toInt()) * scale).toInt()) + ((revealCurtainPullLevel * ConfigurationManager.revealSpotlightYAxisSteps * scale).toInt()))

                        canvas?.drawRGB(0, 0, 0)
                        canvas?.drawBitmap(curtainBackgroundBitmap, sourceRect, destRect, null)
                        canvas?.drawBitmap(spotlight, null, destRect, null)

                        if (canvas != null)
                        {
                            surfaceHolder?.unlockCanvasAndPost(canvas)
                        }
                        revealCurtainPullLevel++
                    }
                }
            }
        }, ConfigurationManager.revealCurtainTimerDelay, ConfigurationManager.revealCurtainTimerDelay)
    }

    private fun shouldBypassReveal(): Boolean
    {
        if (!ConfigurationManager.skipPlayedLevelIntros)
        {
            return false
        }
        else
        {
            if (!IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemBeatenLevels, PersistenceSource.SharedPreferences))
            {
                return false
            }
            val beatenLevelsEntry = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemBeatenLevels, PersistenceSource.SharedPreferences)
            val beatenLevels = beatenLevelsEntry.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
            for (nextLevel in beatenLevels)
            {
                if (nextLevel == currentLevel?.identifier)
                {
                    return true
                }
            }
        }
        return false
    }

    fun updateScore(newScore: Int)
    {
        scoreTextView?.text = "Score: $newScore"
        if (newScore >= GameStateManager.getNextAdditionalLifeScore())
        {
            SoundManager.playExtraLife()
            GameStateManager.advanceAdditionalLifeScore()
        }
        lastUpdatedScore = newScore
    }

    fun updateLives(livesRemaining: Int)
    {
        livesTextView?.text = "Lives: $livesRemaining"
        lastUpdatedNumberOfLives = livesRemaining
    }

    fun revealEscapeLadder()
    {
        val escapeLadderBase = GameStateManager.getEscapeLadderBase()
        for (nextTile in escapeLadderBase.yPos downTo 0)
        {
            val tileNumber = currentLevel.tileMap[nextTile][escapeLadderBase.xPos]
            val attributes = currentLevel.attributeMap[nextTile][escapeLadderBase.xPos]
            var spriteNumber = 0
            var upperLeftTile = -1
            var upperMiddleTile = -1
            var upperRightTile = -1
            var leftTile = -1
            var rightTile = -1
            var lowerLeftTile = -1
            var lowerMiddleTile = -1
            var lowerRightTile = -1
            if (ConfigurationManager.darkBackgroundTiles.contains(tileNumber))
            {
                spriteNumber = ConfigurationManager.ladderTileDarkBackground
            }
            else if (ConfigurationManager.lightBackgroundTiles.contains(tileNumber))
            {
                spriteNumber = ConfigurationManager.ladderTileLightBackground
            }
            else if (tileNumber == ConfigurationManager.steelGirderTile)
            {
                if (escapeLadderBase.xPos == 0)
                {
                    upperMiddleTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos]
                    upperRightTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos + 1]
                    rightTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos + 1]
                    lowerMiddleTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos]
                    lowerRightTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos + 1]
                }
                else if (escapeLadderBase.xPos == currentLevel.width - 1)
                {
                    upperLeftTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos - 1]
                    upperMiddleTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos]
                    leftTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos - 1]
                    lowerLeftTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos - 1]
                    lowerMiddleTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos]
                }
                else
                {
                    if (nextTile > 0)
                    {
                        upperLeftTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos - 1]
                        upperMiddleTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos]
                        upperRightTile = currentLevel.tileMap[nextTile - 1][escapeLadderBase.xPos + 1]
                        leftTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos - 1]
                        rightTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos + 1]
                        lowerLeftTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos - 1]
                        lowerMiddleTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos]
                        lowerRightTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos + 1]
                    }
                    else
                    {
                        leftTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos - 1]
                        rightTile = currentLevel.tileMap[nextTile][escapeLadderBase.xPos + 1]
                        lowerLeftTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos - 1]
                        lowerMiddleTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos]
                        lowerRightTile = currentLevel.tileMap[nextTile + 1][escapeLadderBase.xPos + 1]
                    }
                }
                if (ConfigurationManager.lightBackgroundTiles.contains(lowerLeftTile) || ConfigurationManager.lightBackgroundTiles.contains(lowerMiddleTile) || ConfigurationManager.lightBackgroundTiles.contains(lowerRightTile))
                {
                    spriteNumber = ConfigurationManager.ladderTileLightBackground
                }
                else if ((ConfigurationManager.lightBackgroundTiles.contains(leftTile) && !ConfigurationManager.darkBackgroundTiles.contains(rightTile)) || (ConfigurationManager.lightBackgroundTiles.contains(rightTile) && !ConfigurationManager.darkBackgroundTiles.contains(leftTile)))
                {
                    spriteNumber = ConfigurationManager.ladderTileLightBackground
                }
                else if ((ConfigurationManager.lightBackgroundTiles.contains(upperLeftTile) || ConfigurationManager.lightBackgroundTiles.contains(upperMiddleTile) || ConfigurationManager.lightBackgroundTiles.contains(upperRightTile)) && !(ConfigurationManager.darkBackgroundTiles.contains(leftTile) || ConfigurationManager.darkBackgroundTiles.contains(rightTile)))
                {
                    spriteNumber = ConfigurationManager.ladderTileLightBackground
                }
                else
                {
                    spriteNumber = ConfigurationManager.ladderTileDarkBackground
                }
            }
            val sprite = SpriteManager.getSprite(spriteNumber)
            if (sprite != null)
            {
                currentLevel.setTile(escapeLadderBase.xPos, nextTile, spriteNumber, mutableListOf(0))
                escapeLadderOriginalTileCoordinates.add(Coordinate(escapeLadderBase.xPos, nextTile))
                escapeLadderOriginalTileNumbers.add(tileNumber)
                escapeLadderOriginalTileAttributes.add(attributes)
                var canvas  = Canvas(gameboardBitmap)
                val startX = escapeLadderBase.xPos * spriteWidth
                val startY = nextTile * spriteHeight
                val endX = startX + spriteWidth
                val endY = startY + spriteHeight
                val destRect = Rect(startX, startY, endX, endY)
                canvas.drawBitmap(SpriteManager.bitmapForSprite(sprite), sourceRect, destRect, null)
            }
        }
        escapeLadderRevealed = true
        GameStateManager.setLevelEscapable(true)
    }

    fun retractEscapeLadder()
    {
        val escapeLadderBase = GameStateManager.getEscapeLadderBase()
        var index = 0
        for (nextTile in escapeLadderBase.yPos downTo 0)
        {
            currentLevel.setTile(escapeLadderOriginalTileCoordinates[index].xPos, escapeLadderOriginalTileCoordinates[index].yPos, escapeLadderOriginalTileNumbers[index], escapeLadderOriginalTileAttributes[index])
            var canvas  = Canvas(gameboardBitmap)
            val startX = escapeLadderBase.xPos * spriteWidth
            val startY = nextTile * spriteHeight
            val endX = startX + spriteWidth
            val endY = startY + spriteHeight
            val destRect = Rect(startX, startY, endX, endY)
            val sprite = SpriteManager.getSprite(escapeLadderOriginalTileNumbers[index])
            if (sprite != null)
            {
                canvas.drawBitmap(SpriteManager.bitmapForSprite(sprite), sourceRect, destRect, null)
            }
            index++
        }
        escapeLadderOriginalTileCoordinates.clear()
        escapeLadderOriginalTileNumbers.clear()
        escapeLadderOriginalTileAttributes.clear()
        escapeLadderRevealed = false
        GameStateManager.setLevelEscapable(false)
    }

    // Game State Delegate

    override fun freezeProcessing()
    {
        paused = true
    }

    override fun playerDied(livesRemaining: Int)
    {
        val highScore = GameStateManager.checkForHighScore()
        GameStateManager.saveCurrentHighScore()
        runOnUiThread {
            if (livesRemaining > 0)
            {
                updateLives(livesRemaining)
                endLevel()
                startLevel(true)
            }
            else
            {
                val data = Intent()
                firebaseAnalytics.logEvent("GameEndLostLastLife", Bundle().apply {
                    putInt(FirebaseAnalytics.Param.LEVEL, currentLevel.number)
                })
                endLevel()
                updateScore(GameStateManager.getCurrentScore())
                updateLives(livesRemaining)
                data.putExtra("High Score", highScore)
                setResult(Activity.RESULT_OK, data);
                if (gameLoopTimer != null)
                {
                    gameLoopTimer?.cancel()
                    gameLoopTimer = null
                }
                finish()
            }
            paused = false
        }
    }

    override fun levelWon()
    {
        if (!IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemBeatenLevels, PersistenceSource.SharedPreferences))
        {
            IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemBeatenLevels, arrayOf(currentLevel.identifier), PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
        }
        else
        {
            val beatenLevelsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemBeatenLevels, PersistenceSource.SharedPreferences)
            val levelsList = beatenLevelsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
            var beatenLevels = mutableListOf<String>()
            beatenLevels.addAll(levelsList)
            if (!beatenLevels.contains(currentLevel.identifier))
            {
                beatenLevels.add(currentLevel.identifier)
                IoGPersistenceManager.getSharedManager().saveValue(ConfigurationManager.persistenceItemBeatenLevels, beatenLevels, PersistenceDataType.Array, PersistenceSource.SharedPreferences, PersistenceProtectionLevel.Unsecured, PersistenceLifespan.Immortal, null, true)
            }
        }
        runOnUiThread {
            endLevel()
            if (ConfigurationManager.getLastLevelNumber() > currentLevel.number)
            {
                prepareForNextLevel()
                Timer().schedule(object: TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            startLevel(false)
                        }
                    }
                }, ConfigurationManager.postLevelWinDelay)
            }
            else
            {
                val intent = Intent(this,  ExtrasActivity::class.java)
                startActivity(intent)
                if (gameLoopTimer != null)
                {
                    gameLoopTimer?.cancel()
                    gameLoopTimer = null
                }
                finish()
            }
            paused = false
        }
    }

    // Player Action Delegate

    override fun allGoldRetrieved()
    {
        revealEscapeLadder();
    }

    // ControlDelegate Methods

    override fun directionTapped(direction : ControlView.ControllerDirection)
    {
        val player = GameStateManager.getPlayer()
        if (player.direction == ControlDirection.Still || direction == ControllerDirection.Center)
        {
            when (direction)
            {
                ControllerDirection.Center ->
                {
                    controlImageView?.setImageResource(R.mipmap.buttons_none_pressed)
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                }
                ControllerDirection.Up ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_top_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                    }
                    player.direction = ControlDirection.Up
                    player.desiredDirection = ControlDirection.Up
                }
                ControllerDirection.Down ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_bottom_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                    }
                    player.direction = ControlDirection.Down
                    player.desiredDirection = ControlDirection.Down
                }
                ControllerDirection.Left ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_left_pressed)
                    }
                    else
                    {
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                    }
                    player.direction = ControlDirection.Left
                    player.desiredDirection = ControlDirection.Left
                }
                ControllerDirection.Right ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_right_pressed)
                    }
                    else
                    {
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                    }
                    player.direction = ControlDirection.Right
                    player.desiredDirection = ControlDirection.Right
                }
                ControllerDirection.UpLeft ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_top_and_left_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                    }
                    player.direction = ControlDirection.UpLeft
                    player.desiredDirection = ControlDirection.UpLeft
                }
                ControllerDirection.UpRight ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_top_and_right_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                    }
                    player.direction = ControlDirection.UpRight
                    player.desiredDirection = ControlDirection.UpRight
                }
                ControllerDirection.DownLeft ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_left_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                    }
                    player.direction = ControlDirection.DownLeft
                    player.desiredDirection = ControlDirection.DownLeft
                }
                ControllerDirection.DownRight ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_right_pressed)
                    }
                    else
                    {
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                    }
                    player.direction = ControlDirection.DownRight
                    player.desiredDirection = ControlDirection.DownRight
                }
            }
        }
        else
        {
            when (player.direction)
            {
                ControlDirection.Still ->
                {
                    if (controlImageView != null)
                    {
                        controlImageView?.setImageResource(R.mipmap.buttons_none_pressed)
                    }
                    else
                    {
                        horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_right_empty)
                        verticalControlImageView?.setImageResource(R.mipmap.arrows_up_down_empty)
                    }
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                }
                ControlDirection.Up ->
                {
                    if (direction == ControllerDirection.Left)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_top_and_left_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                        }
                        player.direction = ControlDirection.UpLeft
                        player.desiredDirection = ControlDirection.UpLeft
                    }
                    else if (direction == ControllerDirection.Right)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_top_and_right_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                        }
                        player.direction = ControlDirection.UpRight
                        player.desiredDirection = ControlDirection.UpRight
                    }
                    else if (direction == ControllerDirection.Down)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_bottom_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                        }
                        player.direction = ControlDirection.Down
                        player.desiredDirection = ControlDirection.Down
                    }
                }
                ControlDirection.Down ->
                {
                    if (direction == ControllerDirection.Left)
                    {
                        if (!player.falling)
                        {
                            if (controlImageView != null)
                            {
                                controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_left_pressed)
                            }
                            else
                            {
                                verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                               horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                            }
                            player.direction = ControlDirection.DownLeft
                            player.desiredDirection = ControlDirection.DownLeft
                        }
                        else
                        {
                            if (controlImageView != null)
                            {
                                controlImageView?.setImageResource(R.mipmap.buttons_left_pressed)
                            }
                            else
                            {
                                horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                            }
                            player.direction = ControlDirection.Left
                            player.desiredDirection = ControlDirection.Left
                        }
                    }
                    else if (direction == ControllerDirection.Right)
                    {
                        if (!player.falling)
                        {
                            if (controlImageView != null)
                            {
                                controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_right_pressed)
                            }
                            else
                            {
                                verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                                horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                            }
                            player.direction = ControlDirection.DownRight
                            player.desiredDirection = ControlDirection.DownRight
                        }
                        else
                        {
                            if (controlImageView != null)
                            {
                                controlImageView?.setImageResource(R.mipmap.buttons_right_pressed)
                            }
                            else
                            {
                                horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                            }
                            player.direction = ControlDirection.Right
                            player.desiredDirection = ControlDirection.Right
                        }
                    }
                    else if (direction == ControllerDirection.Up)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_top_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                        }
                        player.direction = ControlDirection.Up
                        player.desiredDirection = ControlDirection.Up
                    }
                }
                ControlDirection.Left ->
                {
                    if (direction == ControllerDirection.Up)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_top_and_left_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                        }
                        player.direction = ControlDirection.UpLeft
                        player.desiredDirection = ControlDirection.UpLeft
                    }
                    else if (direction == ControllerDirection.Down)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_left_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                        }
                        player.direction = ControlDirection.DownLeft
                        player.desiredDirection = ControlDirection.DownLeft
                    }
                    else if (direction == ControllerDirection.Right)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_right_pressed)
                        }
                        else
                        {
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                        }
                        player.direction = ControlDirection.Right
                        player.desiredDirection = ControlDirection.Right
                    }
                }
                ControlDirection.Right ->
                {
                    if (direction == ControllerDirection.Up)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_top_and_right_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                        }
                        player.direction = ControlDirection.UpRight
                        player.desiredDirection = ControlDirection.UpRight
                    }
                    else if (direction == ControllerDirection.Down)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_bottom_and_right_pressed)
                        }
                        else
                        {
                            verticalControlImageView?.setImageResource(R.mipmap.arrows_down_selected)
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_right_selected)
                        }
                        player.direction = ControlDirection.DownRight
                        player.desiredDirection = ControlDirection.DownRight
                    }
                    else if (direction == ControllerDirection.Left)
                    {
                        if (controlImageView != null)
                        {
                            controlImageView?.setImageResource(R.mipmap.buttons_left_pressed)
                        }
                        else
                        {
                            horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_selected)
                        }
                        player.direction = ControlDirection.Left
                        player.desiredDirection = ControlDirection.Left
                    }
                }
                ControlDirection.UpLeft ->
                {
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                    directionTapped(direction)
                }
                ControlDirection.UpRight ->
                {
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                    directionTapped(direction)
                }
                ControlDirection.DownLeft ->
                {
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                    directionTapped(direction)
                }
                ControlDirection.DownRight ->
                {
                    player.direction = ControlDirection.Still
                    player.desiredDirection = ControlDirection.Still
                    directionTapped(direction)
                }
            }
        }
    }

    override fun directionUpdated(direction : ControlView.ControllerDirection)
    {
        val player = GameStateManager.getPlayer()
        val currentDirection = player.direction
        if (!((currentDirection == ControlDirection.Still && direction == ControllerDirection.Center) || (currentDirection == ControlDirection.Up && direction == ControllerDirection.Up) || (currentDirection == ControlDirection.Down && direction == ControllerDirection.Down) || (currentDirection == ControlDirection.Left && direction == ControllerDirection.Left) || (currentDirection == ControlDirection.Right && direction == ControllerDirection.Right) || (currentDirection == ControlDirection.UpLeft && direction == ControllerDirection.UpLeft) || (currentDirection == ControlDirection.UpRight && direction == ControllerDirection.UpRight) || (currentDirection == ControlDirection.DownLeft && direction == ControllerDirection.DownLeft) || (currentDirection == ControlDirection.DownRight && direction == ControllerDirection.DownRight)))
        {
            directionTapped(direction)
        }
    }

    override fun directionSwiped(direction : ControlView.ControllerDirection)
    {

    }

    override fun controlReleased()
    {
        val player = GameStateManager.getPlayer()
        if (controlImageView != null)
        {
            controlImageView?.setImageResource(R.mipmap.buttons_none_pressed)
        }
        else
        {
            horizontalControlImageView?.setImageResource(R.mipmap.arrows_left_right_empty)
            verticalControlImageView?.setImageResource(R.mipmap.arrows_up_down_empty)
        }
        player.direction = ControlDirection.Still
        player.desiredDirection = ControlDirection.Still
    }
}
