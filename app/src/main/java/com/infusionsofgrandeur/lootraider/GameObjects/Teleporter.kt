package com.infusionsofgrandeur.lootraider.GameObjects

import android.widget.ImageView
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager

class Teleporter(xPos: Int, yPos: Int, xTile: Int, yTile: Int, status: Motion, animationFrame: Int, var sendable: Boolean, var receivable: Boolean, var roundtrippable: Boolean, var identifier: Int?) : Entity(xPos, yPos, xTile, yTile, status, animationFrame)
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
    }

    var pair: Teleporter? = null
    var pulseOut = true
    var cyclesToSkip = ConfigurationManager.cyclesToSkipBetweenTeleporterFrames

    fun runCycle(imageView: ImageView)
    {
        if (cyclesToSkip > 0)
        {
            cyclesToSkip -= 1
            return
        }
        if (roundtrippable)
        {
            if (pulseOut)
            {
                if (animationFrame < Teleporter.endFrame)
                {
                    animationFrame += 1
                }
                else
                {
                    animationFrame -= 1
                    pulseOut = false
                }
                val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                imageView.setImageBitmap(image)
            }
            else
            {
                if (animationFrame > Teleporter.startFrame)
                {
                    animationFrame -= 1
                }
                else
                {
                    animationFrame += 1
                    pulseOut = true
                }
                val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
                imageView.setImageBitmap(image)
            }
        }
        else if (receivable)
        {
            if (animationFrame < Teleporter.endFrame)
            {
                animationFrame += 1
            }
            else
            {
                animationFrame = Teleporter.startFrame
            }
            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
            imageView.setImageBitmap(image)
        }
        else if (sendable)
        {
            if (animationFrame == Teleporter.startFrame)
            {
                animationFrame = Teleporter.endFrame
            }
            else
            {
                animationFrame -= 1
            }
            val image = SpriteManager.bitmapForSpriteNumber(animationFrame)
            imageView.setImageBitmap(image)
        }
        cyclesToSkip = ConfigurationManager.cyclesToSkipBetweenTeleporterFrames
    }
}