package com.flow.android.kotlin.pacemaker.view.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.flow.android.kotlin.pacemaker.adapter.ToDoAdapter
import com.flow.android.kotlin.pacemaker.adapter.item_touch_helper.SimpleCallback
import com.flow.android.kotlin.pacemaker.base.BaseFragment
import com.flow.android.kotlin.pacemaker.databinding.FragmentTodayBinding
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

class TodayFragment: BaseFragment<MainViewModel, FragmentTodayBinding>(), CoroutineScope by MainScope() {

    private val blank = ""

    private val uiManager = UiManager()
    private val toDoAdapter = ToDoAdapter { from, to ->
        val priority = from.priority
        to.priority = from.priority
        to.priority = priority

        viewModel.insertToDoList(listOf(from, to))
    }

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

        uiManager.init()
        initLiveData()

        return view
    }

    private inner class UiManager {
        @ExperimentalCoroutinesApi
        fun init() {
            viewBinding.recyclerView.apply {
                adapter = toDoAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

            val itemTouchHelper = ItemTouchHelper(SimpleCallback(toDoAdapter))
            itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

            val callbackFlow = callbackFlow {
                viewBinding.imageButton.setOnClickListener {
                    offer(Unit)
                }

                awaitClose()
            }

            launch {
                callbackFlow.collect {
                    val content = viewBinding.editText.text.toString()

                    if (content.isNotBlank()) {
                        viewModel.insertToDo(content)
                        viewBinding.editText.setText(blank)
                    }
                }
            }
        }
    }

    private fun initLiveData() {
        viewModel.todoList.observe(viewLifecycleOwner, {
            toDoAdapter.submitList(it)
        })
    }

    override fun onDestroyView() {
        cancel()
        super.onDestroyView()
    }
}