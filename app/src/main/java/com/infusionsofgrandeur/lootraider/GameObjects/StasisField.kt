package com.infusionsofgrandeur.lootraider.GameObjects

import android.view.View
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.SoundManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager

import java.util.Date

import android.widget.ImageView

import kotlin.math.abs
import kotlin.experimental.and

class StasisField
{

    companion object
    {
        var startFrame: Int = -1
            set(start: Int)
            {
                field = start
            }
        var endFrame: Int = -1
            set(end: Int)
            {
                field = end
            }

        fun isPositionPlayerBlocked(xPosition: Int, yPosition: Int): Boolean
        {
            val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
            val fieldOne = GameStateManager.getStasisFieldOne()
            val fieldTwo = GameStateManager.getStasisFieldTwo()
            if (xPosition < 0 || xPosition >= currentLevel.width)
            {
                return true
            }
            else if ((fieldOne.activated && fieldOne.animationFrame == endFrame && fieldOne.guardHeld != null && fieldOne.xTile == xPosition && fieldOne.yTile == yPosition) || (fieldTwo.activated && fieldTwo.animationFrame == endFrame && fieldTwo.guardHeld != null && fieldTwo.xTile == xPosition && fieldTwo.yTile == yPosition))
            {
                return false
            }
            else if ((fieldOne.activated && fieldOne.xTile == xPosition && fieldOne.yTile == yPosition) || (fieldTwo.activated && fieldTwo.xTile == xPosition && fieldTwo.yTile == yPosition))
            {
                return true
            }
            else
            {
                return false
            }
        }

        fun isPositionBlocked(xPosition: Int, yPosition: Int): Boolean
        {
            val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
            val fieldOne = GameStateManager.getStasisFieldOne()
            val fieldTwo = GameStateManager.getStasisFieldTwo()
            if (xPosition < 0 || xPosition >= currentLevel.width)
            {
                return true
            }
            else if ((fieldOne.activated && (fieldOne.stage >= ConfigurationManager.stasisFieldBlockingStage || fieldOne.guardHeld != null) && fieldOne.xTile == xPosition && fieldOne.yTile == yPosition) || (fieldTwo.activated && (fieldTwo.stage >= ConfigurationManager.stasisFieldBlockingStage || fieldTwo.guardHeld != null) && fieldTwo.xTile == xPosition && fieldTwo.yTile == yPosition))
            {
                return true
            }
            else
            {
                return false
            }
        }

    }

    var activated = false
    var xTile = -1
    var yTile = -1
    var timeReachedFullStrength: Date? = null
    var guardHeld: Guard? = null
    var animationFrame = -1
    var imageView: ImageView? = null
    var stage = -1

    fun activate(xPosition: Int, yPosition: Int, xAxisMultiplier: Int, yAxisMultiplier: Int)
    {
        SoundManager.playRaiseStasisField()
        animationFrame = StasisField.startFrame
        val sprite = SpriteManager.getSprite(animationFrame)
        val bitmap = SpriteManager.bitmapForSprite(sprite!!)
        xTile = xPosition
        yTile = yPosition
        activated = true
        timeReachedFullStrength = null
        if (imageView == null)
        {
            imageView = ImageView(ConfigurationManager.getAppContext())
        }
        imageView!!.setImageBitmap(bitmap)
        imageView!!.setImageAlpha(ConfigurationManager.stasisFieldAlpha)
        imageView!!.left = xTile * GameStateManager.getTileWidth() * xAxisMultiplier
        imageView!!.right = xTile * GameStateManager.getTileWidth() * xAxisMultiplier + GameStateManager.getTileWidth() * xAxisMultiplier
        imageView!!.top = yTile * GameStateManager.getTileHeight() * yAxisMultiplier
        imageView!!.bottom = yTile * GameStateManager.getTileHeight() * yAxisMultiplier + GameStateManager.getTileHeight() * yAxisMultiplier
        stage = 0
        checkForTrappedGuards()
    }

    fun advance(xAxisMultiplier: Int, yAxisMultiplier: Int)
    {
        if (timeReachedFullStrength != null)
        {
            val duration = Date().time - timeReachedFullStrength!!.time
            if (duration > (ConfigurationManager.stasisFieldDuration * 1000))
            {
                if (animationFrame == StasisField.endFrame)
                {
                    SoundManager.playLowerStasisField()
                }
                animationFrame -= 1
                stage -= 1
                if (animationFrame >= StasisField.startFrame)
                {
                    val sprite = SpriteManager.getSprite(animationFrame)
                    val image = SpriteManager.bitmapForSprite(sprite!!)
                    imageView!!.setImageBitmap(image)
                }
                else
                {
                    activated = false
                    timeReachedFullStrength = null
                    imageView!!.setVisibility(View.GONE)
                    xTile = -1
                    yTile = -1
                }
                if (stage < ConfigurationManager.stasisFieldBlockingStage && guardHeld != null)
                {
                    guardHeld!!.inStasis = false
                    guardHeld = null
                }
            }
        }
        else
        {
            animationFrame += 1
            stage += 1
            val sprite = SpriteManager.getSprite(animationFrame)
            val image = SpriteManager.bitmapForSprite(sprite!!)
            imageView!!.setImageBitmap(image)
            checkForTrappedGuards()
            if (animationFrame == StasisField.endFrame)
            {
                timeReachedFullStrength = Date()
            }
        }
        imageView!!.left = (xTile * GameStateManager.getTileWidth() * xAxisMultiplier).toInt()
        imageView!!.right = (xTile * GameStateManager.getTileWidth() * xAxisMultiplier).toInt() + (GameStateManager.getTileWidth() * xAxisMultiplier).toInt()
        imageView!!.top = (yTile * GameStateManager.getTileHeight() * yAxisMultiplier).toInt()
        imageView!!.bottom = (yTile * GameStateManager.getTileHeight() * yAxisMultiplier).toInt() + (GameStateManager.getTileHeight() * yAxisMultiplier).toInt()
    }

    fun dissipate()
    {
        activated = false
        timeReachedFullStrength = null
        imageView!!.setVisibility(View.GONE)
        xTile = -1
        yTile = -1
        animationFrame = -1
        stage = -1
        if (guardHeld != null)
        {
            guardHeld!!.inStasis = false
            guardHeld = null
        }
    }

    fun checkForTrappedGuards()
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val guards = GameStateManager.getGuards()
        for (nextGuard in guards)
        {
            if (nextGuard.xTile == xTile && nextGuard.yTile == yTile && !nextGuard.inStasis && guardHeld == null)
            {
                guardHeld = nextGuard
                nextGuard.xPos = xTile * GameStateManager.getTileWidth()
                nextGuard.yPos = yTile * GameStateManager.getTileHeight()
                nextGuard.inStasis = true
                if (nextGuard.goldPossessed)
                {
                    nextGuard.goldPossessed = false
                    val bars = GameStateManager.getGoldBars()
                    for (nextBar in bars)
                    {
                        val holdingGuard = nextBar.possessedBy.let()
                        {
                            if (it === nextGuard)
                            {
                                nextBar.possessedBy = null
                                if (xTile == 0)
                                {
                                    val rightTile = currentLevel.tileMap[yTile][xTile + 1]
                                    val tileSprite = SpriteManager.getSprite(rightTile)
                                    val spriteCharacteristic = tileSprite!!.header[0]
                                    if (spriteCharacteristic and (ConfigurationManager.spriteHeaderTraversable) == ConfigurationManager.spriteHeaderTraversable)
                                    {
                                        nextBar.xTile = xTile + 1
                                        nextBar.yTile = yTile
                                    }
                                    else    // This shouldn't happen - if the gold can't be placed next to the guard, the guard keeps it
                                    {
                                        nextGuard.goldPossessed = true
                                        nextBar.possessedBy = nextGuard
                                    }
                                }
                                else if (xTile == currentLevel.width - 1)
                                {
                                    val leftTile = currentLevel.tileMap[yTile][xTile - 1]
                                    val tileSprite = SpriteManager.getSprite(leftTile)
                                    val spriteCharacteristic = tileSprite!!.header[0]
                                    if (spriteCharacteristic and (ConfigurationManager.spriteHeaderTraversable) == ConfigurationManager.spriteHeaderTraversable)
                                    {
                                        nextBar.xTile = xTile - 1
                                        nextBar.yTile = yTile
                                    }
                                    else    // This shouldn't happen - if the gold can't be placed next to the guard, the guard keeps it
                                    {
                                        nextGuard.goldPossessed = true
                                        nextBar.possessedBy = nextGuard
                                    }
                                }
                                else
                                {
                                    val player = GameStateManager.getPlayer()
                                    val leftTile = currentLevel.tileMap[yTile][xTile - 1]
                                    val leftTileSprite = SpriteManager.getSprite(leftTile)
                                    val leftSpriteCharacteristic = leftTileSprite!!.header[0]
                                    val rightTile = currentLevel.tileMap[yTile][xTile + 1]
                                    val rightTileSprite = SpriteManager.getSprite(rightTile)
                                    val rightSpriteCharacteristic = rightTileSprite!!.header[0]
                                    if (player.xTile < xTile && leftSpriteCharacteristic and (ConfigurationManager.spriteHeaderTraversable) == ConfigurationManager.spriteHeaderTraversable)
                                    {
                                        nextBar.xTile = xTile - 1
                                        nextBar.yTile = yTile
                                    }
                                    else if (rightSpriteCharacteristic and (ConfigurationManager.spriteHeaderTraversable) == ConfigurationManager.spriteHeaderTraversable)
                                    {
                                        nextBar.xTile = xTile + 1
                                        nextBar.yTile = yTile
                                    }
                                    else    // This shouldn't happen - if the gold can't be placed next to the guard, the guard keeps it
                                    {
                                        nextGuard.goldPossessed = true
                                        nextBar.possessedBy = nextGuard
                                    }
                                }
                            }
                        }
                    }
                }
                break
            }
        }
    }

}