package com.infusionsofgrandeur.lootraider.GameObjects

open class Gameboard(val width: Int, val height: Int, val header: List<Int>, val identifier: String, val number: Int, val tileMap: List<MutableList<Int>>, val spriteMap: List<List<Int>>, val attributeMap: MutableList<MutableList<MutableList<Int>>>)
{

    init
    {
        val numberOfHeaderBytes = header.size
    }

    fun setTile(xTile: Int, yTile: Int, tileNumber: Int, attributes: MutableList<Int>)
    {
        tileMap[yTile][xTile] = tileNumber
        attributeMap[yTile][xTile] = attributes
    }
}