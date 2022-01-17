package com.infusionsofgrandeur.lootraider.GameObjects

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager.AnimationFrameInfo
import com.infusionsofgrandeur.lootraider.Managers.SoundManager

import android.widget.ImageView

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.random.Random

class Player(xPos: Int, yPos: Int, xTile: Int, yTile: Int, status: Motion, animationFrame: Int) : Entity(xPos, yPos, xTile, yTile, status, animationFrame)
{

    data class BorderingTiles(val topLeft: Int, val topCenter: Int, val topRight: Int, val middleRight: Int, val bottomRight: Int, val bottomCenter: Int, val bottomLeft: Int, val middleLeft: Int)
    {}

    data class BorderingTileAttributes(val topLeft: Int, val topCenter: Int, val topRight: Int, val middleRight: Int, val bottomRight: Int, val bottomCenter: Int, val bottomLeft: Int, val middleLeft: Int)
    {}

    enum class ControlDirection
    {
        Still,
        Up,
        UpRight,
        Right,
        DownRight,
        Down,
        DownLeft,
        Left,
        UpLeft
    }

    interface PlayerActionDelegate
    {
        fun allGoldRetrieved()
    }

    var direction = ControlDirection.Still
        set(newDirection: ControlDirection)
        {
            field = newDirection
//            desiredDirection = newDirection
            if (newDirection == ControlDirection.Left || newDirection == ControlDirection.Right || newDirection == ControlDirection.Up || newDirection == ControlDirection.Down)
            {
                lastPrimaryDirection = newDirection
            }
        }

    var desiredDirection = ControlDirection.Still
    var lastPrimaryDirection = ControlDirection.Still
    var goldPossessed = 0
    var autoPilot = false
    var autoPilotDirection: ControlDirection? = null
    var autoPilotDestinationDirection: ControlDirection? = null
    var currentAnimationName = "Runner Right"
    var onPlatform = false
    var platformRiding: Platform? = null
    var falling = false

    lateinit var delegate: PlayerActionDelegate

    fun updatePosition(imageView: ImageView)
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val currentTileCharacteristics = getCurrentTileCharacteristics()
        val tileDownCharacteristics = getTileDownCharacteristics()
        val tileUpCharacteristics = getTileUpCharacteristics()
        val tileLeftCharacteristics = getTileLeftCharacteristics()
        val tileRightCharacteristics = getTileRightCharacteristics()
        val tileDownLeftCharacteristics = getTileDownLeftCharacteristics()
        val tileDownRightCharacteristics = getTileDownRightCharacteristics()
        val borderingAttributes = getBorderingAttributes()
        val borderingTiles = getBorderingTiles()
        val xOffset = xPos - (xTile * GameStateManager.getTileWidth())
        val yOffset = yPos - (yTile * GameStateManager.getTileHeight())
        val tileClimbable = currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileDownClimbable = tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileLeftClimbable = tileLeftCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileRightClimbable = tileRightCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileDownLeftClimbable = tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileDownRightClimbable = tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileHangable = currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable
        val isLeftBlocked = StasisField.isPositionPlayerBlocked(xTile - 1, yTile)
        val isRightBlocked = StasisField.isPositionPlayerBlocked(xTile + 1, yTile)
        val isDownBlocked = StasisField.isPositionPlayerBlocked(xTile, yTile + 1)
        val deviceMultiplier = 1
        // First, check if we're falling
        if (xOffset == 0 && !onPlatform && ((tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable && (yOffset != 0 || currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == 0)) || (yOffset != 0 && tileUpCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)) && !(currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || (yOffset != 0 && tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable)))
        {
            direction = ControlDirection.Down
            falling = true
        }
        else if (!onPlatform && xOffset > 0 && (((tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable) && (yOffset != 0 || currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == 0)) || (yOffset != 0 && tileUpCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)) && !(currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || (yOffset != 0 && tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable)) && !(currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileRightCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable)
        )
        {
            direction = ControlDirection.Down
            falling = true
        }
        else if (!onPlatform && xOffset < 0 && (((tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable) && (yOffset != 0 || currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == 0)) || (yOffset != 0 && tileUpCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)) && !(currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || (yOffset != 0 && tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable)) && !(currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileDownCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileLeftCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable || tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable)
        )
        {
            direction = ControlDirection.Down
            falling = true
        }
        // Then, check if we need to stop falling
        if (falling && yOffset == 0)
        {
            if (xOffset == 0 && (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0))
            {
                falling = false
                direction = desiredDirection
            }
            else if (xOffset < 0 && (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0 || tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable))
            {
                falling = false
                direction = desiredDirection
            }
            else if (xOffset > 0 && (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0 || tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable))
            {
                falling = false
                direction = desiredDirection
            }
            else if (xOffset == 0 && currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable)
            {
                falling = false
                direction = ControlDirection.Still
            }
            else if (xOffset < 0 && (currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable || tileLeftCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable))
            {
                falling = false
                direction = ControlDirection.Still
            }
            else if (xOffset > 0 && (currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable || tileRightCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable))
            {
                falling = false
                direction = ControlDirection.Still
            }
        }
        // Next, check if we're on a platform
        if (onPlatform)
        {
            if (platformRiding?.axis == Platform.TravelAxis.Horizontal)
            {
                if (platformRiding?.status == Motion.Still && direction == ControlDirection.Still)
                {
                    return
                }
                else if (platformRiding?.status == Motion.PlatformRight)
                {
                    xPos += ConfigurationManager.platformXAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                    {
                        xTile += 1
                    }
                    imageView.left = xPos
                    imageView.top = yPos
                }
                else if (platformRiding?.status == Motion.PlatformLeft)
                {
                    xPos -= ConfigurationManager.platformXAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                    {
                        xTile -= 1
                    }
                    imageView.left = xPos
                    imageView.top = yPos
                }
                if (direction == ControlDirection.Still)
                {
                    return
                }
            }
            else
            {
                if (platformRiding?.status == Motion.Still && direction == ControlDirection.Still)
                {
                    val platformOffset = platformRiding?.getPlatformTopOffset()
                    yPos = platformRiding!!.yPos + platformOffset!! - GameStateManager.getTileHeight()
                    imageView.left = xPos
                    imageView.top = yPos
                }
                else if (platformRiding?.status == Motion.PlatformDown)
                {
                    yPos += ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile += 1
                    }
                    imageView.left = xPos
                    imageView.top = yPos
                }
                else if (platformRiding?.status == Motion.PlatformUp)
                {
                    yPos -= ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile -= 1
                    }
                    imageView.left = xPos
                    imageView.top = yPos
                }
                if (direction == ControlDirection.Still)
                {
                    return
                }
            }
        }
        // If none of the above, move normally
        when (direction)
        {
            ControlDirection.Still ->
            {
                if (xOffset == 0 && yOffset == 0 && !onPlatform)
                {
                    val platforms = GameStateManager.getPlatforms()
                    for (nextPlatform in platforms)
                    {
                        val platformOffset = nextPlatform.getPlatformTopOffset()
                        if (xTile == nextPlatform.xTile && yTile == nextPlatform.yTile && nextPlatform.status == Motion.Still)
                        {
                            onPlatform = true
                            falling = false
                            platformRiding = nextPlatform
                            yPos = nextPlatform.yPos + platformOffset - GameStateManager.getTileHeight()
                        }
                    }
                }
            }
            ControlDirection.Up ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (tileClimbable)
                    {
                        val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Climb")
                        currentAnimationName = "Runner Climb"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                        yPos -= ConfigurationManager.playerYAxisSteps * deviceMultiplier
                    }
                }
                else if (xOffset == 0 && yOffset != 0 && !onPlatform)
                {
                    yPos -= ConfigurationManager.playerYAxisSteps * deviceMultiplier
                    val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                    currentAnimationName = "Runner Climb"
                    imageView.setImageBitmap(nextSprite.bitmap)
                    animationFrame = nextSprite.frame
                }
                else if (xOffset != 0)
                {
                    if (tileClimbable)
                    {
                        if (xPos < xTile * GameStateManager.getTileWidth())
                        {
                            if (xPos + ConfigurationManager.playerXAxisSteps * deviceMultiplier >= xTile * GameStateManager.getTileWidth())
                            {
                                xPos = xTile * GameStateManager.getTileWidth()
                            }
                            else
                            {
                                xPos += xTile * GameStateManager.getTileWidth()
                            }
                            val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                            currentAnimationName = "Runner Right"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            if (xPos - ConfigurationManager.playerXAxisSteps * deviceMultiplier >= xTile * GameStateManager.getTileWidth())
                            {
                                xPos = xTile * GameStateManager.getTileWidth()
                            }
                            else
                            {
                                xPos -= xTile * GameStateManager.getTileWidth()
                            }
                            val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                            currentAnimationName = "Runner Left"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else if (xPos > (xTile * GameStateManager.getTileWidth()) && tileRightClimbable)
                    {
                        val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                        currentAnimationName = "Runner Right"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                        xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                        if (xPos + GameStateManager.getTileWidth() >= currentLevel.width * GameStateManager.getTileWidth())
                        {
                            xPos = currentLevel.width * GameStateManager.getTileWidth() - GameStateManager.getTileWidth()
                        }
                        if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile += 1
                        }
                    }
                    else if (xPos < (xTile * GameStateManager.getTileWidth()) && tileLeftClimbable)
                    {
                        val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                        currentAnimationName = "Runner Left"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                        xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                        if (xPos < 0)
                        {
                            xPos = 0
                        }
                        if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile -= 1
                        }
                    }
                }
                if (yPos < 0)
                {
                    yPos = 0
                }
                if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                {
                    yTile -= 1
                    onPlatform = false
                    platformRiding = null
                }
            }
            ControlDirection.UpRight ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (lastPrimaryDirection == ControlDirection.Right && tileClimbable)
                    {
                        direction = ControlDirection.Up
                        lastPrimaryDirection = ControlDirection.Up
                    }
                    else if (lastPrimaryDirection == ControlDirection.Up && isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderTraversable) == 0)
                    {
                        direction = ControlDirection.Right
                        lastPrimaryDirection = ControlDirection.Right
                    }
                    else if (tileClimbable)
                    {
                        direction = ControlDirection.Up
                        lastPrimaryDirection = ControlDirection.Up
                    }
                    else
                    {
                        direction = ControlDirection.Right
                        lastPrimaryDirection = ControlDirection.Right
                    }
                }
                else
                {
                    if (lastPrimaryDirection == ControlDirection.Up)
                    {
                        direction = ControlDirection.Up
                    }
                    else if (lastPrimaryDirection == ControlDirection.Right)
                    {
                        direction = ControlDirection.Right
                    }
                    else if (tileClimbable || (yOffset != 0 && tileDownClimbable))
                    {
                        direction = ControlDirection.Up
                    }
                    else
                    {
                        direction = ControlDirection.Right
                    }
                }
                updatePosition(imageView)
                direction = desiredDirection
            }
            ControlDirection.Right ->
            {
                if (falling)
                {
                    if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier < (yTile * GameStateManager.getTileHeight()))
                    {
                        yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                    }
                    else
                    {
                        if (isCharacterTraversable(borderingTiles.bottomCenter, borderingAttributes.bottomCenter))
                        {
                            if (!falling)
                            {
                                yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                            }
                            else
                            {
                                yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                            }
                        }
                        else
                        {
                            yPos = yTile * GameStateManager.getTileHeight()
                            falling = true
                        }
                    }
                    val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                    currentAnimationName = "Runner Climb"
                    imageView.setImageBitmap(nextSprite.bitmap)
                    animationFrame = nextSprite.frame
                }
                else if (xPos + ConfigurationManager.playerXAxisSteps * deviceMultiplier < (xTile * GameStateManager.getTileWidth()))
                {
                    xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                    if (tileHangable)
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Shimmy")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Shimmy", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Shimmy")
                        }
                        currentAnimationName = "Runner Shimmy"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Right")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Right", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                        }
                        currentAnimationName = "Runner Right"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
                else
                {
                    var pathClear = true
                    if (!isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) || (xOffset == 0 && isRightBlocked))
                    {
                        pathClear = false
                    }
                    else if (yPos < (yTile * GameStateManager.getTileHeight()) && !isCharacterTraversable(borderingTiles.topRight, borderingAttributes.topRight))
                    {
                        pathClear = false
                    }
                    else if (yPos > (yTile * GameStateManager.getTileHeight()) && !isCharacterTraversable(borderingTiles.bottomRight, borderingAttributes.bottomRight))
                    {
                        pathClear = false
                    }
                    if (!pathClear)
                    {
                        if (xOffset == 0)
                        {
                            if (((tileClimbable && yOffset < 0 && !isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && isCharacterTraversable(borderingTiles.topRight, borderingAttributes.topRight)) || (tileDownClimbable && yOffset > 0 && isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && !isCharacterTraversable(borderingTiles.bottomRight, borderingAttributes.bottomRight))))
                            {
                                val nextYBoundary = if (yOffset < 0) (yTile - 1) * GameStateManager.getTileHeight() else yTile * GameStateManager.getTileHeight()
                                if (yPos - ConfigurationManager.playerYAxisSteps * deviceMultiplier < nextYBoundary)
                                {
                                    yPos = nextYBoundary
                                }
                                else
                                {
                                    yPos -= ConfigurationManager.playerYAxisSteps * deviceMultiplier
                                }
                                if (yPos < 0)
                                {
                                    yPos = 0
                                }
                                if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                                {
                                    yTile -= 1
                                }
                            }
                            else if (tileClimbable && ((yOffset > 0 && !isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && isCharacterTraversable(borderingTiles.bottomRight, borderingAttributes.bottomRight)) || (yOffset < 0 && isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && !isCharacterTraversable(borderingTiles.topRight, borderingAttributes.topRight))))
                            {
                                val nextYBoundary = if (yOffset > 0) (yTile + 1) * GameStateManager.getTileHeight() else yTile * GameStateManager.getTileHeight()
                                if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier > nextYBoundary)
                                {
                                    yPos = nextYBoundary
                                }
                                else
                                {
                                    yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                                }
                                if (yPos + GameStateManager.getTileHeight() >= currentLevel.height * GameStateManager.getTileHeight())
                                {
                                    yPos = currentLevel.height * GameStateManager.getTileHeight() - GameStateManager.getTileHeight()
                                }
                                if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                                {
                                    yTile += 1
                                }
                            }
                        }
                        xPos = xTile * GameStateManager.getTileWidth()
                    }
                    else
                    {
                        xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                    }
                    if (tileHangable)
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Shimmy")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Shimmy", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Shimmy")
                        }
                        currentAnimationName = "Runner Shimmy"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Right")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Right", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                        }
                        currentAnimationName = "Runner Right"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
                if (xOffset == 0 && yOffset == 0 && !onPlatform)
                {
                    val platforms = GameStateManager.getPlatforms()
                    for (nextPlatform in platforms)
                    {
                        val platformOffset = nextPlatform.getPlatformTopOffset()
                        if (xTile == nextPlatform.xTile && yTile == nextPlatform.yTile && nextPlatform.status == Motion.Still)
                        {
                            onPlatform = true
                            falling = false
                            platformRiding = nextPlatform
                            yPos = nextPlatform.yPos + platformOffset - GameStateManager.getTileHeight()
                        }
                    }
                }
                if (xPos + GameStateManager.getTileWidth() >= currentLevel.width * GameStateManager.getTileWidth())
                {
                    xPos = currentLevel.width * GameStateManager.getTileWidth() - GameStateManager.getTileWidth()
                }
                if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                {
                    xTile += 1
                    onPlatform = false
                    platformRiding = null
                }
                if (onPlatform)
                {
                    val platformXpos = platformRiding!!.xPos
                    if (xPos > (platformXpos + GameStateManager.getTileWidth()))
                    {
                        onPlatform = false
                        platformRiding = null
                        direction = ControlDirection.Down
                        falling = true
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                        currentAnimationName = "Runner Climb"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
            }
            ControlDirection.DownRight ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (lastPrimaryDirection == ControlDirection.Right && tileDownClimbable)
                    {
                        direction = ControlDirection.Down
                        lastPrimaryDirection = ControlDirection.Down
                    }
                    else if (lastPrimaryDirection == ControlDirection.Down && isCharacterTraversable(borderingTiles.middleRight, borderingAttributes.middleRight) && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderTraversable) == 0)
                    {
                        direction = ControlDirection.Right
                        lastPrimaryDirection = ControlDirection.Right
                    }
                    else if (tileDownClimbable)
                    {
                        direction = ControlDirection.Down
                        lastPrimaryDirection = ControlDirection.Down
                    }
                    else
                    {
                        direction = ControlDirection.Right
                        lastPrimaryDirection = ControlDirection.Right
                    }
                }
                else
                {
                    if (lastPrimaryDirection == ControlDirection.Down)
                    {
                        direction = ControlDirection.Down
                    }
                    else if (lastPrimaryDirection == ControlDirection.Right)
                    {
                        direction = ControlDirection.Right
                    }
                    else if (tileClimbable || (yOffset != 0 && tileDownClimbable))
                    {
                        direction = ControlDirection.Down
                    }
                    else
                    {
                        direction = ControlDirection.Right
                    }
                }
                updatePosition(imageView)
                direction = desiredDirection
            }
            ControlDirection.Down ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (falling && (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0 || isDownBlocked))
                    {
                        falling = false
                        direction = desiredDirection
                    }
                    else if (isCharacterTraversable(borderingTiles.bottomCenter, borderingAttributes.bottomCenter) && !isDownBlocked)
                    {
                        yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                        currentAnimationName = "Runner Climb"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        if (desiredDirection == ControlDirection.Right)
                        {
                            direction = ControlDirection.Right
                        }
                        else if (desiredDirection == ControlDirection.Left)
                        {
                            direction = ControlDirection.Left
                        }
                        else if (direction != desiredDirection)
                        {
                            direction = desiredDirection
                        }
                        falling = false
                    }
                }
                else if (xOffset == 0 && yOffset != 0)
                {
                    if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier < (yTile * GameStateManager.getTileHeight()))
                    {
                        if (!falling)
                        {
                            yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                        }
                        else
                        {
                            yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                        }
                    }
                    else
                    {
                        if (isCharacterTraversable(borderingTiles.bottomCenter, borderingAttributes.bottomCenter))
                        {
                            if (!falling)
                            {
                                yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                            }
                            else
                            {
                                yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                            }
                        }
                        else
                        {
                            yPos = yTile * GameStateManager.getTileHeight()
                            falling = true
                        }
                    }
                    val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                    currentAnimationName = "Runner Climb"
                    imageView.setImageBitmap(nextSprite.bitmap)
                    animationFrame = nextSprite.frame
                }
                else            // xOffset != 0
                {
                    if (falling && yOffset == 0 && (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0 || (xOffset < 0 && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0) || (xOffset > 0 && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0)))
                    {
                        falling = false
                        direction = desiredDirection
                    }
                    else if (falling && yOffset < 0)
                    {
                        if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier >= yTile * GameStateManager.getTileHeight())
                        {
                            falling = false
                            direction = desiredDirection
                            yPos = yTile * GameStateManager.getTileHeight()
                        }
                        else
                        {
                            yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                            currentAnimationName = "Runner Climb"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else if (tileDownClimbable)
                    {
                        if (xPos < xTile * GameStateManager.getTileWidth())
                        {
                            if (xPos + ConfigurationManager.playerXAxisSteps * deviceMultiplier >= xTile * GameStateManager.getTileWidth())
                            {
                                xPos = xTile * GameStateManager.getTileWidth()
                            }
                            else
                            {
                                xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                            }
                            val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                            currentAnimationName = "Runner Right"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            if (xPos - ConfigurationManager.playerXAxisSteps * deviceMultiplier <= xTile * GameStateManager.getTileWidth())
                            {
                                xPos = xTile * GameStateManager.getTileWidth()
                            }
                            else
                            {
                                xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                            }
                            val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                            currentAnimationName = "Runner Left"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else if (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)
                    {
                        if (tileDownLeftClimbable)
                        {
                            if (xPos < xTile * GameStateManager.getTileWidth())
                            {
                                if (xPos + ConfigurationManager.playerXAxisSteps * deviceMultiplier >= xTile * GameStateManager.getTileWidth())
                                {
                                    xPos = xTile * GameStateManager.getTileWidth()
                                }
                                else
                                {
                                    xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                                }
                                val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                                currentAnimationName = "Runner Right"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                if (xPos - ConfigurationManager.playerXAxisSteps * deviceMultiplier <= xTile * GameStateManager.getTileWidth())
                                {
                                    xPos = xTile * GameStateManager.getTileWidth()
                                }
                                else
                                {
                                    xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                                }
                                val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                                currentAnimationName = "Runner Left"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (!falling)
                            {
                                yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                            }
                            else
                            {
                                yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                                currentAnimationName = "Runner Climb"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                        }
                    }
                    else if (tileDownCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)
                    {
                        if (tileDownRightClimbable)
                        {
                            if (xPos < xTile * GameStateManager.getTileWidth())
                            {
                                if (xPos + ConfigurationManager.playerXAxisSteps * deviceMultiplier >= xTile * GameStateManager.getTileWidth())
                                {
                                    xPos = xTile * GameStateManager.getTileWidth()
                                }
                                else
                                {
                                    xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                                }
                                val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                                currentAnimationName = "Runner Right"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                if (xPos - ConfigurationManager.playerXAxisSteps * deviceMultiplier <= xTile * GameStateManager.getTileWidth())
                                {
                                    xPos = xTile * GameStateManager.getTileWidth()
                                }
                                else
                                {
                                    xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                                }
                                val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                                currentAnimationName = "Runner Left"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (!falling)
                            {
                                yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                            }
                            else
                            {
                                yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                                currentAnimationName = "Runner Climb"
                                imageView.setImageBitmap(nextSprite.bitmap)
                                animationFrame = nextSprite.frame
                            }
                        }
                    }
                    else if (xPos > (xTile * GameStateManager.getTileWidth()) && tileDownRightClimbable)
                    {
                        val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Right")
                        currentAnimationName = "Runner Right"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                        xPos += ConfigurationManager.playerXAxisSteps * deviceMultiplier
                        if (xPos + GameStateManager.getTileWidth() >= currentLevel.width * GameStateManager.getTileWidth())
                        {
                            xPos = currentLevel.width * GameStateManager.getTileWidth() - GameStateManager.getTileWidth()
                        }
                        if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile += 1
                        }
                    }
                    else if (xPos < (xTile * GameStateManager.getTileWidth()) && tileDownLeftClimbable)
                    {
                        val nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                        currentAnimationName = "Runner Left"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                        xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                        if (xPos < 0)
                        {
                            xPos = 0
                        }
                        if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile -= 1
                        }
                    }
                    else if (yOffset < 0 && yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier <= (yTile * GameStateManager.getTileHeight()))
                    {
                        if (!falling)
                        {
                            yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                        }
                        else
                        {
                            yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                            currentAnimationName = "Runner Climb"
                            imageView.setImageBitmap(nextSprite.bitmap)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else if (yOffset < 0 && yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier > (yTile * GameStateManager.getTileHeight()))
                    {
                        yPos = yTile * GameStateManager.getTileHeight()
                        falling = true
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                        currentAnimationName = "Runner Climb"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        if (desiredDirection == ControlDirection.Right)
                        {
                            direction = ControlDirection.Right
                        }
                        else if (desiredDirection == ControlDirection.Left)
                        {
                            direction = ControlDirection.Left
                        }
                        else if (direction != desiredDirection)
                        {
                            direction = desiredDirection
                        }
                        falling = true
                    }
                }
                if (yPos + GameStateManager.getTileHeight() >= currentLevel.height * GameStateManager.getTileHeight())
                {
                    yPos = currentLevel.height * GameStateManager.getTileHeight() - GameStateManager.getTileHeight()
                }
                if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                {
                    yTile += 1
                    onPlatform = false
                    platformRiding = null
                }
            }
            ControlDirection.DownLeft ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (lastPrimaryDirection == ControlDirection.Left && tileDownClimbable)
                    {
                        direction = ControlDirection.Down
                        lastPrimaryDirection = ControlDirection.Down
                    }
                    else if (lastPrimaryDirection == ControlDirection.Down && isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderTraversable) == 0)
                    {
                        direction = ControlDirection.Left
                        lastPrimaryDirection = ControlDirection.Left
                    }
                    else if (tileDownClimbable)
                    {
                        direction = ControlDirection.Down
                        lastPrimaryDirection = ControlDirection.Down
                    }
                    else
                    {
                        direction = ControlDirection.Left
                        lastPrimaryDirection = ControlDirection.Left
                    }
                }
                else
                {
                    if (lastPrimaryDirection == ControlDirection.Down)
                    {
                        direction = ControlDirection.Down
                    }
                    else if (lastPrimaryDirection == ControlDirection.Left)
                    {
                        direction = ControlDirection.Left
                    }
                    else if (tileClimbable || (yOffset != 0 && tileDownClimbable))
                    {
                        direction = ControlDirection.Down
                    }
                    else
                    {
                        direction = ControlDirection.Left
                    }
                }
                updatePosition(imageView)
                direction = desiredDirection
            }
            ControlDirection.Left ->
            {
                if (falling)
                {
                    if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier < (yTile * GameStateManager.getTileHeight()))
                    {
                        yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                    }
                    else
                    {
                        if (isCharacterTraversable(borderingTiles.bottomCenter, borderingAttributes.bottomCenter))
                        {
                            if (!falling)
                            {
                                yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                            }
                            else
                            {
                                yPos += ConfigurationManager.playerFallingSteps * deviceMultiplier
                            }
                        }
                        else
                        {
                            yPos = yTile * GameStateManager.getTileHeight()
                            falling = true
                        }
                    }
                    val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                    currentAnimationName = "Runner Climb"
                    animationFrame = nextSprite.frame
                }
                if (xPos - ConfigurationManager.playerXAxisSteps * deviceMultiplier >= (xTile * GameStateManager.getTileWidth()))
                {
                    xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                    if (tileHangable)
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Shimmy")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Shimmy", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Shimmy")
                        }
                        currentAnimationName = "Runner Shimmy"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Left")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Left", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                        }
                        currentAnimationName = "Runner Left"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
                else
                {
                    var pathClear = true
                    if (!isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) || (xOffset == 0 && isLeftBlocked))
                    {
                        pathClear = false
                    }
                    else if (yPos < (yTile * GameStateManager.getTileHeight()) && !isCharacterTraversable(borderingTiles.topLeft, borderingAttributes.topLeft))
                    {
                        pathClear = false
                    }
                    else if (yPos > (yTile * GameStateManager.getTileHeight()) && !isCharacterTraversable(borderingTiles.bottomLeft, borderingAttributes.bottomLeft))
                    {
                        pathClear = false
                    }
                    if (!pathClear)
                    {
                        if (xOffset == 0)
                        {
                            if (((tileClimbable && yOffset < 0 && !isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && isCharacterTraversable(borderingTiles.topLeft, borderingAttributes.topLeft)) || (tileDownClimbable && yOffset > 0 && isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && !isCharacterTraversable(borderingTiles.bottomLeft, borderingAttributes.bottomLeft))))
                            {
                                val nextYBoundary = if (yOffset < 0) ((yTile - 1) * GameStateManager.getTileHeight()) else (yTile * GameStateManager.getTileHeight())
                                if (yPos - ConfigurationManager.playerYAxisSteps * deviceMultiplier < nextYBoundary)
                                {
                                    yPos = nextYBoundary
                                }
                                else
                                {
                                    yPos -= ConfigurationManager.playerYAxisSteps * deviceMultiplier
                                }
                                if (yPos < 0)
                                {
                                    yPos = 0
                                }
                                if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                                {
                                    yTile -= 1
                                }
                            }
                            else if (tileClimbable && ((yOffset > 0 && !isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && isCharacterTraversable(borderingTiles.bottomLeft, borderingAttributes.bottomLeft)) || (yOffset < 0 && isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && !isCharacterTraversable(borderingTiles.topLeft, borderingAttributes.topLeft))))
                            {
                                val nextYBoundary = if (yOffset > 0) ((yTile + 1) * GameStateManager.getTileHeight()) else (yTile * GameStateManager.getTileHeight())
                                if (yPos + ConfigurationManager.playerYAxisSteps * deviceMultiplier > nextYBoundary)
                                {
                                    yPos = nextYBoundary
                                }
                                else
                                {
                                    yPos += ConfigurationManager.playerYAxisSteps * deviceMultiplier
                                }
                                if (yPos + GameStateManager.getTileHeight() >= currentLevel.height * GameStateManager.getTileHeight())
                                {
                                    yPos = currentLevel.height * GameStateManager.getTileHeight() - GameStateManager.getTileHeight()
                                }
                                if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                                {
                                    yTile += 1
                                }
                            }
                        }
                        xPos = xTile * GameStateManager.getTileWidth()
                    }
                    else
                    {
                        xPos -= ConfigurationManager.playerXAxisSteps * deviceMultiplier
                    }
                    if (tileHangable)
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Shimmy")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Shimmy", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Shimmy")
                        }
                        currentAnimationName = "Runner Shimmy"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                    else
                    {
                        var nextSprite: AnimationFrameInfo
                        if (currentAnimationName == "Runner Left")
                        {
                            nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Left", animationFrame)
                        }
                        else
                        {
                            nextSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Runner Left")
                        }
                        currentAnimationName = "Runner Left"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
                if (xOffset == 0 && yOffset == 0 && !onPlatform)
                {
                    val platforms = GameStateManager.getPlatforms()
                    for (nextPlatform in platforms)
                    {
                        val platformOffset = nextPlatform.getPlatformTopOffset()
                        if (xTile == nextPlatform.xTile && yTile == nextPlatform.yTile && nextPlatform.status == Motion.Still)
                        {
                            onPlatform = true
                            falling = false
                            platformRiding = nextPlatform
                            yPos = nextPlatform.yPos + platformOffset - GameStateManager.getTileHeight()
                        }
                    }
                }
                if (xPos < 0)
                {
                    xPos = 0
                }
                if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                {
                    xTile -= 1
                    onPlatform = false
                    platformRiding = null
                }
                if (onPlatform)
                {
                    val platformXpos = platformRiding!!.xPos
                    if (xPos + GameStateManager.getTileWidth() < platformXpos)
                    {
                        onPlatform = false
                        platformRiding = null
                        direction = ControlDirection.Down
                        falling = true
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Runner Climb", animationFrame)
                        currentAnimationName = "Runner Climb"
                        imageView.setImageBitmap(nextSprite.bitmap)
                        animationFrame = nextSprite.frame
                    }
                }
            }
            ControlDirection.UpLeft ->
            {
                if (xOffset == 0 && yOffset == 0)
                {
                    if (lastPrimaryDirection == ControlDirection.Left && tileClimbable)
                    {
                        direction = ControlDirection.Up
                        lastPrimaryDirection = ControlDirection.Up
                    }
                    else if (lastPrimaryDirection == ControlDirection.Up && isCharacterTraversable(borderingTiles.middleLeft, borderingAttributes.middleLeft) && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderTraversable) == 0)
                    {
                        direction = ControlDirection.Left
                        lastPrimaryDirection = ControlDirection.Left
                    }
                    else if (tileClimbable)
                    {
                        direction = ControlDirection.Up
                        lastPrimaryDirection = ControlDirection.Up
                    }
                    else
                    {
                        direction = ControlDirection.Left
                        lastPrimaryDirection = ControlDirection.Left
                    }
                }
                else
                {
                    if (lastPrimaryDirection == ControlDirection.Up)
                    {
                        direction = ControlDirection.Up
                    }
                    else if (lastPrimaryDirection == ControlDirection.Left)
                    {
                        direction = ControlDirection.Left
                    }
                    else if (tileClimbable || (yOffset != 0 && tileDownClimbable))
                    {
                        direction = ControlDirection.Up
                    }
                    else
                    {
                        direction = ControlDirection.Left
                    }
                }
                updatePosition(imageView)
                direction = desiredDirection
            }
        }
        if (onPlatform)
        {
            if (xPos < platformRiding!!.xPos - (GameStateManager.getTileWidth() / 2) && xPos + GameStateManager.getTileWidth() <= platformRiding!!.xPos + GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2))
            {
                onPlatform = false
                platformRiding = null
            }
        }
        imageView.left = xPos
        imageView.top = yPos
    }

    fun getBorderingTiles(): BorderingTiles
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        var topLeft = -1
        var topCenter = -1
        var topRight = -1
        var middleRight = -1
        var bottomRight = -1
        var bottomCenter = -1
        var bottomLeft = -1
        var middleLeft = -1

        if (xTile > 0)
        {
            middleLeft = currentLevel.tileMap[yTile][xTile - 1]
            if (yTile > 0)
            {
                topLeft = currentLevel.tileMap[yTile - 1][xTile - 1]
            }
            if (yTile < (currentLevel.height - 1))
            {
                bottomLeft = currentLevel.tileMap[yTile + 1][xTile - 1]
            }
        }
        if (xTile < (currentLevel.width - 1))
        {
            middleRight = currentLevel.tileMap[yTile][xTile + 1]
            if (yTile > 0)
            {
                topRight = currentLevel.tileMap[yTile - 1][xTile + 1]
            }
            if (yTile < (currentLevel.height - 1))
            {
                bottomRight = currentLevel.tileMap[yTile + 1][xTile + 1]
            }
        }
        if (yTile > 0)
        {
            topCenter = currentLevel.tileMap[yTile - 1][xTile]
        }
        if (yTile < (currentLevel.height - 1))
        {
            bottomCenter = currentLevel.tileMap[yTile + 1][xTile]
        }

        return (BorderingTiles(topLeft, topCenter, topRight, middleRight, bottomRight, bottomCenter, bottomLeft, middleLeft))
    }

    fun getBorderingAttributes(): BorderingTileAttributes
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        var topLeft = 0
        var topCenter = 0
        var topRight = 0
        var middleRight = 0
        var bottomRight = 0
        var bottomCenter = 0
        var bottomLeft = 0
        var middleLeft = 0

        if (xTile > 0)
        {
            if (currentLevel.attributeMap[yTile][xTile - 1].size > 0)
            {
                middleLeft = currentLevel.attributeMap[yTile][xTile - 1][0]
            }
            if (yTile > 0)
            {
                if (currentLevel.attributeMap[yTile - 1][xTile - 1].size > 0)
                {
                    topLeft = currentLevel.attributeMap[yTile - 1][xTile - 1][0]
                }
            }
            if (yTile < (currentLevel.height - 1))
            {
                if (currentLevel.attributeMap[yTile + 1][xTile - 1].size > 0)
                {
                    bottomLeft = currentLevel.attributeMap[yTile + 1][xTile - 1][0]
                }
            }
        }
        if (xTile < (currentLevel.width - 1))
        {
            if (currentLevel.attributeMap[yTile][xTile + 1].size > 0)
            {
                middleRight = currentLevel.attributeMap[yTile][xTile + 1][0]
            }
            if (yTile > 0)
            {
                if (currentLevel.attributeMap[yTile - 1][xTile + 1].size > 0)
                {
                    topRight = currentLevel.attributeMap[yTile - 1][xTile + 1][0]
                }
            }
            if (yTile < (currentLevel.height - 1))
            {
                if (currentLevel.attributeMap[yTile + 1][xTile + 1].size > 0)
                {
                    bottomRight = currentLevel.attributeMap[yTile + 1][xTile + 1][0]
                }
            }
        }
        if (yTile > 0)
        {
            if (currentLevel.attributeMap[yTile - 1][xTile].size > 0)
            {
                topCenter = currentLevel.attributeMap[yTile - 1][xTile][0]
            }
        }
        if (yTile < (currentLevel.height - 1))
        {
            if (currentLevel.attributeMap[yTile + 1][xTile].size > 0)
            {
                bottomCenter = currentLevel.attributeMap[yTile + 1][xTile][0]
            }
        }
        return (BorderingTileAttributes(topLeft, topCenter, topRight, middleRight, bottomRight, bottomCenter, bottomLeft, middleLeft))
    }

    fun detectCollisions(imageView: ImageView)
    {
        val deviceMultiplier = 1
        // First, check for beating the level
        val escapeLadderTop = GameStateManager.getEscapeLadderTop()
        if (xTile == escapeLadderTop.xPos && yTile == escapeLadderTop.yPos && GameStateManager.getLevelEscapable())
        {
            GameStateManager.winLevel()
            return
        }
        // Second, check for hitting a transporter, which will take precedence over a guard catching you at the same moment
        val teleporters = GameStateManager.getTeleporters()
        var numReceivabvaleleporters = 0
        for (nextTeleporter in teleporters)
        {
            if (nextTeleporter.receivable || nextTeleporter.roundtrippable)
            {
                numReceivabvaleleporters += 1
            }
        }
        for (nextTeleporter in teleporters)
        {
            if (nextTeleporter.xTile == xTile && nextTeleporter.yTile == yTile)
            {
                if (nextTeleporter.pair != null)
                {
                    SoundManager.playTeleporter()
                    sendToTeleporter(nextTeleporter.pair!!, imageView)
                }
                else if (nextTeleporter.sendable || nextTeleporter.roundtrippable)
                {
                    val destination = Random.nextInt(numReceivabvaleleporters)
                    var teleporterIndex = -1
                    for (teleporterOption in teleporters)
                    {
                        if (teleporterOption.receivable || nextTeleporter.roundtrippable)
                        {
                            teleporterIndex += 1
                            if (teleporterIndex == destination)
                            {
                                SoundManager.playTeleporter()
                                sendToTeleporter(teleporterOption, imageView)
                            }
                        }
                    }
                }
            }
        }
        // Next, check for moving onto a platform
        val platforms = GameStateManager.getPlatforms()
        for (nextPlatform in platforms)
        {
            val platformOffset = nextPlatform.getPlatformTopOffset()
            if (yPos + GameStateManager.getTileHeight() >= nextPlatform.yPos + platformOffset && yPos + GameStateManager.getTileHeight() <= nextPlatform.yPos + platformOffset + ConfigurationManager.playerYAxisSteps * deviceMultiplier)
            {
                if (xPos >= nextPlatform.xPos - (GameStateManager.getTileWidth() / 2) && xPos + GameStateManager.getTileWidth() <= nextPlatform.xPos + GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2))
                {
                    onPlatform = true
                    falling = false
                    platformRiding = nextPlatform
                    yPos = nextPlatform.yPos + platformOffset - GameStateManager.getTileHeight()
                    if (direction == ControlDirection.Down)
                    {
                        direction = ControlDirection.Still
                    }
                }
            }
        }
        // Then, check for picking up a gold bar
        val bars = GameStateManager.getGoldBars()
        for (nextBar in bars)
        {
            if (yTile == nextBar.yTile && xTile == nextBar.xTile)
            {
                goldPossessed += 1
                nextBar.possessedBy = this
                nextBar.xTile = -1
                nextBar.yTile = -1
                GameStateManager.addGoldBarToScore()
                SoundManager.playPlayerGetGold()
                if (goldPossessed == GameStateManager.totalLevelGold)
                {
                    SoundManager.playEscapeLadderRevealed()
                    delegate?.allGoldRetrieved();
                }
            }
        }
        // Finally, check for getting caught by a guard
        val guards = GameStateManager.getGuards()
        for (nextGuard in guards)
        {
            if (nextGuard.inStasis)
            {
                continue
            }
            if (nextGuard.yPos == yPos && ((nextGuard.xPos >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && nextGuard.xPos <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier)) || ((nextGuard.xPos + GameStateManager.getTileWidth()) >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && (nextGuard.xPos + GameStateManager.getTileWidth()) <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier))))
            {
                caught()
                break
            }
            else if (nextGuard.xPos == xPos && ((nextGuard.yPos >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && nextGuard.yPos <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier)) || ((nextGuard.yPos + GameStateManager.getTileHeight()) >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && (nextGuard.yPos + GameStateManager.getTileHeight()) <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier))))
            {
                caught()
                break
            }
            else if (nextGuard.xPos >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && nextGuard.xPos <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && ((nextGuard.yPos >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && nextGuard.yPos <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier)) || (((nextGuard.yPos + GameStateManager.getTileHeight()) >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && (nextGuard.yPos + GameStateManager.getTileHeight()) <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier)))))
            {
                caught()
                break
            }
            else if ((nextGuard.xPos + GameStateManager.getTileWidth()) >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && (nextGuard.xPos + GameStateManager.getTileWidth()) <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && ((nextGuard.yPos >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && nextGuard.yPos <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier)) || (((nextGuard.yPos + GameStateManager.getTileHeight()) >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && (nextGuard.yPos + GameStateManager.getTileHeight()) <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier)))))
            {
                caught()
                break
            }
            else if (nextGuard.yPos >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && nextGuard.yPos <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && ((nextGuard.xPos >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && nextGuard.xPos <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier)) || (((nextGuard.xPos + GameStateManager.getTileWidth()) >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && (nextGuard.xPos + GameStateManager.getTileWidth()) <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier)))))
            {
                caught()
                break
            }
            else if ((nextGuard.yPos + GameStateManager.getTileHeight()) >= (yPos + ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && (nextGuard.yPos + GameStateManager.getTileHeight()) <= (yPos + GameStateManager.getTileHeight() - ConfigurationManager.guardCollisionYAxisOverlap * deviceMultiplier) && ((nextGuard.xPos >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && nextGuard.xPos <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier)) || (((nextGuard.xPos + GameStateManager.getTileWidth()) >= (xPos + ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier) && (nextGuard.xPos + GameStateManager.getTileWidth()) <= (xPos + GameStateManager.getTileWidth() - ConfigurationManager.guardCollisionXAxisOverlap * deviceMultiplier)))))
            {
                caught()
                break
            }
            else if (nextGuard.xTile == xTile && nextGuard.yTile == yTile)
            {
                caught()
                break
            }
        }
    }

    fun sendToTeleporter(destination: Teleporter, imageView: ImageView)
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val destinationX = destination.xTile
        val destinationY = destination.yTile
        val xOffset = xPos - (xTile * GameStateManager.getTileWidth())
        val entryFromLeft = if (xOffset < 0) true else false
        val deviceMultiplier = 1
        val xSteps = GameStateManager.getTileWidth() / (ConfigurationManager.playerXAxisSteps * deviceMultiplier)
        val newOffset = (xSteps / 2) * (ConfigurationManager.playerXAxisSteps * deviceMultiplier) + ConfigurationManager.playerXAxisSteps * deviceMultiplier
        xTile = destinationX
        yTile = destinationY
        xPos = xTile * GameStateManager.getTileWidth()
        yPos = yTile * GameStateManager.getTileHeight()
        if (entryFromLeft)
        {
            if (xTile < currentLevel.width - 1)
            {
                xPos += newOffset
                xTile += 1
            }
            else
            {
                xPos -= newOffset
                xTile -= 1
            }
        }
        else if (!entryFromLeft)
        {
            if (xTile > 0)
            {
                xPos -= newOffset
                xTile -= 1
            }
            else
            {
                xPos += newOffset
                xTile += 1
            }
        }
        imageView.left = xPos
        imageView.top = yPos
    }

    fun caught()
    {
        SoundManager.playPlayerCaught()
        GameStateManager.playerDeath()
    }
}