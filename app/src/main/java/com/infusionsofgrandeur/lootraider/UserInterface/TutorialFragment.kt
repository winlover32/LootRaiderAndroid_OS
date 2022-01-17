package com.infusionsofgrandeur.lootraider.UserInterface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.infusionsofgrandeur.lootraider.GameObjects.Teleporter
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.Managers.SpriteManager
import com.infusionsofgrandeur.lootraider.R
import java.util.*

public const val ARG_PAGE_NUMBER = "pageNumber"
public const val ARG_VIEWPAGER = "ViewPager"

class TutorialFragment : Fragment()
{
    public lateinit var viewPager: ViewPager2
    public lateinit var parentActivity: TutorialActivity

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var pageNumber: Int = 1
    private var timer: Timer? = null
    private var sentryFrame = ConfigurationManager.guardSpriteIndex
    private var bidirectionalTeleporterFrame = ConfigurationManager.teleporterSpriteIndex
    private var sendingTeleporterFrame = ConfigurationManager.teleporterSpriteIndex
    private var receivingTeleporterFrame = ConfigurationManager.teleporterSpriteIndex
    private var pulseOut = true

    // UI elements
    private val sentryImageView: ImageView? = null
    private val teleporterOneImageView: ImageView? = null
    private val teleporterTwoImageView: ImageView? = null
    private val teleporterThreeImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var tutorialLayout = inflater.inflate(R.layout.fragment_tutorial_page_1, container, false)
        // Inflate the layout for this fragment
        when (pageNumber)
        {
            1 -> {
                tutorialLayout =  inflater.inflate(R.layout.fragment_tutorial_page_1, container, false)
                val exitButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_1_exit_image_button)
                val nextButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_1_next_image_button)
                nextButton.setOnClickListener {
                    viewPager.setCurrentItem(1, true)
                    firebaseAnalytics.logEvent("AdvanceTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                exitButton.setOnClickListener {
                    parentActivity.finish()
                    firebaseAnalytics.logEvent("EndTutorial", null)
                }
            }
            2 -> {
                tutorialLayout = inflater.inflate(R.layout.fragment_tutorial_page_2, container, false)
                val backButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_2_back_image_button)
                val nextButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_2_next_image_button)
                val sentryImageView = tutorialLayout.findViewById<ImageView>(R.id.tutorial_page_2_empty_guard_image_view)
                nextButton.setOnClickListener {
                    viewPager.setCurrentItem(2, true)
                    firebaseAnalytics.logEvent("AdvanceTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                backButton.setOnClickListener {
                    viewPager.setCurrentItem(0, true)
                    firebaseAnalytics.logEvent("RegressTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                if (timer == null)
                {
                    timer = Timer()
                }
                timer?.scheduleAtFixedRate(object: TimerTask() {
                    override fun run() {
                        getActivity()?.runOnUiThread {
                            val nextSprite = SpriteManager.bitmapForNextFrameOfAnimationNamed( "Robot Right", sentryFrame)
                            sentryImageView.setImageBitmap(nextSprite.bitmap)
                            sentryFrame = nextSprite.frame
                        }
                    }
                }, ConfigurationManager.spriteAnimationLoopTimerDelay, ConfigurationManager.spriteAnimationLoopTimerDelay)
            }
            3 -> {
                tutorialLayout = inflater.inflate(R.layout.fragment_tutorial_page_3, container, false)
                val backButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_3_back_image_button)
                val nextButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_3_next_image_button)
                val teleporterOneImageView = tutorialLayout.findViewById<ImageView>(R.id.tutorial_page_3_bi_directional_teleporter_image_view)
                val teleporterTwoImageView = tutorialLayout.findViewById<ImageView>(R.id.tutorial_page_3_sending_teleporter_image_view)
                val teleporterThreeImageView = tutorialLayout.findViewById<ImageView>(R.id.tutorial_page_3_recieving_teleporter_image_view)
                nextButton.setOnClickListener {
                    viewPager.setCurrentItem(3, true)
                    firebaseAnalytics.logEvent("AdvanceTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                backButton.setOnClickListener {
                    viewPager.setCurrentItem(1, true)
                    firebaseAnalytics.logEvent("RegressTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                if (timer == null)
                {
                    timer = Timer()
                }
                timer?.scheduleAtFixedRate(object: TimerTask() {
                    override fun run() {
                        getActivity()?.runOnUiThread {
                            // Roundtrippable
                            if (pulseOut)
                            {
                                if (bidirectionalTeleporterFrame < Teleporter.endFrame)
                                {
                                    bidirectionalTeleporterFrame += 1
                                }
                                else
                                {
                                    bidirectionalTeleporterFrame -= 1
                                    pulseOut = false
                                }
                                val image = SpriteManager.bitmapForSpriteNumber(bidirectionalTeleporterFrame)
                                teleporterOneImageView.setImageBitmap(image)
                            }
                            else
                            {
                                if (bidirectionalTeleporterFrame > Teleporter.startFrame)
                                {
                                    bidirectionalTeleporterFrame -= 1
                                }
                                else
                                {
                                    bidirectionalTeleporterFrame += 1
                                    pulseOut = true
                                }
                                val image = SpriteManager.bitmapForSpriteNumber(bidirectionalTeleporterFrame)
                                teleporterOneImageView.setImageBitmap(image)
                            }
                            // Sendable
                            if (sendingTeleporterFrame == Teleporter.startFrame)
                            {
                                sendingTeleporterFrame = Teleporter.endFrame
                            }
                            else
                            {
                                sendingTeleporterFrame -= 1
                            }
                            val sendableImage = SpriteManager.bitmapForSpriteNumber(sendingTeleporterFrame)
                            teleporterTwoImageView.setImageBitmap(sendableImage)
                            // Receivable
                            if (receivingTeleporterFrame < Teleporter.endFrame)
                            {
                                receivingTeleporterFrame += 1
                            }
                            else
                            {
                                receivingTeleporterFrame = Teleporter.startFrame
                            }
                            val receivableImage = SpriteManager.bitmapForSpriteNumber(receivingTeleporterFrame)
                            teleporterThreeImageView.setImageBitmap(receivableImage)
                        }
                    }
                }, ConfigurationManager.spriteAnimationLoopTimerDelay, ConfigurationManager.spriteAnimationLoopTimerDelay)
            }
            4 -> {
                tutorialLayout = inflater.inflate(R.layout.fragment_tutorial_page_4, container, false)
                val backButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_4_back_image_button)
                val nextButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_4_next_image_button)
                nextButton.setOnClickListener {
                    viewPager.setCurrentItem(4, true)
                    firebaseAnalytics.logEvent("AdvanceTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                backButton.setOnClickListener {
                    viewPager.setCurrentItem(2, true)
                    firebaseAnalytics.logEvent("RegressTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
            }
            5 -> {
                tutorialLayout = inflater.inflate(R.layout.fragment_tutorial_page_5, container, false)
                val backButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_5_back_image_button)
                val exitButton = tutorialLayout.findViewById<ImageButton>(R.id.tutorial_page_5_exit_image_button)
                backButton.setOnClickListener {
                    viewPager.setCurrentItem(3, true)
                    firebaseAnalytics.logEvent("RegressTutorial", Bundle().apply {
                        putInt(FirebaseAnalytics.Param.VALUE, pageNumber)
                    })
                }
                exitButton.setOnClickListener {
                    parentActivity.finish()
                    firebaseAnalytics.logEvent("EndTutorial", null)
                }
            }
        }
        return tutorialLayout
    }

    override fun onResume()
    {
        super.onResume()
//        configureTutorialPage()
    }

    private fun configureTutorialPage()
    {
        when (pageNumber)
        {
            1 -> {
                val exitButton: ImageButton
            }
        }
    }

    companion object
    {
        @JvmStatic fun newInstance() = TutorialFragment()
    }
}