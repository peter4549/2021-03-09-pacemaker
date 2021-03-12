package com.flow.android.kotlin.pacemaker.view.calendar

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.FragmentCalendarBinding
import com.flow.android.kotlin.pacemaker.databinding.MonthDayViewBinding
import com.flow.android.kotlin.pacemaker.databinding.MonthHeaderBinding
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class CalendarFragment: BaseFragment<MainViewModel, FragmentCalendarBinding>() {

    override val viewBindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCalendarBinding
        get() = { layoutInflater, viewGroup, boolean ->
            FragmentCalendarBinding.inflate(layoutInflater, viewGroup, boolean)
        }

    override val useActivityViewModel: Boolean
        get() = true

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    private val uiManager = UiManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        uiManager.initialize()

        return view
    }

    private val daysOfWeek = daysOfWeekFromLocale()

    private inner class UiManager {
        fun initialize() {
            initializeCalendarView()
            setupCalendarView()
        }

        private fun initializeCalendarView() {
            viewBinding.calendarView.dayBinder = object: DayBinder<MonthDayViewContainer> {
                override fun bind(container: MonthDayViewContainer, day: CalendarDay) {
                    container.viewBinding.textView.text = day.date.dayOfMonth.toString()
                }

                override fun create(view: View): MonthDayViewContainer = MonthDayViewContainer(view)
            }

            viewBinding.calendarView.monthHeaderBinder =
                object: MonthHeaderFooterBinder<MonthHeaderViewContainer> {
                    override fun create(view: View) = MonthHeaderViewContainer(view)
                    override fun bind(container: MonthHeaderViewContainer, month: CalendarMonth) {
                        val monthDaysOfWeekLayout = container.viewBinding.linearLayout
                        val textColor = ContextCompat.getColor(requireContext(), R.color.light_blue_500) // todo change here.
                        if (monthDaysOfWeekLayout.tag == null) {
                            monthDaysOfWeekLayout.tag = month.yearMonth

                            (monthDaysOfWeekLayout as ViewGroup).children.map { it as TextView }.forEachIndexed { index, textView ->
                                textView.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    .toUpperCase(Locale.ENGLISH)
                                textView.setTextColor(textColor)
                                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
                            }
                        }
                    }
                }
        }

        private fun setupCalendarView() {
            val yearMonth = YearMonth.now()
            viewBinding.calendarView.setup(
                yearMonth.minusMonths(12),
                yearMonth.plusMonths(12),
                daysOfWeek.first()
            )
            viewBinding.calendarView.scrollToMonth(yearMonth)
        }
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()

        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }

        return daysOfWeek
    }
}

class MonthDayViewContainer(view: View): ViewContainer(view) {
    val viewBinding = MonthDayViewBinding.bind(view)
}

class MonthHeaderViewContainer(view: View) : ViewContainer(view) {
    val viewBinding = MonthHeaderBinding.bind(view)
}