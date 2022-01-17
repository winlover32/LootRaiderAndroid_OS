package com.infusionsofgrandeur.lootraider.Managers

import android.media.MediaPlayer

import com.infusionsofgrandeur.lootraider.R

object SoundManager
{

    var themeAudioPlayer: MediaPlayer? = null
    var playerGetGoldAudioPlayer: MediaPlayer? = null
    var sentryGetGoldAudioPlayer: MediaPlayer? = null
    var raiseStasisFieldAudioPlayer: MediaPlayer? = null
    var lowerStasisFieldAudioPlayer: MediaPlayer? = null
    var teleporterAudioPlayer: MediaPlayer? = null
    var playerCaughtAudioPlayer: MediaPlayer? = null
    var escapeLadderAudioPlayer: MediaPlayer? = null
    var winLevelAudioPlayer: MediaPlayer? = null
    var highScoreAudioPlayer: MediaPlayer? = null
    var extraLifeAudioPlayer: MediaPlayer? = null

    fun playTheme()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (themeAudioPlayer == null)
        {
            themeAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.intro)
        }
        themeAudioPlayer?.start()
    }

    fun playPlayerGetGold()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (playerGetGoldAudioPlayer == null)
        {
            playerGetGoldAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.player_get_gold_mp3)
        }
        playerGetGoldAudioPlayer?.start()
    }

    fun playSentryGetGold()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (sentryGetGoldAudioPlayer == null)
        {
            sentryGetGoldAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.sentry_get_gold)
        }
        sentryGetGoldAudioPlayer?.start()
    }

    fun playRaiseStasisField()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (raiseStasisFieldAudioPlayer == null)
        {
            raiseStasisFieldAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.raise_stasis_field)
        }
        raiseStasisFieldAudioPlayer?.start()
    }

    fun playLowerStasisField()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (lowerStasisFieldAudioPlayer == null)
        {
            lowerStasisFieldAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.lower_stasis_field)
        }
        lowerStasisFieldAudioPlayer?.start()
    }

    fun playTeleporter()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (teleporterAudioPlayer == null)
        {
            teleporterAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.teleporter)
        }
        teleporterAudioPlayer?.start()
    }

    fun playPlayerCaught()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (playerCaughtAudioPlayer == null)
        {
            playerCaughtAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.player_caught)
        }
        playerCaughtAudioPlayer?.start()
    }

    fun playEscapeLadderRevealed()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (escapeLadderAudioPlayer == null)
        {
            escapeLadderAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.escape_ladder_mp3)
        }
        escapeLadderAudioPlayer?.start()
    }

    fun playWinLevel()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (winLevelAudioPlayer == null)
        {
            winLevelAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.win_level)
        }
        winLevelAudioPlayer?.start()
    }

    fun playExtraLife()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (extraLifeAudioPlayer == null)
        {
            extraLifeAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.extra_life)
        }
        extraLifeAudioPlayer?.start()
    }

    fun playHighScore()
    {
        if (!ConfigurationManager.playSounds)
        {
            return
        }
        if (highScoreAudioPlayer == null)
        {
            highScoreAudioPlayer = MediaPlayer.create(ConfigurationManager.getAppContext(), R.raw.high_score)
        }
        highScoreAudioPlayer?.start()
    }

    fun stopSoundsForPlayerDie()
    {
        playerGetGoldAudioPlayer?.stop()
        sentryGetGoldAudioPlayer?.stop()
        raiseStasisFieldAudioPlayer?.stop()
        lowerStasisFieldAudioPlayer?.stop()
        teleporterAudioPlayer?.stop()
        escapeLadderAudioPlayer?.stop()
    }

    fun stopSoundsForLevelWin()
    {
        playerGetGoldAudioPlayer?.stop()
        sentryGetGoldAudioPlayer?.stop()
        raiseStasisFieldAudioPlayer?.stop()
        lowerStasisFieldAudioPlayer?.stop()
        teleporterAudioPlayer?.stop()
        escapeLadderAudioPlayer?.stop()
    }
}