package com.infusionsofgrandeur.lootraider.GameObjects

import java.util.Date

import kotlin.math.abs
import kotlin.experimental.and

import android.widget.ImageView

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager

class Platform(xPos: Int, yPos: Int, xTile: Int, yTile: Int, status: Motion, animationFrame: Int, var axis: TravelAxis, var speed: PlatformSpeed, var wait: PlatformWait) : Entity(xPos, yPos, xTile, yTile, status, animationFrame)
{

    companion object
    {
        var startFrame: Int = -1
        var endFrame: Int = -1
    }

    enum class TravelAxis
    {
        Horizontal,
        Vertical
    }

    enum class PlatformSpeed
    {
        Slow,
        Moderate,
        Fast
    }

    enum class PlatformWait
    {
        Short,
        Moderate,
        Long
    }

    var timeDocked: Date?
    var speedMultiplier = 0
    var waitMultiplier = 0.0
    var inLandingAndTakeoffZone = false
    var takeoffDirection: Motion

    init
    {
        timeDocked = Date()
        takeoffDirection = status
        speedMultiplier = when (speed)
        {
            PlatformSpeed.Slow -> ConfigurationManager.platformSpeedSlowMultiplier
            PlatformSpeed.Moderate -> ConfigurationManager.platformSpeedModerateMultiplier
            PlatformSpeed.Fast -> ConfigurationManager.platformSpeedFastMultiplier
        }
        waitMultiplier = when (wait)
        {
            PlatformWait.Short -> ConfigurationManager.platformWaitShortMultiplier
            PlatformWait.Moderate -> ConfigurationManager.platformWaitModerateMultiplier
            PlatformWait.Long -> ConfigurationManager.platformWaitLongMultiplier
        }
    }

    fun runCycle(imageView: ImageView)
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val surroundingAttributes = getSurroundingAttributes()
        val surroundingTiles = getSurroundingTiles()
        val currentTileAttribute = if (currentLevel.attributeMap[yTile][xTile].size > 0) currentLevel.attributeMap[yTile][xTile][0] else 0
        val xOffset = xPos - (xTile * GameStateManager.getTileWidth())
        val yOffset = yPos - (yTile * GameStateManager.getTileHeight())
        val isLeftBlocked = StasisField.isPositionBlocked(xTile - 1, yTile)
        val isRightBlocked = StasisField.isPositionBlocked(xTile + 1, yTile)
        val isDownBlocked = StasisField.isPositionBlocked(xTile, yTile + 1)
        val deviceMultiplier = 1
        if (timeDocked != null)
        {
            if ((Date().time - timeDocked!!.time) >= ConfigurationManager.platformDefaultWaitTime * 1000 * waitMultiplier)
            {
                val guards = GameStateManager.getGuards()
                val player = GameStateManager.getPlayer()
                timeDocked = null
                status = takeoffDirection
                takeoffDirection = Motion.Still
                val playerPlatform = player.platformRiding.let()
                {
                    if (it === this)
                    {
                        if (player.xPos != xPos)
                        {
                            player.xPos = xPos
                        }
                    }
                }
                for (nextGuard in guards)
                {
                    val guardPlatform = nextGuard.platformRiding.let()
                    {
                        if (it === this)
                        {
                            if (nextGuard.xPos != xPos)
                            {
                                nextGuard.xPos = xPos
                            }
                        }
                    }
                }
            }
            else
            {
                return
            }
        }
        when (status)
        {
            Motion.PlatformLeft ->
            {
                if (xOffset == 0 && (!isPlatformTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked))
                {
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformRight
                    timeDocked = Date()
                }
                else if (xPos - ConfigurationManager.platformXAxisSteps * deviceMultiplier * speedMultiplier < xTile * GameStateManager.getTileWidth() && (!isPlatformTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked))
                {
                    xPos = xTile * GameStateManager.getTileWidth()
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformRight
                    timeDocked = Date()
                }
                else
                {
                    xPos -= ConfigurationManager.platformXAxisSteps * deviceMultiplier * speedMultiplier
                }
                // Determine new tile position
                if ((xTile > 0 && xPos <= ((xTile - 1) * GameStateManager.getTileWidth() + GameStateManager.getTileWidth() / 2)))
                {
                    xTile -= 1
                }
            }
            Motion.PlatformRight ->
            {
                if (xOffset == 0 && (!isPlatformTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked))
                {
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformLeft
                    timeDocked = Date()
                }
                else if (xPos + ConfigurationManager.platformXAxisSteps * deviceMultiplier * speedMultiplier > xTile * GameStateManager.getTileWidth() && (!isPlatformTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked))
                {
                    xPos = xTile * GameStateManager.getTileWidth()
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformLeft
                    timeDocked = Date()
                }
                else
                {
                    xPos += ConfigurationManager.platformXAxisSteps * deviceMultiplier * speedMultiplier
                }
                // Determine new tile position
                if (xTile < currentLevel.width - 1 && xPos >= (xTile * GameStateManager.getTileWidth() + GameStateManager.getTileWidth() / 2))
                {
                    xTile += 1
                }
            }
            Motion.PlatformUp ->
            {
                if (inLandingAndTakeoffZone && animationFrame == Platform.endFrame)
                {
                    animationFrame -= ConfigurationManager.platformYAxisSteps * speedMultiplier
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (inLandingAndTakeoffZone && animationFrame > Platform.startFrame)
                {
                    animationFrame -= ConfigurationManager.platformYAxisSteps * speedMultiplier
                    if (animationFrame < Platform.startFrame)
                    {
                        val diff = Platform.startFrame - animationFrame
                        animationFrame = Platform.startFrame
                        yPos -= diff
                        inLandingAndTakeoffZone = false
                    }
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (inLandingAndTakeoffZone && animationFrame == Platform.startFrame)
                {
                    inLandingAndTakeoffZone = false
                    yPos -= ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier
                }
                else if (yOffset == 0 && isPlatformEndpoint(currentTileAttribute))
                {
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformDown
                    timeDocked = Date()
                }
                else if (yOffset == 0 && !isPlatformTraversable(surroundingTiles.up, surroundingAttributes.up))
                {
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformDown
                    timeDocked = Date()
                }
                else if (yPos - ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier < yTile * GameStateManager.getTileHeight() && !isPlatformTraversable(surroundingTiles.up, surroundingAttributes.up))
                {
                    yPos = yTile * GameStateManager.getTileHeight()
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformDown
                    timeDocked = Date()
                }
                else if (yPos - ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier < yTile * GameStateManager.getTileHeight() && isPlatformEndpoint(currentTileAttribute))
                {
                    yPos = yTile * GameStateManager.getTileHeight()
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformDown
                    timeDocked = Date()
                }
                else
                {
                    yPos -= ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier
                }
                // Determine new tile position
                if (yTile > 0 && yPos <= ((yTile - 1) * GameStateManager.getTileHeight() + GameStateManager.getTileHeight() / 2))
                {
                    yTile -= 1
                }
            }
            Motion.PlatformDown ->
            {
                if (inLandingAndTakeoffZone && animationFrame + (ConfigurationManager.platformYAxisSteps * speedMultiplier) >= Platform.endFrame)
                {
                    status = Motion.Still
                    takeoffDirection = Motion.PlatformUp
                    timeDocked = Date()
                    animationFrame = Platform.endFrame
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (inLandingAndTakeoffZone)
                {
                    animationFrame += ConfigurationManager.platformYAxisSteps * speedMultiplier
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (yOffset == 0 && (!isPlatformTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked))
                {
                    inLandingAndTakeoffZone = true
                    animationFrame = Platform.startFrame + 1
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                // TODO: See if we ever enter this case
                else if (yPos + ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier > yTile * GameStateManager.getTileHeight() && (!isPlatformTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked))
                {
                    yPos = yTile * GameStateManager.getTileHeight()
                    inLandingAndTakeoffZone = true
                    animationFrame = Platform.startFrame + 1
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else
                {
                    yPos += ConfigurationManager.platformYAxisSteps * deviceMultiplier * speedMultiplier
                }
                // Determine new tile position
                if (yTile < currentLevel.width - 1 && yPos >= (yTile * GameStateManager.getTileHeight() + GameStateManager.getTileHeight() / 2))
                {
                    yTile += 1
                }
            }
        }
        imageView.left = xPos
        imageView.top = yPos
    }

    fun isPlatformTraversable(tileNumber: Int, tileAttribute: Int): Boolean
    {
        if (tileNumber < 0)
        {
            return false
        }
        val tileSprite = SpriteManager.getSprite(tileNumber)
        val spriteAttribute = tileSprite!!.header[0]
        var traversable = false
        // First, check the tile sprite's attributes
        if (spriteAttribute and (ConfigurationManager.spriteHeaderTraversable) == ConfigurationManager.spriteHeaderTraversable && spriteAttribute and (ConfigurationManager.spriteHeaderFallthroughable) == ConfigurationManager.spriteHeaderFallthroughable)
        {
            traversable = true
        }
        else if ((spriteAttribute and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable) || (spriteAttribute and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable))
        {
            return false
        }
        else if (spriteAttribute == 0)
        {
            return false
        }
        // Then, check the attributes of the tiles position
        if (tileAttribute and (ConfigurationManager.platformStoppableHeaderValue) == ConfigurationManager.platformStoppableHeaderValue)
        {
            return true
        }
        else if (tileAttribute == 0)
        {
            traversable = true
        }
        return traversable
    }

    fun isPlatformEndpoint(tileAttribute: Int): Boolean
    {
        if (tileAttribute and (ConfigurationManager.platformStoppableHeaderValue) == ConfigurationManager.platformStoppableHeaderValue)
        {
            return true
        }
        else
        {
            return false
        }
    }

    fun getPlatformTopOffset(): Int
    {
        if (axis == TravelAxis.Horizontal)
        {
            return 0
        }
        else
        {
            return animationFrame - Platform.startFrame
        }
    }
}