package com.infusionsofgrandeur.lootraider.GameObjects

class GoldBar(xPos: Int, yPos: Int, xTile: Int, yTile: Int, status: Motion, animationFrame: Int) : Entity(xPos, yPos, xTile, yTile, status, animationFrame)
{
    var possessedBy: Entity? = null
}