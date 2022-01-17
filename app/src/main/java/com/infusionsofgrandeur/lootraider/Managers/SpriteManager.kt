package com.infusionsofgrandeur.lootraider.Managers

import com.infusionsofgrandeur.lootraider.GameObjects.Sprite

import android.graphics.Color
import android.graphics.Bitmap
import androidx.appcompat.app.AlertDialog
import com.infusionsofgrandeur.lootraider.R
import java.nio.ByteBuffer
import java.nio.IntBuffer

object SpriteManager
{

    data class AnimationFrameInfo(val bitmap: Bitmap, val frame: Int)
    {}

    lateinit private var header: List<Int>
    lateinit var sprites: List<Sprite>
    private var images = hashMapOf<Bitmap, Sprite>()

    fun loadSprites()
    {
        val spriteSetIdentifier = ConfigurationManager.getAppContext().resources.getIdentifier(ConfigurationManager.defaultSpriteFilename, "raw", ConfigurationManager.getAppContext().packageName)
        val spriteDataInputStream = ConfigurationManager.getAppContext().resources.openRawResource(spriteSetIdentifier)
        val spriteBytes = spriteDataInputStream.readBytes()
        spriteDataInputStream.close()
        header = parseSpritesetHeader(spriteBytes)
        sprites = parseSpritesetData(spriteBytes)
    }

    fun getSprite(number: Int): Sprite?
    {
        if (number < sprites.size)
        {
            return sprites[number]
        }
        else
        {
            return null
        }
    }

    fun bitmapForSprite(sprite: Sprite): Bitmap
    {
        for (nextKey in images.keys)
        {
            val value = images[nextKey]
            if (value === sprite)
            {
                return nextKey
            }
        }
        return images.keys.first()      // Should never reach this but put it in as a default to keep from declaring method as returning a nullable value
    }

    fun bitmapForSpriteNumber(spriteNumber: Int): Bitmap
    {
        return bitmapForSprite(sprites[spriteNumber])
    }

    fun spriteForFirstFrameOfAnimationNamed(name: String): Sprite
    {
        var index = 0
        for (nextSprite in sprites)
        {
            if (nextSprite.firstFrame == true && nextSprite.identifier == name)
            {
                return sprites[index]
            }
            index += 1
        }
        return sprites[0]
    }

    fun bitmapForFirstFrameOfAnimationNamed(name: String): AnimationFrameInfo
    {
        var index = 0
        for (nextSprite in sprites)
        {
            if (nextSprite.firstFrame == true && nextSprite.identifier == name)
            {
                return AnimationFrameInfo(bitmapForSprite(nextSprite), index)
            }
            index += 1
        }
        return AnimationFrameInfo(bitmapForSprite(sprites[0]), index)
    }

    fun bitmapForNextFrameOfAnimationNamed(name: String, currentFrame: Int): AnimationFrameInfo
    {
        if (currentFrame == sprites.size - 1)
        {
            return bitmapForFirstFrameOfAnimationNamed(name)
        }
        for (index in currentFrame until (sprites.size - 1))
        {
            val currentSprite = sprites[index]
            val nextSprite = sprites[index + 1]
            if (currentSprite.lastFrame)
            {
                return bitmapForFirstFrameOfAnimationNamed(name)
            }
            else
            {
                return AnimationFrameInfo(bitmapForSprite(nextSprite), currentFrame + 1)
            }
        }
        return AnimationFrameInfo(bitmapForSprite(sprites[0]), 0)
    }

    fun imageForLastFrameOfAnimationNamed(name: String): AnimationFrameInfo
    {
        var index = 0
        var currentAnimationIdentifier: String? = null
        for (nextSprite in sprites)
        {
            if (nextSprite.firstFrame)
            {
                currentAnimationIdentifier = nextSprite.identifier
            }
            if (nextSprite.lastFrame == true && currentAnimationIdentifier == name)
            {
                return AnimationFrameInfo(bitmapForSprite(nextSprite), index)
            }
            index += 1
        }
        return AnimationFrameInfo(bitmapForSprite(sprites[0]), 0)
    }

    private fun parseSpritesetHeader(bytes: ByteArray): List<Int>
    {
        var header = mutableListOf<Int>()
        var index = ConfigurationManager.spriteSetDefaultHeader.size
        if (isSpriteSetHeader(bytes))
        {
            val headerCount = bytes.get(index++).toInt()
            if (headerCount > 0)
            {
                for (x in 1..headerCount)
                {
                    header.add(bytes.get(index++).toInt())
                }
            }
        }
        else
        {
            AlertDialog.Builder(ConfigurationManager.getAppContext())
                .setTitle(R.string.data_parsing_error_title)
                .setMessage(R.string.data_parsing_error_description)
                .setPositiveButton(R.string.button_title_ok, null)
                .create()
                .show()
        }
        return (header)
    }

    private fun parseSpritesetData(bytes: ByteArray): MutableList<Sprite>
    {
        var spriteList = mutableListOf<Sprite>()
        if (isSpriteSetHeader(bytes))
        {
            var header = parseSpritesetHeader(bytes)
            var index = ConfigurationManager.spriteSetDefaultHeader.size + 1
            index += if (header.isNotEmpty()) header.size else 1
            var subArray = bytes.takeLast(bytes.size - index).toByteArray()
            while (isSpriteHeader(subArray))
            {
                var subIndex = ConfigurationManager.spriteDelineator.size
                var headerBytes = mutableListOf<Int>()
                var identifier = ""
                if (isSpriteHeaderDataHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionHeaderDelineator.size
                    val headerCount = subArray.get(subIndex++).toInt()
                    if (headerCount > 0)
                    {
                        for (x in 1..headerCount)
                        {
                            headerBytes.add(subArray.get(subIndex++).toInt())
                        }
                    }
                }
                if (isSpriteIdentifierHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionIdentifierDelineator.size
                    val identifierCount = subArray.get(subIndex++).toInt()
                    val identifierBytes = subArray.takeLast(subArray.size - subIndex).take(identifierCount)
                    identifier = String(identifierBytes.toByteArray())
                    subIndex += identifierCount
                }
                var animStartFrame = false
                var animEndFrame = false
                if (isAnimationHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionAnimationDelineator.size
                    val animationByte = subArray.get(subIndex++).toInt()
                    if (animationByte == 0x01)
                    {
                        animStartFrame = true
                    }
                    else if (animationByte == 0x02)
                    {
                        animEndFrame = true
                    }
                }
                var backgroundColor: Color = Color.valueOf(Color.TRANSPARENT)
                if (isBackgroundHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionBackgroundColorDelineator.size
                    val alpha = subArray.get((subIndex++).toInt() / 255).toFloat()
                    val red = subArray.get((subIndex++).toInt() / 255).toFloat()
                    val green = subArray.get((subIndex++).toInt() / 255).toFloat()
                    val blue = subArray.get((subIndex++).toInt() / 255).toFloat()
                    backgroundColor = Color.valueOf(red, green, blue, alpha)
                }
                var width = 0
                var height = 0
                if (isSpriteDimensionsHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionDimensionsDelineator.size
                    var dimenHigherByte = subArray.get(subIndex++).toInt()
                    var dimenLowerByte = subArray.get(subIndex++).toInt()
                    width = dimenLowerByte + (dimenHigherByte shl 8)
                    dimenHigherByte = subArray.get(subIndex++).toInt()
                    dimenLowerByte = subArray.get(subIndex++).toInt()
                    height = dimenLowerByte + (dimenHigherByte shl 8)
                }
                val transparentPixel = Color.valueOf(0)
                var pixelMapRow: MutableList<Color> = MutableList(width) { transparentPixel }
                var pixelMap: MutableList<MutableList<Color>> = MutableList(height) { pixelMapRow }
                var pixelIntArray = IntArray(width * height)
                var pixelIndex = 0
                var bitmap: Bitmap? = null
                if (isPixelDataHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionPixelMapDelineator.size
                    for (y in 0 until height)
                    {
                        for (x in 0 until width)
                        {
                            val alpha = subArray.get(subIndex++).toByte()
                            var alphaInt = alpha.toInt()
                            val red = subArray.get(subIndex++).toByte()
                            val redInt = red.toInt()
                            val green = subArray.get(subIndex++).toByte()
                            val greenInt = green.toInt()
                            val blue = subArray.get(subIndex++).toByte()
                            val blueInt = blue.toInt()
                            if (ConfigurationManager.makeBlackPixelsTransparent && red == 0x00.toByte() && green == 0x00.toByte() && blue == 0x00.toByte())
                            {
                                alphaInt = 0
                                pixelMap[y][x] = Color.valueOf((red.toFloat() / 255.0).toFloat(), (green.toFloat() / 255.0).toFloat(), (blue.toFloat() / 255.0).toFloat(), 0.0.toFloat())
                            }
                            else
                            {
                                pixelMap[y][x] = Color.valueOf((red.toFloat() / 255.0).toFloat(), (green.toFloat() / 255.0).toFloat(), (blue.toFloat() / 255.0).toFloat(), (alpha.toFloat() / 255.0).toFloat())
                            }
                            pixelIntArray.set(pixelIndex++, ((alphaInt and 0xff) shl 24) + ((redInt and 0xff) shl 16) + ((greenInt and 0xff) shl + 8) + (blueInt and 0xff))
                        }
                    }
                    bitmap = Bitmap.createBitmap(pixelIntArray, width, height, Bitmap.Config.ARGB_8888)
                }
                var pixelMaskRow: MutableList<Boolean> = MutableList(width) { false }
                var pixelMask: MutableList<MutableList<Boolean>> = MutableList(height) { pixelMaskRow }
                if (isPixelMaskDataHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.spriteSectionPixelMaskDelineator.size
                    for (y in 0 until height)
                    {
                        for (x in 0 until width)
                        {
                            val maskByte = subArray.get(subIndex++)     // NOTE: The iOS code checks here to make sure there is data left to parse
                            if (maskByte.toInt() == 0)
                            {
                                pixelMask[y][x] = false
                            }
                            else
                            {
                                pixelMask[y][x] = true
                            }
                        }
                    }
                }
                if (bitmap != null)
                {
                    val sprite = Sprite(width, height, headerBytes, identifier, animStartFrame, animEndFrame, backgroundColor, pixelMap, pixelMask, bitmap)
                    images[bitmap] = sprite
                    spriteList.add(sprite)
                    subArray = bytes.takeLast(subArray.size - subIndex).toByteArray()
                }
            }
        }
        return (spriteList)
    }

    private fun isSpriteSetHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSetDefaultHeader[0] && data[1] == ConfigurationManager.spriteSetDefaultHeader[1] && data[2] == ConfigurationManager.spriteSetDefaultHeader[2]
    }

    private fun isSpriteHeader(data: ByteArray): Boolean
    {
        if (data.size < ConfigurationManager.spriteDelineator.size)
        {
            return false
        }
        else return data[0] == ConfigurationManager.spriteDelineator[0] && data[1] == ConfigurationManager.spriteDelineator[1] && data[2] == ConfigurationManager.spriteDelineator[2] && data[3] == ConfigurationManager.spriteDelineator[3] && data[4] == ConfigurationManager.spriteDelineator[4]
    }

    private fun isSpriteHeaderDataHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionHeaderDelineator[0] && data[1] == ConfigurationManager.spriteSectionHeaderDelineator[1]
    }

    private fun isSpriteIdentifierHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionIdentifierDelineator[0] && data[1] == ConfigurationManager.spriteSectionIdentifierDelineator[1]
    }

    private fun isAnimationHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionAnimationDelineator[0] && data[1] == ConfigurationManager.spriteSectionAnimationDelineator[1]
    }

    private fun isBackgroundHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionBackgroundColorDelineator[0] && data[1] == ConfigurationManager.spriteSectionBackgroundColorDelineator[1]
    }

    private fun isSpriteDimensionsHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionDimensionsDelineator[0] && data[1] == ConfigurationManager.spriteSectionDimensionsDelineator[1]
    }

    private fun isPixelDataHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionPixelMapDelineator[0] && data[1] == ConfigurationManager.spriteSectionPixelMapDelineator[1]
    }

    private fun isPixelMaskDataHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.spriteSectionPixelMaskDelineator[0] && data[1] == ConfigurationManager.spriteSectionPixelMaskDelineator[1]
    }

}