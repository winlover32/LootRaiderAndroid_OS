package com.infusionsofgrandeur.lootraider.GameObjects

import android.widget.ImageView

import kotlin.experimental.and

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.SoundManager

import kotlin.random.Random

class Guard(xPos: Int, yPos: Int, xTile: Int, yTile: Int, status: Motion, animationFrame: Int) : Entity(xPos, yPos, xTile, yTile, status, animationFrame)
{

    var goldPossessed = false
    var onPlatform = false
    var platformRiding: Platform? = null
    var readyToDisembark = false
    var inStasis = false

    fun runChasePattern(imageView: ImageView)
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val currentTileCharacteristics = getCurrentTileCharacteristics()
        val surroundingAttributes = getSurroundingAttributes()
        val surroundingTiles = getSurroundingTiles()
        val xOffset = xPos - (xTile * GameStateManager.getTileWidth())
        val yOffset = yPos - (yTile * GameStateManager.getTileHeight())
        val playerPosition = GameStateManager.getPlayerPosition()
        val playerFalling = GameStateManager.getPlayerFalling()
        val guardSmartDecision = if (Random.nextInt(ConfigurationManager.optionsForGuardSmartBehavior) > 0) true else false
        val tileClimbable = currentTileCharacteristics and (ConfigurationManager.spriteHeaderClimable) == ConfigurationManager.spriteHeaderClimable
        val tileHangable = currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable
        val isCurrentBlocked = StasisField.isPositionBlocked(xTile, yTile)
        val isLeftBlocked = StasisField.isPositionBlocked(xTile - 1, yTile)
        val isRightBlocked = StasisField.isPositionBlocked(xTile + 1, yTile)
        val isDownBlocked = StasisField.isPositionBlocked(xTile, yTile + 1)
        val tileDownLeftCharacteristics = getTileDownLeftCharacteristics()
        val tileDownRightCharacteristics = getTileDownRightCharacteristics()
        val deviceMultiplier = 1

        // First, check if guard is in stasis field
        if (inStasis)
        {
            return
        }
        // Then, check if guard is on a platform
        else if (onPlatform && !readyToDisembark)
        {
            if (platformRiding?.axis == Platform.TravelAxis.Horizontal)
            {
                if (platformRiding?.status == Motion.Still)
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
                    readyToDisembark = true
                }
                else if (platformRiding?.status == Motion.PlatformLeft)
                {
                    xPos -= ConfigurationManager.platformXAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                    {
                        xTile -= 1
                    }
                    readyToDisembark = true
                }
                return
            }
            else
            {
                if (platformRiding?.status == Motion.Still)
                {
                    val platformOffset = platformRiding!!.getPlatformTopOffset()
                    yPos = platformRiding!!.yPos + platformOffset - GameStateManager.getTileHeight()
                }
                else if (platformRiding?.status == Motion.PlatformDown)
                {
                    yPos += ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile += 1
                    }
                    readyToDisembark = true
                }
                else if (platformRiding?.status == Motion.PlatformUp)
                {
                    yPos -= ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                    if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile -= 1
                    }
                    readyToDisembark = true
                }
                return
            }
        }
        when (status)
        {
            Motion.Still ->
            {
                if (onPlatform && platformRiding?.status != Motion.Still)
                {
                    if (platformRiding?.status == Motion.PlatformRight)
                    {
                        xPos += ConfigurationManager.platformXAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                        if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile += 1
                        }
                    }
                    else if (platformRiding?.status == Motion.PlatformLeft)
                    {
                        xPos -= ConfigurationManager.platformXAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                        if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                        {
                            xTile -= 1
                        }
                    }
                    else if (platformRiding?.status == Motion.PlatformDown)
                    {
                        yPos += ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                        if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                        {
                            yTile += 1
                        }
                    }
                    else if (platformRiding?.status == Motion.PlatformUp)
                    {
                        yPos -= ConfigurationManager.platformYAxisSteps * deviceMultiplier * platformRiding!!.speedMultiplier
                        if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                        {
                            yTile -= 1
                        }
                    }
                }
                else if (playerPosition.yPos < yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable && !onPlatform)
                {
                    status = Motion.ClimbingUp
                    yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                    if (goldPossessed)
                    {
                        val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                        animationFrame = firstSprite.frame
                    }
                    else
                    {
                        val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                        animationFrame = firstSprite.frame
                    }
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                {
                    status = Motion.ClimbingDown
                    yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                    if (goldPossessed)
                    {
                        val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                        animationFrame = firstSprite.frame
                    }
                    else
                    {
                        val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                        animationFrame = firstSprite.frame
                    }
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (playerPosition.xPos < xTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                {
                    status = Motion.Left
                    xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                    if (goldPossessed)
                    {
                        if (tileHangable)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                            animationFrame = firstSprite.frame
                        }
                    }
                    else
                    {
                        if (tileHangable)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                            animationFrame = firstSprite.frame
                        }
                    }
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else if (playerPosition.xPos > xTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                {
                    status = Motion.Right
                    xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                    if (goldPossessed)
                    {
                        if (tileHangable)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                            animationFrame = firstSprite.frame
                        }
                    }
                    else
                    {
                        if (tileHangable)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                            animationFrame = firstSprite.frame
                        }
                    }
                    val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                    imageView.setImageBitmap(image)
                }
                else    // If the guard can't make the smart move, make a random one
                {
                    var directionChosen = false
                    // Character is stuck in an unmovable position. Stay there until something changes
                    if ((!isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked) && (!isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked) && (!isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked || onPlatform) && !tileClimbable)
                    {
                        status = Motion.Still
                        directionChosen = true
                    }
                    while (directionChosen == false)
                    {
                        val randomDirection = Random.nextInt(ConfigurationManager.guardPossibleRandomDirections)
                        when (randomDirection)
                        {
                            0 ->                // Left
                            {
                                if (xTile == 0)
                                {
                                }
                                else
                                {
                                    if (isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                                    {
                                        status = Motion.Left
                                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                        if (goldPossessed)
                                        {
                                            if (tileHangable)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                        }
                                        else
                                        {
                                            if (tileHangable)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                                animationFrame = firstSprite.frame
                                            }
                                        }
                                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                        imageView.setImageBitmap(image)
                                        directionChosen = true
                                    }
                                }
                            }
                            1 ->                // Right
                            {
                                if (xTile == currentLevel.width - 1)
                                {
                                }
                                else
                                {
                                    if (isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                                    {
                                        status = Motion.Right
                                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                        if (goldPossessed)
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                            animationFrame = firstSprite.frame
                                        }
                                        else
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                            animationFrame = firstSprite.frame
                                        }
                                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                        imageView.setImageBitmap(image)
                                        directionChosen = true
                                    }
                                }
                            }
                            2 ->                // Up
                            {
                                if (yTile == 0)
                                {
                                }
                                else
                                {
                                    if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                                    {
                                        status = Motion.ClimbingUp
                                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                        if (goldPossessed)
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                            animationFrame = firstSprite.frame
                                        }
                                        else
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                            animationFrame = firstSprite.frame
                                        }
                                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                        imageView.setImageBitmap(image)
                                        directionChosen = true
                                    }
                                }
                            }

                            3 ->                // Down
                            {
                                if (yTile == currentLevel.height - 1)
                                {
                                }
                                else
                                {
                                    if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                                    {
                                        status = Motion.ClimbingDown
                                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                        if (goldPossessed)
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                            animationFrame = firstSprite.frame
                                        }
                                        else
                                        {
                                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                            animationFrame = firstSprite.frame
                                        }
                                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                        imageView.setImageBitmap(image)
                                        directionChosen = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Motion.Left ->
            {
                if (isCurrentBlocked || (isLeftBlocked && xOffset == 0 && xTile > 0))
                {
                    status = Motion.Right
                    if (isCurrentBlocked && xTile < currentLevel.width - 1)
                    {
                        xTile += 1
                        xPos = xTile * GameStateManager.getTileWidth()
                    }
                }
                else if (xOffset != 0)
                {
                    if (xPos - ConfigurationManager.guardXAxisSteps * deviceMultiplier >= ((xTile - 1) * GameStateManager.getTileWidth()))
                    {
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                    }
                    else
                    {
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        xPos = xTile * GameStateManager.getTileWidth()
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    if (xPos < ((xTile - 1) * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                    {
                        xTile -= 1
                        onPlatform = false
                        platformRiding = null
                        if (yOffset != 0)
                        {
                            status = Motion.Falling
                        }
                    }
                }
                else
                {
                    // See if there's a platform occupying the same space
                    val platforms = GameStateManager.getPlatforms()
                    var currentPlatform: Platform? = null
                    var platformOffset: Int? = null
                    for (nextPlatform in platforms)
                    {
                        if (nextPlatform.xTile == xTile && nextPlatform.yTile == yTile)
                        {
                            currentPlatform = nextPlatform
                            platformOffset = nextPlatform.getPlatformTopOffset()
                            break
                        }
                    }
                    if (isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == 0 && !onPlatform && !isDownBlocked)
                    {
                        status = Motion.Falling
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                    {
                        status = Motion.Falling
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos < yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                    {
                        status = Motion.ClimbingUp
                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos < yTile && guardSmartDecision && currentPlatform != null)
                    {
                        onPlatform = true
                        readyToDisembark = false
                        platformRiding = currentPlatform
                        yPos = currentPlatform!!.yPos + platformOffset!! - GameStateManager.getTileHeight()
                        status = Motion.Still
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                    {
                        status = Motion.ClimbingDown
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && currentPlatform != null)
                    {
                        onPlatform = true
                        readyToDisembark = false
                        platformRiding = currentPlatform
                        yPos = currentPlatform!!.yPos + platformOffset!! - GameStateManager.getTileHeight()
                        status = Motion.Still
                    }
                    else if (playerPosition.xPos < xTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                    {
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                    {
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Left", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (!guardSmartDecision)        // Pick a random direction
                    {
                        var directionChosen = false
                        // Character is stuck in an unmovable position. Stay there until something changes
                        if ((!isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked) && (!isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked) && (!isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked || onPlatform) && !tileClimbable)
                        {
                            status = Motion.Still
                            directionChosen = true
                        }
                        while (directionChosen == false)
                        {
                            val randomDirection = Random.nextInt(ConfigurationManager.guardPossibleRandomDirections)
                            when (randomDirection)
                            {
                                0 ->                        // Left
                                {
                                    if (xTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                                        {
                                            xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                1 ->
                                {                               // Right
                                    if (xTile == currentLevel.width - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                                        {
                                            status = Motion.Right
                                            xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                2 ->                    // Up
                                {
                                    if (yTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                                        {
                                            status = Motion.ClimbingUp
                                            yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                3 ->                            // Down
                                {
                                    if (yTile == currentLevel.height - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                                        {
                                            if (isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down))
                                            {
                                                status = Motion.Falling
                                            }
                                            else
                                            {
                                                status = Motion.ClimbingDown
                                            }
                                            yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        status = Motion.Still
                    }
                }
            }
            Motion.Right ->
            {
                if (isCurrentBlocked || (isRightBlocked && xOffset == 0 && xTile < currentLevel.width - 1))
                {
                    status = Motion.Left
                    if (isCurrentBlocked && xTile > 0)
                    {
                        xTile -= 1
                        xPos = xTile * GameStateManager.getTileWidth()
                    }
                }
                else if (xOffset != 0)
                {
                    if (xPos + ConfigurationManager.guardXAxisSteps * deviceMultiplier < ((xTile + 1) * GameStateManager.getTileWidth()))
                    {
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else
                    {
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                        xPos = (xTile + 1) * GameStateManager.getTileWidth()
                    }
                    if (xPos > (xTile * GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2)))
                    {
                        xTile += 1
                        onPlatform = false
                        platformRiding = null
                        if (yOffset != 0)
                        {
                            status = Motion.Falling
                        }
                    }
                }
                else
                {
                    // See if there's a platform occupying the same space
                    val platforms = GameStateManager.getPlatforms()
                    var currentPlatform: Platform? = null
                    var platformOffset: Int? = null
                    for (nextPlatform in platforms)
                    {
                        if (nextPlatform.xTile == xTile && nextPlatform.yTile == yTile)
                        {
                            currentPlatform = nextPlatform
                            platformOffset = nextPlatform.getPlatformTopOffset()
                            break
                        }
                    }
                    if ((isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) || yOffset < 0 && isCharacterFallthroughable(surroundingTiles.up, surroundingAttributes.up)) && currentTileCharacteristics and (ConfigurationManager.spriteHeaderHangable) == 0 && !onPlatform && !isDownBlocked)
                    {
                        status = Motion.Falling
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                    {
                        status = Motion.Falling
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos < yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                    {
                        status = Motion.ClimbingUp
                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos < yTile && guardSmartDecision && currentPlatform != null)
                    {
                        onPlatform = true
                        readyToDisembark = false
                        platformRiding = currentPlatform
                        yPos = currentPlatform!!.yPos + platformOffset!! - GameStateManager.getTileHeight()
                        status = Motion.Still
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                    {
                        status = Motion.ClimbingDown
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                            animationFrame = firstSprite.frame
                        }
                        else
                        {
                            val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                            animationFrame = firstSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && currentPlatform != null)
                    {
                        onPlatform = !true
                        readyToDisembark = false
                        platformRiding = currentPlatform
                        yPos = currentPlatform!!.yPos + platformOffset!! - GameStateManager.getTileHeight()
                        status = Motion.Still
                    }
                    else if (playerPosition.xPos > xTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                    {
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                    {
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right With Gold", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Shimmy", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                            else
                            {
                                val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Right", animationFrame)
                                animationFrame = nextSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (!guardSmartDecision)        // Pick a random direction
                    {
                        var directionChosen = false
                        // Character is stuck in an unmovable position. Stay there until something changes
                        if ((!isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked) && (!isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked) && (!isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked || onPlatform) && !tileClimbable)
                        {
                            status = Motion.Still
                            directionChosen = true
                        }
                        while (directionChosen == false)
                        {
                            val randomDirection = Random.nextInt(ConfigurationManager.guardPossibleRandomDirections)
                            when (randomDirection)
                            {
                                0 ->                // Left
                                {
                                    if (xTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                                        {
                                            status = Motion.Left
                                            xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                1 ->                // Right
                                {
                                    if (xTile == currentLevel.width - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                                        {
                                            xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                2 ->                    // Up
                                {
                                    if (yTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                                        {
                                            status = Motion.ClimbingUp
                                            yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                3 ->                      // Down
                                {
                                    if (yTile == currentLevel.height - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && !onPlatform && !isDownBlocked)
                                        {
                                            if (isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down))
                                            {
                                                status = Motion.Falling
                                            }
                                            else
                                            {
                                                status = Motion.ClimbingDown
                                            }
                                            yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        status = Motion.Still
                    }
                }
            }
            Motion.ClimbingUp ->
            {
                onPlatform = false
                platformRiding = null
                if (yOffset != 0)
                {
                    if (yPos - ConfigurationManager.guardYAxisSteps * deviceMultiplier >= ((yTile - 1) * GameStateManager.getTileHeight()))
                    {
                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else
                    {
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        yPos = yTile * GameStateManager.getTileHeight()
                    }
                    if (yPos < ((yTile - 1) * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile -= 1
                    }
                }
                else
                {
                    // Player is likely hanging off ladder adjacent to guard, leave ladder to catch him
                    if (playerPosition.yPos == yTile && playerPosition.xPos == xTile - 1 && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked && !playerFalling)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Player is likely hanging off ladder adjacent to guard, leave ladder to catch him
                    else if (playerPosition.yPos == yTile && playerPosition.xPos == xTile + 1 && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked && !playerFalling)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos < yTile && guardSmartDecision && tileClimbable && isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up))
                    {
                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.xPos < xTile && playerPosition.yPos >= yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Only val guard fall off ladder in the rare occasion of NOT making the smart decision
                    else if (playerPosition.xPos < xTile && playerPosition.yPos > yTile && !guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.xPos > xTile && playerPosition.yPos >= yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Only val guard fall off ladder in the rare occasion of NOT making the smart decision
                    else if (playerPosition.xPos > xTile && playerPosition.yPos > yTile && !guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                    {
                        yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (!guardSmartDecision)        // Pick a random direction
                    {
                        var directionChosen = false
                        // Character is stuck in an unmovable position. Stay there until something changes
                        if ((!isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked) && (!isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked) && (!isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked || onPlatform) && !tileClimbable)
                        {
                            status = Motion.Still
                            directionChosen = true
                        }
                        while (directionChosen == false)
                        {
                            val randomDirection = Random.nextInt(ConfigurationManager.guardPossibleRandomDirections)
                            when (randomDirection)
                            {
                                0 ->                // Left
                                {
                                    if (xTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                                        {
                                            status = Motion.Left
                                            xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                1 ->               // Right
                                {
                                    if (xTile == currentLevel.width - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                                        {
                                            status = Motion.Right
                                            xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                2 ->                   // Up
                                {
                                    if (yTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                                        {
                                            yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                3 ->               // Down
                                {
                                    if (yTile == currentLevel.height - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                                        {
                                            status = Motion.ClimbingDown
                                            yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        status = Motion.Still
                    }
                }
            }
            Motion.ClimbingDown ->
            {
                onPlatform = false
                platformRiding = null
                if (yOffset != 0)
                {
                    if (yPos + ConfigurationManager.guardYAxisSteps * deviceMultiplier < ((yTile + 1) * GameStateManager.getTileHeight()))
                    {
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else
                    {
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                        animationFrame = nextSprite.frame
                        yPos = (yTile + 1) * GameStateManager.getTileHeight()
                    }
                    if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile += 1
                    }
                }
                else
                {
                    // Player is likely hanging off ladder adjacent to guard, leave ladder to catch him
                    if (playerPosition.yPos == yTile && playerPosition.xPos == xTile - 1 && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked && !playerFalling)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Player is likely hanging off ladder adjacent to guard, leave ladder to catch him
                    else if (playerPosition.yPos == yTile && playerPosition.xPos == xTile + 1 && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked && !playerFalling)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.yPos > yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                    {
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Only val guard fall off ladder in the rare occasion of NOT making the smart decision
                    else if (playerPosition.xPos < xTile && playerPosition.yPos < yTile && !guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.xPos < xTile && playerPosition.yPos <= yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked && tileDownLeftCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0)
                    {
                        status = Motion.Left
                        xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    // Only val guard fall off ladder in the rare occasion of NOT making the smart decision
                    else if (playerPosition.xPos > xTile && playerPosition.yPos < yTile && !guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (playerPosition.xPos > xTile && playerPosition.yPos <= yTile && guardSmartDecision && isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked && tileDownRightCharacteristics and (ConfigurationManager.spriteHeaderFallthroughable) == 0)
                    {
                        status = Motion.Right
                        xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                animationFrame = firstSprite.frame
                            }
                        }
                        else
                        {
                            if (tileHangable)
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                animationFrame = firstSprite.frame
                            }
                            else
                            {
                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                animationFrame = firstSprite.frame
                            }
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                    {
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else if (!guardSmartDecision)        // Pick a random direction
                    {
                        var directionChosen = false
                        // Character is stuck in an unmovable position. Stay there until something changes
                        if ((!isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) || isLeftBlocked) && (!isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) || isRightBlocked) && (!isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) || isDownBlocked || onPlatform) && !tileClimbable)
                        {
                            status = Motion.Still
                            directionChosen = true
                        }
                        while (directionChosen == false)
                        {
                            val randomDirection = Random.nextInt(ConfigurationManager.guardPossibleRandomDirections)
                            when (randomDirection)
                            {
                                0 ->               // Left
                                {
                                    if (xTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.left, surroundingAttributes.left) && !isLeftBlocked)
                                        {
                                            status = Motion.Left
                                            xPos -= ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Left")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                1 ->                // Right
                                {
                                    if (xTile == currentLevel.width - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.right, surroundingAttributes.right) && !isRightBlocked)
                                        {
                                            status = Motion.Right
                                            xPos += ConfigurationManager.guardXAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right With Gold")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            else
                                            {
                                                if (tileHangable)
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Shimmy")
                                                    animationFrame = firstSprite.frame
                                                }
                                                else
                                                {
                                                    val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Right")
                                                    animationFrame = firstSprite.frame
                                                }
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                2 ->                // Up
                                {
                                    if (yTile == 0)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.up, surroundingAttributes.up) && tileClimbable)
                                        {
                                            status = Motion.ClimbingUp
                                            yPos -= ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                                3 ->                // Down
                                {
                                    if (yTile == currentLevel.height - 1)
                                    {
                                    }
                                    else
                                    {
                                        if (isCharacterTraversable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked)
                                        {
                                            yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                                            if (goldPossessed)
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb With Gold")
                                                animationFrame = firstSprite.frame
                                            }
                                            else
                                            {
                                                val firstSprite = SpriteManager.bitmapForFirstFrameOfAnimationNamed("Robot Climb")
                                                animationFrame = firstSprite.frame
                                            }
                                            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                                            imageView.setImageBitmap(image)
                                            directionChosen = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        status = Motion.Still
                    }
                }
            }
            Motion.Falling ->
            {
                if (yOffset != 0)
                {
                    if (yPos + ConfigurationManager.guardYAxisSteps * deviceMultiplier < ((yTile + 1) * GameStateManager.getTileHeight()))
                    {
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                    }
                    else
                    {
                        val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                        animationFrame = nextSprite.frame
                        yPos = (yTile + 1) * GameStateManager.getTileHeight()
                    }
                    if (yPos > (yTile * GameStateManager.getTileHeight() + (GameStateManager.getTileHeight() / 2)))
                    {
                        yTile += 1
                    }
                }
                else
                {
                    if (isCharacterFallthroughable(surroundingTiles.down, surroundingAttributes.down) && !isDownBlocked && !tileHangable)
                    {
                        yPos += ConfigurationManager.guardYAxisSteps * deviceMultiplier
                        if (goldPossessed)
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb With Gold", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        else
                        {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed("Robot Climb", animationFrame)
                            animationFrame = nextSprite.frame
                        }
                        val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                        imageView.setImageBitmap(image)
                    }
                    else
                    {
                        status = Motion.Still
                    }
                }
            }
        }
        if (onPlatform)
        {
            if (xPos < platformRiding!!.xPos - (GameStateManager.getTileWidth() / 2) && xPos + GameStateManager.getTileWidth() <= platformRiding!!.xPos + GameStateManager.getTileWidth() + (GameStateManager.getTileWidth() / 2))
            {
                onPlatform = false
                platformRiding = null
                if (yOffset != 0)
                {
                    status = Motion.Falling
                }
            }
        }
        imageView.left = xPos
        imageView.top = yPos
    }

    fun detectCollisions(imageView: ImageView)
    {
        // First, check for hitting a transporter
        val teleporters = GameStateManager.getTeleporters()
        val deviceMultiplier = 1
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
        if (!onPlatform)
        {
            val platforms = GameStateManager.getPlatforms()
            for (nextPlatform in platforms)
            {
                val platformOffset = nextPlatform.getPlatformTopOffset()
                if (yPos + GameStateManager.getTileHeight() >= nextPlatform.yPos + platformOffset && yPos + GameStateManager.getTileHeight() <= nextPlatform.yPos + platformOffset + ConfigurationManager.playerYAxisSteps * deviceMultiplier)
                {
                    if (xPos == nextPlatform.xPos)
                    {
                        onPlatform = true
                        platformRiding = nextPlatform
                        readyToDisembark = false
                        yPos = nextPlatform.yPos + platformOffset - GameStateManager.getTileHeight()
                        status = Motion.Still
                    }
                }
            }
        }
        // Then, check for picking up a gold bar
        if (!goldPossessed)
        {
            val bars = GameStateManager.getGoldBars()
            for (nextBar in bars)
            {
                if (yTile == nextBar.yTile && xTile == nextBar.xTile)
                {
                    goldPossessed = true
                    nextBar.possessedBy = this
                    nextBar.xTile = -1
                    nextBar.yTile = -1
                    SoundManager.playSentryGetGold()
                }
            }
        }
    }

    fun sendToTeleporter(destination: Teleporter, imageView: ImageView)
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val destinationX = destination.xTile
        val destinationY = destination.yTile
        val deviceMultiplier = 1
        val xOffset = xPos - (xTile * GameStateManager.getTileWidth())
        val entryFromLeft = if (xOffset < 0) true else false
        val xSteps = GameStateManager.getTileWidth() / ConfigurationManager.playerXAxisSteps
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

    fun isAboveStasisField(): Boolean
    {
        val stasisFieldOne = GameStateManager.getStasisFieldOne()
        val stasisFieldTwo = GameStateManager.getStasisFieldTwo()
        if ((stasisFieldOne.xTile == xTile && stasisFieldOne.yTile == yTile + 1 && stasisFieldOne.stage >= ConfigurationManager.stasisFieldBlockingStage) || (stasisFieldTwo.xTile == xTile && stasisFieldTwo.yTile == yTile + 1 && stasisFieldTwo.stage >= ConfigurationManager.stasisFieldBlockingStage))
        {
            return true
        }
        else
        {
            return false
        }
    }

}