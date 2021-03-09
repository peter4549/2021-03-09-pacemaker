package com.flow.android.kotlin.pacemaker.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.adapter.FragmentStateAdapter
import com.flow.android.kotlin.pacemaker.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment: Fragment() {

    private lateinit var viewBinding: FragmentMainBinding
    private val uiManager = UiManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMainBinding.inflate(inflater, container, false)

        uiManager.init()

        return viewBinding.root
    }

    private inner class UiManager {

        fun init() {
            viewBinding.viewPager2.adapter = FragmentStateAdapter(requireActivity())
            viewBinding.viewPager2.isUserInputEnabled = true

            val texts = arrayOf(
                getString(R.string.today),
                getString(R.string.calendar),
                getString(R.string.settings)
            )

            TabLayoutMediator(viewBinding.tabLayout, viewBinding.viewPager2) { tab, position ->
                tab.tag = position
                tab.text = texts[position]
            }.attach()
        }
    }
}