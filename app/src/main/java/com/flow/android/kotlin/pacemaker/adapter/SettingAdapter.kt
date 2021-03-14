package com.flow.android.kotlin.pacemaker.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.flow.android.kotlin.pacemaker.databinding.ItemRadioGroupBinding

class SettingAdapter(private val settingItems: ArrayList<SettingItem>): RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    object Tag {
        const val FontSize = "font_size"
    }

    private object ViewType {
        const val RadioGroup = 808
    }

    inner class ViewHolder(val viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root) {

        fun bind(item: SettingItem) {
            when(item) {
                is SettingItem.RadioGroupItem -> {
                    viewBinding as ItemRadioGroupBinding
                    viewBinding.textTitle.text = item.title

                    item.radioButtons.forEach {
                        val radioButton = RadioButton(viewBinding.root.context)
                        radioButton.text = it
                        viewBinding.radioGroup.addView(radioButton)
                    }

                    handleAccordingToTag(this, item)
                }
            }
        }
    }

    private fun from(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val viewBinding = when(viewType) {
            ViewType.RadioGroup -> ItemRadioGroupBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid viewType.")
        }

        return ViewHolder(viewBinding)
    }

    override fun getItemViewType(position: Int): Int {
        return when(settingItems[position]) {
            is SettingItem.RadioGroupItem -> ViewType.RadioGroup
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(settingItems[position])
    }

    override fun getItemCount(): Int = settingItems.count()

    private fun handleAccordingToTag(viewHolder: ViewHolder, item: SettingItem) {
        when(item.tag) {
            Tag.FontSize -> {
                viewHolder.viewBinding as ItemRadioGroupBinding

                var textSize = 12F

                viewHolder.viewBinding.radioGroup.children.forEach {
                    if (it is RadioButton) {
                        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
                        textSize += 2F
                    }
                }
            }
        }
    }
}

sealed class SettingItem {
    data class RadioGroupItem(
            override val tag: String,
            override val title: String,
            val radioButtons: Array<String>
    ): SettingItem() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RadioGroupItem

            if (title != other.title) return false
            if (!radioButtons.contentEquals(other.radioButtons)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + radioButtons.contentHashCode()
            return result
        }
    }

    abstract val tag: String
    abstract val title: String
}