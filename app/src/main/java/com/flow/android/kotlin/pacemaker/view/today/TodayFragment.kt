package com.flow.android.kotlin.pacemaker.view.today

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.adapter.ToDoAdapter
import com.flow.android.kotlin.pacemaker.adapter.item_touch_helper.Callback
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.DayViewBinding
import com.flow.android.kotlin.pacemaker.databinding.FragmentTodayBinding
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.view.util.shareToDo
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.Size
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class TodayFragment: BaseFragment<MainViewModel, FragmentTodayBinding>(), ToDoAdapter.OnItemClickListener, CoroutineScope by MainScope() {

    private val blank = ""
    private val job = Job()
    private val toDoAdapter = ToDoAdapter(arrayListOf())
    private val uiManager = UiManager()

    private val itemTouchHelper = ItemTouchHelper(Callback(toDoAdapter))

    override val viewBindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTodayBinding
        get() = { layoutInflater, viewGroup, boolean ->
            FragmentTodayBinding.inflate(layoutInflater, viewGroup, boolean)
        }

    override val useActivityViewModel: Boolean
        get() = true

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        uiManager.initialize()
        initializeLiveData()

        return view
    }

    private inner class UiManager {
        @ExperimentalCoroutinesApi
        fun initialize() {
            initializeCalendar()

            toDoAdapter.setOnItemClickListener(this@TodayFragment)

            viewBinding.recyclerView.apply {
                adapter = toDoAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

            val callbackFlow = callbackFlow {
                viewBinding.imageButton.setOnClickListener {
                    offer(Unit)
                }

                awaitClose()
            }

            launch(job) {
                callbackFlow.collect {
                    val content = viewBinding.editText.text.toString()

                    if (content.isNotBlank()) {
                        viewModel.insertToDo(content) {
                            toDoAdapter.add(it)
                        }

                        viewBinding.editText.setText(blank)
                    }
                }
            }
        }

        private fun initializeCalendar() {
            val displayMetrics = DisplayMetrics()
            val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = requireActivity().display
                display?.getRealMetrics(displayMetrics)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getMetrics(displayMetrics)
            }

            viewBinding.calendarView.apply {
                val width = displayMetrics.widthPixels / 5
                val height = (width * 1.25).toInt()
                daySize = Size(width, height)
            }

            viewBinding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, day: CalendarDay) = container.bind(day)
            }

            val yearMonth = YearMonth.now()

            viewBinding.calendarView.setup(yearMonth, yearMonth.plusMonths(3), DayOfWeek.values().random())
            viewBinding.calendarView.scrollToDate(LocalDate.now())
        }
    }

    private fun initializeLiveData() {
        viewModel.toDoList.observe(viewLifecycleOwner, {
            toDoAdapter.clear()
            toDoAdapter.addAll(it)
        })
    }

    override fun onPause() {
        viewModel.updateToDoList(toDoAdapter.toDoList())
        super.onPause()
    }

    override fun onDestroyView() {
        job.cancel()
        cancel()
        super.onDestroyView()
    }

    private inner class DayViewContainer(view: View) : ViewContainer(view) {
        private val dayOfMonthFormatter = DateTimeFormatter.ofPattern("dd")
        private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
        private val monthFormatter = DateTimeFormatter.ofPattern("M")

        lateinit var calendarDay: CalendarDay
        val dayViewBinding = DayViewBinding.bind(view)

        init {
            view.setOnClickListener {
                val firstVisibleDay = viewBinding.calendarView.findFirstVisibleDay()
                val lastVisibleDay = viewBinding.calendarView.findLastVisibleDay()
                if (firstVisibleDay == calendarDay) {
                    // If the first date on screen was clicked, we scroll to the date to ensure
                    // it is fully visible if it was partially off the screen when clicked.
                    viewBinding.calendarView.smoothScrollToDate(calendarDay.date)
                } else if (lastVisibleDay == calendarDay) {
                    viewBinding.calendarView.smoothScrollToDate(calendarDay.date.minusDays(4))
                }

                // Example: If you want the clicked date to always be centered on the screen,
                // you would use: exSevenCalendar.smoothScrollToDate(day.date.minusDays(2))

                if (viewModel.selectedDate() != calendarDay.date) {
                    val previousDate = viewModel.selectedDate()
                    viewModel.setSelectedDate(calendarDay.date)
                    viewBinding.calendarView.notifyDateChanged(calendarDay.date)
                    viewBinding.calendarView.notifyDateChanged(previousDate)
                }
            }
        }

        fun bind(calendarDay: CalendarDay) {
            this.calendarDay = calendarDay

            dayViewBinding.textDayOfMonth.text = dayOfMonthFormatter.format(calendarDay.date)
            dayViewBinding.textDayOfWeek.text = dayOfWeekFormatter.format(calendarDay.date)
            dayViewBinding.textMonth.text = monthFormatter.format(calendarDay.date)

            // todo. show rate.. use progress.
            //
            launch {
                val count: Int

                withContext(Dispatchers.IO) {
                    count = viewModel.getCountByLocalDate(calendarDay.date)
                }

                dayViewBinding.textMonth.text = count.toString()
            }

            dayViewBinding.textDayOfMonth.setTextColor(textColor())
            dayViewBinding.viewSelected.isVisible = calendarDay.date == viewModel.selectedDate()
        }

        @ColorInt
        fun textColor(): Int {
            return if (calendarDay.date == viewModel.selectedDate()) {
                ContextCompat.getColor(requireContext(), R.color.light_blue_500)
            } else
                ContextCompat.getColor(requireContext(), R.color.on_background)
        }
    }

    /** ToDoAdapter.OnItemClickListener */
    override fun onItemClick(toDo: ToDo) {

    }

    override fun onImageSwapTouch(viewHolder: ToDoAdapter.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onImageNotificationsClick(toDo: ToDo) {

    }

    override fun onImageShareClick(toDo: ToDo) {
        shareToDo(requireActivity(), toDo)
    }

    override fun onImageEditClick(toDo: ToDo) {

    }

    override fun onImageDeleteClick(toDo: ToDo) {
        viewModel.deleteToDo(toDo) {
            toDoAdapter.remove(toDo)
        }
    }
}