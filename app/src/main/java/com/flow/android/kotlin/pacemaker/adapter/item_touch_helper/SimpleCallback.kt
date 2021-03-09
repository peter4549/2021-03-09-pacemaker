package com.flow.android.kotlin.pacemaker.adapter.item_touch_helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.pacemaker.adapter.ToDoAdapter

class SimpleCallback(private val adapter: ToDoAdapter): ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP, ItemTouchHelper.START) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition

        adapter.swap(from, to)

        return true
    }

    override fun isLongPressDragEnabled(): Boolean = true

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}