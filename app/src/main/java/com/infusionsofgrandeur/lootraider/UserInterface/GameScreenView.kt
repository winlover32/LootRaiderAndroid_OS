package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.GameStateManager
import com.infusionsofgrandeur.lootraider.Managers.GameboardManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import java.util.*


class GameScreenView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
//class GameScreenView  : View
{

	private val currentGameboard = GameboardManager.getGameboard(GameStateManager.getCurrentLevel() - 1)
	lateinit var onscreenSpritesForEntities: Map<Int, GameScreenActivity.OnscreenSprite>
	private var spriteWidth = 0
	private var spriteHeight = 0
	private lateinit var sourceRect: Rect
	private var gameLoopTimer = Timer()
/*
	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
	{
		setWillNotDraw(false)
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0)
	{
		setWillNotDraw(false)
	}

	constructor(context: Context) : super(context)
	{
		setWillNotDraw(false)
	}
*/
	override fun onDraw(canvas: Canvas)
	{
		if (spriteWidth == 0 || spriteHeight == 0)
		{
			spriteWidth = GameStateManager.getTileWidth()
			spriteHeight = GameStateManager.getTileHeight()
			sourceRect = Rect(0, 0, spriteWidth, spriteHeight)
		}
		// Draw background
		for (row in 0 until currentGameboard.height)
		{
			for (column in 0 until currentGameboard.width)
			{
				val tileNumber = currentGameboard.tileMap[row][column]
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
		// Now draw the game objects
		val keys = onscreenSpritesForEntities.keys
		for (nextKey in keys)
		{
			val entityOnscreenSprite = onscreenSpritesForEntities[nextKey]
			val imageView = entityOnscreenSprite!!.imageView
			val startX = entityOnscreenSprite!!.xPos
			val startY = entityOnscreenSprite!!.yPos
			val endX = startX + spriteWidth
			val endY = startY + spriteHeight
			val bitmapDrawable = imageView.drawable as BitmapDrawable
			val bitmap = bitmapDrawable.bitmap
			val destRect = Rect(startX, startY, endX, endY)
			canvas.drawBitmap(bitmap, sourceRect, destRect,null)
		}
	}
}