package com.infusionsofgrandeur.lootraider.GameObjects

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager

import kotlin.experimental.and
import kotlin.experimental.or

open class Entity(var xPos: Int, var yPos: Int, var xTile: Int, var yTile: Int, var status: Motion, var animationFrame: Int)
{
    companion object
    {
        var nextEntityID = 0

        fun getNextID(): Int
        {
            return ++nextEntityID
        }

        fun getLastID(): Int
        {
            return nextEntityID
        }
    }

    data class SurroundingTiles(val left: Int, val right: Int, val up: Int, val down: Int)
    {}

    data class SurroundingTileAttributes(val left: Int, val right: Int, val up: Int, val down: Int)
    {}

    enum class Motion
    {
        Still,
        Left,
        Right,
        ClimbingDown,
        ClimbingUp,
        Falling,
        PlatformLeft,
        PlatformRight,
        PlatformUp,
        PlatformDown
    }

    var entityID: Int

    init
    {
        entityID = getNextID()
    }

    fun getCurrentTileAttributes(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        var attributes = 0
        if (currentLevel.attributeMap[yTile][xTile].size > 0)
        {
            attributes = currentLevel.attributeMap[yTile][xTile][0]
        }
        else
        {
            attributes = 0
        }
        return attributes
    }

    fun getCurrentTileCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        val tileNumber = currentLevel.tileMap[yTile][xTile]
        if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        return getCharacteristicsForTileNumber(tileNumber)
    }

    fun getTileLeftCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (xTile == 0)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile][xTile - 1]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getTileRightCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (xTile == currentLevel.width - 1)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile][xTile + 1]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getTileDownLeftCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (xTile == 0 || yTile == currentLevel.height - 1)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile + 1][xTile - 1]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getTileDownRightCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (xTile == currentLevel.width - 1 || yTile == currentLevel.height - 1)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile + 1][xTile + 1]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getTileUpCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (yTile == 0)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile - 1][xTile]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getTileDownCharacteristics(): Int
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        if (yTile == currentLevel.height - 1)
        {
            return 0
        }
        else if (StasisField.isPositionBlocked(xTile, yTile))
        {
            return 0
        }
        else
        {
            val tileNumber = currentLevel.tileMap[yTile + 1][xTile]
            return getCharacteristicsForTileNumber(tileNumber)
        }
    }

    fun getCharacteristicsForTileNumber(tileNumber: Int): Int
    {
        val tileSprite = SpriteManager.getSprite(tileNumber)
        val spriteCharacteristic = tileSprite?.header?.get(0)
        // Tweak made because sprite files didn't properly set rope tiles as fallthroughable
        if (((spriteCharacteristic ?: 0) and ConfigurationManager.spriteHeaderHangable) == ConfigurationManager.spriteHeaderHangable)
        {
            return ((spriteCharacteristic ?: 0) or ConfigurationManager.spriteHeaderFallthroughable)
        }
        return spriteCharacteristic ?: 0
    }

    fun getSurroundingTiles(): SurroundingTiles
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        var left = -1
        var right = -1
        var up = -1
        var down = -1

        if (xTile > 0)
        {
            left = currentLevel.tileMap[yTile][xTile - 1]
        }
        if (xTile < (currentLevel.width - 1))
        {
            right = currentLevel.tileMap[yTile][xTile + 1]
        }
        if (yTile > 0)
        {
            up = currentLevel.tileMap[yTile - 1][xTile]
        }
        if (yTile < (currentLevel.height - 1))
        {
            down = currentLevel.tileMap[yTile + 1][xTile]
        }

        return (SurroundingTiles(left, right, up, down))
    }

    fun getSurroundingAttributes(): SurroundingTileAttributes
    {
        val currentLevel = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
        var left = 0
        var right = 0
        var up = 0
        var down = 0

        // Figure Left clearness
        if (xTile > 0)
        {
            if (currentLevel.attributeMap[yTile][xTile - 1].size > 0)
            {
                left = currentLevel.attributeMap[yTile][xTile - 1][0]
            }
        }
        // Figure Right clearness
        if (xTile < (currentLevel.width - 1))
        {
            if (currentLevel.attributeMap[yTile][xTile + 1].size > 0)
            {
                right = currentLevel.attributeMap[yTile][xTile + 1][0]
            }
        }
        // Figure Top clearness
        if (yTile > 0)
        {
            if (currentLevel.attributeMap[yTile - 1][xTile].size > 0)
            {
                up = currentLevel.attributeMap[yTile - 1][xTile][0]
            }
        }
        // Figure Bottom clearness
        if (yTile < (currentLevel.height - 1))
        {
            if (currentLevel.attributeMap[yTile + 1][xTile].size > 0)
            {
                down = currentLevel.attributeMap[yTile + 1][xTile][0]
            }
        }

        return (SurroundingTileAttributes(left, right, up, down))
    }

    fun isCharacterTraversable(tileNumber: Int, tileAttribute: Int): Boolean
    {
        if (!(tileNumber > -1))
        {
            return false
        }
        val tileSprite = SpriteManager.getSprite(tileNumber)
        val spriteAttribute = tileSprite?.header?.get(0)
        var traversable = false
        // First, check the tile sprite's attributes
        if ((spriteAttribute ?: 0) and ConfigurationManager.spriteHeaderTraversable == ConfigurationManager.spriteHeaderTraversable)
        {
            traversable = true
        }
        else if (spriteAttribute?.toInt() == 0)
        {
            return false
        }
        // Then, check the attributes of the tiles position
        if (tileAttribute.toInt() == 0)
        {
            traversable = true
        }
        return traversable
    }

    fun isCharacterFallthroughable(tileNumber: Int, tileAttribute: Int): Boolean
    {
        if (!(tileNumber > -1))
        {
            return false
        }
        val tileSprite = SpriteManager.getSprite(tileNumber)
        val spriteAttribute = tileSprite?.header?.get(0)
        var fallthroughable = false
        // First, check the tile sprite's attributes
        // Tweak made because sprite files didn't properly set rope tiles as fallthroughable
        if (((spriteAttribute ?: 0) and ConfigurationManager.spriteHeaderFallthroughable == ConfigurationManager.spriteHeaderFallthroughable) || ((spriteAttribute ?: 0) and ConfigurationManager.spriteHeaderHangable == ConfigurationManager.spriteHeaderHangable))
        {
            fallthroughable = true
        }
        else if (spriteAttribute?.toInt() == 0)
        {
            return false
        }
        return fallthroughable
    }
}