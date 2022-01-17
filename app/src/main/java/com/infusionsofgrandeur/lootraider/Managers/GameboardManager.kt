package com.infusionsofgrandeur.lootraider.Managers

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.ioginfrastructure.Managers.Configuration.IoGConfigurationManager
import com.infusionsofgrandeur.ioginfrastructure.Managers.Persistence.IoGPersistenceManager
import com.infusionsofgrandeur.lootraider.GameObjects.Gameboard
import com.infusionsofgrandeur.lootraider.R

object GameboardManager
{

    lateinit private var header: List<Int>
    private var gameboards = mutableListOf<Gameboard>()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun loadGameboards()
    {
        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        // Load default gameboard set first
        val gameboardIdentifier = ConfigurationManager.getAppContext().resources.getIdentifier(ConfigurationManager.defaultGameboardFilename, "raw", ConfigurationManager.getAppContext().packageName)
        val gameboardDataInputStream = ConfigurationManager.getAppContext().resources.openRawResource(gameboardIdentifier)
        val gameboardBytes = gameboardDataInputStream.readBytes()
        gameboardDataInputStream.close()
        header = parseGameboardsetHeader(gameboardBytes)
        gameboards = parseGameboardsetData(gameboardBytes)
        // Then load any additional sets downloaded as In-App purchases
        if (IoGPersistenceManager.getSharedManager().checkForValue(ConfigurationManager.persistenceItemPurchasedItems, IoGPersistenceManager.PersistenceSource.SharedPreferences))
        {
            val purchasedItemsResponse = IoGPersistenceManager.getSharedManager().readValue(ConfigurationManager.persistenceItemPurchasedItems,IoGPersistenceManager.PersistenceSource.SharedPreferences)
            val purchasedItemEntries = purchasedItemsResponse.get(IoGConfigurationManager.persistenceReadResultValue) as List<String>
            for (nextItem in purchasedItemEntries)
            {
                var purchasedItemList = mutableListOf<String>()
                if (ConfigurationManager.comboIdentifiers.contains(nextItem))
                {
                    purchasedItemList.addAll(productIdentifiersForComposite(nextItem))
                }
                else
                {
                    if (nextItem != ConfigurationManager.unlockAllLevelsIdentifier)
                    {
                        purchasedItemList.add(nextItem)
                    }
                }
                for (item in purchasedItemList)
                {
                    val additionalGameboardIdentifier = ConfigurationManager.getAppContext().resources.getIdentifier(ConfigurationManager.filenameForProductIdentifier(item), "raw", ConfigurationManager.getAppContext().packageName)
                    val additionalGameboardDataInputStream = ConfigurationManager.getAppContext().resources.openRawResource(additionalGameboardIdentifier)
                    val additionalGameboardBytes = additionalGameboardDataInputStream.readBytes()
                    additionalGameboardDataInputStream.close()
                    gameboards.addAll(parseGameboardsetData(additionalGameboardBytes))
                }
            }
        }
    }

    private fun productIdentifiersForComposite(identifier: String): List<String>
    {
        when (identifier)
        {
            ConfigurationManager.levels7Through30Identifier -> return (arrayOf(ConfigurationManager.levels7Through12Identifier, ConfigurationManager.levels13Through18Identifier, ConfigurationManager.levels19Through24Identifier, ConfigurationManager.levels25Through30Identifier).toList())
            ConfigurationManager.levels7Through30AndOOPIdentifier -> return (arrayOf(ConfigurationManager.levels7Through12Identifier, ConfigurationManager.levels13Through18Identifier, ConfigurationManager.levels19Through24Identifier, ConfigurationManager.levels25Through30Identifier).toList())
        }
        return mutableListOf<String>()
    }

    fun getGameboard(number: Int): Gameboard
    {
        for (nextGameboard in gameboards)
        {
            if (nextGameboard.number == number + 1)
            {
                return nextGameboard
            }
        }
        // The above should catch the correct level. If for some reason it doesn't, default to the first level
        firebaseAnalytics.logEvent("GameboardError", Bundle().apply {
            putInt(FirebaseAnalytics.Param.LEVEL, number)
        })
        return gameboards[0]
    }

    private fun parseGameboardsetHeader(bytes: ByteArray): List<Int>
    {
        var header = mutableListOf<Int>()
        var index = ConfigurationManager.gameboardSetDefaultHeader.size
        if (isGameboardSetHeader(bytes))
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

    private fun parseGameboardsetData(bytes: ByteArray): MutableList<Gameboard>
    {
        var gameboardList = mutableListOf<Gameboard>()
        if (isGameboardSetHeader(bytes))
        {
            var header = parseGameboardsetHeader(bytes)
            var index = ConfigurationManager.gameboardSetDefaultHeader.size + 1
            index += if (header.isNotEmpty()) header.size else 1
            var subArray = bytes.takeLast(bytes.size - index).toByteArray()
            while (isGameboardHeader(subArray))
            {
                var subIndex = ConfigurationManager.gameboardDelineator.size
                var headerBytes = mutableListOf<Byte>()
                var identifier = ""
                var number = 0
                if (isGameboardHeaderDataHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.gameboardSectionHeaderDelineator.size
                    val headerCount = subArray.get(subIndex++).toInt()
                    if (headerCount > 0)
                    {
                        for (x in 1..headerCount)
                        {
                            headerBytes.add(subArray.get(subIndex++))
                        }
                        number = headerBytes.get(0).toInt()
                    }
                }
                if (isGameboardIdentifierHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.gameboardSectionIdentifierDelineator.size
                    val identifierCount = subArray.get(subIndex++).toInt()
                    val identifierBytes = subArray.takeLast(subArray.size - subIndex).take(identifierCount)
                    identifier = String(identifierBytes.toByteArray())
                    subIndex += identifierCount
                }
                var width = 0
                var height = 0
                if (isGameboardDimensionsHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.gameboardSectionDimensionsDelineator.size
                    var dimenHigherByte = subArray.get(subIndex++).toInt()
                    var dimenLowerByte = subArray.get(subIndex++).toInt()
                    width = dimenLowerByte + dimenHigherByte.shl(8)
                    dimenHigherByte = subArray.get(subIndex++).toInt()
                    dimenLowerByte = subArray.get(subIndex++).toInt()
                    height = dimenLowerByte + dimenHigherByte.shl(8)
                }
                var tileMap = MutableList(height){MutableList(width){0}}
                var spriteMap = MutableList(height){MutableList(width){-1}}
                var attributeMap = MutableList(height){MutableList(width){mutableListOf<Int>()}}
                if (isTileMapHeader(subArray.takeLast(subArray.size - subIndex).toByteArray()))
                {
                    subIndex += ConfigurationManager.gameboardSectionTileMapDelineator.size
                    for (rows in 0 until height)
                    {
                        for (columns in 0 until width)
                        {
                            var tile = subArray.get(subIndex++).toInt()
                            var sprite = subArray.get(subIndex++).toInt()
                            var attributeCount = subArray.get(subIndex++).toInt()
                            if (tile > 0)
                            {
                                tileMap[rows][columns] = tile - 1
                            }
                            spriteMap[rows][columns] = sprite - 1
                            if (attributeCount > 0)
                            {
                                var attributes = attributeMap[rows][columns]
                                for (attribute in 0 until attributeCount)
                                {
                                    var attr = subArray.get(subIndex++).toInt()
                                    if (attr == -128)
                                    {
                                        attr = 128
                                    }
                                    attributes.add(attr)
                                }
                            }
                        }
                    }
                }
                val gameboard = Gameboard(width, height, header, identifier, number, tileMap, spriteMap, attributeMap)
                gameboardList.add(gameboard)
                // Add gameboard to list of authorized gameboards if necessary
                ConfigurationManager.addAuthorizedLevel(number, identifier)
                subArray = bytes.takeLast(subArray.size - subIndex).toByteArray()
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
        return (gameboardList)
    }

    private fun isGameboardSetHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.gameboardSetDefaultHeader[0] && data[1] == ConfigurationManager.gameboardSetDefaultHeader[1] && data[2] == ConfigurationManager.gameboardSetDefaultHeader[2]
    }

    private fun isGameboardHeader(data: ByteArray): Boolean
    {
        if (data.size < ConfigurationManager.gameboardDelineator.size)
        {
            return false
        }
        else return data[0] == ConfigurationManager.gameboardDelineator[0] && data[1] == ConfigurationManager.gameboardDelineator[1] && data[2] == ConfigurationManager.gameboardDelineator[2] && data[3] == ConfigurationManager.gameboardDelineator[3] && data[4] == ConfigurationManager.gameboardDelineator[4]
    }

    private fun isGameboardHeaderDataHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.gameboardSectionHeaderDelineator[0] && data[1] == ConfigurationManager.gameboardSectionHeaderDelineator[1]
    }

    private fun isGameboardIdentifierHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.gameboardSectionIdentifierDelineator[0] && data[1] == ConfigurationManager.gameboardSectionIdentifierDelineator[1]
    }

    private fun isGameboardDimensionsHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.gameboardSectionDimensionsDelineator[0] && data[1] == ConfigurationManager.gameboardSectionDimensionsDelineator[1]
    }

    private fun isTileMapHeader(data: ByteArray): Boolean
    {
        return data[0] == ConfigurationManager.gameboardSectionTileMapDelineator[0] && data[1] == ConfigurationManager.gameboardSectionTileMapDelineator[1]
    }

}
