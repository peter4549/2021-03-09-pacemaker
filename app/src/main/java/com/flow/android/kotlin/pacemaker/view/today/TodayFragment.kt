package com.flow.android.kotlin.pacemaker.view.today

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.adapter.ToDoAdapter
import com.flow.android.kotlin.pacemaker.adapter.item_touch_helper.Callback
import com.flow.android.kotlin.pacemaker.application.CHANNEL_ID
import com.flow.android.kotlin.pacemaker.application.NOTIFICATION_ID
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.WeekDayViewBinding
import com.flow.android.kotlin.pacemaker.databinding.FragmentTodayBinding
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.view.dialog_fragment.EditDialogFragment
import com.flow.android.kotlin.pacemaker.view.util.hide
import com.flow.android.kotlin.pacemaker.view.util.shareToDo
import com.flow.android.kotlin.pacemaker.view.util.show
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
            (viewBinding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            toDoAdapter.clear()
            toDoAdapter.addAll(it)
        })

        viewModel.modifiedToDo.observe(viewLifecycleOwner, { toDo ->
            toDo?.also {
                toDoAdapter.modifyItem(it)
            }
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
        val weekDayViewBinding = WeekDayViewBinding.bind(view)

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
                    viewModel.updateToDoList(toDoAdapter.toDoList())

                    val previousDate = viewModel.selectedDate()
                    viewModel.setSelectedDate(calendarDay.date)
                    viewBinding.calendarView.notifyDateChanged(calendarDay.date)
                    viewBinding.calendarView.notifyDateChanged(previousDate)
                }
            }
        }

        fun bind(calendarDay: CalendarDay) {
            this.calendarDay = calendarDay

            weekDayViewBinding.textDayOfMonth.text = dayOfMonthFormatter.format(calendarDay.date)
            weekDayViewBinding.textDayOfWeek.text = dayOfWeekFormatter.format(calendarDay.date)
            weekDayViewBinding.textMonth.text = monthFormatter.format(calendarDay.date)

            // todo. show rate.. use progress.
            //
            launch {
                val doneList: List<Boolean>

                withContext(Dispatchers.IO) {
                    doneList = viewModel.getDoneListByLocalDate(calendarDay.date)
                }

                if (doneList.isNullOrEmpty())
                    weekDayViewBinding.arcProgress.hide(false)
                else {
                    weekDayViewBinding.arcProgress.show()
                    weekDayViewBinding.arcProgress.max = doneList.count()
                    weekDayViewBinding.arcProgress.progress = doneList.filter { it }.count()
                }
            }

            weekDayViewBinding.textDayOfMonth.setTextColor(textColor())
            weekDayViewBinding.viewSelected.isVisible = calendarDay.date == viewModel.selectedDate()
        }

        @ColorInt
        fun textColor(): Int {
            return if (calendarDay.date == viewModel.selectedDate()) {
                ContextCompat.getColor(requireContext(), R.color.light_blue_500)
            } else
                ContextCompat.getColor(requireContext(), R.color.on_background)
        }
    }

    /** Notification */
    private fun createNotification(toDo: ToDo) {
        val contentTitle = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(toDo.localDate())
        val contentText = toDo.content

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_done_24)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_MIN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(requireContext())) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    /** ToDoAdapter.OnItemClickListener */
    override fun onItemClick(viewHolder: ToDoAdapter.ViewHolder, toDo: ToDo) {

        val drawable: AnimatedVectorDrawable?

        if (toDo.done) {
            drawable = ContextCompat.getDrawable(requireContext(), R.drawable.animated_vector_done_to_keyboard_arrow_left) as? AnimatedVectorDrawable
            toDo.done = false
        } else {
            drawable = ContextCompat.getDrawable(requireContext(), R.drawable.animated_vector_keyboard_arrow_left_to_done) as? AnimatedVectorDrawable
            toDo.done = true
        }

        viewHolder.viewBinding.imageDone.setImageDrawable(drawable)
        drawable?.start()
    }

    override fun onImageSwapTouch(viewHolder: ToDoAdapter.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onImageNotificationsClick(toDo: ToDo) {
        createNotification(toDo)
    }

    override fun onImageShareClick(toDo: ToDo) {
        shareToDo(requireActivity(), toDo)
    }

    override fun onImageEditClick(toDo: ToDo) {
        val title = getString(R.string.modify_to_do)

        EditDialogFragment().apply {
            setTitle(title)
            setToDo(toDo)
            show(this@TodayFragment.requireActivity().supportFragmentManager, tag)
        }
    }

    override fun onImageDeleteClick(toDo: ToDo) {
        showMaterialAlertDialog(
                title = getString(R.string.delete_to_do_title),
                message = getString(R.string.delete_to_do_message),
                negativeButtonClickListener = { dialogInterface, _ ->
                    dialogInterface?.dismiss()
                },
                negativeButtonText = getString(R.string.cancel),
                neutralButtonClickListener = null,
                neutralButtonText = null,
                positiveButtonClickListener = { dialogInterface, _ ->
                    viewModel.deleteToDo(toDo) {
                        toDoAdapter.remove(toDo)
                    }

                    dialogInterface?.dismiss()
                },
                positiveButtonText = getString(R.string.delete)
        )
    }
}