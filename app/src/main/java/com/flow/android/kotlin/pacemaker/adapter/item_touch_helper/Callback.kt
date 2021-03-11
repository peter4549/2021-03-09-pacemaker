package com.flow.android.kotlin.pacemaker.adapter.item_touch_helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.pacemaker.adapter.ToDoAdapter

class Callback(private val adapter: ToDoAdapter): ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.absoluteAdapterPosition
        val to = target.absoluteAdapterPosition

        adapter.onMove(from, to)

        return true
    }



    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}