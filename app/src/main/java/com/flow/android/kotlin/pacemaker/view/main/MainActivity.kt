package com.flow.android.kotlin.pacemaker.view.main

import android.os.Bundle
import android.view.LayoutInflater
import com.flow.android.kotlin.pacemaker.base.BaseActivity
import com.flow.android.kotlin.pacemaker.databinding.ActivityMainBinding
import com.flow.android.kotlin.pacemaker.model.data.ToDo
import com.flow.android.kotlin.pacemaker.view.dialog_fragment.EditDialogFragment
import com.flow.android.kotlin.pacemaker.view_model.MainViewModel
import timber.log.Timber

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>(), EditDialogFragment.OnToDoUpdatedListener {

    override val viewBindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = { ActivityMainBinding.inflate(it) }

    override fun viewModel(): Class<MainViewModel> = MainViewModel::class.java

    /** EditDialogFragment.OnToDoUpdatedListener */
    override fun onToDoUpdated(toDo: ToDo) {
        viewModel.setModifiedToDo(toDo)
    }

    override fun onError(throwable: Throwable) {
        Timber.e(throwable)
    }
}