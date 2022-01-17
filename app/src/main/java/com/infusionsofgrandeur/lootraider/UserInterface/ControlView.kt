package com.infusionsofgrandeur.lootraider.UserInterface

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager.ControlType

import kotlin.math.sqrt
import kotlin.math.abs

class ControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
{

    enum class ControlSet
    {
        BothAxes,
        Horizontal,
        Vertical
    }

    enum class ControllerDirection
    {
        Center,
        Up,
        UpRight,
        Right,
        DownRight,
        Down,
        DownLeft,
        Left,
        UpLeft
    }

    interface ControlDelegate
    {
        fun directionTapped(direction : ControlView.ControllerDirection)
        fun directionUpdated(direction : ControlView.ControllerDirection)
        fun directionSwiped(direction : ControlView.ControllerDirection)
        fun controlReleased()
    }

    private var controlType = ConfigurationManager.getControlType()
    lateinit var controlSet: ControlSet
    private var touchStartPositionX: Float = 0.0F
    private var touchStartPositionY: Float = 0.0F
    private val scale = resources.displayMetrics.density

    lateinit var delegate: ControlDelegate

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        when (event?.action)
        {
            MotionEvent.ACTION_DOWN -> {
                touchStartPositionX = event.x
                touchStartPositionY = event.y
                if (controlType == ControlType.Tap)
                {
                    if (controlSet == ControlSet.BothAxes)
                    {
                        val direction = getControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionTapped(direction)
                    }
                    else if (controlSet == ControlSet.Horizontal)
                    {
                        val direction = getHorizontalControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionTapped(direction)
                    }
                    else if (controlSet == ControlSet.Vertical)
                    {
                        val direction = getVerticalControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionTapped(direction)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val touchEndPositionX = event.x
                val touchEndPositionY = event.y
                if (controlType == ControlType.Tap)
                {
                    if (controlSet == ControlSet.BothAxes)
                    {
                        val direction = getControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionUpdated(direction)
                    }
                    else if (controlSet == ControlSet.Horizontal)
                    {
                        val direction = getHorizontalControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionTapped(direction)
                    }
                    else if (controlSet == ControlSet.Vertical)
                    {
                        val direction = getVerticalControlDirectionFor(touchStartPositionX, touchStartPositionY)
                        delegate?.directionTapped(direction)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (controlType == ControlType.Tap)
                {
                    delegate?.controlReleased()
                }
            }
        }
        return true
    }

    private fun getControlDirectionFor(touchPointX: Float, touchPointY: Float) : ControllerDirection
    {
        val width = if (scale == 1.0F) this.width.toFloat() else this.width.toFloat() / scale
        val height = if (scale == 1.0F) this.height.toFloat() else this.height.toFloat() / scale
        val scaledTouchPointX = if (scale == 1.0F) touchPointX else touchPointX / scale
        val scaledTouchPointY = if (scale == 1.0F) touchPointY else touchPointY / scale
        val centerPointX = width / 2.0F
        val centerPointY = height / 2.0F
        val normalizedX = scaledTouchPointX - centerPointX
        val normalizedY = centerPointY - scaledTouchPointY
        val distanceFromCenter = sqrt((abs(normalizedX) * abs(normalizedX)) + (abs(normalizedY) * abs(normalizedY)))
        var aboveLine1 = false
        var aboveLine2 = false
        var nearUpperRight = false
        var nearUpperLeft = false
        var nearLowerLeft = false
        var nearLowerRight = false
        var deadRadius = (width * ConfigurationManager.defaultControllerCenterDeadRadius) / ConfigurationManager.defaultControllerSideLength
        if (distanceFromCenter < deadRadius)
        {
            return ControllerDirection.Center
        }
        else if (scaledTouchPointX >= centerPointX && scaledTouchPointY <= centerPointY)
        {
            aboveLine1 = true
            if (normalizedX <= normalizedY)
            {
                aboveLine2 = true
                if ((((normalizedY - normalizedX) / width) * 100.0F).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearUpperRight = true
                }
            }
            else
            {
                if ((((normalizedX - normalizedY) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearUpperRight = true
                }
            }
        }
        else if (scaledTouchPointX <= centerPointX && scaledTouchPointY <= centerPointY)
        {
            aboveLine2 = true
            if (abs(normalizedX) < normalizedY)
            {
                aboveLine1 = true
                if ((((normalizedY - abs(normalizedX)) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearUpperLeft = true
                }
            }
            else
            {
                if ((((abs(normalizedX) - normalizedY) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearUpperLeft = true
                }
            }
        }
        else if (scaledTouchPointX <= centerPointX && scaledTouchPointY >= centerPointY)
        {
            aboveLine1 = false
            if (normalizedX < normalizedY)
            {
                aboveLine2 = true
                if ((((abs(normalizedX) - abs(normalizedY)) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearLowerLeft = true
                }
            }
            else
            {
                if ((((abs(normalizedY) - abs(normalizedX)) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearLowerLeft = true
                }
            }
        }
        else if (scaledTouchPointX >= centerPointX && scaledTouchPointY >= centerPointY)
        {
            aboveLine2 = false
            if (normalizedX > abs(normalizedY))
            {
                aboveLine1 = true
                if ((((normalizedX - abs(normalizedY)) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearLowerRight = true
                }
            }
            else
            {
                if ((((abs(normalizedY) - normalizedX) / width) * 100).toInt() < ConfigurationManager.defaultMultiControlButtonProximityPercent)
                {
                    nearLowerRight = true
                }
            }
        }
        if (nearUpperLeft)
        {
            return ControllerDirection.UpLeft
        }
        else if (nearUpperRight)
        {
            return ControllerDirection.UpRight
        }
        else if (nearLowerLeft)
        {
            return ControllerDirection.DownLeft
        }
        else if (nearLowerRight)
        {
            return ControllerDirection.DownRight
        }
        else if (aboveLine1 && aboveLine2)
        {
            return ControllerDirection.Up
        }
        else if ((!aboveLine2) && aboveLine1)
        {
            return ControllerDirection.Right
        }
        else if ((!aboveLine2) && !aboveLine2)
        {
            return ControllerDirection.Down
        }
        else if ((!aboveLine1) && aboveLine2)
        {
            return ControllerDirection.Left
        }
        return ControllerDirection.Up
    }

    private fun getHorizontalControlDirectionFor(touchPointX: Float, touchPointY: Float) : ControllerDirection
    {
        val width = if (scale == 1.0F) this.width.toFloat() else this.width.toFloat() / scale
        val height = if (scale == 1.0F) this.height.toFloat() else this.height.toFloat() / scale
        val scaledTouchPointX = if (scale == 1.0F) touchPointX else touchPointX / scale
        val scaledTouchPointY = if (scale == 1.0F) touchPointY else touchPointY / scale
        val centerPointX = width / 2.0F
        val centerPointY = height / 2.0F
        if (scaledTouchPointX <= centerPointX)
        {
            return ControllerDirection.Left
        }
        else
        {
            return ControllerDirection.Right
        }
    }

    private fun getVerticalControlDirectionFor(touchPointX: Float, touchPointY: Float) : ControllerDirection
    {
        val width = if (scale == 1.0F) this.width.toFloat() else this.width.toFloat() / scale
        val height = if (scale == 1.0F) this.height.toFloat() else this.height.toFloat() / scale
        val scaledTouchPointX = if (scale == 1.0F) touchPointX else touchPointX / scale
        val scaledTouchPointY = if (scale == 1.0F) touchPointY else touchPointY / scale
        val centerPointX = width / 2.0F
        val centerPointY = height / 2.0F
        if (scaledTouchPointY <= centerPointY)
        {
            return ControllerDirection.Up
        }
        else
        {
            return ControllerDirection.Down
        }
    }
}
