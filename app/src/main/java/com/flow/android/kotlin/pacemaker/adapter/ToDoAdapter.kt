package com.flow.android.kotlin.pacemaker.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.flow.android.kotlin.pacemaker.R
import com.flow.android.kotlin.pacemaker.databinding.ItemToDoBinding
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.view.util.*
import java.util.*
import kotlin.collections.ArrayList

class ToDoAdapter(private val list: ArrayList<AdapterItem>): RecyclerView.Adapter<ToDoAdapter.ViewHolder>(){

    private val duration = 200L
    private var onItemClickListener: OnItemClickListener? = null
    private var recyclerView: RecyclerView? = null

    private var done: Drawable? = null
    private var keyboardArrowLeft: Drawable? = null

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(viewHolder: ViewHolder, toDo: ToDo)
        fun onImageSwapTouch(viewHolder: ViewHolder)
        fun onImageNotificationsClick(toDo: ToDo)
        fun onImageShareClick(toDo: ToDo)
        fun onImageEditClick(toDo: ToDo)
        fun onImageDeleteClick(toDo: ToDo)
    }

    fun toDoList() = list.map { it.toDo }

    fun addAll(list: List<ToDo>) {
        this.list.addAll(list.map { AdapterItem(false, it) })
    }

    fun add(toDo: ToDo) {
        list.add(0, AdapterItem(false, toDo))
        notifyItemInserted(0)
        recyclerView?.scrollToPosition(0)
    }

    fun remove(toDo: ToDo) {
        val adapterItem = list.findLast { it.toDo.id == toDo.id } ?: return
        val position = list.indexOf(adapterItem)

        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun modifyItem(item: ToDo) {
        val adapterItem = list.findLast { it.toDo.id == item.id } ?: return
        val position = list.indexOf(adapterItem)

        list[position] = AdapterItem(adapterItem.isMenuOpened, item)
        notifyItemChanged(position)
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(val viewBinding: ItemToDoBinding): RecyclerView.ViewHolder(viewBinding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: AdapterItem) {
            val toDo = item.toDo

            if (done == null)
                done = ContextCompat.getDrawable(viewBinding.root.context, R.drawable.ic_done_24)

            if (keyboardArrowLeft == null)
                keyboardArrowLeft = ContextCompat.getDrawable(viewBinding.root.context, R.drawable.ic_keyboard_arrow_left_24)

            if (toDo.done)
                viewBinding.imageDone.setImageDrawable(done)
            else
                viewBinding.imageDone.setImageDrawable(keyboardArrowLeft)

            viewBinding.text.text = toDo.content

            viewBinding.imageSwap.setOnTouchListener { _, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        onItemClickListener?.onImageSwapTouch(this)
                        return@setOnTouchListener true
                    }
                }

                false
            }

            if (item.isMenuOpened) {
                viewBinding.imageMenu.rotate(180F, 0)
                viewBinding.cardViewMenu.expand(0) {
                    viewBinding.constraintLayoutMenu.fadeIn(0)
                }
            } else {
                viewBinding.imageMenu.rotate(0F, 0)
                viewBinding.constraintLayoutMenu.fadeOut(0, false) {
                    viewBinding.cardViewMenu.collapse(0, 0)
                }
            }

            viewBinding.imageMenu.setOnClickListener {
                if (item.isMenuOpened) {
                    it.rotate(0F, duration)
                    viewBinding.constraintLayoutMenu.fadeOut(duration, false) {
                        viewBinding.cardViewMenu.collapse(0, duration)
                    }

                    item.isMenuOpened = false
                } else {
                    it.rotate(180F, duration)
                    viewBinding.cardViewMenu.expand(duration) {
                        viewBinding.constraintLayoutMenu.fadeIn(duration)
                    }

                    item.isMenuOpened = true
                }
            }

            viewBinding.cardView.setOnClickListener {
                onItemClickListener?.onItemClick(this, toDo)
            }

            viewBinding.imageNotifications.setOnClickListener {
                onItemClickListener?.onImageNotificationsClick(toDo)
            }

            viewBinding.imageShare.setOnClickListener {
                onItemClickListener?.onImageShareClick(toDo)
            }

            viewBinding.imageEdit.setOnClickListener {
                onItemClickListener?.onImageEditClick(toDo)
            }

            viewBinding.imageDelete.setOnClickListener {
                onItemClickListener?.onImageDeleteClick(toDo)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun from(layoutInflater: LayoutInflater, viewGroup: ViewGroup, boolean: Boolean) =
        ViewHolder(ItemToDoBinding.inflate(layoutInflater, viewGroup, boolean))

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun onMove(from: Int, to: Int) {
        val priority = list[from].toDo.priority
        list[from].toDo.priority = list[to].toDo.priority
        list[to].toDo.priority = priority
        Collections.swap(list, from, to)

        notifyItemMoved(from, to)
    }

    override fun getItemCount(): Int = list.count()
}

data class AdapterItem(
    var isMenuOpened: Boolean,
    val toDo: ToDo
)