package com.flow.android.kotlin.pacemaker.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.flow.android.kotlin.pacemaker.view.calendar.CalendarFragment
import com.flow.android.kotlin.pacemaker.view.settings.SettingsFragment
import com.flow.android.kotlin.pacemaker.view.today.TodayFragment

class FragmentStateAdapter(fragmentActivity: FragmentActivity):
    androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
    private val pageCount = 3

    override fun getItemCount(): Int = pageCount

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> CalendarFragment()
            2 -> SettingsFragment()
            else -> throw IllegalStateException("Invalid position.")
        }
    }
}