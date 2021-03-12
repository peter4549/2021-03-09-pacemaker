package com.flow.android.kotlin.pacemaker.view.dialog_fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.flow.android.kotlin.pacemaker.application.BLANK
import com.flow.android.kotlin.pacemaker.databinding.FragmentEditDialogBinding
import com.flow.android.kotlin.pacemaker.model.dao.ToDoDao
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class EditDialogFragment: DialogFragment() {

    @Inject
    lateinit var toDoDao: ToDoDao

    private object Key {
        const val ToDo = "com.flow.android.kotlin.pacemaker.view.dialog_fragment" +
                ".edit_dialog_fragment.key.to_do"
    }

    private var title = BLANK
    private var toDo: ToDo? = null
    private var viewBinding: FragmentEditDialogBinding? = null

    private var onToDoUpdatedListener: OnToDoUpdatedListener? = null

    interface OnToDoUpdatedListener {
        fun onToDoUpdated(toDo: ToDo)
        fun onError(throwable: Throwable)
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setToDo(toDo: ToDo) {
        this.toDo = toDo
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Key.ToDo, toDo)
    }
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        if (context is OnToDoUpdatedListener)
            onToDoUpdatedListener = context

        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentEditDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        savedInstanceState?.also {
            toDo = it.getParcelable(Key.ToDo)
        }

        viewBinding?.textTitle?.text = title

        viewBinding?.editText?.setText(toDo?.content)

        viewBinding?.buttonCancel?.setOnClickListener {
            dismiss()
        }

        viewBinding?.buttonOk?.setOnClickListener {
            val content = viewBinding?.editText?.text.toString()

            if (content.isBlank()) {
                viewBinding?.editText?.error = "Error!"
            } else {
                toDo?.also {
                    val toDo = ToDo(
                        id = it.id,
                        content = content,
                        dateTime = it.dateTime,
                        done = it.done,
                        priority = it.priority
                    )

                    toDoDao.update(toDo).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            onToDoUpdatedListener?.onToDoUpdated(toDo)
                            dismiss()
                        }, { throwable ->
                            onToDoUpdatedListener?.onError(throwable)
                            dismiss()
                        })
                }
            }
        }

        return viewBinding?.root
    }
}