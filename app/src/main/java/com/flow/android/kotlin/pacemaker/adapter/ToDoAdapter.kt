package com.flow.android.kotlin.pacemaker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.pacemaker.databinding.ItemToDoBinding
import com.flow.android.kotlin.pacemaker.model.data.ToDo

class ToDoAdapter(private val onSwiped: (from: ToDo, to: ToDo) -> Unit): ListAdapter<ToDo, ToDoAdapter.ViewHolder>(ToDoDiffCallback()) {

    class ViewHolder(private val viewBinding: ItemToDoBinding): RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: ToDo) {
            viewBinding.text.text = item.content
        }

        companion object {
            fun from(layoutInflater: LayoutInflater, viewGroup: ViewGroup, boolean: Boolean) =
                ViewHolder(ItemToDoBinding.inflate(layoutInflater, viewGroup, boolean))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun swap(from: Int, to: Int) {
        onSwiped(getItem(from), getItem(to))
        notifyDataSetChanged()
    }
}

class ToDoDiffCallback: DiffUtil.ItemCallback<ToDo>() {
    override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
        return true
    }
}