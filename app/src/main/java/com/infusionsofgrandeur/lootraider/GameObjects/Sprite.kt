package com.infusionsofgrandeur.lootraider.GameObjects

import android.graphics.Color
import android.graphics.Bitmap

open class Sprite(val width: Int, val height: Int, val header: List<Int>, val identifier: String, val firstFrame: Boolean, val lastFrame: Boolean, val background: Color, val pixelMap: List<List<Color>>, val pixelMask: List<List<Boolean>>, val bitmap: Bitmap)
{

    init
    {
        val numberOfHeaderBytes = header.size
    }

}