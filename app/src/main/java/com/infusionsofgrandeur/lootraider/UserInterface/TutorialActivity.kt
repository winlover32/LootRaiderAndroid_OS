package com.infusionsofgrandeur.lootraider.UserInterface

import androidx.fragment.app.FragmentActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.infusionsofgrandeur.lootraider.Managers.ConfigurationManager
import com.infusionsofgrandeur.lootraider.databinding.ActivityTutorialBinding

class TutorialActivity : FragmentActivity()
{

    lateinit var binding: ActivityTutorialBinding
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = binding.tutorialPager

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = TutorialPagerAdapter(this)
        binding.tutorialPager.adapter = pagerAdapter
    }

    override fun onBackPressed()
    {
        if (viewPager.currentItem == 0)
        {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        }
        else
        {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    private inner class TutorialPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = ConfigurationManager.numberTutorialSegments

        override fun createFragment(position: Int): Fragment
        {
            val fragment = TutorialFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PAGE_NUMBER, position + 1)
            }
            fragment.viewPager = viewPager
            fragment.parentActivity = this@TutorialActivity
            return fragment
        }
    }
}