package com.flow.android.kotlin.pacemaker.view.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.adapter.SettingAdapter
import com.flow.android.kotlin.pacemaker.adapter.SettingItem
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.pacemaker.databinding.FragmentSettingsBinding
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel

class SettingsFragment: BaseFragment<MainViewModel, FragmentSettingsBinding>() {

    override val viewBindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSettingsBinding
        get() = { layoutInflater, viewGroup, boolean ->
            FragmentSettingsBinding.inflate(layoutInflater, viewGroup, boolean)
        }

    override val useActivityViewModel: Boolean
        get() = true

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    private val settingAdapter: SettingAdapter by lazy {
        SettingAdapter(arrayListOf(
                SettingItem.RadioGroupItem(
                        tag = SettingAdapter.Tag.FontSize,
                        title = getString(R.string.font_size),
                        radioButtons = resources.getStringArray(R.array.font_size)
                ))
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewBinding.recyclerView.apply {
            adapter = settingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        return view
    }
}