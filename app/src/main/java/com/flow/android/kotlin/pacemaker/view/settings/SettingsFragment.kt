package com.flow.android.kotlin.pacemaker.view.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel

class SettingsFragment: BaseFragment<MainViewModel, FragmentCalendarBinding>() {

    override val viewBindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCalendarBinding
        get() = { layoutInflater, viewGroup, boolean ->
            FragmentCalendarBinding.inflate(layoutInflater, viewGroup, boolean)
        }

    override val useActivityViewModel: Boolean
        get() = true

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java
}